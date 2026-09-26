package ru.akvine.wild.bot.infrastructure.monitoring.threads;

import static ru.akvine.commons.util.ScheduledExecutors.newSingleThreadScheduledExecutor;
import static ru.akvine.commons.util.Threads.newThreadFactory;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Периодически наводит порядок в заданных папках.
 * <p>
 * Операция ARCHIVE - сжимает файлы старше срока хранения в *.tar.gz и перемещает архив в папку
 * архива (сами исходные файлы после этого удаляются).
 * <p>
 * Операция DELETE - удаляет файлы старше срока хранения.
 * <p>
 * Список папок для архивации/очистки передаётся через {@link FoldersHolder}.
 */
@Slf4j
public class HouseKeeper {
    private static final String ARCHIVAL_FILE_PREFIX = "tdumps-archive";

    private final FoldersHolder foldersHolder;
    private final long intervalMillis;
    private final ScheduledExecutorService executorService;

    private volatile String pathToBeExcluded = "";
    private volatile int taredFiles = 0;
    private volatile List<Folder> folders = new ArrayList<>();

    public HouseKeeper(FoldersHolder foldersHolder, long intervalMillis) {
        this.foldersHolder = foldersHolder;

        if (intervalMillis < 1000) {
            throw new IllegalArgumentException(
                    "intervalMillis must be more that 1 second but was " + intervalMillis + " millis");
        }
        this.intervalMillis = intervalMillis;

        executorService = newSingleThreadScheduledExecutor(newThreadFactory("house-keeper"));
    }

