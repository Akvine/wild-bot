package ru.akvine.wild.bot.infrastructure.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.HouseKeeper;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.HouseKeeper.Folder;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.HouseKeeper.FoldersHolder;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.HouseKeeper.OperationType;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.HouseKeeper.TimeUnitEnum;
import ru.akvine.wild.bot.infrastructure.monitoring.threads.StackTracePrinter;

@DisplayName("Уборка папок и дампы потоков")
class ThreadsHousekeepingTest {
    private static final long TWO_DAYS_MILLIS = 2L * 24 * 60 * 60 * 1000;

    @TempDir
    Path tempDir;

    private static void run(HouseKeeper keeper) {
        ReflectionTestUtils.invokeMethod(keeper, "launchHouseKeeping");
    }

    private Path file(Path directory, String name, long ageMillis) throws IOException {
        Files.createDirectories(directory);
        Path file = directory.resolve(name);
        Files.writeString(file, "content of " + name);
        assertThat(file.toFile().setLastModified(System.currentTimeMillis() - ageMillis))
                .isTrue();
        return file;
    }

    private static Folder folder(Path name, Path archive, OperationType type) {
        return new Folder()
                .setName(name.toString())
                .setArchivalFolder(archive == null ? null : archive.toString())
                .setRetentionPeriod(1)
                .setTimeUnit(TimeUnitEnum.DAYS)
                .setOperationType(type);
    }

    private static HouseKeeper keeperFor(Folder... folders) {
        return new HouseKeeper(new FoldersHolder().setFolders(List.of(folders)), 1000);
    }

    @Test
    @DisplayName("DELETE удаляет только старые файлы, в том числе во вложенных папках, и не трогает папки")
    void deleteRemovesOldFilesOnly() throws IOException {
        Path logs = tempDir.resolve("logs");
        Path oldFile = file(logs, "old.log", TWO_DAYS_MILLIS);
        Path freshFile = file(logs, "fresh.log", 1000);
        Path nestedOld = file(logs.resolve("nested"), "nested-old.log", TWO_DAYS_MILLIS);

        run(keeperFor(folder(logs, null, OperationType.DELETE)));

        assertThat(oldFile).doesNotExist();
        assertThat(nestedOld).doesNotExist();
        assertThat(freshFile).exists();
        assertThat(logs.resolve("nested")).isDirectory();
    }

    @Test
    @DisplayName("DELETE: отсутствующая или пустая папка не вызывает ошибок")
    void deleteHandlesMissingAndEmptyDirectories() throws IOException {
        Path empty = Files.createDirectories(tempDir.resolve("empty"));

        run(keeperFor(
                folder(tempDir.resolve("missing"), null, OperationType.DELETE),
                folder(empty, null, OperationType.DELETE)));

        assertThat(empty).isDirectory();
    }

    @Test
    @DisplayName("ARCHIVE кладёт старые файлы в tar.gz в папке архива и удаляет исходники")
    void archiveMovesOldFilesToTarGz() throws Exception {
        Path dumps = tempDir.resolve("dumps");
        Path archive = tempDir.resolve("dumps-archive");
        Path oldFile = file(dumps, "old.tdump", TWO_DAYS_MILLIS);
        Path nestedOld = file(dumps.resolve("sub"), "nested-old.tdump", TWO_DAYS_MILLIS);
        Path freshFile = file(dumps, "fresh.tdump", 1000);

        run(keeperFor(folder(dumps, archive, OperationType.ARCHIVE)));

        assertThat(oldFile).doesNotExist();
        assertThat(nestedOld).doesNotExist();
        assertThat(freshFile).exists();
        File[] archives = archive.toFile().listFiles();
        assertThat(archives).hasSize(1);
        assertThat(archives[0].getName()).startsWith("tdumps-archive_").endsWith(".tar.gz");
        assertThat(entries(archives[0])).contains("old.tdump").contains("sub/nested-old.tdump");
        assertThat(entries(archives[0])).doesNotContain("fresh.tdump");
    }

    @Test
    @DisplayName("ARCHIVE без старых файлов не оставляет пустых архивов")
    void archiveWithoutOldFilesLeavesNothing() throws IOException {
        Path dumps = tempDir.resolve("dumps");
        Path archive = tempDir.resolve("dumps-archive");
        Path freshFile = file(dumps, "fresh.tdump", 1000);

        run(keeperFor(folder(dumps, archive, OperationType.ARCHIVE)));

        assertThat(freshFile).exists();
        try (Stream<Path> files = Files.list(archive)) {
            assertThat(files).isEmpty();
        }
    }

