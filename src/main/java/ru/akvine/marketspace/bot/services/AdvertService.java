package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.CardEntity;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.enums.AdvertType;
import ru.akvine.marketspace.bot.enums.DebitType;
import ru.akvine.marketspace.bot.exceptions.AdvertNotFoundException;
import ru.akvine.marketspace.bot.repositories.AdvertRepository;
import ru.akvine.marketspace.bot.services.domain.AdvertModel;
import ru.akvine.marketspace.bot.services.domain.CardModel;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertCreateRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.SkuDto;
import ru.akvine.marketspace.bot.utils.UUIDGenerator;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertService {
    private final AdvertRepository advertRepository;
    private final ClientService clientService;
    private final WildberriesIntegrationService wildberriesIntegrationService;
    private final CardService cardService;

    @Value("${create.adverts.by.api.enabled}")
    private boolean createAdvertsByApi;
    @Value("${advert.budget.sum.increase.value}")
    private int advertBudgetSumIncrease;
    @Value("${advert.min.cpm}")
    private int advertMinCpm;
    @Value("${wildberries.warehouse.id}")
    private int warehouseId;
    @Value("${wildberries.change.stocks.count}")
    private int changeStocksCount;

    public void saveAll(List<AdvertDto> adverts) {
        Preconditions.checkNotNull(adverts, "loadedAdverts is null");
        logger.info("Save new adverts, size = {}", adverts.size());

        adverts.forEach(advertDto -> {
            CardEntity card = cardService.verifyExistsByExternalId(
                    advertDto.getAdvertParams().getNms().getFirst()
            );
            AdvertEntity advertEntity = new AdvertEntity()
                    .setUuid(UUIDGenerator.uuidWithoutDashes())
                    .setExternalId(advertDto.getAdvertId())
                    .setExternalTitle(advertDto.getName())
                    .setChangeTime(advertDto.getChangeTime())
                    .setType(AdvertType.getByCode(advertDto.getType()))
                    .setOrdinalType(advertDto.getType())
                    .setStatus(AdvertStatus.getByCode(advertDto.getStatus()))
                    .setOrdinalStatus(advertDto.getStatus())
                    .setCard(card);
            if (advertDto.getAdvertParams() != null) {
                advertEntity.setCpm(advertDto.getAdvertParams().getCpm());
            }
            advertRepository.save(advertEntity);
        });

        logger.info("Successful save new adverts");
    }

    public List<AdvertModel> getAdvertsByStatuses(List<AdvertStatus> statuses) {
        Preconditions.checkNotNull(statuses, "advertStatuses is null");
        logger.info("Get adverts by statuses = {}", statuses);
        return advertRepository
                .findByStatuses(statuses)
                .stream()
                .map(AdvertModel::new)
                .collect(Collectors.toList());
    }

    public List<AdvertModel> getAdvertsByChatIdAndStatuses(String chatId, List<AdvertStatus> statuses) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        Preconditions.checkNotNull(statuses, "advertStatuses is null");

        logger.info("Get adverts by statuses = {}", statuses);

        Long clientId = clientService.verifyExistsByChatId(chatId).getId();
        return advertRepository
                .findByClientIdAndStatuses(clientId, statuses)
                .stream()
                .map(AdvertModel::new)
                .toList();

    }

    public AdvertModel update(AdvertModel advertBean) {
        Preconditions.checkNotNull(advertBean, "advertBean is null");
        logger.info("Update advert by bean = [{}]", advertBean);

        AdvertEntity advertEntity = advertRepository
                .findByUuid(advertBean.getUuid())
                .orElseThrow(() -> new AdvertNotFoundException("Advert with uuid = [" + advertBean.getUuid() + "] not found!"));
        if (StringUtils.isNotBlank(advertBean.getChatId())) {
            ClientEntity client = clientService.verifyExistsByChatId(advertBean.getChatId());
            advertEntity.setClient(client);
        }
        advertEntity
                .setStartBudgetSum(advertBean.getStartBudgetSum())
                .setNextCheckDateTime(advertBean.getNextCheckDateTime())
                .setExternalTitle(advertBean.getName())
                .setCpm(advertBean.getCpm())
                .setStatus(advertBean.getStatus())
                .setOrdinalStatus(advertBean.getStatus().getCode())
                .setType(advertBean.getType())
                .setOrdinalType(advertBean.getType().getCode())
                .setStartCheckDateTime(advertBean.getStartCheckDateTime())
                .setCheckBudgetSum(advertBean.getCheckBudgetSum())
                .setLocked(advertBean.isLocked())
                .setAvailableForStart(advertBean.getAvailableForStart())
                .setUpdatedDate(LocalDateTime.now());
        AdvertModel updatedAdvert = new AdvertModel(advertRepository.save(advertEntity));

        logger.info("Successful update advert, result = [{}]", updatedAdvert);
        return updatedAdvert;
    }

    public AdvertModel getFirst(String cardType, int categoryId) {
        Preconditions.checkNotNull(cardType, "cardType is null");
        logger.info("Get first advert by card type = {} and category id = {}", cardType, categoryId);

        List<AdvertModel> advertBeans = advertRepository
                .findByStatusesAndCardTypeAndCategoryId(
                        List.of(AdvertStatus.PAUSE, AdvertStatus.READY_FOR_START),
                        cardType,
                        categoryId)
                .stream()
                .map(AdvertModel::new)
                .toList();

        List<AdvertModel> pauseAdvertBeans = advertBeans
                .stream()
                .filter(advertBean -> advertBean.getStatus().equals(AdvertStatus.PAUSE))
                .filter(advertBean -> !advertBean.isLocked())
                .filter(AdvertModel::isAvailableForStart)
                .toList();
        if (!pauseAdvertBeans.isEmpty()) {
            AdvertModel pauseAdvert = pauseAdvertBeans.getFirst();
            logger.info("Return pause advert = [{}]", pauseAdvert);
            return pauseAdvert;
        }

        List<AdvertModel> readyForStartAdvertBeans = advertBeans
                .stream()
                .filter(advertBean -> advertBean.getStatus().equals(AdvertStatus.READY_FOR_START))
                .filter(advertBean -> !advertBean.isLocked())
                .filter(AdvertModel::isAvailableForStart)
                .toList();
        if (!readyForStartAdvertBeans.isEmpty()) {
            AdvertModel advertReadyForStart = readyForStartAdvertBeans.getFirst();
            logger.info("Return ready for start advert = [{}]", advertReadyForStart);
            return advertReadyForStart;
        }

        if (createAdvertsByApi) {
            logger.info("Create advert with category id = {} by API", categoryId);
            CardModel cardBean = cardService.getFirst(categoryId);
            wildberriesIntegrationService.changeStocks(new ChangeStocksRequest()
                    .setStocks(List.of(
                            new SkuDto()
                                    .setAmount(changeStocksCount)
                                    .setSku(cardBean.getBarcode())
                    )), warehouseId);

            String advertName = "Created by API: " + LocalDateTime.now();
            AdvertCreateRequest request = new AdvertCreateRequest()
                    .setSubjectId(categoryId)
                    .setSum(advertBudgetSumIncrease)
                    .setName(advertName)
                    .setType(AdvertType.AUTO.getCode())
                    .setBtype(DebitType.ACCOUNT.getCode())
                    .setOnPause(true)
                    .setCpm(advertMinCpm)
                    .setNms(new int[]{cardBean.getExternalId()});
            int advertId = wildberriesIntegrationService.createAdvert(request);

            AdvertEntity advertEntity = new AdvertEntity()
                    .setUuid(UUIDGenerator.uuidWithoutDashes())
                    .setExternalId(advertId)
                    .setChangeTime(new Date())
                    .setLocked(false)
                    .setCpm(advertMinCpm)
                    .setExternalId(cardBean.getExternalId())
                    .setStatus(AdvertStatus.PAUSE)
                    .setOrdinalStatus(AdvertStatus.PAUSE.getCode())
                    .setType(AdvertType.AUTO)
                    .setOrdinalType(AdvertType.AUTO.getCode())
                    .setExternalTitle(advertName);
            AdvertEntity savedAdvert = advertRepository.save(advertEntity);
            savedAdvert.setAvailableForStart(LocalDateTime.now().plusMinutes(3));
            advertRepository.save(savedAdvert);
        }

        String errorMessage = String.format(
                "Pause or ready for start advert with specified category id = [%s] not found!",
                categoryId);
        throw new AdvertNotFoundException(errorMessage);
    }

    public AdvertModel getByAdvertId(int advertId) {
        logger.info("Get advert with id = {}", advertId);
        return new AdvertModel(verifyExistsByExternalId(advertId));
    }

    public AdvertEntity verifyExistsByExternalId(int externalId) {
        logger.info("Verify advert exists with external id = {}", externalId);
        return advertRepository
                .findByExternalId(externalId)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with external id = [" + externalId + "] not found!"));
    }

    public AdvertEntity verifyExistsByAdvertIdAndClientId(int advertId, long clientId) {
        logger.info("Verify advert exists with id = {} launched by client with id = {}", advertId, clientId);
        return advertRepository
                .findByExternalIdAndClientId(advertId, clientId)
                .orElseThrow(() -> {
                    String errorMessage = String.format(
                            "Advert with advertId = [%s] and launched client with id = [%s] not found!",
                            advertId, clientId
                    );
                    return new AdvertNotFoundException(errorMessage);
                });
    }

    public AdvertEntity verifyExistsByUuid(String uuid) {
        Preconditions.checkNotNull(uuid, "uuid is null");
        logger.info("Verify advert exists with uuid = {}", uuid);
        return advertRepository
                .findByUuid(uuid)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with uuid = [" + uuid + "] not found!"));
    }
}
