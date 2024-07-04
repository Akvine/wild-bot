package ru.akvine.marketspace.bot.services.integration.telegram;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.akvine.marketspace.bot.exceptions.IntegrationException;
import ru.akvine.marketspace.bot.telegram.TelegramLongPoolingBot;
import ru.akvine.marketspace.bot.utils.ByteUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramIntegrationServiceOrigin implements TelegramIntegrationService {
    private TelegramLongPoolingBot bot;
    private AbsSender absSender;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    public void setBot(@Lazy TelegramLongPoolingBot bot) {
        this.bot = bot;
    }

    @Autowired
    public void setAbsSender(@Lazy AbsSender absSender) {
        this.absSender = absSender;
    }

    @Override
    public byte[] downloadPhoto(String photoId, String chatId) {
        Preconditions.checkNotNull(photoId, "photoId is null");

        try {
            GetFile getFileRequest = new GetFile();
            getFileRequest.setFileId(photoId);
            File file = bot.execute(getFileRequest);

            String fileUrl = file.getFileUrl(botToken);

            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            return ByteUtils.convertToBytes(connection.getInputStream());
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling telegram api method = [%s]. Message = %s",
                    TelegramApiMethods.DOWNLOAD_PHOTO, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public void sendFile(InputStream file, String fileName, String chatId) {
        try {
            SendDocument sendDocument = new SendDocument();
            sendDocument.setChatId(chatId);
            sendDocument.setDocument(new InputFile(file, fileName));
            absSender.execute(sendDocument);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling telegram api method = [%s]. Message = %s",
                    TelegramApiMethods.SEND_FILE, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }

    @Override
    public void sendMessage(List<String> chatIds, String message) {
        try {
            for (String chatId : chatIds) {
                bot.execute(new SendMessage(chatId, message));
            }
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling telegram api method = [%s]. Message = %s",
                    TelegramApiMethods.SEND_MESSAGE, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }
}