    public void start() {
        executorService.scheduleWithFixedDelay(
                this::launchHouseKeeping, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        executorService.shutdownNow();
    }

    private void launchHouseKeeping() {
        try {
            logger.info("HOUSE KEEPING PROCESS - START");

            if (foldersHolder != null) {
                folders = foldersHolder.folders;
            } else {
                logger.debug("Folders were not reloaded properly. Previous folders configuration is used (if any).");
            }

            if (folders == null || folders.isEmpty()) {
                logger.info("No configuration found for housekeeping. Nothing will be done.");
                return;
            }

            String srcDir;
            String archDir;
            long retentionPeriod;
            String tarFileName = null;

            // LOOP THROUGH THE FOLDER LIST
            for (Folder folder : folders) {
                srcDir = folder.name;
                if (StringUtils.isBlank(srcDir)) {
                    continue;
                }

                retentionPeriod = TimeUnit.valueOf(folder.timeUnit.name()).toMillis(folder.retentionPeriod);
                logger.debug(
                        "RETENTION PERIOD - [{}] [{}]  ([{}] MILLIS)",
                        folder.retentionPeriod,
                        folder.timeUnit.name(),
                        retentionPeriod);

                archDir = folder.archivalFolder;

                // CHECK FOR DELETE / ARCHIVE
                if (folder.operationType == OperationType.DELETE) {
                    logger.debug("DELETE: CHECKING FOR OLD FILES IN DIR - [{}]", srcDir);
                    try {
                        deleteOutdatedFiles(new File(srcDir), retentionPeriod);
                    } catch (IOException ex) {
                        logger.error("error deleting old files", ex);
                    }
                } else if (folder.operationType == OperationType.ARCHIVE) {
                    /*
                     * COMPRESS OLD FILES TO *.TAR.GZ AND MOVE TO ARCHIVE FOLDER.
                     * ALSO DELETE THE FILES ADDED TO THE ARCHIVE.
                     */
                    try {
                        logger.debug("ARCHIVE: CHECKING FOR OLD FILES IN DIR - [{}]", srcDir);
                        // CREATE TAR FILE
                        tarFileName = archiveFiles(srcDir, archDir, retentionPeriod);
                        if (StringUtils.isBlank(tarFileName)) {
                            logger.warn("ARCHIVAL PROCESS FAILED");
                        } else {
                            // GZIP FILE
                            File gzipFile = gzipCompressFile(tarFileName);
                            // DELETE TAR FILE AFTER CREATION OF GZIP
                            deleteFile(new File(tarFileName));
                            if (taredFiles == 0) {
                                // remove empty tar if nothing was placed there.
                                gzipFile.delete();
                            }
                        }
                    } catch (FileNotFoundException fileEx) {
                        logger.error("file not found [{}]", tarFileName, fileEx);
                    } catch (ArchiveException | IOException archiveEx) {
                        logger.error("housekeeping failed", archiveEx);
                    }
                } else {
                    // INVALID OPTION
                    logger.warn("INVALID OPERATION. SHOULD BE DELETE / ARCHIVE");
                }

                // CONTINUE WITH NEXT RULE IF ANY
            }

            logger.info("HOUSE KEEPING PROCESS - COMPLETED");
        } catch (Exception e) {
            logger.error("Problems during reinitializing housekeeper", e);
        }
    }

    /**
     * CRAETE A TAR FILE
     *
     * @param strSrcDir  src dir ending with File.separator
     * @param strArchDir target archive dir ending with File.separator. tar files will be created here.
     */
    private String archiveFiles(String strSrcDir, String strArchDir, long archivalPeriod)
            throws ArchiveException, IOException {

        taredFiles = 0;

        if (StringUtils.isBlank(strSrcDir)) {
            logger.error("SRC DIRECTORY IS MISSING");
            return null;
        }

        if (StringUtils.isBlank(strArchDir)) {
            logger.error("ARCHIVE DIRECTORY NOT SPECIFIED. USE INPUT DIR AS ARCHIVE DIR.");
            // SO ARCHIVE IN THE SRC DIR
            strArchDir = strSrcDir;
        }

        File srcDir = new File(strSrcDir);
        if (!srcDir.exists()) {
            logger.warn("SKIP ARCHIVAL... INVALID SOURCE DIRECTORY [{}]", strSrcDir);
            return null;
        }

        File archDir = new File(strArchDir);
        if (!archDir.exists()) {
            // AIX UNIX - FOLDER WILL GET CREATED AUTOMATICALLY. BUT
            // IN WINDOWS - ERROR!
            logger.info("ARCHIVE DIRECTORY DOES NOT EXIST [{}] will create.", archDir);
            archDir.mkdirs();
        }

        // ARCHIVE

        pathToBeExcluded = srcDir.getCanonicalPath();

        File[] dirList; // LIST OF FILES IN SRC DIR
        if (srcDir.isDirectory()) {
            logger.debug("LOOKING FOR FILES IN [{}]", srcDir.getCanonicalPath());
            FileFilter noArchiveDirFilter = pathname -> !(pathname.equals(archDir));
            dirList = srcDir.listFiles(noArchiveDirFilter);

            if (dirList == null || dirList.length == 0) {
                logger.warn("NO FILES FOUND IN DIR - [{}]", strSrcDir);
                return null;
            }
        } else {
            // JUST ADD THE SINGLE FILE TO TAR
            dirList = new File[] {srcDir};
            pathToBeExcluded = srcDir.getCanonicalPath()
                    .substring(0, srcDir.getCanonicalPath().lastIndexOf(File.separator));
        }

        // TAR FILE NAME WILL BE CURRENT DATE & TIMESTAMP
        String tarFileName = ARCHIVAL_FILE_PREFIX + "_" + formattedCurrentDateTime() + ".tar";
        File tarFile = new File(archDir, tarFileName);
        tarFileName = tarFile.getCanonicalPath();
        OutputStream out = null;
        TarArchiveOutputStream aos = null;

        try {
            out = new FileOutputStream(tarFile);
            aos = (TarArchiveOutputStream)
                    new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.TAR, out);

            // LOOP THROUGH LIST OF FILES IN SRC DIR
            for (File file : dirList) {
                sendToArchivation(archivalPeriod, aos, file);
            }
        } finally {
            this.finish(aos);
            this.close(aos);
            this.close(out);
        }
        return tarFileName;
    }

