package ru.akvine.marketspace.bot.services;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.akvine.marketspace.bot.entities.AdvertEntity;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.enums.AdvertType;
import ru.akvine.marketspace.bot.exceptions.AdvertNotFoundException;
import ru.akvine.marketspace.bot.repositories.AdvertRepository;
import ru.akvine.marketspace.bot.services.domain.AdvertBean;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.AdvertDto;
import ru.akvine.marketspace.bot.utils.UUIDGenerator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvertService {
    @Value("${advert.uuid.length}")
    private int length;

    private final AdvertRepository advertRepository;
    private final Random random = new Random();

    public void saveAll(List<AdvertDto> adverts) {
        Preconditions.checkNotNull(adverts, "loadedAdverts is null");
        logger.info("Save new adverts, size = {}", adverts.size());

        adverts.forEach(advertDto -> {
            AdvertEntity advertEntity = new AdvertEntity()
                    .setUuid(UUIDGenerator.uuid(length))
                    .setAdvertId(advertDto.getAdvertId())
                    .setName(advertDto.getName())
                    .setChangeTime(advertDto.getChangeTime())
                    .setType(AdvertType.getByCode(advertDto.getType()))
                    .setOrdinalType(advertDto.getType())
                    .setStatus(AdvertStatus.getByCode(advertDto.getStatus()))
                    .setOrdinalStatus(advertDto.getStatus())
                    .setItemId(advertDto.getAdvertParams().getNms().get(0))
                    .setCpm(advertDto.getAdvertParams() != null ? advertDto.getAdvertParams().getCpm(): null)
                    .setCategoryId(advertDto.getAdvertParams().getSubject().getId());
            advertRepository.save(advertEntity);
            });

        logger.info("Successful save new adverts");
    }

    public List<AdvertBean> getAdvertsByStatuses(List<AdvertStatus> statuses) {
        Preconditions.checkNotNull(statuses, "advertStatuses is null");
        return advertRepository
                .findByStatuses(statuses)
                .stream()
                .map(AdvertBean::new)
                .collect(Collectors.toList());
    }

    public AdvertBean update(AdvertBean advertBean) {
        logger.info("Update advert by bean = [{}]", advertBean);
        Preconditions.checkNotNull(advertBean, "advertBean is null");

        AdvertEntity advertEntity = advertRepository
                .findByUuid(advertBean.getUuid())
                .orElseThrow(() -> new AdvertNotFoundException("Advert with uuid = [" + advertBean.getUuid() + "] not found!"));
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
                .setUpdatedDate(LocalDateTime.now());
        AdvertBean updatedAdvert = new AdvertBean(advertRepository.save(advertEntity));

        logger.info("Successful update advert, result = [{}]", updatedAdvert);
        return updatedAdvert;
    }

    public AdvertBean randomlyGetAdvert(String categoryId) {
        Preconditions.checkNotNull(categoryId, "categoryId is null");

        List<AdvertBean> advertBeans = advertRepository
                .findByStatusesAndCategoryId(
                        List.of(AdvertStatus.PAUSE, AdvertStatus.READY_FOR_START),
                        categoryId)
                .stream()
                .map(AdvertBean::new)
                .collect(Collectors.toList());

        List<AdvertBean> pauseAdvertBeans = advertBeans
                .stream()
                .filter(advertBean -> advertBean.getStatus().equals(AdvertStatus.PAUSE))
                .collect(Collectors.toList());
        if (!pauseAdvertBeans.isEmpty()) {
            int size = pauseAdvertBeans.size();
            return pauseAdvertBeans.get(random.nextInt(size));
        }

        List<AdvertBean> readyForStartAdvertBeans = advertBeans
                .stream()
                .filter(advertBean -> advertBean.getStatus().equals(AdvertStatus.READY_FOR_START))
                .collect(Collectors.toList());
        if (!readyForStartAdvertBeans.isEmpty()) {
            int size = pauseAdvertBeans.size();
            return readyForStartAdvertBeans.get(random.nextInt(size));
        }

        String errorMessage = String.format(
                "Pause or ready for start advert with specified category id = [%s] not found!",
                categoryId);
        throw new AdvertNotFoundException(errorMessage);
    }

    public AdvertEntity verifyExistsByAdvertId(String advertId) {
        Preconditions.checkNotNull(advertId, "advertId is null");
        return advertRepository
                .findByAdvertId(advertId)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with advertUd = [" + advertId + "] not found!"));
    }

    public AdvertEntity verifyExistsByUuid(String uuid) {
        Preconditions.checkNotNull(uuid, "uuid is null");
        return advertRepository
                .findByUuid(uuid)
                .orElseThrow(() -> new AdvertNotFoundException("Advert with uuid = [" + uuid + "] not found!"));
    }
}
