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

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.statistics.StatisticsGateway;
import net.sf.ehcache.statistics.extended.ExtendedStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class CacheInformationProvider implements ServerInfoProvider {

    @Autowired
    public CacheInformationProvider() {
    }

    @Override
    public double priority() {
        return 7.0;
    }

    @Override
    public Map<String, Object> asJson() {
        LinkedHashMap<String, Object> json = new LinkedHashMap<>();

        for (CacheManager cacheManager : CacheManager.ALL_CACHE_MANAGERS) {
            LinkedHashMap<String, Object> jsonForManager = new LinkedHashMap<>();
            json.put(cacheManager.getName(), jsonForManager);

            for (String cacheName : cacheManager.getCacheNames()) {
                Cache cache = cacheManager.getCache(cacheName);
                LinkedHashMap<String, Object> cacheJson = new LinkedHashMap<>();
                jsonForManager.put(cacheName, cacheJson);

                cacheJson.put("Cache configuration information", getCacheConfigurationInformationAsJson(cache));
                cacheJson.put("Cache runtime information", getCacheRuntimeInformationAsJson(cache));
            }
        }

        return json;
    }

    @Override
    public String name() {
        return "Cache Information";
    }

    @Override
    public void write(ServerInfoWriter serverInfoWriter) {
        for (CacheManager cacheManager : CacheManager.ALL_CACHE_MANAGERS) {
            serverInfoWriter.addChild(cacheManager.getName(), writer -> {
                for (String cacheName : cacheManager.getCacheNames()) {
                    writer.addChild(cacheName, cacheWriter -> {
                        Cache cache = cacheManager.getCache(cacheName);
                        cacheWriter.addChild("Cache configuration information", addCacheConfigurationInformation(cache));
                        cacheWriter.addChild("Cache runtime information", addCacheRuntimeInformation(cache));
                    });
                }
            });
        }
    }

    private Consumer<ServerInfoWriter> addCacheRuntimeInformation(Cache cache) {
        StatisticsGateway statistics = cache.getStatistics();
        return serverInfoWriter -> {
            serverInfoWriter.addChild("Get Time in milliseconds", addStatistics(statistics.cacheGetOperation()));
            serverInfoWriter.addChild("Put Time in milliseconds", addStatistics(statistics.cachePutOperation()));
            serverInfoWriter.addChild("Remove Time in milliseconds", addStatistics(statistics.cacheRemoveOperation()));
            serverInfoWriter.add("Cache Size", statistics.getSize());
            serverInfoWriter.add("Cache Size (Disk)", statistics.getLocalDiskSize());
            serverInfoWriter.add("Cache Count (Disk)", statistics.localDiskHitCount());
            serverInfoWriter.addChild("Cache Counts", addCacheCount(statistics));
        };
    }

    private Consumer<ServerInfoWriter> addCacheCount(StatisticsGateway statistics) {
        return serverInfoWriter -> {
            serverInfoWriter.add("Hits", statistics.cacheHitCount());
            serverInfoWriter.add("Miss", statistics.cacheMissCount());
            serverInfoWriter.add("Expired", statistics.cacheExpiredCount());
            serverInfoWriter.add("Eviction", statistics.cacheEvictedCount());
            serverInfoWriter.add("Put", statistics.cachePutCount());
            serverInfoWriter.add("Remove", statistics.cacheRemoveCount());
        };
    }

    private Consumer<ServerInfoWriter> addStatistics(ExtendedStatistics.Result result) {
        return writer -> {
            writer.add("Average", String.valueOf(result.latency().average().value()));
            writer.add("Minimum", String.valueOf(result.latency().minimum().value()));
            writer.add("Maximum", String.valueOf(result.latency().maximum().value()));
        };
    }

    public Map<String, Object> getCacheRuntimeInformationAsJson(Cache cache) {
        LinkedHashMap<String, Object> json = new LinkedHashMap<>();
        StatisticsGateway statistics = cache.getStatistics();

        json.put("Get Time in milliseconds", getStatisticsFrom(statistics.cacheGetOperation()));
        json.put("Put Time in milliseconds", getStatisticsFrom(statistics.cachePutOperation()));
        json.put("Remove Time in milliseconds", getStatisticsFrom(statistics.cacheRemoveOperation()));

        json.put("Cache Size", statistics.getSize());

        LinkedHashMap<String, Long> cacheCount = new LinkedHashMap<>();
        cacheCount.put("Hits", statistics.cacheHitCount());
        cacheCount.put("Miss", statistics.cacheMissCount());
        cacheCount.put("Expired", statistics.cacheExpiredCount());
        cacheCount.put("Eviction", statistics.cacheEvictedCount());
        cacheCount.put("Put", statistics.cachePutCount());
        cacheCount.put("Remove", statistics.cacheRemoveCount());
        json.put("Cache Counts", cacheCount);

        json.put("Cache Size (Disk)", statistics.getLocalDiskSize());
        json.put("Cache Count (Disk)", statistics.localDiskHitCount());

        return json;
    }

    private LinkedHashMap<String, Object> getStatisticsFrom(ExtendedStatistics.Result result) {
        LinkedHashMap<String, Object> time = new LinkedHashMap<>();
        time.put("Average", String.valueOf(result.latency().average().value()));
        time.put("Minimum", String.valueOf(result.latency().minimum().value()));
        time.put("Maximum", String.valueOf(result.latency().maximum().value()));
        return time;
    }

    private Consumer<ServerInfoWriter> addCacheConfigurationInformation(Cache cache) {
        CacheConfiguration config = cache.getCacheConfiguration();
        return serverInfoWriter -> {
            serverInfoWriter.add("Name", config.getName());
            serverInfoWriter.add("Maximum Elements in Memory", config.getMaxEntriesLocalHeap());
            serverInfoWriter.add("Maximum Elements on Disk", config.getMaxBytesLocalDisk());
            serverInfoWriter.add("Memory Store Eviction Policy", config.getMemoryStoreEvictionPolicy().toString());
            serverInfoWriter.add("Clean or Flush", config.isClearOnFlush());
            serverInfoWriter.add("Eternal", config.isEternal());
            serverInfoWriter.add("Time To Idle Seconds", config.getTimeToIdleSeconds());
            serverInfoWriter.add("time To Live Seconds", config.getTimeToLiveSeconds());
            if (config.getPersistenceConfiguration() != null) {
                serverInfoWriter.add("Persistence Configuration Strategy", config.getPersistenceConfiguration().getStrategy().toString());
                serverInfoWriter.add("Persistence Configuration Synchronous writes", config.getPersistenceConfiguration().getSynchronousWrites());
            } else {
                serverInfoWriter.add("Persistence Configuration Strategy", "NONE");
                serverInfoWriter.add("Persistence Configuration Synchronous writes", false);
            }
            serverInfoWriter.add("Disk Spool Buffer Size in MB", config.getDiskSpoolBufferSizeMB());
            serverInfoWriter.add("Disk Access Stripes", config.getDiskAccessStripes());
            serverInfoWriter.add("Disk Expiry Thread Interval Seconds", config.getDiskExpiryThreadIntervalSeconds());
            serverInfoWriter.add("Logging Enabled", config.getLogging());
            serverInfoWriter.addJsonNode("Terracotta Configuration", config.getTerracottaConfiguration());
            serverInfoWriter.addJsonNode("Cache Writer Configuration", config.getCacheWriterConfiguration());
            serverInfoWriter.addJsonNode("Cache Loader Configurations", config.getCacheLoaderConfigurations());
            serverInfoWriter.add("Frozen", config.isFrozen());
            serverInfoWriter.add("Transactional Mode", config.getTransactionalMode().toString());
            serverInfoWriter.add("Statistics Enabled", config.getStatistics());
        };
    }

    public Map<String, Object> getCacheConfigurationInformationAsJson(Cache cache) {
        CacheConfiguration config = cache.getCacheConfiguration();
        LinkedHashMap<String, Object> json = new LinkedHashMap<>();

        json.put("Name", config.getName());
        json.put("Maximum Elements in Memory", config.getMaxEntriesLocalHeap());
        json.put("Maximum Elements on Disk", config.getMaxBytesLocalDisk());
        json.put("Memory Store Eviction Policy", config.getMemoryStoreEvictionPolicy().toString());
        json.put("Clean or Flush", config.isClearOnFlush());
        json.put("Eternal", config.isEternal());
        json.put("Time To Idle Seconds", config.getTimeToIdleSeconds());
        json.put("time To Live Seconds", config.getTimeToLiveSeconds());
        if (config.getPersistenceConfiguration() != null) {
            json.put("Persistence Configuration Strategy", config.getPersistenceConfiguration().getStrategy());
            json.put("Persistence Configuration Synchronous writes", config.getPersistenceConfiguration().getSynchronousWrites());
        } else {
            json.put("Persistence Configuration Strategy", "NONE");
            json.put("Persistence Configuration Synchronous writes", false);
        }
        json.put("Disk Spool Buffer Size in MB", config.getDiskSpoolBufferSizeMB());
        json.put("Disk Access Stripes", config.getDiskAccessStripes());
        json.put("Disk Expiry Thread Interval Seconds", config.getDiskExpiryThreadIntervalSeconds());
        json.put("Logging Enabled", config.getLogging());
        json.put("Terracotta Configuration", config.getTerracottaConfiguration());
        json.put("Cache Writer Configuration", config.getCacheWriterConfiguration());
        json.put("Cache Loader Configurations", config.getCacheLoaderConfigurations());
        json.put("Frozen", config.isFrozen());
        json.put("Transactional Mode", config.getTransactionalMode());
        json.put("Statistics Enabled", config.getStatistics());

        return json;
    }
}
