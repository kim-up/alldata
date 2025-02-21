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

package org.apache.hudi.adapter;

import org.apache.flink.api.common.accumulators.Accumulator;
import org.apache.flink.metrics.groups.OperatorMetricGroup;
import org.apache.flink.metrics.groups.UnregisteredMetricsGroup;
import org.apache.flink.runtime.execution.Environment;
import org.apache.flink.streaming.api.operators.AbstractStreamOperator;
import org.apache.flink.streaming.api.operators.StreamingRuntimeContext;

import java.util.Map;

/**
 * Adapter clazz for {@link StreamingRuntimeContext}.
 */
public class StreamingRuntimeContextAdapter extends StreamingRuntimeContext {

  public StreamingRuntimeContextAdapter(AbstractStreamOperator<?> operator, Environment env,
                                        Map<String, Accumulator<?, ?>> accumulators) {
    super(operator, env, accumulators);
  }

  @Override
  public OperatorMetricGroup getMetricGroup() {
    return UnregisteredMetricsGroup.createOperatorMetricGroup();
  }
}
