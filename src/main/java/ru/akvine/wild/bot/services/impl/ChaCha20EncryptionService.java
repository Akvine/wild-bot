package ru.akvine.wild.bot.services.impl;

import java.nio.charset.StandardCharsets;
import org.bouncycastle.crypto.engines.ChaChaEngine;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.services.EncryptionService;

@Service
public class ChaCha20EncryptionService implements EncryptionService {

    private final byte[] keyBytes;
    private final byte[] ivBytes;

    public ChaCha20EncryptionService(
            @Value("${crypt.chacha20.secret.key}") String chaChaSecretKey,
            @Value("${crypt.chacha20.iv}") String chaChaIv) {
        keyBytes = Hex.decode(chaChaSecretKey);
        ivBytes = Hex.decode(chaChaIv);
    }

    @Override
    public String decrypt(String encryptedHex) {
        if (encryptedHex == null || encryptedHex.isEmpty()) {
            return "";
        }
        byte[] encryptedData = Hex.decode(encryptedHex);
        byte[] decryptedData = new byte[encryptedData.length];

        ChaChaEngine engine = new ChaChaEngine();
        engine.init(false, new ParametersWithIV(new KeyParameter(keyBytes), ivBytes));
        engine.processBytes(encryptedData, 0, encryptedData.length, decryptedData, 0);
        return new String(decryptedData, StandardCharsets.UTF_8).trim();
    }

    @Override
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return "";
        }
        byte[] inputData = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedData = new byte[inputData.length];

        ChaChaEngine engine = new ChaChaEngine();
        engine.init(false, new ParametersWithIV(new KeyParameter(keyBytes), ivBytes));
        engine.processBytes(inputData, 0, inputData.length, encryptedData, 0);
        return Hex.toHexString(encryptedData);
    }
}
