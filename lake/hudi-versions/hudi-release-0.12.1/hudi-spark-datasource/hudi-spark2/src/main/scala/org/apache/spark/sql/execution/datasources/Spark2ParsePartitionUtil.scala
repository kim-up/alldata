/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.datasources

import java.util.TimeZone

import org.apache.hadoop.fs.Path

import org.apache.spark.sql.types._
import org.apache.spark.sql.catalyst.InternalRow

object Spark2ParsePartitionUtil extends SparkParsePartitionUtil {

  override def parsePartition(path: Path,
                              typeInference: Boolean,
                              basePaths: Set[Path],
                              userSpecifiedDataTypes: Map[String, DataType],
                              timeZone: TimeZone,
                              validatePartitionValues: Boolean = false): InternalRow = {
    val (partitionValues, _) = PartitioningUtils.parsePartition(path, typeInference,
      basePaths, userSpecifiedDataTypes, timeZone)

    partitionValues.map(_.literals.map(_.value)).map(InternalRow.fromSeq)
      .getOrElse(InternalRow.empty)
  }
}
