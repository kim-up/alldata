/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.store.file.schema;

import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.flink.table.store.file.operation.Lock;
import org.apache.flink.table.store.file.schema.SchemaChange.AddColumn;
import org.apache.flink.table.store.file.schema.SchemaChange.RemoveOption;
import org.apache.flink.table.store.file.schema.SchemaChange.SetOption;
import org.apache.flink.table.store.file.schema.SchemaChange.UpdateColumnComment;
import org.apache.flink.table.store.file.schema.SchemaChange.UpdateColumnNullability;
import org.apache.flink.table.store.file.schema.SchemaChange.UpdateColumnType;
import org.apache.flink.table.store.file.utils.AtomicFileWriter;
import org.apache.flink.table.store.file.utils.FileUtils;
import org.apache.flink.table.store.file.utils.JsonSerdeUtil;
import org.apache.flink.table.types.logical.RowType;
import org.apache.flink.util.Preconditions;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.apache.flink.table.store.CoreOptions.BUCKET_KEY;
import static org.apache.flink.table.store.CoreOptions.WRITE_MODE;
import static org.apache.flink.table.store.file.utils.FileUtils.listVersionedFiles;

/** Schema Manager to manage schema versions. */
public class SchemaManager implements Serializable {

    private static final String SCHEMA_PREFIX = "schema-";

    private final Path tableRoot;

    @Nullable private transient Lock lock;

    public SchemaManager(Path tableRoot) {
        this.tableRoot = tableRoot;
    }

    public SchemaManager withLock(@Nullable Lock lock) {
        this.lock = lock;
        return this;
    }

