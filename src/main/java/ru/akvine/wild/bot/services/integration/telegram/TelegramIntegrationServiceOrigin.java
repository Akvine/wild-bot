package ru.akvine.wild.bot.services.integration.telegram;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.bots.AbsSender;
import ru.akvine.wild.bot.enums.TelegramDataType;
import ru.akvine.wild.bot.exceptions.IntegrationException;
import ru.akvine.wild.bot.telegram.TelegramData;
import ru.akvine.wild.bot.telegram.bot.TelegramDevBot;
import ru.akvine.wild.bot.utils.ByteUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramIntegrationServiceOrigin implements TelegramIntegrationService {
    // TODO : нужно инжектить разных ботов в зависимости от среды
    private TelegramDevBot bot;
    private AbsSender absSender;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Autowired
    public void setBot(@Lazy TelegramDevBot bot) {
        this.bot = bot;
    }

    @Autowired
    public void setAbsSender(@Lazy AbsSender absSender) {
        this.absSender = absSender;
    }

    @Override
    public void answerCallback(TelegramData telegramData) {
        if (telegramData.getType() == TelegramDataType.CALLBACK) {
            try {
                absSender.execute(
                        AnswerCallbackQuery.builder()
                                .callbackQueryId(telegramData.getData().getCallbackQuery().getId())
                                .build()
                );
            } catch (Exception exception) {
                String errorMessage = String.format(
                        "Error while calling telegram api method = [%s]. Message = %s",
                        TelegramApiMethods.ANSWER_CALLBACK, exception.getMessage());
                throw new IntegrationException(errorMessage);
            }
        }
    }

                               @Override
    public byte[] downloadPhoto(String photoId, String chatId) {
        Preconditions.checkNotNull(photoId, "photoId is null");
        logger.info("Download photo with id = [{}] for chat with id = {}", photoId, chatId);

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

    public void sendFile(String chatId, String fileName, byte[] file) {
        sendFile(chatId, fileName, new ByteArrayInputStream(file));
    }

    @Override
    public void sendFile(String chatId, String fileName, InputStream file) {
        logger.info("Send file with file name = [{}]", fileName);

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
        logger.info("Send message = [{}] to chat with ids = {}", message, chatIds);

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

    @Override
    public void sendImage(String chatId, byte[] image) {
        sendImage(chatId, new ByteArrayInputStream(image));
    }

    @Override
    public void sendImage(String chatId, InputStream image) {
       sendImage(chatId, image, null);
    }

    @Override
    public void sendImage(String chatId, byte[] image, String caption) {
        sendImage(chatId, new ByteArrayInputStream(image), caption);
    }

    @Override
    public void sendImage(String chatId, InputStream image, String caption) {
        try {
            SendPhoto sendPhoto = new SendPhoto();
            InputFile inputFile = new InputFile();
            inputFile.setMedia(image, "photo");
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(inputFile);
            if (StringUtils.isNotBlank(caption)) {
                sendPhoto.setCaption(caption);
            }

            absSender.execute(sendPhoto);
        } catch (Exception exception) {
            String errorMessage = String.format(
                    "Error while calling telegram api method = [%s]. Message = %s",
                    TelegramApiMethods.SEND_IMAGE, exception.getMessage());
            throw new IntegrationException(errorMessage);
        }
    }
}
