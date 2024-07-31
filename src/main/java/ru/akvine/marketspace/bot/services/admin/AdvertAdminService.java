package ru.akvine.marketspace.bot.services.admin;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.AdvertStatisticEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.exceptions.AdvertAlreadyInPauseStateException;
import ru.akvine.marketspace.bot.services.AdvertService;
import ru.akvine.marketspace.bot.services.AdvertStatisticService;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;
import ru.akvine.marketspace.bot.services.domain.AdvertStatisticBean;
import ru.akvine.marketspace.bot.services.dto.admin.advert.ListAdvert;
import ru.akvine.marketspace.bot.services.dto.admin.advert.PauseAdvert;
import ru.akvine.marketspace.bot.services.dto.admin.advert.RenameAdvert;
import ru.akvine.marketspace.bot.services.integration.telegram.TelegramIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertsInfoResponse;

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

    public AdvertStatisticBean pauseAdvert(PauseAdvert pauseAdvert) {
        Preconditions.checkNotNull(pauseAdvert, "pauseAdvert is null");
        logger.info("Pause advert by [{}]", pauseAdvert);

        AdvertEntity advertEntity;
        if (StringUtils.isNotBlank(pauseAdvert.getAdvertUuid())) {
            advertEntity = advertService.verifyExistsByUuid(pauseAdvert.getAdvertUuid());
        } else {
            advertEntity = advertService.verifyExistsByAdvertId(pauseAdvert.getAdvertId());
        }

        if (advertEntity.getStatus() == AdvertStatus.PAUSE) {
            throw new AdvertAlreadyInPauseStateException("Advert with id = [" + advertEntity.getItemId() + "] already in pause!");
        }

        int advertId = advertEntity.getAdvertId();
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
            wildberriesIntegrationService.pauseAdvert(advertEntity.getAdvertId());
        }
        AdvertStatisticBean advertStatisticBean = advertStatisticService.getAndSave(advertEntity);

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
        AdvertBean updatedAdvert = advertService.update(new AdvertBean(advertEntity));

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
            advertEntity = advertService.verifyExistsByAdvertId(pauseAdvert.getAdvertId());
        }

        if (advertEntity.getStatus() == AdvertStatus.PAUSE) {
            throw new AdvertAlreadyInPauseStateException("Advert with id = [" + advertEntity.getItemId() + "] already in pause!");
        }

        int advertId = advertEntity.getAdvertId();
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
        AdvertBean updatedAdvert = advertService.update(new AdvertBean(advertEntity));

        logger.info("Successful force pause advert = [{}]", updatedAdvert);
    }

    public List<AdvertBean> listAdvert(ListAdvert listAdvert) {
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
            advertEntity = advertService.verifyExistsByAdvertId(renameAdvert.getAdvertId());
        }
        wildberriesIntegrationService.renameAdvert(advertEntity.getAdvertId(), renameAdvert.getName());
        advertEntity.setName(renameAdvert.getName());
        advertService.update(new AdvertBean(advertEntity));

        logger.info("Successful rename advert = [{}]", renameAdvert);
    }
}
