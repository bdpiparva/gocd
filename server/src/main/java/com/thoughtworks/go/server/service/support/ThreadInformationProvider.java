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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.management.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;

@Component
public class ThreadInformationProvider implements ServerInfoProvider {
    private final DaemonThreadStatsCollector daemonThreadStatsCollector;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadInformationProvider.class.getName());

    @Autowired
    public ThreadInformationProvider(DaemonThreadStatsCollector daemonThreadStatsCollector) {
        this.daemonThreadStatsCollector = daemonThreadStatsCollector;
    }

    @Override
    public double priority() {
        return 13.0;
    }

    private Map<String, Object> getThreadCount(ThreadMXBean threadMXBean) {
        LinkedHashMap<String, Object> count = new LinkedHashMap<>();
        count.put("Current", threadMXBean.getThreadCount());
        count.put("Total", threadMXBean.getTotalStartedThreadCount());
        count.put("Daemon", threadMXBean.getDaemonThreadCount());
        count.put("Peak", threadMXBean.getPeakThreadCount());
        return count;
    }

    private Map<String, Object> getDeadLockThreadInformation(ThreadMXBean threadMXBean) {
        LinkedHashMap<String, Object> json = new LinkedHashMap<>();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();

        if (deadlockedThreads != null && deadlockedThreads.length > 0) {
            json.put("Count", deadlockedThreads.length);
            for (long deadlockedThread : deadlockedThreads) {
                LinkedHashMap<String, Object> threadsInfo = new LinkedHashMap<>();
                LinkedHashMap<String, Object> lockedMonitorsInfo = new LinkedHashMap<>();
                LinkedHashMap<String, Object> stackTrackInfo = new LinkedHashMap<>();
                ThreadInfo threadInfo = threadMXBean.getThreadInfo(deadlockedThread);
                LockInfo lockInfo = threadInfo.getLockInfo();
                if (lockInfo != null) {
                    threadsInfo.put(threadInfo.getThreadName(), lockInfo);
                } else {
                    threadsInfo.put(threadInfo.getThreadName(), "This thread is not waiting for any locks");
                }
                MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
                for (MonitorInfo lockedMonitor : lockedMonitors) {
                    lockedMonitorsInfo.put("Monitor for class " + lockedMonitor.getClassName(), "taken at stack frame " + lockedMonitor.getLockedStackFrame());
                }
                stackTrackInfo.put(Long.toString(deadlockedThread), Arrays.toString(threadInfo.getStackTrace()));
                json.put("Thread Information", threadsInfo);
                json.put("Monitor Information Stack Frame where locks were taken", lockedMonitorsInfo);
                json.put("Stack Trace Of DeadLock Threads", stackTrackInfo);
            }
        }
        return json;
    }

    private TreeMap<Long, Map<String, Object>> getThreadInformation(ThreadMXBean threadMXBean) {
        TreeMap<Long, Map<String, Object>> traces = new TreeMap<>();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        for (ThreadInfo threadInfo : threadInfos) {
            LinkedHashMap<String, Object> threadStackTrace = new LinkedHashMap<>();
            threadStackTrace.put("Id", threadInfo.getThreadId());
            threadStackTrace.put("Name", threadInfo.getThreadName());
            threadStackTrace.put("State", threadInfo.getThreadState());
            threadStackTrace.put("UserTime(nanoseconds)", threadMXBean.getThreadUserTime(threadInfo.getThreadId()));
            threadStackTrace.put("CPUTime(nanoseconds)", threadMXBean.getThreadCpuTime(threadInfo.getThreadId()));
            threadStackTrace.put("DaemonThreadInfo", daemonThreadStatsCollector.statsFor(threadInfo.getThreadId()));
            threadStackTrace.put("AllocatedMemory(Bytes)", getAllocatedMemory(threadMXBean, threadInfo));
            LinkedHashMap<String, Object> lockMonitorInfo = new LinkedHashMap<>();
            MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();

            lockMonitorInfo.put("Locked Monitors", getLockedMonitor(lockedMonitors));
            lockMonitorInfo.put("Locked Synchronizers", asJSON(threadInfo.getLockedSynchronizers()));
            threadStackTrace.put("Lock Monitor Info", lockMonitorInfo);

            LinkedHashMap<String, Object> blockedInfo = new LinkedHashMap<>();
            blockedInfo.put("Blocked Time(ms)", threadInfo.getBlockedTime());
            blockedInfo.put("Blocked Count", threadInfo.getBlockedCount());
            threadStackTrace.put("Blocked Info", blockedInfo);

            LinkedHashMap<String, Object> timeInfo = new LinkedHashMap<>();
            timeInfo.put("Waited Time(ms)", threadInfo.getWaitedTime());
            timeInfo.put("Waited Count", threadInfo.getWaitedCount());
            threadStackTrace.put("Time Info", timeInfo);

            LinkedHashMap<String, Object> lockInfoMap = new LinkedHashMap<>();
            LockInfo lockInfo = threadInfo.getLockInfo();
            lockInfoMap.put("Locked On", asJSON(lockInfo));
            lockInfoMap.put("Lock Owner Thread Id", threadInfo.getLockOwnerId());
            lockInfoMap.put("Lock Owner Thread Name", threadInfo.getLockOwnerName());
            threadStackTrace.put("Lock Info", lockInfoMap);

            LinkedHashMap<String, Object> stateInfo = new LinkedHashMap<>();
            stateInfo.put("Suspended", threadInfo.isSuspended());
            stateInfo.put("InNative", threadInfo.isInNative());
            threadStackTrace.put("State Info", stateInfo);

            threadStackTrace.put("Stack Trace", asJSON(threadInfo.getStackTrace()));
            traces.put(threadInfo.getThreadId(), threadStackTrace);
        }
        return traces;
    }

    private ArrayList<Map<String, Object>> getLockedMonitor(MonitorInfo[] lockedMonitors) {
        ArrayList<Map<String, Object>> lockedMonitorsJson = new ArrayList<>();

        for (MonitorInfo lockedMonitor : lockedMonitors) {
            LinkedHashMap<String, Object> lockedMonitorJson = new LinkedHashMap<>();
            lockedMonitorJson.put("Class", lockedMonitor.getClassName());
            lockedMonitorJson.put("IdentityHashCode", lockedMonitor.getIdentityHashCode());
            lockedMonitorJson.put("LockedStackDepth", lockedMonitor.getLockedStackDepth());
            lockedMonitorJson.put("StackFrame", lockedMonitor.getLockedStackFrame().toString());
            lockedMonitorsJson.add(lockedMonitorJson);
        }
        return lockedMonitorsJson;
    }

    private long getAllocatedMemory(ThreadMXBean threadMXBean, ThreadInfo threadInfo) {
        Method method = ReflectionUtils.findMethod(threadMXBean.getClass(), "getThreadAllocatedBytes", long.class);
        if (method != null) {
            try {
                method.setAccessible(true);
                return (long) method.invoke(threadMXBean, threadInfo.getThreadId());
            } catch (Exception e) {
                LOGGER.error("Error while capturing allocatedMemory for api/support : {}", e.getMessage());
            }
        }
        return -1;
    }

    private Object asJSON(StackTraceElement[] stackTrace) {
        ArrayList<String> strings = new ArrayList<>();

        for (StackTraceElement o : stackTrace) {
            strings.add(o.toString());
        }
        return strings;

    }

    private ArrayList<LinkedHashMap<String, Object>> asJSON(LockInfo[] lockInfos) {
        ArrayList<LinkedHashMap<String, Object>> objects = new ArrayList<>();

        for (LockInfo lockInfo : lockInfos) {
            objects.add(asJSON(lockInfo));
        }
        return objects;
    }

    private LinkedHashMap<String, Object> asJSON(LockInfo lockInfo) {
        LinkedHashMap<String, Object> lockedOn = new LinkedHashMap<>();
        if (lockInfo != null) {
            lockedOn.put("Class", lockInfo.getClassName());
            lockedOn.put("IdentityHashCode", lockInfo.getIdentityHashCode());
        }
        return lockedOn;
    }

    @Override
    public Map<String, Object> asJson() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        LinkedHashMap<String, Object> json = new LinkedHashMap<>();
        json.put("Thread Count", getThreadCount(threadMXBean));
        json.put("DeadLock Threads", getDeadLockThreadInformation(threadMXBean));
        json.put("Stack Trace", getThreadInformation(threadMXBean));
        return json;
    }

    @Override
    public String name() {
        return "Thread Information";
    }

    @Override
    public void write(ServerInfoWriter serverInfoWriter) {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        serverInfoWriter
                .addChild("Thread Count", addThreadCount(threadMXBean))
                .addChild("DeadLock Threads", addDeadLockThreadInformation(threadMXBean))
                .addChild("Stack Trace", addThreadInformation(threadMXBean));
    }

    private Consumer<ServerInfoWriter> addThreadInformation(ThreadMXBean threadMXBean) {
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(true, true);
        return serverInfoWriter -> Arrays.stream(threadInfos).forEach(threadInfo -> serverInfoWriter
                .addChild(valueOf(threadInfo.getThreadId()), threadInfoWriter -> threadInfoWriter
                        .add("Id", threadInfo.getThreadId())
                        .add("Name", threadInfo.getThreadName())
                        .add("State", threadInfo.getThreadState().toString())
                        .add("UserTime(nanoseconds)", threadMXBean.getThreadUserTime(threadInfo.getThreadId()))
                        .add("CPUTime(nanoseconds)", threadMXBean.getThreadCpuTime(threadInfo.getThreadId()))
                        .addJsonNode("DaemonThreadInfo", daemonThreadStatsCollector.statsFor(threadInfo.getThreadId()))
                        .add("AllocatedMemory(Bytes)", getAllocatedMemory(threadMXBean, threadInfo))
                        .addChild("Lock Monitor Info", addLockMonitorInfo(threadInfo))
                        .addChild("Blocked Info", writer -> writer
                                .add("Blocked Time(ms)", threadInfo.getBlockedTime())
                                .add("Blocked Count", threadInfo.getBlockedCount())
                        )
                        .addChild("Time Info", writer -> writer
                                .add("Waited Time(ms)", threadInfo.getWaitedTime())
                                .add("Waited Count", threadInfo.getWaitedCount())
                        )
                        .addChild("Lock Info", lockInfoWriter -> lockInfoWriter
                                .addJsonNode("Locked On", asJSON(threadInfo.getLockInfo()))
                                .add("Lock Owner Thread Id", threadInfo.getLockOwnerId())
                                .add("Lock Owner Thread Name", threadInfo.getLockOwnerName())
                        )
                        .addChild("State Info", writer -> writer
                                .add("Suspended", threadInfo.isSuspended())
                                .add("InNative", threadInfo.isInNative())
                        )
                        .addChildList("Stack Trace", Arrays.stream(threadInfo.getStackTrace())
                                .map(StackTraceElement::toString).collect(Collectors.toList())
                        )
                )
        );
    }

    private Consumer<ServerInfoWriter> addLockMonitorInfo(ThreadInfo threadInfo) {
        return lockMonitorInfoWriter -> lockMonitorInfoWriter
                .addJsonNode("Locked Synchronizers", asJSON(threadInfo.getLockedSynchronizers()))
                .addJsonNode("Locked Monitors", getLockedMonitor(threadInfo.getLockedMonitors()));
    }

    private Consumer<ServerInfoWriter> addDeadLockThreadInformation(ThreadMXBean threadMXBean) {
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        return writer -> {
            if (deadlockedThreads != null && deadlockedThreads.length > 0) {
                writer.add("Count", deadlockedThreads.length);
                for (long deadlockedThread : deadlockedThreads) {
                    ThreadInfo threadInfo = threadMXBean.getThreadInfo(deadlockedThread);
                    LockInfo lockInfo = threadInfo.getLockInfo();
                    MonitorInfo[] lockedMonitors = threadInfo.getLockedMonitors();
                    writer
                            .addChild("Thread Information", threadInfoWriter -> {
                                if (lockInfo != null) {
                                    threadInfoWriter.addJsonNode(threadInfo.getThreadName(), lockInfo);
                                } else {
                                    threadInfoWriter.add(threadInfo.getThreadName(), "This thread is not waiting for any locks");
                                }
                            })
                            .addChild("Monitor Information Stack Frame where locks were taken", lockWriter -> {
                                for (MonitorInfo lockedMonitor : lockedMonitors) {
                                    lockWriter.add("Monitor for class " + lockedMonitor.getClassName(), "taken at stack frame " + lockedMonitor.getLockedStackFrame());
                                }
                            });
                    writer.addChild("Stack Trace Of DeadLock Threads", stackTrackInfoWriter -> {
                        stackTrackInfoWriter.add(Long.toString(deadlockedThread), Arrays.toString(threadInfo.getStackTrace()));
                    });
                }
            }
        };
    }

    private Consumer<ServerInfoWriter> addThreadCount(ThreadMXBean threadMXBean) {
        return serverInfoWriter -> serverInfoWriter.add("Current", threadMXBean.getThreadCount())
                .add("Total", threadMXBean.getTotalStartedThreadCount())
                .add("Daemon", threadMXBean.getDaemonThreadCount())
                .add("Peak", threadMXBean.getPeakThreadCount());
    }
}
