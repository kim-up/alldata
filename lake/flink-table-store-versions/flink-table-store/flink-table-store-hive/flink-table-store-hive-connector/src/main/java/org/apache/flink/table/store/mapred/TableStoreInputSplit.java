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

package org.apache.flink.table.store.mapred;

import org.apache.flink.core.memory.DataInputDeserializer;
import org.apache.flink.core.memory.DataOutputSerializer;
import org.apache.flink.table.store.file.data.DataFileMeta;
import org.apache.flink.table.store.table.source.Split;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapred.FileSplit;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

/**
 * {@link FileSplit} for table store. It contains all files to read from a certain partition and
 * bucket.
 */
public class TableStoreInputSplit extends FileSplit {

    private static final String[] ANYWHERE = new String[] {"*"};

    private String path;
    private Split split;

    // public no-argument constructor for deserialization
    public TableStoreInputSplit() {}

    public TableStoreInputSplit(String path, Split split) {
        this.path = path;
        this.split = split;
    }

    public Split split() {
        return split;
    }

    @Override
    public Path getPath() {
        return new Path(path);
    }

    @Override
    public long getStart() {
        return 0;
    }

    @Override
    public long getLength() {
        return split.files().stream().mapToLong(DataFileMeta::fileSize).sum();
    }

    @Override
    public String[] getLocations() {
        return ANYWHERE;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeUTF(path);
        DataOutputSerializer out = new DataOutputSerializer(128);
        split.serialize(out);
        dataOutput.writeInt(out.length());
        dataOutput.write(out.getCopyOfBuffer());
    }

    @Override
    public void readFields(DataInput dataInput) throws IOException {
        path = dataInput.readUTF();
        int length = dataInput.readInt();
        byte[] bytes = new byte[length];
        dataInput.readFully(bytes);
        split = Split.deserialize(new DataInputDeserializer(bytes));
    }

    @Override
    public String toString() {
        return "{" + "path='" + path + '\'' + ", split=" + split + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TableStoreInputSplit that = (TableStoreInputSplit) o;
        return Objects.equals(path, that.path) && Objects.equals(split, that.split);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, split);
    }
}
