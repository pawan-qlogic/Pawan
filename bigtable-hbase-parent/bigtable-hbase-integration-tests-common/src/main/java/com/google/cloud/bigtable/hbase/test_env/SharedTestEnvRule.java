/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.bigtable.hbase.test_env;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.rules.ExternalResource;

public abstract class SharedTestEnvRule extends ExternalResource {

  public static final int MAX_VERSIONS = 6;
  public static final byte[] COLUMN_FAMILY = Bytes.toBytes("test_family");
  public static final byte[] COLUMN_FAMILY2 = Bytes.toBytes("test_family2");
  private static SharedTestEnvRule instance;

  /**
   * This class is generally a singleton, where implementation can change.  Some startup utility
   * will set this instance.
   */
  public static void setInstance(SharedTestEnvRule instance) {
    SharedTestEnvRule.instance = instance;
  }

  public static SharedTestEnvRule getInstance() {
    return instance;
  }

  protected static final Log LOG = LogFactory.getLog(SharedTestEnvRule.class);
  private TableName defaultTableName;
  private SharedTestEnv sharedTestEnv;
  private Connection connection;

  @Override
  protected void before() throws Throwable {
    sharedTestEnv = SharedTestEnv.get();
    connection = createConnection();

    defaultTableName = newTestTableName();
    createTable(defaultTableName);
  }

  public abstract void createTable(TableName defaultTableName) throws IOException;

  @Override
  protected void after() {
    try (Admin admin = connection.getAdmin()) {
      LOG.info("Deleting table " + defaultTableName.getNameAsString());
      admin.disableTable(defaultTableName);
      admin.deleteTable(defaultTableName);
    } catch (Exception e) {
      throw new RuntimeException("Error deleting table after the integration tests", e);
    }

    try {
      connection.close();
    } catch (IOException e) {
      LOG.error("Failed to close connection after test", e);
    }
    connection = null;
    try {
      sharedTestEnv.release();
    } catch (IOException e) {
      LOG.error("Failed to release the environment after test", e);
    }
    sharedTestEnv = null;
  }

  public Configuration getConfiguration() {
    return sharedTestEnv.getConfiguration();
  }

  public Connection getConnection() {
    return connection;
  }

  public Connection createConnection() throws IOException {
    return ConnectionFactory.createConnection(sharedTestEnv.getConfiguration());
  }

  public Table getDefaultTable() throws IOException {
    return getConnection().getTable(defaultTableName);
  }

  public boolean isBigtable() {
    // TODO(igorbernstein2): clean this up
    return sharedTestEnv instanceof BigtableEnv;
  }

  public TableName getDefaultTableName() {
    return defaultTableName;
  }

  public TableName newTestTableName() {
    return TableName.valueOf("test_table-" + UUID.randomUUID().toString());
  }
}
