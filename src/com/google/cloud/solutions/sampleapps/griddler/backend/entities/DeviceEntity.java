/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.solutions.sampleapps.griddler.backend.entities;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

/**
 * Device Datastore entity.
 */
public class DeviceEntity extends DatastoreEntity {
  private static final long serialVersionUID = 1;
  /**
   * Name of Device entity kind in Datastore. Public because it is also used for Datastore queries.
   */
  public static final String KIND = "Device";

  /**
   * Names of properties in Datastore. Public if used in queries. Private otherwise.
   */
  public static final String DEVICEID_PROPERTY = "deviceId";
  private static final String DEVICETYPE_PROPERTY = "deviceType";

  /**
   * Android device type.
   */
  public static final int DEVICE_TYPE_ANDROID = 1;

  /**
   * iOS device type.
   */
  public static final int DEVICE_TYPE_IOS = 2;

  /**
   * Constructor used to create a new device Datastore entity.
   *
   * @param deviceId the ID of the device, typically assigned by the push notifications system.
   * @param deviceType the type of device.
   * @param playerKey the key of the player who is using the device.
   */
  public DeviceEntity(String deviceId, Integer deviceType, Key playerKey) {
    super(new Entity(KIND, playerKey), true);

    setProperty(DEVICEID_PROPERTY, deviceId);
    setProperty(DEVICETYPE_PROPERTY, deviceType);
  }

  /**
   * Constructor used when retrieving an existing Device entity from Datastore.
   *
   * @param entity {@link Entity} to populate the model with.
   */
  public DeviceEntity(Entity entity) {
    super(entity, false);
  }

  /**
   * Gets the device ID.
   *
   */
  public String getDeviceId() {
    return getPropertyOfType(DEVICEID_PROPERTY);
  }

  /**
   * Gets the device type.
   *
   */
  public int getDeviceType() {
    Long deviceType = getPropertyOfType(DEVICETYPE_PROPERTY);
    return deviceType.intValue();
  }

  /**
   * Updates the device ID.
   *
   * @param newDeviceId the ID this device should be changed to.
   */
  public void updateDeviceId(String newDeviceId) {
    setProperty(DEVICEID_PROPERTY, newDeviceId);
  }
}
