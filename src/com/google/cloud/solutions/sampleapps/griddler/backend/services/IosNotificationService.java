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

import com.google.appengine.api.datastore.Key;
import com.google.cloud.solutions.sampleapps.griddler.backend.api.Configuration;
import com.google.cloud.solutions.sampleapps.griddler.backend.entities.DeviceEntity;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.ResponsePacket;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for sending push notifications to iOS devices.
 */
public class IosNotificationService {
  private static final Logger logger =
      Logger.getLogger(IosNotificationService.class.getSimpleName());
  private static final int INVALID_TOKEN = 8;
  private DeviceService deviceService = new DeviceService();

  /**
   * Sends push notifications to iOS devices. The implementation in this class is simplified and not
   * suitable for production use. Please refer to CloudPushSample at
   * https://github.com/GoogleCloudPlatform/solutions-ios-push-notification-sample-backend-java for
   * a more complete implementation of sending push notifications to iOS devices from App Engine
   * appliactions.
   *
   * @param devices the list of devices that the notification is sent to.
   * @param payload the notification payload.
   * @param playerKey the key of the player that the notification is sent to.
   * @throws CommunicationException if there was an error while sending push notifications.
   */
  protected void sendPushNotification(
      List<DeviceEntity> devices, PushNotificationPayload payload, Key playerKey)
      throws CommunicationException {

    List<BasicDevice> iosDevices = new ArrayList<BasicDevice>();

    for (DeviceEntity device : devices) {
      try {
        iosDevices.add(new BasicDevice(device.getDeviceId()));
      } catch (InvalidDeviceTokenFormatException e) {
        logger.log(Level.INFO, "Invalid device token format: " + device.getDeviceId());
      }
    }

    if (iosDevices.size() == 0) {
      return;
    }

    InputStream certificateStream = null;
    try {

      certificateStream =
          getClass().getClassLoader().getResourceAsStream(Configuration.APNS_CERTIFICATE_FILE);

      List<PushedNotification> notifications = Push.payload(
          payload, certificateStream, Configuration.APNS_CERTIFICATE_PASSWORD, false, iosDevices);

      for (int index = 0; index < notifications.size(); index++) {
        PushedNotification notification = notifications.get(index);

        if (!notification.isSuccessful()) {
          logger.log(Level.INFO, "Some push notifications haven't been delivered",
              notification.getException());

          ResponsePacket responsePacket = notification.getResponse();
          if (responsePacket != null) {
            logger.log(Level.INFO, responsePacket.getMessage());
            if (responsePacket.getStatus() == INVALID_TOKEN) {
              logger.log(Level.INFO, "Invalid device token " + notification.getDevice().getToken());

              deviceService.unregisterDevice(playerKey, devices.get(index).getKey());
            }
          }
        }
      }
    } catch (KeystoreException e) {
      logger.log(Level.SEVERE, "Invalid certificate", e);
    } finally {
      if (certificateStream != null) {
        try {
          certificateStream.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Error closing certificate stream", e);
        }
      }
    }
  }

  /**
   * Processes results from APNs feedback service and removes invalid or inactive devices.
   *
   * @throws CommunicationException if there was an error while contacting the feedback service.
   */
  public void processNotificationFeedback() throws CommunicationException {
    InputStream certificateStream =
        getClass().getClassLoader().getResourceAsStream(Configuration.APNS_CERTIFICATE_FILE);

    try {
      List<Device> devicesToBeUnregistered =
          Push.feedback(certificateStream, Configuration.APNS_CERTIFICATE_PASSWORD, false);

      if (devicesToBeUnregistered == null) {
        return;
      }

      for (Device device : devicesToBeUnregistered) {
        for (DeviceEntity registeredDevice : deviceService.getByDeviceId(device.getToken())) {

          Date dateCreated = registeredDevice.getDateCreated();

          // Unregister the device unless it was registered later than the date provided
          // by the feedback service.

          if (dateCreated == null || dateCreated.getTime() < device.getLastRegister().getTime()) {
            deviceService.unregisterDevice(
                registeredDevice.getParentKey(), registeredDevice.getKey());
          }
        }
      }
    } catch (KeystoreException e) {
      logger.log(Level.SEVERE, "Invalid certificate", e);
    } finally {
      if (certificateStream != null) {
        try {
          certificateStream.close();
        } catch (IOException e) {
          logger.log(Level.WARNING, "Error closing certificate stream", e);
        }
      }
    }
  }
}
