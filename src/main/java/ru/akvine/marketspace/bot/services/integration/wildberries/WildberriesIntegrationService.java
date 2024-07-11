package ru.akvine.marketspace.bot.services.integration.wildberries;

import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;

import java.util.List;

public interface WildberriesIntegrationService {
    /**
     * Получение списка карточек продавца
     * @return
     */
    List<CardDto> getCards();

    /**
     * Получение списка рекламных кампаний продавца
     * @return
     */
    AdvertListResponse getAdverts();


    /**
     * Узнать бюджет рекламной кампании (в рублях)
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    AdvertBudgetInfoResponse getAdvertBudgetInfo(String advertId);

    /**
     * Пополнить бюджет рекламной кампании. Сумма фиксировано пополняется на 1000 рублей
     * @param advertId идентификатор кампании в системе WB
     * @param sum сумма пополнения бюджета в рублях
     * @return
     */
    AdvertBudgetDepositResponse advertBudgetDeposit(String advertId, int sum);


    /**
     * Запуск рекламной кампании
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    void startAdvert(String advertId);

    /**
     * Получение детальной информации о рекламных кампаниях
     * @param advertIds список id рекламных кампаний продавца
     * @return
     */
    AdvertsInfoResponse getAdvertsInfo(List<String> advertIds);

    /**
     * Получение краткой статистики по кампании (количество кликов, ctr и т.д.)
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    AdvertStatisticResponse getAdvertStatistic(String advertId);

    /**
     * Приостановка на паузу рекламной кампании
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    void pauseAdvert(String advertId);

    /**
     * Изменение ставки кампании
     * @param request запрос на изменение ставки у кампании
     * @return
     */
    void changeAdvertCpm(AdvertChangeCpmRequest request);

    /**
     * Переименование кампании
     * @param advertId идентификатор кампании в WB
     * @param name новое имя (Не может быть больше 100 символов)
     */
    void renameAdvert(String advertId, String name);

    /**
     * Добавление фотографии
     * @param request данные запроса
     */
    AdvertUploadPhotoResponse uploadPhoto(AdvertUploadPhotoRequest request);

    /**
     * Изменение остатка товара (карточки) на складе
     * @param request
     */
    void changeStocks(ChangeStocksRequest request);

    /**
     * Получение полной статистики по кампании за определенные даты
     * @param request
     */
    AdvertFullStatisticResponse[] getAdvertsFullStatisticByDates(List<AdvertFullStatisticDatesDto> request);

    /**
     * Получение полной статистики по кампании за интервал времени
     * @param request
     */
    AdvertFullStatisticResponse[] getAdvertsFullStatisticByInterval(List<AdvertFullStatisticIntervalDto> request);
}
