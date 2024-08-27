package ru.akvine.wild.bot.services.admin;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.akvine.wild.bot.entities.AdvertEntity;
import ru.akvine.wild.bot.entities.AdvertStatisticEntity;
import ru.akvine.wild.bot.enums.AdvertStatus;
import ru.akvine.wild.bot.exceptions.AdvertAlreadyInPauseStateException;
import ru.akvine.wild.bot.services.AdvertService;
import ru.akvine.wild.bot.services.AdvertStatisticService;
import ru.akvine.wild.bot.services.domain.AdvertModel;
import ru.akvine.wild.bot.services.domain.AdvertStatisticModel;
import ru.akvine.wild.bot.services.dto.admin.advert.ListAdvert;
import ru.akvine.wild.bot.services.dto.admin.advert.PauseAdvert;
import ru.akvine.wild.bot.services.dto.admin.advert.RenameAdvert;
import ru.akvine.wild.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.AdvertsInfoResponse;
import ru.akvine.wild.bot.utils.DateUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertAdminService {
    private final AdvertService advertService;
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final TelegramIntegrationService telegramIntegrationService;
    private final AdvertStatisticService advertStatisticService;

    private final static int ADVERT_PAUSE_STATUS_CODE = 11;

    public AdvertStatisticModel pauseAdvert(PauseAdvert pauseAdvert) {
        Preconditions.checkNotNull(pauseAdvert, "pauseAdvert is null");
        logger.info("Pause advert by [{}]", pauseAdvert);

        AdvertEntity advertEntity;
        if (StringUtils.isNotBlank(pauseAdvert.getAdvertUuid())) {
            advertEntity = advertService.verifyExistsByUuid(pauseAdvert.getAdvertUuid());
        } else {
            advertEntity = advertService.verifyExistsByExternalId(pauseAdvert.getAdvertId());
        }

        if (advertEntity.getStatus() == AdvertStatus.PAUSE) {
            throw new AdvertAlreadyInPauseStateException("Advert with id = [" + advertEntity.getExternalId() + "] already in pause!");
        }

        int advertId = advertEntity.getExternalId();
        AdvertsInfoResponse infoResponse = wildberriesIntegrationService.getAdvertsInfo(List.of(advertId));
        List<AdvertDto> advertsInfo = infoResponse.getAdverts();
        if (advertsInfo.isEmpty()) {
            String errorMessage = String.format(
                    "Error info adverts response, get info for %s but advertsInfo is empty",
                    advertId);
            throw new IllegalStateException(errorMessage);
        }
        AdvertDto advertDto = advertsInfo.getFirst();
        if (advertDto.getStatus() != ADVERT_PAUSE_STATUS_CODE) {
            wildberriesIntegrationService.pauseAdvert(advertEntity.getExternalId());
        }
        AdvertStatisticModel advertStatisticBean = advertStatisticService.getAndSave(advertEntity);

        String finishedTestMessage = String.format(
                "Тест с advert id = %s успешно завершился.\nВведите команду /report для просмотра отчета",
                advertId
        );
        telegramIntegrationService.sendMessage(advertEntity.getClient().getChatId(), finishedTestMessage);

        advertEntity.setNextCheckDateTime(null);
        advertEntity.setStatus(AdvertStatus.PAUSE);
        advertEntity.setOrdinalStatus(AdvertStatus.PAUSE.getCode());
        advertEntity.setClient(null);
        advertEntity.setLocked(false);
        AdvertModel updatedAdvert = advertService.update(new AdvertModel(advertEntity));

        logger.info("Successful pause advert = [{}]", updatedAdvert);
        return advertStatisticBean;
    }

    public void pauseAdvertForce(PauseAdvert pauseAdvert) {
        Preconditions.checkNotNull(pauseAdvert, "pauseAdvert is null");
        logger.info("Force pause advert by [{}]", pauseAdvert);

        AdvertEntity advertEntity;
        if (StringUtils.isNotBlank(pauseAdvert.getAdvertUuid())) {
            advertEntity = advertService.verifyExistsByUuid(pauseAdvert.getAdvertUuid());
        } else {
            advertEntity = advertService.verifyExistsByExternalId(pauseAdvert.getAdvertId());
        }

        if (advertEntity.getStatus() == AdvertStatus.PAUSE) {
            throw new AdvertAlreadyInPauseStateException("Advert with id = [" + advertEntity.getExternalId() + "] already in pause!");
        }

        int advertId = advertEntity.getExternalId();
        AdvertsInfoResponse infoResponse = wildberriesIntegrationService.getAdvertsInfo(List.of(advertId));
        List<AdvertDto> advertsInfo = infoResponse.getAdverts();
        if (advertsInfo.isEmpty()) {
            String errorMessage = String.format(
                    "Error info adverts response, get info for %s but advertsInfo is empty",
                    advertId);
            throw new IllegalStateException(errorMessage);
        }
        AdvertDto advertDto = advertsInfo.getFirst();
        if (advertDto.getStatus() != ADVERT_PAUSE_STATUS_CODE) {
            wildberriesIntegrationService.pauseAdvert(advertId);
        }

        Long clientId = advertEntity.getClient().getId();
        AdvertStatisticEntity advertStatistic = advertStatisticService.verifyExistsByClientIdAndAdvertId(clientId, advertEntity.getId());
        advertStatisticService.delete(advertStatistic);

        advertEntity.setNextCheckDateTime(null);
        advertEntity.setStatus(AdvertStatus.PAUSE);
        advertEntity.setOrdinalStatus(AdvertStatus.PAUSE.getCode());
        advertEntity.setClient(null);
        advertEntity.setLocked(false);
        advertEntity.setAvailableForStart(DateUtils.getStartOfNextDay());
        AdvertModel updatedAdvert = advertService.update(new AdvertModel(advertEntity));

        logger.info("Successful force pause advert = [{}]", updatedAdvert);
    }

    public List<AdvertModel> listAdvert(ListAdvert listAdvert) {
        Preconditions.checkNotNull(listAdvert, "listAdvert is null");
        logger.info("List adverts by statuses = {}", listAdvert.getStatuses());
        return advertService.getAdvertsByStatuses(listAdvert.getStatuses());
    }

    public void renameAdvert(RenameAdvert renameAdvert) {
        Preconditions.checkNotNull(renameAdvert, "renameAdvert is null");
        logger.info("Rename advert by [{}]", renameAdvert);

        AdvertEntity advertEntity;
        if (StringUtils.isNotBlank(renameAdvert.getAdvertUuid())) {
            advertEntity = advertService.verifyExistsByUuid(renameAdvert.getAdvertUuid());
        } else {
            advertEntity = advertService.verifyExistsByExternalId(renameAdvert.getAdvertId());
        }
        wildberriesIntegrationService.renameAdvert(advertEntity.getExternalId(), renameAdvert.getName());
        advertEntity.setExternalTitle(renameAdvert.getName());
        advertService.update(new AdvertModel(advertEntity));

        logger.info("Successful rename advert = [{}]", renameAdvert);
    }
}