    private void sendToArchivation(long archivalPeriod, TarArchiveOutputStream aos, File file) throws IOException {
        if (file.isDirectory()) {
            // CHK SUB FOLDERS FOR FILES & ADD TO TAR
            logger.debug("looking for files in directory [{}]", file.getCanonicalPath());
            File[] dirList = file.listFiles();

            // ADD A EMPTY DIRECTORY
            addDirectoryToTar(file, aos);

            for (File directoryFile : dirList) {
                logger.trace("processing file: [{}]", directoryFile);
                sendToArchivation(archivalPeriod, aos, directoryFile);
            }
            return;
        }
        if (file.isFile()) {
            if (isFileOld(file, archivalPeriod)) {
                tarFile(file, aos);
                // DELETE THE FILE WHICH HAS BEEN ADDED TO TAR
                deleteFile(file);
            }
            return;
        }
        logger.debug("Skip unknown file type: [{}]", file.getCanonicalPath());
    }

    /**
     * DELETE FILES OLDER THAN SPECIFIED RETENTION PERIOD
     */
    private void deleteOutdatedFiles(File deleteDir, long retentionPeriod) throws IOException {
        if (!deleteDir.exists()) {
            logger.warn("SKIP RETENTION... INVALID DIRECTORY TO DELETE FILE FROM - [{}]", deleteDir);
            return;
        }

        logger.info("CHECKING FOR OLD FILES IN DIR - [{}]", deleteDir.getCanonicalPath());

        File[] dirList = deleteDir.listFiles();
        if (dirList == null || dirList.length == 0) {
            logger.info("NO FILES FOUND IN [{}]", deleteDir.getCanonicalPath());
            return;
        }
        for (File file : dirList) {
            if (file.isDirectory()) {
                // DO NOT DELETE DIRECTORIES!!!!
                // RECURSIVELY CHECK SUB-DIRECTORIES FOR OLDER FILES
                deleteOutdatedFiles(file, retentionPeriod);
            } else if (file.isFile() && isFileOld(file, retentionPeriod)) {
                logger.info("File [{}] keeping time expired. Will be deleted", file.getName());
                deleteFile(file);
            }
        }
    }

    /**
     * @param file                    file to be verified
     * @param retentionPeriodInMillis number of days to retain files
     */
    private boolean isFileOld(File file, long retentionPeriodInMillis) throws IOException {
        Date lastModifiedDate = new Date(file.lastModified());
        long differenceInMillis = getDifferenceInMillis(lastModifiedDate, new Date());

        if (differenceInMillis > retentionPeriodInMillis) {
            logger.trace("[{}] IS OLD. WILL BE REMOVED.", file.getCanonicalPath());
            return true;
        }
        return false;
    }

    private void addDirectoryToTar(File file, ArchiveOutputStream aos) throws IOException {
        try {
            String dirName = getFileName(file);
            logger.debug("ADD DIR TO TAR - [{}]", dirName);
            TarArchiveEntry entry = new TarArchiveEntry(file, dirName);
            aos.putArchiveEntry(entry);
            aos.closeArchiveEntry();
        } catch (IOException ioEx) {
            logger.warn("error adding dir to tar - [{}]", file.getCanonicalPath(), ioEx);
            throw ioEx;
        }
    }

    private void tarFile(File file, ArchiveOutputStream aos) throws IOException {
        TarArchiveEntry entry;
        FileInputStream fin = null;
        try {
            String fileName = getFileName(file);
            entry = new TarArchiveEntry(file, fileName);
            entry.setSize(file.length());
            aos.putArchiveEntry(entry);
            fin = new FileInputStream(file);
            IOUtils.copy(fin, aos);
            aos.closeArchiveEntry();
            taredFiles++;
        } catch (IOException ioEx) {
            logger.warn("error adding file to tar - [{}]", file.getCanonicalPath(), ioEx);
            throw ioEx;
        } finally {
            this.close(fin);
        }
    }

    /**
     * @param srcFileName file to gzipped!
     */
    private File gzipCompressFile(String srcFileName) throws IOException {
        OutputStream out = null;
        InputStream fin = null;
        GzipCompressorOutputStream gzip = null;
        String gzipFileName = srcFileName + ".gz";

        try {
            File srcFile = new File(srcFileName);
            if (!srcFile.exists() || !srcFile.isFile()) {
                logger.error("INVALID FILE - [{}]", srcFileName);
                return null;
            }

            fin = new FileInputStream(srcFile);
            out = new FileOutputStream(gzipFileName);
            gzip = new GzipCompressorOutputStream(out);
            IOUtils.copy(fin, gzip);
            return new File(gzipFileName);
        } finally {
            this.close(fin);
            this.close(gzip);
            this.close(out);
        }
    }

