/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.management.api;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import org.apache.geode.management.configuration.AbstractConfiguration;
import org.apache.geode.management.configuration.Region;
import org.apache.geode.management.runtime.RuntimeInfo;
import org.apache.geode.management.runtime.RuntimeRegionInfo;
import org.apache.geode.util.internal.GeodeJsonMapper;

public class ConfigurationInfoTest {
  abstract static class TestRuntimeInfo extends RuntimeInfo {
  }

  abstract static class TestConfiguration extends AbstractConfiguration<TestRuntimeInfo> {
  }

  @Test
  public void extractsRuntimeInfosFromGroupResults() {
    List<String> groups = asList("group1", "group2", "group3");

    Map<String, List<TestRuntimeInfo>> runtimeInfosPerGroup = runtimeInfosFor(groups);

    List<ConfigurationResult<TestConfiguration, TestRuntimeInfo>> groupResults =
        createGroupResults(configurationsFor(groups), runtimeInfosPerGroup);

    ConfigurationInfo<TestConfiguration, TestRuntimeInfo> configurationInfo =
        new ConfigurationInfo<>("some.element.id", groupResults);

    List<TestRuntimeInfo> runtimeInfosForAllGroups = runtimeInfosPerGroup.values().stream()
        .flatMap(List::stream)
        .collect(toList());

    assertThat(configurationInfo.getRuntimeInfos())
        .containsExactlyInAnyOrderElementsOf(runtimeInfosForAllGroups);
  }

  @Test
  public void extractsConfigurationsFromGroupResults() {
    List<String> groups = asList("group1", "group2", "group3");

    Map<String, TestConfiguration> configurationPerGroup = configurationsFor(groups);

    List<ConfigurationResult<TestConfiguration, TestRuntimeInfo>> groupResults =
        createGroupResults(configurationPerGroup, runtimeInfosFor(groups));

    ConfigurationInfo<TestConfiguration, TestRuntimeInfo> configurationInfo =
        new ConfigurationInfo<>("some.element.id", groupResults);

    Collection<TestConfiguration> configurationsForAllGroups = configurationPerGroup.values();

    assertThat(configurationInfo.getConfigurations())
        .containsExactlyInAnyOrderElementsOf(configurationsForAllGroups);
  }

  @Test
  public void serialization() throws JsonProcessingException {

    Region region = new Region();
    RuntimeRegionInfo runtimeRegionInfo = new RuntimeRegionInfo();

    ConfigurationResult<Region, RuntimeRegionInfo> configurationResult =
        new ConfigurationResult<>(region, singletonList(runtimeRegionInfo));

    ConfigurationInfo<Region, RuntimeRegionInfo> original =
        new ConfigurationInfo<>("my.element", singletonList(configurationResult));

    ObjectMapper mapper = GeodeJsonMapper.getMapper();
    String json = mapper.writeValueAsString(original);

    assertThat(json)
        .containsOnlyOnce("id")
        .containsOnlyOnce("groupResult");

    System.out.println(json);

    ConfigurationInfo<Region, RuntimeRegionInfo> deserialized =
        mapper.readValue(json, ConfigurationInfo.class);

    System.out.println(mapper.writeValueAsString(deserialized));

    assertThat(deserialized).isEqualTo(original);
  }

  private static Map<String, TestConfiguration> configurationsFor(Collection<String> groups) {
    return groups.stream()
        .collect(toMap(group -> group, group -> mock(TestConfiguration.class)));
  }

  private static Map<String, List<TestRuntimeInfo>> runtimeInfosFor(Collection<String> groups) {
    return groups.stream()
        .collect(toMap(group -> group, group -> listOf(3, TestRuntimeInfo.class)));
  }

  private static List<ConfigurationResult<TestConfiguration, TestRuntimeInfo>> createGroupResults(
      Map<String, TestConfiguration> configurationPerGroup,
      Map<String, List<TestRuntimeInfo>> runtimeInfosPerGroup) {
    return configurationPerGroup.keySet().stream()
        .map(group -> new ConfigurationResult<>(configurationPerGroup.get(group),
            runtimeInfosPerGroup.get(group)))
        .collect(toList());
  }

  private static <T> List<T> listOf(int count, Class<T> type) {
    List<T> list = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      list.add(mock(type));
    }
    return list;
  }
}