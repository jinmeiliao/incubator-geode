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
package org.apache.geode.internal.cache.control;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.geode.DataSerializer;
import org.apache.geode.cache.partition.PartitionRebalanceInfo;
import org.apache.geode.internal.serialization.DataSerializableFixedID;
import org.apache.geode.internal.serialization.DeserializationContext;
import org.apache.geode.internal.serialization.SerializationContext;
import org.apache.geode.internal.serialization.Version;
import org.apache.geode.management.internal.operation.RestoreRedundancyResultsImpl;

/**
 * result object produced by the servers. These need to be transferred to the locators
 * via functions so they need to be DataSerializable
 */
public class SerializableRestoreRedundancyResultsImpl
    extends RestoreRedundancyResultsImpl
    implements DataSerializableFixedID {

  public void addPrimaryReassignmentDetails(PartitionRebalanceInfo details) {
    this.totalPrimaryTransfersCompleted += details.getPrimaryTransfersCompleted();
    this.totalPrimaryTransferTime =
        this.totalPrimaryTransferTime.plusMillis(details.getPrimaryTransferTime());
  }

  @Override
  public int getDSFID() {
    return RESTORE_REDUNDANCY_RESULTS;
  }

  @Override
  public void toData(DataOutput out, SerializationContext context) throws IOException {
    DataSerializer.writeHashMap(satisfiedRedundancyRegions, out);
    DataSerializer.writeHashMap(underRedundancyRegions, out);
    DataSerializer.writeHashMap(zeroRedundancyRegions, out);
    out.writeInt(totalPrimaryTransfersCompleted);
    DataSerializer.writeObject(totalPrimaryTransferTime, out);
    out.writeBoolean(success);
    DataSerializer.writeString(statusMessage, out);
  }

  @Override
  public void fromData(DataInput in, DeserializationContext context)
      throws IOException, ClassNotFoundException {
    this.satisfiedRedundancyRegions = DataSerializer.readHashMap(in);
    this.underRedundancyRegions = DataSerializer.readHashMap(in);
    this.zeroRedundancyRegions = DataSerializer.readHashMap(in);
    this.totalPrimaryTransfersCompleted = in.readInt();
    this.totalPrimaryTransferTime = DataSerializer.readObject(in);
    this.success = DataSerializer.readBoolean(in);
    this.statusMessage = DataSerializer.readString(in);
  }

  @Override
  public Version[] getSerializationVersions() {
    return null;
  }
}
