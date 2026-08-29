package ru.akvine.wild.bot.services.integration.wildberries;

import java.util.List;
import ru.akvine.wild.bot.services.integration.wildberries.dto.advert.*;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.CardDto;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.ChangeStocksRequest;
import ru.akvine.wild.bot.services.integration.wildberries.dto.card.type.CardTypeResponse;

public interface WildberriesIntegrationService {
    /**
     * Получение списка карточек продавца
     *
     * @return
     */
    List<CardDto> getCards(String apiToken);

    /**
     * Получение списка рекламных кампаний продавца
     *
     * @return
     */
    AdvertListResponse getAdverts(String apiToken);

    /**
     * Узнать бюджет рекламной кампании (в рублях)
     *
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    AdvertBudgetInfoResponse getAdvertBudgetInfo(int advertId, String apiToken);

    /**
     * Пополнить бюджет рекламной кампании. Сумма фиксировано пополняется на 1000 рублей
     *
     * @param advertId идентификатор кампании в системе WB
     * @param sum      сумма пополнения бюджета в рублях
     * @return
     */
    AdvertBudgetDepositResponse advertBudgetDeposit(int advertId, int sum, String apiToken);

    /**
     * Запуск рекламной кампании
     *
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    void startAdvert(int advertId, String apiToken);

    /**
     * Получение детальной информации о рекламных кампаниях
     *
     * @param advertIds список id рекламных кампаний продавца
     * @return
     */
    AdvertsInfoResponse getAdvertsInfo(List<Integer> advertIds, String apiToken);

    /**
     * Получение краткой статистики по кампании (количество кликов, ctr и т.д.)
     *
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    AdvertStatisticResponse getAdvertStatistic(String advertId, String apiToken);

    /**
     * Приостановка на паузу рекламной кампании
     *
     * @param advertId идентификатор кампании в системе WB
     * @return
     */
    void pauseAdvert(int advertId, String apiToken);

    /**
     * Изменение ставки кампании
     *
     * @param request запрос на изменение ставки у кампании
     * @return
     */
    void changeAdvertCpm(AdvertChangeCpmRequest request, String apiToken);

    /**
     * Переименование кампании
     *
     * @param advertId идентификатор кампании в WB
     * @param name     новое имя (Не может быть больше 100 символов)
     */
    void renameAdvert(int advertId, String name, String apiToken);

    /**
     * Добавление фотографии
     *
     * @param request данные запроса
     */
    AdvertUploadPhotoResponse uploadPhoto(AdvertUploadPhotoRequest request, String apiToken);

    /**
     * Изменение остатка товара (карточки) на складе
     *
     * @param request
     * @param warehouseId - идентификатор склада
     */
    void changeStocks(ChangeStocksRequest request, int warehouseId, String apiToken);

    /**
     * Получение полной статистики по кампании за определенные даты
     *
     * @param request
     */
    AdvertFullStatisticResponse[] getAdvertsFullStatisticByDates(List<AdvertFullStatisticDatesDto> request, String apiToken);

    /**
     * Получение полной статистики по кампании за интервал времени
     *
     * @param request
     */
    AdvertFullStatisticResponse[] getAdvertsFullStatisticByInterval(List<AdvertFullStatisticIntervalDto> request, String apiToken);

    /**
     * Получение товаров
     *
     * @param request
     */
    GetGoodsResponse getGoods(GetGoodsRequest request, String apiToken);

    /**
     * Установить цену и скидку для товара
     *
     * @param request
     */
    void setGoodPriceAndDiscount(SetGoodPriceRequest request, String apiToken);

    /**
     * Создать автоматическую рекламную кампанию.
     * В ответе возвращает id рекламной кампании (advertId)
     *
     * @param request
     * @return
     */
    int createAdvert(AdvertCreateRequest request, String apiToken);

    /**
     * Получение списка типов товаров (Женский, Мужской, Детский и т.д.)
     * @return список типов товаров или ошибку
     */
    CardTypeResponse getTypes(String apiToken);
}
