/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iceberg.hadoop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;
import org.apache.iceberg.FileScanTask;
import org.apache.iceberg.HasTableOperations;
import org.apache.iceberg.MetadataTableType;
import org.apache.iceberg.SerializableTable;
import org.apache.iceberg.Table;
import org.apache.iceberg.TableProperties;
import org.apache.iceberg.TestHelpers;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;
import org.apache.iceberg.relocated.com.google.common.collect.Sets;
import org.apache.iceberg.types.Types;
import org.junit.Assert;
import org.junit.Test;

public class TestTableSerialization extends HadoopTableTestBase {

  @Test
  public void testSerializableTable() throws IOException, ClassNotFoundException {
    table.replaceSortOrder().asc("id").commit();

    table.updateProperties().set("k1", "v1").set("k2", "v2").commit();

    table.updateSchema().addColumn("new_col", Types.IntegerType.get()).commit();

    TestHelpers.assertSerializedAndLoadedMetadata(table, TestHelpers.roundTripSerialize(table));
    Table serializableTable = SerializableTable.copyOf(table);
    TestHelpers.assertSerializedAndLoadedMetadata(
        serializableTable, TestHelpers.KryoHelpers.roundTripSerialize(serializableTable));
  }

  @Test
  public void testSerializableTableWithSnapshot() throws IOException, ClassNotFoundException {
    table.newAppend().appendFile(FILE_A).commit();
    TestHelpers.assertSerializedAndLoadedMetadata(table, TestHelpers.roundTripSerialize(table));
    Table serializableTable = SerializableTable.copyOf(table);
    TestHelpers.assertSerializedAndLoadedMetadata(
        serializableTable, TestHelpers.KryoHelpers.roundTripSerialize(serializableTable));
  }

  @Test
  public void testSerializableTxnTable() throws IOException, ClassNotFoundException {
    table.replaceSortOrder().asc("id").commit();

    table.updateProperties().set("k1", "v1").set("k2", "v2").commit();

    table.updateSchema().addColumn("new_col", Types.IntegerType.get()).commit();

    Transaction txn = table.newTransaction();

    txn.updateProperties().set("k3", "v3").commit();

    // txn tables have metadata locations as null so we check only serialized metadata
    TestHelpers.assertSerializedMetadata(txn.table(), TestHelpers.roundTripSerialize(txn.table()));
  }

  @Test
  public void testSerializableMetadataTable() throws IOException, ClassNotFoundException {
    for (MetadataTableType type : MetadataTableType.values()) {
      Table metadataTable = getMetaDataTable(table, type);
      TestHelpers.assertSerializedAndLoadedMetadata(
          metadataTable, TestHelpers.roundTripSerialize(metadataTable));
      Table serializableTable = SerializableTable.copyOf(metadataTable);
      TestHelpers.assertSerializedAndLoadedMetadata(
          serializableTable, TestHelpers.KryoHelpers.roundTripSerialize(serializableTable));
    }
  }

  @Test
  public void testSerializableTablePlanning() throws IOException {
    table.newAppend().appendFile(FILE_A).commit();

    byte[] serialized = serializeToBytes(table);

    Set<CharSequence> expected = getFiles(table);

    table.newAppend().appendFile(FILE_B).commit();

    Table deserialized = deserializeFromBytes(serialized);

    Set<CharSequence> deserializedFiles = getFiles(deserialized);

    // Checks that the deserialized data stays the same
    Assert.assertEquals(expected, deserializedFiles);

    // We expect that the files changed in the meantime
    Assert.assertNotEquals(getFiles(table), deserializedFiles);
  }

  @Test
  public void testSerializableMetadataTablesPlanning() throws IOException {
    table.updateProperties().set(TableProperties.FORMAT_VERSION, "2").commit();

    table.newAppend().appendFile(FILE_A).commit();

    Map<MetadataTableType, byte[]> serialized = Maps.newHashMap();
    Map<MetadataTableType, Set<CharSequence>> expected = Maps.newHashMap();
    for (MetadataTableType type : MetadataTableType.values()) {
      Table metaTable = getMetaDataTable(table, type);
      // Serialize the table
      serialized.put(type, serializeToBytes(metaTable));

      // Collect the expected result
      expected.put(type, getFiles(metaTable));
    }

    table.newAppend().appendFile(FILE_B).commit();
    table.newRowDelta().addDeletes(FILE_B_DELETES).commit();

    for (MetadataTableType type : MetadataTableType.values()) {
      // Collect the deserialized data
      Set<CharSequence> deserializedFiles = getFiles(deserializeFromBytes(serialized.get(type)));

      // Checks that the deserialized data stays the same
      Assert.assertEquals(expected.get(type), deserializedFiles);

      // Collect the current data
      Set<CharSequence> newFiles = getFiles(getMetaDataTable(table, type));

      // Expect that the new data is changed in the meantime
      Assert.assertNotEquals(newFiles, deserializedFiles);
    }
  }

  private static Table getMetaDataTable(Table table, MetadataTableType type) {
    return TABLES.load(
        ((HasTableOperations) table).operations().current().metadataFileLocation() + "#" + type);
  }

  private static Set<CharSequence> getFiles(Table table) throws IOException {
    Set<CharSequence> files = Sets.newHashSet();
    try (CloseableIterable<FileScanTask> tasks = table.newScan().planFiles()) {
      for (FileScanTask task : tasks) {
        files.add(task.file().path());
      }
    }

    return files;
  }

  private static byte[] serializeToBytes(Object obj) {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(obj);
      return baos.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to serialize object", e);
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> T deserializeFromBytes(byte[] bytes) {
    if (bytes == null) {
      return null;
    }

    try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais)) {
      return (T) ois.readObject();
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to deserialize object", e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("Could not read object ", e);
    }
  }
}
