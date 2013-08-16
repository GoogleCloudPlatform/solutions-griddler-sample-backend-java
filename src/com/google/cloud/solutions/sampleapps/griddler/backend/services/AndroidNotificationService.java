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

package com.google.cloud.solutions.sampleapps.griddler.backend.services;

import com.google.android.gcm.server.Constants;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.google.appengine.api.datastore.Key;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.Configuration;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.DeviceEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for sending push notifications to Android devices using Google Cloud Messaging for
 * Android.
 */
public class AndroidNotificationService {
  private static final Logger logger =
      Logger.getLogger(AndroidNotificationService.class.getSimpleName());
  private static final int NUMBER_OF_RETRIES = 3;

  /**
   * Sends a message to Android devices.
   *
   * @param devices the list of devices that the notification is sent to.
   * @param message the message to be sent.
   * @param playerKey the key of the player that the notification is sent to.
   * @throws IOException if there was IOException thrown by Google Cloud Messaging for Android.
   */
  protected void sendMessage(List<DeviceEntity> devices, Message message, Key playerKey)
      throws IOException {
    List<String> androidDeviceIds = new ArrayList<String>();
    for (DeviceEntity device : devices) {
      androidDeviceIds.add(device.getDeviceId());
    }

    Sender messageSender = new Sender(Configuration.CLOUD_MESSAGING_API_KEY);
    MulticastResult messageResults =
        messageSender.send(message, androidDeviceIds, NUMBER_OF_RETRIES);

    if (messageResults != null) {
      DeviceService deviceService = new DeviceService();

      for (int i = 0; i < messageResults.getTotal(); i++) {
        Result result = messageResults.getResults().get(i);

        if (result.getMessageId() != null) {
          String canonicalRegId = result.getCanonicalRegistrationId();
          if (canonicalRegId != null) {
            deviceService.updateDeviceRegistration(devices.get(i), canonicalRegId);
          }
        } else {
          String error = result.getErrorCodeName();
          if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
            // The user has uninstalled the application or turned off notifications.
            // Remove the device from Griddler.
            deviceService.unregisterDevice(playerKey, devices.get(i).getKey());
          } else {
            logger.log(Level.INFO, "Error when sending Android push notification: " + error);
          }
        }
      }
    }
  }
}