    /** @return latest schema. */
    public Optional<TableSchema> latest() {
        try {
            return listVersionedFiles(schemaDirectory(), SCHEMA_PREFIX)
                    .reduce(Math::max)
                    .map(this::schema);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** List all schema. */
    public List<TableSchema> listAll() {
        return listAllIds().stream().map(this::schema).collect(Collectors.toList());
    }

    /** List all schema IDs. */
    public List<Long> listAllIds() {
        try {
            return listVersionedFiles(schemaDirectory(), SCHEMA_PREFIX)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Create a new schema from {@link UpdateSchema}. */
    public TableSchema commitNewVersion(UpdateSchema updateSchema) throws Exception {
        RowType rowType = updateSchema.rowType();
        List<String> partitionKeys = updateSchema.partitionKeys();
        List<String> primaryKeys = updateSchema.primaryKeys();
        Map<String, String> options = updateSchema.options();

        while (true) {
            long id;
            int highestFieldId;
            List<DataField> fields;
            Optional<TableSchema> latest = latest();
            if (latest.isPresent()) {
                TableSchema oldTableSchema = latest.get();
                Preconditions.checkArgument(
                        oldTableSchema.primaryKeys().equals(primaryKeys),
                        "Primary key modification is not supported, "
                                + "old primaryKeys is %s, new primaryKeys is %s",
                        oldTableSchema.primaryKeys(),
                        primaryKeys);

                if (!updateSchema
                                .rowType()
                                .getFields()
                                .equals(oldTableSchema.logicalRowType().getFields())
                        || !updateSchema.partitionKeys().equals(oldTableSchema.partitionKeys())) {
                    throw new UnsupportedOperationException(
                            "TODO: support update field types and partition keys. ");
                }

                fields = oldTableSchema.fields();
                id = oldTableSchema.id() + 1;
                highestFieldId = oldTableSchema.highestFieldId();
            } else {
                fields = TableSchema.newFields(rowType);
                highestFieldId = TableSchema.currentHighestFieldId(fields);
                id = 0;
            }

            TableSchema newSchema =
                    new TableSchema(
                            id,
                            fields,
                            highestFieldId,
                            partitionKeys,
                            primaryKeys,
                            options,
                            updateSchema.comment());

            boolean success = commit(newSchema);
            if (success) {
                return newSchema;
            }
        }
    }

    /** Create {@link SchemaChange}s. */
    public TableSchema commitChanges(List<SchemaChange> changes) throws Exception {
        while (true) {
            TableSchema schema =
                    latest().orElseThrow(
                                    () -> new RuntimeException("Table not exists: " + tableRoot));
            Map<String, String> newOptions = new HashMap<>(schema.options());
            List<DataField> newFields = new ArrayList<>(schema.fields());
            AtomicInteger highestFieldId = new AtomicInteger(schema.highestFieldId());
            for (SchemaChange change : changes) {
                if (change instanceof SetOption) {
                    SetOption setOption = (SetOption) change;
                    checkAlterTableOption(setOption.key());
                    newOptions.put(setOption.key(), setOption.value());
                } else if (change instanceof RemoveOption) {
                    RemoveOption removeOption = (RemoveOption) change;
                    checkAlterTableOption(removeOption.key());
                    newOptions.remove(removeOption.key());
                } else if (change instanceof AddColumn) {
                    AddColumn addColumn = (AddColumn) change;
                    if (newFields.stream()
                            .anyMatch(
                                    f ->
                                            StringUtils.equalsIgnoreCase(
                                                    f.name(), addColumn.fieldName()))) {
                        throw new IllegalArgumentException(
                                String.format(
                                        "The column [%s] exists in the table[%s].",
                                        addColumn.fieldName(), tableRoot));
                    }
                    int id = highestFieldId.incrementAndGet();
                    DataType dataType =
                            TableSchema.toDataType(addColumn.logicalType(), highestFieldId);
                    newFields.add(
                            new DataField(
                                    id, addColumn.fieldName(), dataType, addColumn.description()));
                } else if (change instanceof UpdateColumnType) {
                    UpdateColumnType update = (UpdateColumnType) change;
                    updateColumn(
                            newFields,
                            update.fieldName(),
                            (field) -> {
                                AtomicInteger dummyId = new AtomicInteger(0);
                                DataType newType =
                                        TableSchema.toDataType(
                                                update.newLogicalType(), new AtomicInteger(0));
                                if (dummyId.get() != 0) {
                                    throw new RuntimeException(
                                            String.format(
                                                    "Update column to nested row type '%s' is not supported.",
                                                    update.newLogicalType()));
                                }
                                return new DataField(field.id(), field.name(), newType);
                            });
                } else if (change instanceof UpdateColumnNullability) {
                    UpdateColumnNullability update = (UpdateColumnNullability) change;
                    if (update.fieldNames().length == 1
                            && update.newNullability()
                            && schema.primaryKeys().contains(update.fieldNames()[0])) {
                        throw new UnsupportedOperationException(
                                "Cannot change nullability of primary key");
                    }
                    updateNestedColumn(
                            newFields,
                            update.fieldNames(),
                            0,
                            (field) ->
                                    new DataField(
                                            field.id(),
                                            field.name(),
                                            field.type().copy(update.newNullability()),
                                            field.description()));
                } else if (change instanceof UpdateColumnComment) {
                    UpdateColumnComment update = (UpdateColumnComment) change;
                    updateNestedColumn(
                            newFields,
                            update.fieldNames(),
                            0,
                            (field) ->
                                    new DataField(
                                            field.id(),
                                            field.name(),
                                            field.type(),
                                            update.newDescription()));
                } else {
                    throw new UnsupportedOperationException(
                            "Unsupported change: " + change.getClass());
                }
            }

            TableSchema newSchema =
                    new TableSchema(
                            schema.id() + 1,
                            newFields,
                            highestFieldId.get(),
                            schema.partitionKeys(),
                            schema.primaryKeys(),
                            newOptions,
                            schema.comment());

            boolean success = commit(newSchema);
            if (success) {
                return newSchema;
            }
        }
    }

    private void updateNestedColumn(
            List<DataField> newFields,
            String[] updateFieldNames,
            int index,
            Function<DataField, DataField> updateFunc) {
        boolean found = false;
        for (int i = 0; i < newFields.size(); i++) {
            DataField field = newFields.get(i);
            if (field.name().equals(updateFieldNames[index])) {
                found = true;
                if (index == updateFieldNames.length - 1) {
                    newFields.set(i, updateFunc.apply(field));
                    break;
                } else {
                    assert field.type() instanceof RowDataType;
                    updateNestedColumn(
                            ((RowDataType) field.type()).fields(),
                            updateFieldNames,
                            index + 1,
                            updateFunc);
                }
            }
        }
        if (!found) {
            throw new RuntimeException("Can not find column: " + Arrays.asList(updateFieldNames));
        }
    }

    private void updateColumn(
            List<DataField> newFields,
            String updateFieldName,
            Function<DataField, DataField> updateFunc) {
        updateNestedColumn(newFields, new String[] {updateFieldName}, 0, updateFunc);
    }

    private boolean commit(TableSchema newSchema) throws Exception {
        Path schemaPath = toSchemaPath(newSchema.id());
        FileSystem fs = schemaPath.getFileSystem();
        Callable<Boolean> callable =
                () -> {
                    if (fs.exists(schemaPath)) {
                        return false;
                    }
                    return AtomicFileWriter.writeFileUtf8(schemaPath, newSchema.toString());
                };
        if (lock == null) {
            return callable.call();
        }
        return lock.runWithLock(callable);
    }

    /** Read schema for schema id. */
    public TableSchema schema(long id) {
        try {
            return JsonSerdeUtil.fromJson(
                    FileUtils.readFileUtf8(toSchemaPath(id)), TableSchema.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Path schemaDirectory() {
        return new Path(tableRoot + "/schema");
    }

    private Path toSchemaPath(long id) {
        return new Path(tableRoot + "/schema/" + SCHEMA_PREFIX + id);
    }

    private void checkAlterTableOption(String key) {
        if (BUCKET_KEY.key().equals(key) || WRITE_MODE.key().equals(key)) {
            throw new UnsupportedOperationException(
                    String.format("Change %s is not supported yet.", key));
        }
    }
}