    /**
     * DELETE SPECIFIED FILE
     *
     * @param file file to be deleted
     */
    private void deleteFile(File file) throws IOException {
        String strCanonicalPath = file.getCanonicalPath();
        // DO NOT DELETE ANY DIRECTORIES
        if (!file.exists() || !file.isFile()) {
            logger.error("INVALID FILE [{}]", strCanonicalPath);
            return;
        }
        boolean deleteStatus = file.delete();
        if (!deleteStatus) {
            logger.error("FAILED TO DELETE FILE [{}]", strCanonicalPath);
        }
    }

    /**
     * derive file name to be used in tar. basically removes the directory path.
     *
     * @return file name to be used in tar
     */
    private String getFileName(File file) throws IOException {
        // filename to be used in tar
        String strCanonicalPath = file.getCanonicalPath();
        String fileName = "";
        // INDEX OF SRC
        int index = strCanonicalPath.indexOf(pathToBeExcluded);
        if (index >= 0) {
            fileName = strCanonicalPath.substring(index + pathToBeExcluded.length() + 1);
        }
        return fileName;
    }

    // ************ COMMON UTILITY FUNCTIONS ****************

    /**
     * CLOSE STREAM
     */
    private void close(Closeable stream) throws IOException {
        try {
            if (stream != null) {
                stream.close();
            }
        } catch (IOException ioEx) {
            logger.warn("error closing stream", ioEx);
            throw ioEx;
        }
    }

    /**
     * Ends the TAR archive without closing the underlying OutputStream
     */
    private void finish(TarArchiveOutputStream aos) throws IOException {
        try {
            if (aos != null) {
                aos.finish();
            }
        } catch (IOException ioEx) {
            logger.warn("error while finish TarArchiveOutputStream", ioEx);
            throw ioEx;
        }
    }

    /**
     * get the difference in millis between two dates. considers DST.
     *
     * @param date1 the first date
     * @param date2 the second date
     * @return int the difference in days (-ve if date1 < date2)
     */
    private static long getDifferenceInMillis(Date date1, Date date2) {
        long interval = date2.getTime() - date1.getTime();
        if (interval < 0) {
            return 0;
        }
        return interval;
    }

    /**
     * returns formatted current date
     */
    private static String formattedCurrentDateTime() {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        return formatter.format(new Date());
    }

    public enum OperationType {
        DELETE,
        ARCHIVE
    }

    public enum TimeUnitEnum {
        NANOSECONDS,
        MICROSECONDS,
        MILLISECONDS,
        SECONDS,
        MINUTES,
        HOURS,
        DAYS
    }

    public static class FoldersHolder {
        private List<Folder> folders;

        public List<Folder> getFolders() {
            return folders;
        }

        public FoldersHolder setFolders(List<Folder> folders) {
            this.folders = folders;
            return this;
        }
    }

    public static class Folder {
        private String name;
        private String archivalFolder;
        private int retentionPeriod;
        private TimeUnitEnum timeUnit;
        private OperationType operationType;

        public String getName() {
            return name;
        }

        public Folder setName(String name) {
            this.name = name;
            return this;
        }

        public String getArchivalFolder() {
            return archivalFolder;
        }

        public Folder setArchivalFolder(String archivalFolder) {
            this.archivalFolder = archivalFolder;
            return this;
        }

        public int getRetentionPeriod() {
            return retentionPeriod;
        }

        public Folder setRetentionPeriod(int retentionPeriod) {
            this.retentionPeriod = retentionPeriod;
            return this;
        }

        public TimeUnitEnum getTimeUnit() {
            return timeUnit;
        }

        public Folder setTimeUnit(TimeUnitEnum timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public OperationType getOperationType() {
            return operationType;
        }

        public Folder setOperationType(OperationType operationType) {
            this.operationType = operationType;
            return this;
        }
    }
}
