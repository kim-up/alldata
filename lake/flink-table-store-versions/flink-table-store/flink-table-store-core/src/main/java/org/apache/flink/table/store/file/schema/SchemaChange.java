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

import org.apache.flink.table.types.logical.LogicalType;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

/** Schema change to table. */
public interface SchemaChange {

    static SchemaChange setOption(String key, String value) {
        return new SetOption(key, value);
    }

    static SchemaChange removeOption(String key) {
        return new RemoveOption(key);
    }

    static SchemaChange addColumn(String fieldName, LogicalType logicalType) {
        return addColumn(fieldName, logicalType, true, null);
    }

    static SchemaChange addColumn(
            String fieldName, LogicalType logicalType, boolean isNullable, String comment) {
        return new AddColumn(fieldName, logicalType, isNullable, comment);
    }

    static SchemaChange updateColumnType(String fieldName, LogicalType newLogicalType) {
        return new UpdateColumnType(fieldName, newLogicalType);
    }

    static SchemaChange updateColumnNullability(String[] fieldNames, boolean newNullability) {
        return new UpdateColumnNullability(fieldNames, newNullability);
    }

    static SchemaChange updateColumnComment(String[] fieldNames, String comment) {
        return new UpdateColumnComment(fieldNames, comment);
    }

    /** A SchemaChange to set a table option. */
    final class SetOption implements SchemaChange {
        private final String key;
        private final String value;

        private SetOption(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String key() {
            return key;
        }

        public String value() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            SetOption that = (SetOption) o;
            return key.equals(that.key) && value.equals(that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }

    /** A SchemaChange to remove a table option. */
    final class RemoveOption implements SchemaChange {
        private final String key;

        private RemoveOption(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RemoveOption that = (RemoveOption) o;
            return key.equals(that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
    }

    /** A SchemaChange to add a field. */
    final class AddColumn implements SchemaChange {
        private final String fieldName;
        private final LogicalType logicalType;
        private final boolean isNullable;
        private final String description;

        private AddColumn(
                String fieldName, LogicalType logicalType, boolean isNullable, String description) {
            this.fieldName = fieldName;
            this.logicalType = logicalType;
            this.isNullable = isNullable;
            this.description = description;
        }

        public String fieldName() {
            return fieldName;
        }

        public LogicalType logicalType() {
            return logicalType;
        }

        public boolean isNullable() {
            return isNullable;
        }

        @Nullable
        public String description() {
            return description;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            AddColumn addColumn = (AddColumn) o;
            return isNullable == addColumn.isNullable
                    && Objects.equals(fieldName, addColumn.fieldName)
                    && logicalType.equals(addColumn.logicalType)
                    && Objects.equals(description, addColumn.description);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(logicalType, isNullable, description);
            result = 31 * result + Objects.hashCode(fieldName);
            return result;
        }
    }

    /** A SchemaChange to update the field type. */
    final class UpdateColumnType implements SchemaChange {
        private final String fieldName;
        private final LogicalType newLogicalType;

        private UpdateColumnType(String fieldName, LogicalType newLogicalType) {
            this.fieldName = fieldName;
            this.newLogicalType = newLogicalType;
        }

        public String fieldName() {
            return fieldName;
        }

        public LogicalType newLogicalType() {
            return newLogicalType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            UpdateColumnType that = (UpdateColumnType) o;
            return Objects.equals(fieldName, that.fieldName)
                    && newLogicalType.equals(that.newLogicalType);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(newLogicalType);
            result = 31 * result + Objects.hashCode(fieldName);
            return result;
        }
    }

    /** A SchemaChange to update the (nested) field nullability. */
    final class UpdateColumnNullability implements SchemaChange {
        private final String[] fieldNames;
        private final boolean newNullability;

        public UpdateColumnNullability(String[] fieldNames, boolean newNullability) {
            this.fieldNames = fieldNames;
            this.newNullability = newNullability;
        }

        public String[] fieldNames() {
            return fieldNames;
        }

        public boolean newNullability() {
            return newNullability;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UpdateColumnNullability)) {
                return false;
            }
            UpdateColumnNullability that = (UpdateColumnNullability) o;
            return newNullability == that.newNullability
                    && Arrays.equals(fieldNames, that.fieldNames);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(newNullability);
            result = 31 * result + Arrays.hashCode(fieldNames);
            return result;
        }
    }

    /** A SchemaChange to update the (nested) field comment. */
    final class UpdateColumnComment implements SchemaChange {
        private final String[] fieldNames;
        private final String newDescription;

        public UpdateColumnComment(String[] fieldNames, String newDescription) {
            this.fieldNames = fieldNames;
            this.newDescription = newDescription;
        }

        public String[] fieldNames() {
            return fieldNames;
        }

        public String newDescription() {
            return newDescription;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UpdateColumnComment)) {
                return false;
            }
            UpdateColumnComment that = (UpdateColumnComment) o;
            return Arrays.equals(fieldNames, that.fieldNames)
                    && newDescription.equals(that.newDescription);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(newDescription);
            result = 31 * result + Arrays.hashCode(fieldNames);
            return result;
        }
    }
}
