/*
 * Copyright 2019 ThoughtWorks, Inc.
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
package com.thoughtworks.go.server.service.support;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class HibernateInformationProvider implements ServerInfoProvider {

  private SessionFactory sessionFactory;

  @Autowired
  public HibernateInformationProvider(SessionFactory sessionFactory) {
    this.sessionFactory = sessionFactory;
  }

  @Override
  public double priority() {
    return 12.0;
  }


  @Override
  public void write(ServerInfoWriter serverInfoWriter) {
    Statistics statistics = sessionFactory.getStatistics();
    if (!statistics.isStatisticsEnabled()) {
      return;
    }

    serverInfoWriter.add("EntityDeleteCount", statistics.getEntityDeleteCount())
      .add("EntityInsertCount", statistics.getEntityInsertCount())
      .add("EntityLoadCount", statistics.getEntityLoadCount())
      .add("EntityFetchCount", statistics.getEntityFetchCount())
      .add("EntityUpdateCount", statistics.getEntityUpdateCount())
      .add("QueryExecutionCount", statistics.getQueryExecutionCount())
      .add("QueryExecutionMaxTime", statistics.getQueryExecutionMaxTime())
      .add("QueryExecutionMaxTimeQueryString", statistics.getQueryExecutionMaxTimeQueryString())
      .add("QueryCacheHitCount", statistics.getQueryCacheHitCount())
      .add("QueryCacheMissCount", statistics.getQueryCacheMissCount())
      .add("QueryCachePutCount", statistics.getQueryCachePutCount())
      .add("FlushCount", statistics.getFlushCount())
      .add("ConnectCount", statistics.getConnectCount())
      .add("SecondLevelCacheHitCount", statistics.getSecondLevelCacheHitCount())
      .add("SecondLevelCacheMissCount", statistics.getSecondLevelCacheMissCount())
      .add("SecondLevelCachePutCount", statistics.getSecondLevelCachePutCount())
      .add("SessionCloseCount", statistics.getSessionCloseCount())
      .add("SessionOpenCount", statistics.getSessionOpenCount())
      .add("CollectionLoadCount", statistics.getCollectionLoadCount())
      .add("CollectionFetchCount", statistics.getCollectionFetchCount())
      .add("CollectionUpdateCount", statistics.getCollectionUpdateCount())
      .add("CollectionRemoveCount", statistics.getCollectionRemoveCount())
      .add("CollectionRecreateCount", statistics.getCollectionRecreateCount())
      .add("StartTime", statistics.getStartTime())
      .add("SuccessfulTransactionCount", statistics.getSuccessfulTransactionCount())
      .add("TransactionCount", statistics.getTransactionCount())
      .add("PrepareStatementCount", statistics.getPrepareStatementCount())
      .add("CloseStatementCount", statistics.getCloseStatementCount())
      .add("OptimisticFailureCount", statistics.getOptimisticFailureCount())
      .addChildList("SecondLevelCacheRegionNames", Arrays.asList(statistics.getSecondLevelCacheRegionNames()))
      .addChild("Queries", queriesWriter -> {
        String[] queries = statistics.getQueries();
        for (String query : queries) {
          queriesWriter.addJsonNode(query, statistics.getQueryStatistics(query));
        }
      })
      .addChild("EntityStatistics", queriesWriter -> {
        String[] entityNames = statistics.getEntityNames();
        for (String entityName : entityNames) {
          queriesWriter.addJsonNode(entityName, statistics.getEntityStatistics(entityName));
        }
      })
      .addChild("RoleStatistics", roleStatisticsWriter -> {
        String[] roleNames = statistics.getCollectionRoleNames();
        for (String roleName : roleNames) {
          roleStatisticsWriter.addJsonNode(roleName, statistics.getCollectionStatistics(roleName));
        }
      });

  }

  @Override
  public Map<String, Object> asJson() {
    LinkedHashMap<String, Object> json = new LinkedHashMap<>();
    Statistics statistics = sessionFactory.getStatistics();
    if (!statistics.isStatisticsEnabled()) {
      return json;
    }
    json.put("EntityDeleteCount", statistics.getEntityDeleteCount());
    json.put("EntityInsertCount", statistics.getEntityInsertCount());
    json.put("EntityLoadCount", statistics.getEntityLoadCount());
    json.put("EntityFetchCount", statistics.getEntityFetchCount());
    json.put("EntityUpdateCount", statistics.getEntityUpdateCount());
    json.put("QueryExecutionCount", statistics.getQueryExecutionCount());
    json.put("QueryExecutionMaxTime", statistics.getQueryExecutionMaxTime());
    json.put("QueryExecutionMaxTimeQueryString", statistics.getQueryExecutionMaxTimeQueryString());
    json.put("QueryCacheHitCount", statistics.getQueryCacheHitCount());
    json.put("QueryCacheMissCount", statistics.getQueryCacheMissCount());
    json.put("QueryCachePutCount", statistics.getQueryCachePutCount());
    json.put("FlushCount", statistics.getFlushCount());
    json.put("ConnectCount", statistics.getConnectCount());
    json.put("SecondLevelCacheHitCount", statistics.getSecondLevelCacheHitCount());
    json.put("SecondLevelCacheMissCount", statistics.getSecondLevelCacheMissCount());
    json.put("SecondLevelCachePutCount", statistics.getSecondLevelCachePutCount());
    json.put("SessionCloseCount", statistics.getSessionCloseCount());
    json.put("SessionOpenCount", statistics.getSessionOpenCount());
    json.put("CollectionLoadCount", statistics.getCollectionLoadCount());
    json.put("CollectionFetchCount", statistics.getCollectionFetchCount());
    json.put("CollectionUpdateCount", statistics.getCollectionUpdateCount());
    json.put("CollectionRemoveCount", statistics.getCollectionRemoveCount());
    json.put("CollectionRecreateCount", statistics.getCollectionRecreateCount());
    json.put("StartTime", statistics.getStartTime());
    json.put("SecondLevelCacheRegionNames", statistics.getSecondLevelCacheRegionNames());
    json.put("SuccessfulTransactionCount", statistics.getSuccessfulTransactionCount());
    json.put("TransactionCount", statistics.getTransactionCount());
    json.put("PrepareStatementCount", statistics.getPrepareStatementCount());
    json.put("CloseStatementCount", statistics.getCloseStatementCount());
    json.put("OptimisticFailureCount", statistics.getOptimisticFailureCount());

    LinkedHashMap<String, Object> queryStats = new LinkedHashMap<>();
    json.put("Queries", queryStats);

    String[] queries = statistics.getQueries();
    for (String query : queries) {
      queryStats.put(query, statistics.getQueryStatistics(query));
    }

    LinkedHashMap<String, Object> entityStatistics = new LinkedHashMap<>();
    json.put("EntityStatistics", entityStatistics);

    String[] entityNames = statistics.getEntityNames();
    for (String entityName : entityNames) {
      entityStatistics.put(entityName, statistics.getEntityStatistics(entityName));
    }

    LinkedHashMap<String, Object> roleStatistics = new LinkedHashMap<>();
    json.put("RoleStatistics", roleStatistics);

    String[] roleNames = statistics.getCollectionRoleNames();
    for (String roleName : roleNames) {
      roleStatistics.put(roleName, statistics.getCollectionStatistics(roleName));
    }

    return json;
  }

  @Override
  public String name() {
    return "Hibernate Statistics";
  }
}
