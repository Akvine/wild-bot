package ru.akvine.wild.bot.infrastructure.lock;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Метод-джоба ({@code void}) выполняется только в одном из запущенных экземпляров приложения: перед запуском берётся
 * распределённая блокировка без ожидания ({@link DistributedLockProvider#tryLock(String, Runnable)}); если её уже
 * держит другой экземпляр (или предыдущий запуск ещё идёт) - этот запуск пропускается. Блокировка снимается по
 * окончании метода, а если процесс умер - освобождается сама (keepalive в БД, watchdog в Redis).
 * <p>
 * Ставится на методы с {@code @Scheduled}, которые нельзя выполнять параллельно в нескольких экземплярах (изменяют
 * данные, ходят в Wildberries, шлют уведомления). Джобы, которые обслуживают сам процесс (обновление кеша,
 * синхронизация настроек), и джобы с собственной защитой от повторов (outbox с lease) не помечаются.
 * См. {@link SingleInstanceAspect}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SingleInstance {

    /** Идентификатор блокировки; по умолчанию {@code JOB_<Класс>_<метод>} */
    String value() default "";
}
