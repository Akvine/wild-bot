package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.entities.ClientEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.enums.AdvertType;
import ru.akvine.marketspace.bot.enums.DebitType;
import ru.akvine.marketspace.bot.exceptions.AdvertNotFoundException;
import ru.akvine.marketspace.bot.repositories.AdvertRepository;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;
import ru.akvine.marketspace.bot.services.domain.CardBean;
import ru.akvine.marketspace.bot.services.integration.wildberries.WildberriesIntegrationService;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertCreateRequest;
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

    public void saveAll(List<AdvertDto> adverts) {
        Preconditions.checkNotNull(adverts, "loadedAdverts is null");
        logger.info("Save new adverts, size = {}", adverts.size());

        adverts.forEach(advertDto -> {
            AdvertEntity advertEntity = new AdvertEntity()
                    .setUuid(UUIDGenerator.uuidWithoutDashes())
                    .setAdvertId(advertDto.getAdvertId())
                    .setName(advertDto.getName())
                    .setChangeTime(advertDto.getChangeTime())
                    .setType(AdvertType.getByCode(advertDto.getType()))
                    .setOrdinalType(advertDto.getType())
                    .setStatus(AdvertStatus.getByCode(advertDto.getStatus()))
                    .setOrdinalStatus(advertDto.getStatus())
                    .setItemId(advertDto.getAdvertParams().getNms().getFirst())
                    .setCategoryId(advertDto.getAdvertParams().getSubject().getId());
            if (advertDto.getAdvertParams() != null) {
                advertEntity.setCpm(advertDto.getAdvertParams().getCpm());
            }
            advertRepository.save(advertEntity);
        });

        logger.info("Successful save new adverts");
    }

    public List<AdvertBean> getAdvertsByStatuses(List<AdvertStatus> statuses) {
        Preconditions.checkNotNull(statuses, "advertStatuses is null");
        logger.info("Get adverts by statuses = {}", statuses);
        return advertRepository
                .findByStatuses(statuses)
                .stream()
                .map(AdvertBean::new)
                .collect(Collectors.toList());
    }

    public List<AdvertBean> getAdvertsByChatIdAndStatuses(String chatId, List<AdvertStatus> statuses) {
        Preconditions.checkNotNull(chatId, "chatId is null");
        Preconditions.checkNotNull(statuses, "advertStatuses is null");

        logger.info("Get adverts by statuses = {}", statuses);

        Long clientId = clientService.verifyExistsByChatId(chatId).getId();
        return advertRepository
                .findByClientIdAndStatuses(clientId, statuses)
                .stream()
                .map(AdvertBean::new)
                .toList();

    }

    public AdvertBean update(AdvertBean advertBean) {
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
                .setName(advertBean.getName())
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
        AdvertBean updatedAdvert = new AdvertBean(advertRepository.save(advertEntity));

        logger.info("Successful update advert, result = [{}]", updatedAdvert);
        return updatedAdvert;
    }

    public AdvertBean getFirst(Integer categoryId) {
        Preconditions.checkNotNull(categoryId, "categoryId is null");
        logger.info("Get first advert by category id = {}", categoryId);

        List<AdvertBean> advertBeans = advertRepository
                .findByStatusesAndCategoryId(
                        List.of(AdvertStatus.PAUSE, AdvertStatus.READY_FOR_START),
                        categoryId)
                .stream()
                .map(AdvertBean::new)
                .toList();

        List<AdvertBean> pauseAdvertBeans = advertBeans
                .stream()
                .filter(advertBean -> advertBean.getStatus().equals(AdvertStatus.PAUSE))
                .filter(advertBean -> !advertBean.isLocked())
                .filter(AdvertBean::isAvailableForStart)
                .toList();
        if (!pauseAdvertBeans.isEmpty()) {
            AdvertBean pauseAdvert = pauseAdvertBeans.getFirst();
            logger.info("Return pause advert = [{}]", pauseAdvert);
            return pauseAdvert;
        }

        List<AdvertBean> readyForStartAdvertBeans = advertBeans
                .stream()
                .filter(advertBean -> advertBean.getStatus().equals(AdvertStatus.READY_FOR_START))
                .filter(advertBean -> !advertBean.isLocked())
                .filter(AdvertBean::isAvailableForStart)
                .toList();
        if (!readyForStartAdvertBeans.isEmpty()) {
            AdvertBean advertReadyForStart = readyForStartAdvertBeans.getFirst();
            logger.info("Return ready for start advert = [{}]", advertReadyForStart);
            return advertReadyForStart;
        }

        if (createAdvertsByApi) {
            logger.info("Create advert with category id = {} by API", categoryId);
            CardBean cardBean = cardService.getFirst(categoryId);
            String advertName = "Created by API: " + LocalDateTime.now();
            AdvertCreateRequest request = new AdvertCreateRequest()
                    .setSubjectId(categoryId)
                    .setSum(advertBudgetSumIncrease)
                    .setName(advertName)
                    .setType(AdvertType.AUTO.getCode())
                    .setBtype(DebitType.ACCOUNT.getCode())
                    .setOnPause(true)
                    .setCpm(advertMinCpm)
                    .setNms(new int[]{cardBean.getItemId()});
            int advertId = wildberriesIntegrationService.createAdvert(request);

            AdvertEntity advertEntity = new AdvertEntity()
                    .setUuid(UUIDGenerator.uuidWithoutDashes())
                    .setAdvertId(advertId)
                    .setChangeTime(new Date())
                    .setLocked(false)
                    .setCpm(advertMinCpm)
                    .setCategoryId(categoryId)
                    .setItemId(cardBean.getItemId())
                    .setStatus(AdvertStatus.PAUSE)
                    .setOrdinalStatus(AdvertStatus.PAUSE.getCode())
                    .setType(AdvertType.AUTO)
                    .setOrdinalType(AdvertType.AUTO.getCode())
                    .setName(advertName);
            AdvertEntity savedAdvert = advertRepository.save(advertEntity);
            savedAdvert.setAvailableForStart(LocalDateTime.now().plusMinutes(3));
            advertRepository.save(savedAdvert);
        }

        String errorMessage = String.format(
                "Pause or ready for start advert with specified category id = [%s] not found!",
                categoryId);
        throw new AdvertNotFoundException(errorMessage);
    }

    public AdvertBean getByAdvertId(int advertId) {
        logger.info("Get advert with id = {}", advertId);
        return new AdvertBean(verifyExistsByAdvertId(advertId));
    }

    public AdvertEntity verifyExistsByAdvertId(int advertId) {
        logger.info("Verify advert exists with id = {}", advertId);
        return advertRepository
                .findByAdvertId(advertId)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with advertId = [" + advertId + "] not found!"));
    }

    public AdvertEntity verifyExistsByAdvertIdAndClientId(int advertId, long clientId) {
        logger.info("Verify advert exists with id = {} launched by client with id = {}", advertId, clientId);
        return advertRepository
                .findByAdvertIdAndClientId(advertId, clientId)
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
