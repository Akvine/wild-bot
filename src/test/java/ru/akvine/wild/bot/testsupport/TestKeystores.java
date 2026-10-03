package ru.akvine.wild.bot.testsupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Создаёт настоящую PKCS12-ключницу средствами {@code keytool} из текущего JDK: два клиентских сертификата с
 * приватным ключом ({@value #CLIENT_1}, {@value #CLIENT_2}) и один доверенный ({@value #TRUST_1}). Ключница
 * собирается один раз на весь запуск тестов.
 */
public final class TestKeystores {
    public static final String PASSWORD = "changeit";
    public static final String CLIENT_1 = "client1";
    public static final String CLIENT_2 = "client2";
    public static final String TRUST_1 = "trust1";

    public static final String EXPIRED = "expired";
    public static final String VALID = "valid";

    private static Path cached;
    private static Path cachedExpired;

    private TestKeystores() {}

    public static synchronized Path path() {
        if (cached == null) {
            try {
                cached = create(Files.createTempDirectory("wild-bot-keystore"));
            } catch (IOException | InterruptedException e) {
                throw new IllegalStateException("Can't create test keystore", e);
            }
        }
        return cached;
    }

    /**
     * Ключница с двумя сертификатами: {@value #EXPIRED} уже истёк 5 дней назад, {@value #VALID} действует ещё
     * 365 дней
     */
    public static synchronized Path expiredPath() {
        if (cachedExpired == null) {
            try {
                Path directory = Files.createTempDirectory("wild-bot-expired-keystore");
                Path keystore = directory.resolve("expired.p12");
                generate(keystore, VALID);
                keytool(
                        "-genkeypair",
                        "-alias",
                        EXPIRED,
                        "-keyalg",
                        "RSA",
                        "-keysize",
                        "2048",
                        "-dname",
                        "CN=" + EXPIRED,
                        "-startdate",
                        "-10d",
                        "-validity",
                        "5",
                        "-storetype",
                        "PKCS12",
                        "-keystore",
                        keystore.toString(),
                        "-storepass",
                        PASSWORD,
                        "-keypass",
                        PASSWORD);
                cachedExpired = keystore;
            } catch (IOException | InterruptedException e) {
                throw new IllegalStateException("Can't create expired test keystore", e);
            }
        }
        return cachedExpired;
    }

    private static Path create(Path directory) throws IOException, InterruptedException {
        Path keystore = directory.resolve("test.p12");
        Path certificate = directory.resolve("trust.cer");

        generate(keystore, CLIENT_1);
        generate(keystore, CLIENT_2);
        generate(keystore, "tmp");
        keytool(
                "-exportcert",
                "-alias",
                "tmp",
                "-keystore",
                keystore.toString(),
                "-storepass",
                PASSWORD,
                "-file",
                certificate.toString());
        keytool(
                "-importcert",
                "-alias",
                TRUST_1,
                "-file",
                certificate.toString(),
                "-keystore",
                keystore.toString(),
                "-storepass",
                PASSWORD,
                "-noprompt");
        keytool("-delete", "-alias", "tmp", "-keystore", keystore.toString(), "-storepass", PASSWORD);
        return keystore;
    }

    private static void generate(Path keystore, String alias) throws IOException, InterruptedException {
        keytool(
                "-genkeypair",
                "-alias",
                alias,
                "-keyalg",
                "RSA",
                "-keysize",
                "2048",
                "-dname",
                "CN=" + alias,
                "-validity",
                "365",
                "-storetype",
                "PKCS12",
                "-keystore",
                keystore.toString(),
                "-storepass",
                PASSWORD,
                "-keypass",
                PASSWORD);
    }

    private static void keytool(String... arguments) throws IOException, InterruptedException {
        List<String> command = new ArrayList<>();
        command.add(Path.of(System.getProperty("java.home"), "bin", "keytool").toString());
        command.addAll(List.of(arguments));
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        String output = new String(process.getInputStream().readAllBytes());
        if (!process.waitFor(60, TimeUnit.SECONDS) || process.exitValue() != 0) {
            throw new IllegalStateException("keytool failed: " + command + "\n" + output);
        }
    }
}
