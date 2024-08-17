package ru.akvine.marketspace.bot.services.integration.wildberries;

import ru.akvine.marketspace.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.marketspace.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

import java.util.List;

public interface WildberriesIntegrationService {
    /**
     * Получение списка карточек продавца
     *
     * @return
     */
    List<CardDto> getCards();

    /**
     * Получение списка рекламных кампаний продавца
     *
     * @return
     */
    AdvertListResponse getAdverts();


    /**
     * Узнать бюджет рекламной кампании (в рублях)
     *
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    AdvertBudgetInfoResponse getAdvertBudgetInfo(int advertId);

    /**
     * Пополнить бюджет рекламной кампании. Сумма фиксировано пополняется на 1000 рублей
     *
     * @param advertId идентификатор кампании в системе WB
     * @param sum      сумма пополнения бюджета в рублях
     * @return
     */
    AdvertBudgetDepositResponse advertBudgetDeposit(int advertId, int sum);


    /**
     * Запуск рекламной кампании
     *
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    void startAdvert(int advertId);

    /**
     * Получение детальной информации о рекламных кампаниях
     *
     * @param advertIds список id рекламных кампаний продавца
     * @return
     */
    AdvertsInfoResponse getAdvertsInfo(List<Integer> advertIds);

    /**
     * Получение краткой статистики по кампании (количество кликов, ctr и т.д.)
     *
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    AdvertStatisticResponse getAdvertStatistic(String advertId);

    /**
     * Приостановка на паузу рекламной кампании
     *
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    void pauseAdvert(int advertId);

    /**
     * Изменение ставки кампании
     *
     * @param request запрос на изменение ставки у кампании
     * @return
     */
    void changeAdvertCpm(AdvertChangeCpmRequest request);

    /**
     * Переименование кампании
     *
     * @param advertId идентификатор кампании в WB
     * @param name     новое имя (Не может быть больше 100 символов)
     */
    void renameAdvert(int advertId, String name);

    /**
     * Добавление фотографии
     *
     * @param request данные запроса
     */
    AdvertUploadPhotoResponse uploadPhoto(AdvertUploadPhotoRequest request);

    /**
     * Изменение остатка товара (карточки) на складе
     *
     * @param request
     * @param warehouseId - идентификатор склада
     */
    void changeStocks(ChangeStocksRequest request, int warehouseId);

    /**
     * Получение полной статистики по кампании за определенные даты
     *
     * @param request
     */
    AdvertFullStatisticResponse[] getAdvertsFullStatisticByDates(List<AdvertFullStatisticDatesDto> request);

    /**
     * Получение полной статистики по кампании за интервал времени
     *
     * @param request
     */
    AdvertFullStatisticResponse[] getAdvertsFullStatisticByInterval(List<AdvertFullStatisticIntervalDto> request);

    /**
     * Получение товаров
     *
     * @param request
     */
    GetGoodsResponse getGoods(GetGoodsRequest request);

    /**
     * Установить цену и скидку для товара
     *
     * @param request
     */
    void setGoodPriceAndDiscount(SetGoodPriceRequest request);

    /**
     * Создать автоматическую рекламную кампанию.
     * В ответе возвращает id рекламной кампании (advertId)
     *
     * @param request
     * @return
     */
    int createAdvert(AdvertCreateRequest request);

    /**
     * Получение списка типов товаров (Женский, Мужской, Детский и т.д.)
     * @return список типов товаров или ошибку
     */
    CardTypeResponse getTypes();
}
