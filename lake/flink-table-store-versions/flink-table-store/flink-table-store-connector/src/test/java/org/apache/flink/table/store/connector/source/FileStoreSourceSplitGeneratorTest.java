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

package org.apache.flink.table.store.connector.source;

import org.apache.flink.table.store.file.data.DataFileMeta;
import org.apache.flink.table.store.file.manifest.FileKind;
import org.apache.flink.table.store.file.manifest.ManifestEntry;
import org.apache.flink.table.store.file.operation.FileStoreScan;
import org.apache.flink.table.store.file.stats.StatsTestUtils;
import org.apache.flink.table.store.table.source.Split;
import org.apache.flink.table.store.table.source.TableScan;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.flink.table.store.file.mergetree.compact.MergeTreeCompactManagerTest.row;
import static org.assertj.core.api.Assertions.assertThat;

/** Test for {@link FileStoreSourceSplitGenerator}. */
public class FileStoreSourceSplitGeneratorTest {

    @TempDir java.nio.file.Path tempDir;

    @Test
    public void test() {
        FileStoreScan.Plan plan =
                new FileStoreScan.Plan() {
                    @Nullable
                    @Override
                    public Long snapshotId() {
                        return 1L;
                    }

                    @Override
                    public List<ManifestEntry> files() {
                        return Arrays.asList(
                                makeEntry(1, 0, "f0"),
                                makeEntry(1, 0, "f1"),
                                makeEntry(1, 1, "f2"),
                                makeEntry(2, 0, "f3"),
                                makeEntry(2, 0, "f4"),
                                makeEntry(2, 0, "f5"),
                                makeEntry(2, 1, "f6"),
                                makeEntry(3, 0, "f7"),
                                makeEntry(3, 1, "f8"),
                                makeEntry(4, 0, "f9"),
                                makeEntry(4, 1, "f10"),
                                makeEntry(5, 0, "f11"),
                                makeEntry(5, 1, "f12"),
                                makeEntry(6, 0, "f13"),
                                makeEntry(6, 1, "f14"));
                    }
                };
        List<Split> scanSplits =
                TableScan.generateSplits(
                        false, Collections::singletonList, plan.groupByPartFiles());
        TableScan.Plan tableScanPlan = new TableScan.Plan(1L, scanSplits);

        List<FileStoreSourceSplit> splits =
                new FileStoreSourceSplitGenerator().createSplits(tableScanPlan);
        assertThat(splits.size()).isEqualTo(12);
        splits.sort(
                Comparator.comparingInt(
                                o -> ((FileStoreSourceSplit) o).split().partition().getInt(0))
                        .thenComparing(o -> ((FileStoreSourceSplit) o).split().bucket()));
        assertSplit(splits.get(0), "0000000007", 1, 0, Arrays.asList("f0", "f1"));
        assertSplit(splits.get(1), "0000000008", 1, 1, Collections.singletonList("f2"));
        assertSplit(splits.get(2), "0000000003", 2, 0, Arrays.asList("f3", "f4", "f5"));
        assertSplit(splits.get(3), "0000000004", 2, 1, Collections.singletonList("f6"));
        assertSplit(splits.get(4), "0000000001", 3, 0, Collections.singletonList("f7"));
        assertSplit(splits.get(5), "0000000002", 3, 1, Collections.singletonList("f8"));
        assertSplit(splits.get(6), "0000000011", 4, 0, Collections.singletonList("f9"));
        assertSplit(splits.get(7), "0000000012", 4, 1, Collections.singletonList("f10"));
        assertSplit(splits.get(8), "0000000005", 5, 0, Collections.singletonList("f11"));
        assertSplit(splits.get(9), "0000000006", 5, 1, Collections.singletonList("f12"));
        assertSplit(splits.get(10), "0000000009", 6, 0, Collections.singletonList("f13"));
        assertSplit(splits.get(11), "0000000010", 6, 1, Collections.singletonList("f14"));
    }

    private void assertSplit(
            FileStoreSourceSplit split, String splitId, int part, int bucket, List<String> files) {
        assertThat(split.splitId()).isEqualTo(splitId);
        assertThat(split.split().partition().getInt(0)).isEqualTo(part);
        assertThat(split.split().bucket()).isEqualTo(bucket);
        assertThat(
                        split.split().files().stream()
                                .map(DataFileMeta::fileName)
                                .collect(Collectors.toList()))
                .isEqualTo(files);
    }

    private ManifestEntry makeEntry(int partition, int bucket, String fileName) {
        return new ManifestEntry(
                FileKind.ADD,
                row(partition), // not used
                bucket, // not used
                0, // not used
                new DataFileMeta(
                        fileName,
                        0, // not used
                        0, // not used
                        null, // not used
                        null, // not used
                        StatsTestUtils.newEmptyTableStats(), // not used
                        StatsTestUtils.newEmptyTableStats(), // not used
                        0, // not used
                        0, // not used
                        0, // not used
                        0 // not used
                        ));
    }
}
