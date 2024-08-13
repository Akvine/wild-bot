package ru.akvine.marketspace.bot.admin.converters;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.akvine.marketspace.bot.admin.dto.advert.*;
import ru.akvine.marketspace.bot.enums.AdvertStatus;
import ru.akvine.marketspace.bot.services.domain.AdvertModel;
import ru.akvine.marketspace.bot.services.domain.AdvertStatisticModel;
import ru.akvine.marketspace.bot.services.dto.admin.advert.ListAdvert;
import ru.akvine.marketspace.bot.services.dto.admin.advert.PauseAdvert;
import ru.akvine.marketspace.bot.services.dto.admin.advert.RenameAdvert;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdvertConverter {
    public PauseAdvert convertToPauseAdvert(PauseAdvertRequest request) {
        return new PauseAdvert()
                .setAdvertId(request.getAdvertId() == null ? null : request.getAdvertId())
                .setAdvertUuid(StringUtils.isBlank(request.getAdvertUuid()) ? null : request.getAdvertUuid());
    }

    public ListAdvert convertToListAdvert(ListAdvertRequest request) {
        List<AdvertStatus> statuses = request
                .getStatuses()
                .stream()
                .map(status -> AdvertStatus.valueOf(status.toUpperCase()))
                .collect(Collectors.toList());
        return new ListAdvert()
                .setStatuses(statuses);
    }

    public PauseAdvertResponse convertToPauseAdvert(AdvertStatisticModel advertStatisticBean) {
        return new PauseAdvertResponse()
                .setAdvertId(advertStatisticBean.getAdvertBean().getAdvertId())
                .setAdvertStatistic(new AdvertStatisticDto()
                        .setAtbs(advertStatisticBean.getAtbs())
                        .setCr(advertStatisticBean.getCr())
                        .setSum(advertStatisticBean.getSum())
                        .setCpc(advertStatisticBean.getCpc())
                        .setCtr(advertStatisticBean.getCtr())
                        .setShks(advertStatisticBean.getShks())
                        .setViews(advertStatisticBean.getViews())
                        .setSum(advertStatisticBean.getSum())
                        .setSumPrice(advertStatisticBean.getSumPrice())
                        .setOrders(advertStatisticBean.getOrders())
                        .setClicks(advertStatisticBean.getClicks())
                );
    }

    public ListAdvertResponse convertToAdvertListResponse(List<AdvertModel> adverts) {
        return new ListAdvertResponse()
                .setCount(adverts.size())
                .setAdverts(adverts.stream().map(this::convertToAdvertDto).collect(Collectors.toList()));
    }

    public RenameAdvert convertToRenameAdvert(RenameAdvertRequest request) {
        return new RenameAdvert()
                .setAdvertId(request.getAdvertId() == null ? null : request.getAdvertId())
                .setAdvertUuid(StringUtils.isBlank(request.getAdvertUuid()) ? null : request.getAdvertUuid())
                .setName(request.getName());
    }

    private AdvertDto convertToAdvertDto(AdvertModel advertBean) {
        return new AdvertDto()
                .setUuid(advertBean.getUuid())
                .setName(advertBean.getName())
                .setAdvertId(advertBean.getAdvertId())
                .setCategoryId(advertBean.getCategoryId())
                .setChangeTime(advertBean.getChangeTime())
                .setCpm(advertBean.getCpm())
                .setStartBudgetSum(advertBean.getStartBudgetSum())
                .setCheckBudgetSum(advertBean.getCheckBudgetSum())
                .setNextCheckDateTime(advertBean.getNextCheckDateTime())
                .setStatus(advertBean.getStatus())
                .setType(advertBean.getType())
                .setAvailableForStart(advertBean.getAvailableForStart())
                .setAvailable(advertBean.isAvailableForStart())
                .setCreatedDate(advertBean.getCreatedDate())
                .setUpdatedDate(advertBean.getUpdatedDate());
    }
}
