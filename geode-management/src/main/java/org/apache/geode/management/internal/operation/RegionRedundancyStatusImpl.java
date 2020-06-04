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
package org.apache.geode.management.internal.operation;

import org.apache.geode.management.runtime.RegionRedundancyStatus;

/**
 * result object used by the cms that only needs to be json serializable
 */
public class RegionRedundancyStatusImpl implements RegionRedundancyStatus {

  public static final String OUTPUT_STRING =
      "%s redundancy status: %s. Desired redundancy is %s and actual redundancy is %s.";

  /**
   * The name of the region used to create this object.
   */
  protected String regionName;

  /**
   * The configured redundancy of the region used to create this object.
   */
  protected int configuredRedundancy;

  /**
   * The actual redundancy of the region used to create this object at time of creation.
   */
  protected int actualRedundancy;

  /**
   * The {@link RedundancyStatus} of the region used to create this object at time of creation.
   */
  protected RedundancyStatus status;

  /**
   * Default constructor used for serialization
   */
  public RegionRedundancyStatusImpl() {}

  public RegionRedundancyStatusImpl(int configuredRedundancy, int actualRedundancy,
      String regionName, RedundancyStatus status) {
    this.configuredRedundancy = configuredRedundancy;
    this.actualRedundancy = actualRedundancy;
    this.regionName = regionName;
    this.status = status;
  }

  @Override
  public String getRegionName() {
    return regionName;
  }

  @Override
  public int getConfiguredRedundancy() {
    return configuredRedundancy;
  }

  @Override
  public int getActualRedundancy() {
    return actualRedundancy;
  }

  @Override
  public RedundancyStatus getStatus() {
    return status;
  }


  /**
   * Determines the {@link RedundancyStatus} for the region. If redundancy is not configured (i.e.
   * configured redundancy = 0), this always returns {@link RedundancyStatus#SATISFIED}.
   *
   * @param desiredRedundancy The configured redundancy of the region.
   * @param actualRedundancy The actual redundancy of the region.
   * @return The {@link RedundancyStatus} for the region.
   */
  private RedundancyStatus determineStatus(int desiredRedundancy, int actualRedundancy) {
    boolean zeroRedundancy = desiredRedundancy == 0;
    if (actualRedundancy == 0) {
      return zeroRedundancy ? RedundancyStatus.SATISFIED : RedundancyStatus.NO_REDUNDANT_COPIES;
    }
    return desiredRedundancy == actualRedundancy ? RedundancyStatus.SATISFIED
        : RedundancyStatus.NOT_SATISFIED;
  }

  @Override
  public String toString() {
    return String.format(OUTPUT_STRING, regionName, status.name(), configuredRedundancy,
        actualRedundancy);
  }
}