    @Test
    @DisplayName("ARCHIVE без папки архива использует исходную папку; пустая и отсутствующая папки пропускаются")
    void archiveWithoutArchiveFolderAndSkippedSources() throws IOException {
        Path dumps = tempDir.resolve("in-place");
        file(dumps, "old.tdump", TWO_DAYS_MILLIS);

        run(keeperFor(
                folder(dumps, null, OperationType.ARCHIVE),
                folder(tempDir.resolve("absent"), tempDir.resolve("a1"), OperationType.ARCHIVE),
                folder(
                        Files.createDirectories(tempDir.resolve("blank")),
                        tempDir.resolve("a2"),
                        OperationType.ARCHIVE)));

        try (Stream<Path> files = Files.list(dumps)) {
            assertThat(files.map(p -> p.getFileName().toString())).anyMatch(name -> name.endsWith(".tar.gz"));
        }
    }

    @Test
    @DisplayName("ARCHIVE одного файла: путь к файлу вместо папки")
    void archiveSingleFile() throws IOException {
        Path source = file(tempDir.resolve("single"), "one.tdump", TWO_DAYS_MILLIS);
        Path archive = tempDir.resolve("single-archive");

        run(keeperFor(folder(source, archive, OperationType.ARCHIVE)));

        assertThat(source).doesNotExist();
        assertThat(archive.toFile().listFiles()).hasSize(1);
    }

    @Test
    @DisplayName("Пустая настройка, пустое имя папки и неизвестная операция не приводят к ошибкам")
    void tolerantToBadConfiguration() throws IOException {
        run(new HouseKeeper(null, 1000));
        run(new HouseKeeper(new FoldersHolder().setFolders(List.of()), 1000));
        run(new HouseKeeper(new FoldersHolder().setFolders(null), 1000));
        Path dir = tempDir.resolve("x");
        run(keeperFor(
                new Folder().setName(" ").setTimeUnit(TimeUnitEnum.DAYS).setOperationType(OperationType.DELETE),
                folder(dir, null, null),
                new Folder().setName(dir.toString()).setOperationType(OperationType.DELETE)));
    }

    @Test
    @DisplayName("Интервал не меньше секунды; старт и остановка работают; геттеры папки возвращают значения")
    void lifecycleAndAccessors() {
        assertThatThrownBy(() -> new HouseKeeper(new FoldersHolder(), 10)).isInstanceOf(IllegalArgumentException.class);
        HouseKeeper keeper = new HouseKeeper(new FoldersHolder(), 1000);
        keeper.start();
        keeper.stop();

        Folder folder = folder(tempDir, tempDir, OperationType.ARCHIVE);
        assertThat(folder.getName()).isEqualTo(tempDir.toString());
        assertThat(folder.getArchivalFolder()).isEqualTo(tempDir.toString());
        assertThat(folder.getRetentionPeriod()).isEqualTo(1);
        assertThat(folder.getTimeUnit()).isEqualTo(TimeUnitEnum.DAYS);
        assertThat(folder.getOperationType()).isEqualTo(OperationType.ARCHIVE);
        assertThat(new FoldersHolder().setFolders(List.of(folder)).getFolders()).containsExactly(folder);
    }

    @Test
    @DisplayName("simpleHouseKeeperConfig: архивация дампов старше 2 часов и удаление архивов старше 7 дней")
    void simpleConfig() {
        FoldersHolder holder = StackTracePrinter.simpleHouseKeeperConfig(
                tempDir.resolve("dumps").toString());

        assertThat(holder.getFolders()).hasSize(2);
        assertThat(holder.getFolders().get(0).getOperationType()).isEqualTo(OperationType.ARCHIVE);
        assertThat(holder.getFolders().get(0).getTimeUnit()).isEqualTo(TimeUnitEnum.HOURS);
        assertThat(holder.getFolders().get(1).getOperationType()).isEqualTo(OperationType.DELETE);
        assertThat(holder.getFolders().get(1).getRetentionPeriod()).isEqualTo(7);
    }

    @Test
    @DisplayName("StackTracePrinter пишет файл дампа со стеками текущих потоков")
    void stackTracePrinterWritesDump() throws IOException {
        Path dumps = tempDir.resolve("tdumps");
        StackTracePrinter printer = new StackTracePrinter(dumps.toString(), 1000);

        ReflectionTestUtils.invokeMethod(printer, "dumpStacks");

        File[] files = dumps.toFile().listFiles();
        assertThat(files).hasSize(1);
        assertThat(files[0].getName()).endsWith(".tdump");
        assertThat(Files.readString(files[0].toPath()))
                .contains("Dump of")
                .contains("main")
                .contains("prio=");

        printer.start();
        printer.stop();
        assertThatThrownBy(() -> new StackTracePrinter(dumps.toString(), 10))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new StackTracePrinter(null, 1000)).isInstanceOf(NullPointerException.class);
    }

    private static List<String> entries(File archive) throws IOException {
        List<String> names = new java.util.ArrayList<>();
        try (TarArchiveInputStream tar =
                new TarArchiveInputStream(new GzipCompressorInputStream(Files.newInputStream(archive.toPath())))) {
            TarArchiveEntry entry;
            while ((entry = tar.getNextTarEntry()) != null) {
                names.add(entry.getName());
            }
        }
        return names;
    }
}
