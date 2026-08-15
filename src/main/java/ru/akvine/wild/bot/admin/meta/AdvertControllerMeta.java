package ru.akvine.wild.bot.admin.meta;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.akvine.wild.bot.admin.dto.advert.ListAdvertRequest;
import ru.akvine.wild.bot.admin.dto.advert.PauseAdvertRequest;
import ru.akvine.wild.bot.admin.dto.advert.RenameAdvertRequest;
import ru.akvine.wild.bot.admin.dto.advert.UpdateAdvertRequest;
import ru.akvine.wild.bot.admin.dto.common.Response;

/**
 * Контракт админ-панели для управления рекламными кампаниями (тестами) клиентов —
 * просмотр, переименование, пауза и обновление кампании сотрудником поддержки.
 */
@RequestMapping(value = "/admin/adverts")
public interface AdvertControllerMeta {

    /**
     * Ставит рекламную кампанию на паузу.
     *
     * @param request идентификатор кампании
     * @return успешный ответ
     */
    @PostMapping(value = "/pause")
    Response pause(@Valid @RequestBody PauseAdvertRequest request);

    /**
     * Принудительно ставит рекламную кампанию на паузу, минуя обычные проверки
     * (например, состояние кампании).
     *
     * @param request идентификатор кампании
     * @return успешный ответ
     */
    @PostMapping(value = "/pause/force")
    Response pauseForce(@Valid @RequestBody PauseAdvertRequest request);

    /**
     * Возвращает список рекламных кампаний по заданным условиям фильтрации/страницы.
     *
     * @param request параметры фильтрации и постраничной выборки
     * @return список найденных кампаний
     */
    @PostMapping(value = "/list")
    Response list(@Valid @RequestBody ListAdvertRequest request);

    /**
     * Переименовывает рекламную кампанию.
     *
     * @param request идентификатор кампании и новое имя
     * @return успешный ответ
     */
    @PostMapping(value = "/rename")
    Response rename(@Valid @RequestBody RenameAdvertRequest request);

    /**
     * Обновляет данные рекламной кампании.
     *
     * @param request идентификатор кампании и поля для обновления
     * @return кампания после обновления
     */
    @PutMapping
    Response update(@Valid @RequestBody UpdateAdvertRequest request);
}
