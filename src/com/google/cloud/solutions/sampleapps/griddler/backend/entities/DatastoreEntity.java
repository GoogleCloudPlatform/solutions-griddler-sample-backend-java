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

import java.io.Serializable;
import java.util.Date;

/**
 * Base class encapsulating basic functionality for Griddler Datastore entities.
 */
public abstract class DatastoreEntity implements Serializable {
  private static final long serialVersionUID = 1;
  private Entity entity = null;

  /**
   * Datastore property name.
   */
  public static final String DATECREATED_PROPERTY = "dateCreated";

  /**
   * Constructor.
   *
   * @param entity the {@link Entity}.
   * @param isNew indicates whether the entity is newly created or retrieved from Datastore.
   * @throws IllegalArgumentException if entity is null.
   */
  protected DatastoreEntity(Entity entity, boolean isNew) {
    if (entity == null) {
      throw new IllegalArgumentException("entity cannot be null");
    }

    this.entity = entity;

    if (isNew) {
      this.setProperty(DATECREATED_PROPERTY, new Date());
    }
  }

  /**
   * Sets the underlying Datastore entity instance.
   *
   * @param entity the {@link Entity}.
   * @throws IllegalArgumentException if entity is null.
   */
  public void setEntity(Entity entity) {
    if (entity == null) {
      throw new IllegalArgumentException("entity cannot be null");
    }

    this.entity = entity;
  }

  /**
   * Gets the underlying Datastore entity.
   *
   * @return {@link Entity}.
   */
  public Entity getEntity() {
    return entity;
  }

  /**
   * Gets the entity key.
   *
   * @return {@link Key}.
   */
  public Key getKey() {
    return entity.getKey();
  }

  /**
   * Gets the parent entity key.
   *
   * @return {@link Key}.
   */
  public Key getParentKey() {
    return entity.getParent();
  }

  /**
   * Gets the numeric identifier of the entity key.
   *
   * @return {@link Long}.
   */
  public long getId() {
    return entity.getKey().getId();
  }

  /**
   * Gets the date that the entity was created.
   */
  public Date getDateCreated() {
    return getPropertyOfType(DATECREATED_PROPERTY);
  }

  protected void setProperty(String propertyName, Object value) {
    entity.setProperty(propertyName, value);
  }

  protected Object getProperty(String propertyName) {
    return entity.getProperty(propertyName);
  }

  @SuppressWarnings("unchecked")
  protected <T> T getPropertyOfType(String propertyName) {
    return (T) getProperty(propertyName);
  }
}
