package ru.akvine.wild.bot.infrastructure.monitoring.threads;

import static ru.akvine.commons.util.ScheduledExecutors.newSingleThreadScheduledExecutor;
import static ru.akvine.commons.util.Threads.newThreadFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Периодически сбрасывает дамп стеков всех потоков в файлы {@code <dir>/<время>.tdump} - по
 * ним можно разобрать, где именно "висело" приложение, когда в этот момент никто не смотрел на
 * него в отладчике/jstack. Старые дампы архивируются и удаляются {@link HouseKeeper}
 * (см. {@link #simpleHouseKeeperConfig}).
 * <p>
 */
@Slf4j
public class StackTracePrinter {
    private static final DateTimeFormatter FILENAME_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss.SSS");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss z");

    private final String dumpDir;
    private final ScheduledExecutorService executorService;
    private final long intervalMillis;

    public StackTracePrinter(String dumpDir, long intervalMillis) {
        this.dumpDir = Objects.requireNonNull(dumpDir, "dumpDir");

        if (intervalMillis < 1000) {
            throw new IllegalArgumentException(
                    "intervalMillis must be more that 1 second but was " + intervalMillis + " millis");
        }
        this.intervalMillis = intervalMillis;

        executorService = newSingleThreadScheduledExecutor(newThreadFactory("stack-trace-printer"));
    }

    public void start() {
        executorService.scheduleWithFixedDelay(this::dumpStacks, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executorService.shutdownNow();
    }

    private void dumpStacks() {
        try {
            ThreadMXBean mxBean = ManagementFactory.getThreadMXBean();
            ThreadInfo[] threadInfos = mxBean.getThreadInfo(mxBean.getAllThreadIds(), 0);
            Map<Long, ThreadInfo> threadInfoMap = new HashMap<>();
            for (ThreadInfo threadInfo : threadInfos) {
                if (threadInfo != null) {
                    threadInfoMap.put(threadInfo.getThreadId(), threadInfo);
                }
            }

            // choose our dump-file
            File directory = new File(dumpDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            File dumpFile = new File(directory, FILENAME_DATE_FORMAT.format(ZonedDateTime.now()) + ".tdump");
            if (!dumpFile.exists()) {
                dumpFile.createNewFile();
            }
            try (Writer writer = new BufferedWriter(new FileWriter(dumpFile))) {
                dumpTraces(mxBean, threadInfoMap, writer);
            }
        } catch (Exception ex) {
            logger.error("can't print stack trace", ex);
        }
    }

    private static void dumpTraces(ThreadMXBean mxBean, Map<Long, ThreadInfo> threadInfoMap, Writer writer)
            throws IOException {
        Map<Thread, StackTraceElement[]> stacks = Thread.getAllStackTraces();
        writer.write("Dump of " + stacks.size() + " thread at " + DATE_FORMAT.format(ZonedDateTime.now()) + "\n\n");
        for (Map.Entry<Thread, StackTraceElement[]> entry : stacks.entrySet()) {
            Thread thread = entry.getKey();
            writer.write("\"" + thread.getName() + "\" prio=" + thread.getPriority() + " tid=" + thread.threadId()
                    + " " + thread.getState() + " " + (thread.isDaemon() ? "deamon" : "worker") + "\n");
            ThreadInfo threadInfo = threadInfoMap.get(thread.threadId());
            if (threadInfo != null) {
                writer.write("    native=" + threadInfo.isInNative() + ", suspended=" + threadInfo.isSuspended()
                        + ", block=" + threadInfo.getBlockedCount() + ", wait=" + threadInfo.getWaitedCount() + "\n");
                writer.write("    lock=" + threadInfo.getLockName() + " owned by " + threadInfo.getLockOwnerName()
                        + " (" + threadInfo.getLockOwnerId() + "), cpu="
                        + (mxBean.getThreadCpuTime(threadInfo.getThreadId()) / 1000000L) + ", user="
                        + (mxBean.getThreadUserTime(threadInfo.getThreadId()) / 1000000L) + "\n");
            }
            for (StackTraceElement element : entry.getValue()) {
                writer.write("        ");
                writer.write(element.toString());
                writer.write("\n");
            }
            writer.write("\n");
        }
    }

    public static HouseKeeper.FoldersHolder simpleHouseKeeperConfig(String dumpDir) {
        Path dumpDirPath = Paths.get(dumpDir);
        Path archivalFolderPath = Paths.get(dumpDir + "-archive");

        HouseKeeper.Folder archiveFolder = new HouseKeeper.Folder()
                .setName(dumpDirPath.toAbsolutePath().toString())
                .setArchivalFolder(archivalFolderPath.toAbsolutePath().toString())
                .setRetentionPeriod(2)
                .setTimeUnit(HouseKeeper.TimeUnitEnum.HOURS)
                .setOperationType(HouseKeeper.OperationType.ARCHIVE);

        HouseKeeper.Folder deleteFolder = new HouseKeeper.Folder()
                .setName(archivalFolderPath.toAbsolutePath().toString())
                .setRetentionPeriod(7)
                .setTimeUnit(HouseKeeper.TimeUnitEnum.DAYS)
                .setOperationType(HouseKeeper.OperationType.DELETE);

        return new HouseKeeper.FoldersHolder().setFolders(Arrays.asList(archiveFolder, deleteFolder));
    }
}
