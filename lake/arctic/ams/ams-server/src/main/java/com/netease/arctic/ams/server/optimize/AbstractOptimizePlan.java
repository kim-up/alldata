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

package com.netease.arctic.ams.server.optimize;

import com.netease.arctic.ams.api.OptimizeType;
import com.netease.arctic.ams.server.model.BasicOptimizeTask;
import com.netease.arctic.ams.server.model.TableOptimizeRuntime;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.TableIdentifier;
import com.netease.arctic.table.TableProperties;
import com.netease.arctic.utils.CompatiblePropertyUtil;
import org.apache.iceberg.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractOptimizePlan {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractOptimizePlan.class);

  protected final ArcticTable arcticTable;
  protected final TableOptimizeRuntime tableOptimizeRuntime;
  protected final int queueId;
  protected final long currentTime;
  protected final String planGroup;
  private final long currentSnapshotId;

  // all partitions
  protected final Set<String> allPartitions = new HashSet<>();
  // partitions to optimizing
  protected final Set<String> affectedPartitions = new HashSet<>();
  
  private int collectFileCnt = 0;

  public AbstractOptimizePlan(ArcticTable arcticTable, TableOptimizeRuntime tableOptimizeRuntime,
                              int queueId, long currentTime, long currentSnapshotId) {
    this.arcticTable = arcticTable;
    this.tableOptimizeRuntime = tableOptimizeRuntime;
    this.queueId = queueId;
    this.currentTime = currentTime;
    this.currentSnapshotId = currentSnapshotId;
    this.planGroup = UUID.randomUUID().toString();
  }

  public TableIdentifier tableId() {
    return arcticTable.id();
  }

  public OptimizePlanResult plan() {
    long startTime = System.nanoTime();

    addOptimizeFiles();

    if (!hasFileToOptimize()) {
      return buildOptimizePlanResult(Collections.emptyList());
    }

    List<BasicOptimizeTask> tasks = collectTasks(getPartitionsToOptimizeInOrder());

    long endTime = System.nanoTime();
    LOG.info("{} ==== {} plan tasks get {} tasks, cost {} ns, {} ms", tableId(), getOptimizeType(), tasks.size(),
        endTime - startTime, (endTime - startTime) / 1_000_000);
    return buildOptimizePlanResult(tasks);
  }

  protected List<BasicOptimizeTask> collectTasks(List<String> partitions) {
    List<BasicOptimizeTask> results = new ArrayList<>();

    for (String partition : partitions) {
      List<BasicOptimizeTask> optimizeTasks = collectTask(partition);
      if (optimizeTasks.size() > 0) {
        this.affectedPartitions.add(partition);
        LOG.info("{} partition {} ==== collect {} {} tasks", tableId(), partition, optimizeTasks.size(),
            getOptimizeType());
        results.addAll(optimizeTasks);
        if (reachMaxFileCnt(optimizeTasks)) {
          LOG.info("{} get enough files {} > {}, ignore left partitions", tableId(), this.collectFileCnt,
              getMaxFileCntLimit());
          break;
        }
      }
    }
    LOG.info("{} ==== after collect, get {} task of partitions {}/{}", tableId(), getOptimizeType(),
        affectedPartitions.size(), partitions.size());
    return results;
  }

  private boolean reachMaxFileCnt(List<BasicOptimizeTask> newTasks) {
    int newFileCnt = 0;
    for (BasicOptimizeTask optimizeTask : newTasks) {
      int taskFileCnt = optimizeTask.getBaseFileCnt() + optimizeTask.getDeleteFileCnt() +
          optimizeTask.getInsertFileCnt() + optimizeTask.getPosDeleteFileCnt();
      newFileCnt += taskFileCnt;
    }
    this.collectFileCnt += newFileCnt;
    return limitFileCnt() && this.collectFileCnt >= getMaxFileCntLimit();
  }

  private OptimizePlanResult buildOptimizePlanResult(List<BasicOptimizeTask> optimizeTasks) {
    return new OptimizePlanResult(this.affectedPartitions, optimizeTasks, getOptimizeType(), this.currentSnapshotId,
        getCurrentChangeSnapshotId(), this.planGroup);
  }

  protected List<String> getPartitionsToOptimizeInOrder() {
    List<String> partitionNeedOptimizedInOrder = allPartitions.stream()
        .filter(this::partitionNeedPlan)
        .map(partition -> new PartitionWeight(partition, getPartitionWeight(partition)))
        .sorted()
        .map(PartitionWeight::getPartition)
        .collect(Collectors.toList());
    if (partitionNeedOptimizedInOrder.size() > 0) {
      LOG.info("{} filter partitions to optimize, partition count {}", tableId(),
          partitionNeedOptimizedInOrder.size());
    } else {
      LOG.debug("{} filter partitions to optimize, partition count 0", tableId());
    }
    return partitionNeedOptimizedInOrder;
  }

  protected long getPartitionWeight(String partitionToPath) {
    return 0;
  }

  private long getMaxFileCntLimit() {
    Map<String, String> properties = arcticTable.asUnkeyedTable().properties();
    return CompatiblePropertyUtil.propertyAsInt(properties,
        TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT, TableProperties.SELF_OPTIMIZING_MAX_FILE_CNT_DEFAULT);
  }

  protected long getSmallFileSize(Map<String, String> properties) {
    if (!properties.containsKey(TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO) &&
        properties.containsKey(TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD)) {
      return Long.parseLong(properties.get(TableProperties.OPTIMIZE_SMALL_FILE_SIZE_BYTES_THRESHOLD));
    } else {
      long targetSize = PropertyUtil.propertyAsLong(properties, TableProperties.SELF_OPTIMIZING_TARGET_SIZE,
          TableProperties.SELF_OPTIMIZING_TARGET_SIZE_DEFAULT);
      int fragmentRatio = PropertyUtil.propertyAsInt(properties, TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO,
          TableProperties.SELF_OPTIMIZING_FRAGMENT_RATIO_DEFAULT);
      return targetSize / fragmentRatio;
    }
  }

  private static class PartitionWeight implements Comparable<PartitionWeight> {
    private final String partition;
    private final long weight;

    public PartitionWeight(String partition, long weight) {
      this.partition = partition;
      this.weight = weight;
    }

    public String getPartition() {
      return partition;
    }

    public long getWeight() {
      return weight;
    }

    @Override
    public String toString() {
      return "[" + partition + ":" + weight + "]";
    }

    @Override
    public int compareTo(PartitionWeight o) {
      return Long.compare(o.weight, this.weight);
    }
  }

  protected long getCurrentSnapshotId() {
    return this.currentSnapshotId;
  }

  protected long getCurrentChangeSnapshotId() {
    return TableOptimizeRuntime.INVALID_SNAPSHOT_ID;
  }

  protected int getCollectFileCnt() {
    return collectFileCnt;
  }

  /**
   * this optimizing plan should limit the files by "self-optimizing.max-file-count"
   *
   * @return true for limit file cnt
   */
  protected abstract boolean limitFileCnt();

  /**
   * check whether partition need to plan
   *
   * @param partitionToPath target partition
   * @return whether partition need to plan. if true, partition try to plan, otherwise skip.
   */
  protected abstract boolean partitionNeedPlan(String partitionToPath);

  /**
   * init optimize files structure, such as construct NodeTree for ArcticTable
   */
  protected abstract void addOptimizeFiles();

  /**
   * check whether table has files need to optimize after addOptimizeFiles
   *
   * @return whether table has files need to optimize, if true, table try to plan, otherwise skip.
   */
  protected abstract boolean hasFileToOptimize();

  /**
   * collect tasks of given partition
   *
   * @param partition target partition
   * @return tasks of given partition
   */
  protected abstract List<BasicOptimizeTask> collectTask(String partition);

  protected abstract OptimizeType getOptimizeType();
}
