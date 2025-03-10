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

package org.apache.flink.table.store.file.mergetree;

import org.apache.flink.annotation.VisibleForTesting;
import org.apache.flink.table.data.RowData;
import org.apache.flink.table.store.file.data.DataFileMeta;
import org.apache.flink.util.Preconditions;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A {@link SortedRun} is a list of files sorted by their keys. The key intervals [minKey, maxKey]
 * of these files do not overlap.
 */
public class SortedRun {

    private final List<DataFileMeta> files;

    private final long totalSize;

    private SortedRun(List<DataFileMeta> files) {
        this.files = Collections.unmodifiableList(files);
        long totalSize = 0L;
        for (DataFileMeta file : files) {
            totalSize += file.fileSize();
        }
        this.totalSize = totalSize;
    }

    public static SortedRun empty() {
        return new SortedRun(Collections.emptyList());
    }

    public static SortedRun fromSingle(DataFileMeta file) {
        return new SortedRun(Collections.singletonList(file));
    }

    public static SortedRun fromSorted(List<DataFileMeta> sortedFiles) {
        return new SortedRun(sortedFiles);
    }

    public static SortedRun fromUnsorted(
            List<DataFileMeta> unsortedFiles, Comparator<RowData> keyComparator) {
        unsortedFiles.sort((o1, o2) -> keyComparator.compare(o1.minKey(), o2.minKey()));
        SortedRun run = new SortedRun(unsortedFiles);
        run.validate(keyComparator);
        return run;
    }

    public List<DataFileMeta> files() {
        return files;
    }

    public boolean nonEmpty() {
        return !files.isEmpty();
    }

    public long totalSize() {
        return totalSize;
    }

    @VisibleForTesting
    public void validate(Comparator<RowData> comparator) {
        for (int i = 1; i < files.size(); i++) {
            Preconditions.checkState(
                    comparator.compare(files.get(i).minKey(), files.get(i - 1).maxKey()) > 0,
                    "SortedRun is not sorted and may contain overlapping key intervals. This is a bug.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SortedRun)) {
            return false;
        }
        SortedRun that = (SortedRun) o;
        return files.equals(that.files);
    }

    @Override
    public int hashCode() {
        return Objects.hash(files);
    }

    @Override
    public String toString() {
        return "["
                + files.stream().map(DataFileMeta::toString).collect(Collectors.joining(", "))
                + "]";
    }
}
