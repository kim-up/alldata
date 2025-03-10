/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.store.hive.objectinspector;

import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.data.GenericArrayData;
import org.apache.flink.table.data.StringData;

import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/** Tests for {@link TableStoreListObjectInspector}. */
public class TableStoreListObjectInspectorTest {

    @Test
    public void testCategoryAndTypeName() {
        TableStoreListObjectInspector oi =
                new TableStoreListObjectInspector(DataTypes.STRING().getLogicalType());

        assertThat(oi.getCategory()).isEqualTo(ObjectInspector.Category.LIST);
        assertThat(oi.getTypeName()).isEqualTo("array<string>");
    }

    @Test
    public void testGetListAndElement() {
        TableStoreListObjectInspector oi =
                new TableStoreListObjectInspector(DataTypes.STRING().getLogicalType());

        StringData[] stringDataArray =
                new StringData[] {
                    StringData.fromString("Hi"),
                    StringData.fromString("Hello"),
                    null,
                    StringData.fromString("Test")
                };
        GenericArrayData arrayData = new GenericArrayData(stringDataArray);
        assertThat(oi.getListLength(arrayData)).isEqualTo(4);
        for (int i = 0; i < 4; i++) {
            assertThat(oi.getListElement(arrayData, i)).isEqualTo(stringDataArray[i]);
        }
        assertThat(oi.getList(arrayData)).isEqualTo(Arrays.asList(stringDataArray));

        assertThat(oi.getListLength(null)).isEqualTo(-1);
        assertThat(oi.getListElement(null, 3)).isNull();
        assertThat(oi.getList(null)).isNull();
    }
}
