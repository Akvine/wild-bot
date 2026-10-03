package ru.akvine.wild.bot.unit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import ru.akvine.wild.bot.entities.security.BlockedCredentialsEntity;
import ru.akvine.wild.bot.exceptions.security.BlockedCredentialsException;
import ru.akvine.wild.bot.repositories.security.BlockedCredentialsRepository;
import ru.akvine.wild.bot.services.security.SupportBlockingService;

@DisplayName("SupportBlockingService: блокировки логинов с кешем и очисткой")
class SupportBlockingServiceTest {
    private BlockedCredentialsRepository repository;
    private SupportBlockingService service;

    @BeforeEach
    void setUp() {
        repository = mock(BlockedCredentialsRepository.class);
        service = new SupportBlockingService(repository);
        ReflectionTestUtils.setField(service, "otpBlockTimeMinutes", 15L);
    }

    private static BlockedCredentialsEntity block(long id, String login, LocalDateTime end) {
        BlockedCredentialsEntity entity = new BlockedCredentialsEntity();
        entity.setId(id);
        entity.setLogin(login);
        entity.setBlockStartDate(end.minusMinutes(15));
        entity.setBlockEndDate(end);
        return entity;
    }

    @Test
    @DisplayName("Нет блокировки в БД - дата разблокировки null")
    void noBlock() {
        when(repository.findByLogin("a@x.com")).thenReturn(Optional.empty());

        assertThat(service.getUnblockDate("a@x.com")).isNull();
        assertThat(service.isBlocked("a@x.com")).isFalse();
    }

    @Test
    @DisplayName("Действующая блокировка из БД кешируется: повторный запрос в БД не ходит")
    void activeBlockIsCached() {
        LocalDateTime end = LocalDateTime.now().plusMinutes(10);
        when(repository.findByLogin("a@x.com")).thenReturn(Optional.of(block(1, "a@x.com", end)));

        assertThat(service.getUnblockDate("a@x.com")).isEqualTo(end);
        assertThat(service.getUnblockDate("a@x.com")).isEqualTo(end);

        verify(repository, times(1)).findByLogin("a@x.com");
        assertThat(service.isBlocked("a@x.com")).isTrue();
    }

    @Test
    @DisplayName("Истёкшая блокировка в БД не считается действующей")
    void expiredBlockInDatabase() {
        when(repository.findByLogin("a@x.com"))
                .thenReturn(Optional.of(block(1, "a@x.com", LocalDateTime.now().minusMinutes(1))));

        assertThat(service.getUnblockDate("a@x.com")).isNull();
    }

    @Test
    @DisplayName("Новая блокировка сохраняется на заданное число минут и попадает в кеш; id возвращается из БД")
    void setBlockCreatesRecord() {
        when(repository.findByLogin("a@x.com")).thenReturn(Optional.empty());
        when(repository.save(any(BlockedCredentialsEntity.class))).thenAnswer(invocation -> {
            BlockedCredentialsEntity saved = invocation.getArgument(0);
            saved.setId(77L);
            return saved;
        });

        long id = service.setBlock("a@x.com", 30L);

        assertThat(id).isEqualTo(77L);
        ArgumentCaptor<BlockedCredentialsEntity> captor = ArgumentCaptor.forClass(BlockedCredentialsEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getLogin()).isEqualTo("a@x.com");
        assertThat(captor.getValue().getBlockEndDate())
                .isAfter(captor.getValue().getBlockStartDate().plusMinutes(29));
        assertThat(service.getUnblockDate("a@x.com"))
                .isEqualTo(captor.getValue().getBlockEndDate());
    }

    @Test
    @DisplayName("Блокировка по умолчанию использует время из настроек")
    void setBlockDefaultMinutes() {
        when(repository.findByLogin("a@x.com")).thenReturn(Optional.empty());
        when(repository.save(any(BlockedCredentialsEntity.class))).thenAnswer(SupportBlockingServiceTest::withId);

        service.setBlock("a@x.com");

        ArgumentCaptor<BlockedCredentialsEntity> captor = ArgumentCaptor.forClass(BlockedCredentialsEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getBlockEndDate())
                .isAfter(captor.getValue().getBlockStartDate().plusMinutes(14))
                .isBefore(captor.getValue().getBlockStartDate().plusMinutes(16));
    }

    @Test
    @DisplayName("Повторная блокировка уже заблокированного логина новую запись не создаёт и возвращает минуты")
    void setBlockForAlreadyBlocked() {
        when(repository.findByLogin("a@x.com"))
                .thenReturn(Optional.of(block(1, "a@x.com", LocalDateTime.now().plusMinutes(15))));

        long minutes = service.setBlock("a@x.com", 60L);

        assertThat(minutes).isEqualTo(15L);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Снятие блокировки удаляет запись и кеш; без записи - ошибка")
    void removeBlock() {
        BlockedCredentialsEntity entity =
                block(1, "a@x.com", LocalDateTime.now().plusMinutes(10));
        when(repository.findByLogin("a@x.com")).thenReturn(Optional.of(entity));
        service.getUnblockDate("a@x.com");

        assertThat(service.removeBlock("a@x.com")).isTrue();

        verify(repository).delete(entity);
        when(repository.findByLogin("a@x.com")).thenReturn(Optional.empty());
        assertThat(service.getUnblockDate("a@x.com")).isNull();
        assertThatThrownBy(() -> service.removeBlock("nobody@x.com")).isInstanceOf(BlockedCredentialsException.class);
    }

    @Test
    @DisplayName("Список блокировок берётся из репозитория")
    void list() {
        List<BlockedCredentialsEntity> all =
                List.of(block(1, "a@x.com", LocalDateTime.now().plusMinutes(1)));
        when(repository.findAll()).thenReturn(all);

        assertThat(service.list()).isSameAs(all);
    }

    @Test
    @DisplayName("Очистка кеша убирает истёкшие блокировки и оставляет действующие")
    void clearExpiredInCache() {
        LocalDateTime active = LocalDateTime.now().plusMinutes(10);
        LocalDateTime expired = LocalDateTime.now().minusMinutes(1);
        when(repository.findByLogin("active@x.com")).thenReturn(Optional.of(block(1, "active@x.com", active)));
        service.getUnblockDate("active@x.com");
        // истёкшая запись попадает в кеш через setBlock с нулевой длительностью
        when(repository.findByLogin("expired@x.com")).thenReturn(Optional.empty());
        when(repository.save(any(BlockedCredentialsEntity.class))).thenAnswer(SupportBlockingServiceTest::withId);
        service.setBlock("expired@x.com", -5L);
        assertThat(expired).isBefore(LocalDateTime.now());

        service.clearExpiredInCache();
        service.clearExpiredInCache();

        when(repository.findByLogin("expired@x.com")).thenReturn(Optional.empty());
        assertThat(service.getUnblockDate("expired@x.com")).isNull();
        assertThat(service.getUnblockDate("active@x.com")).isEqualTo(active);
    }

    @Test
    @DisplayName("Очистка БД удаляет истёкшие блокировки по id")
    void clearExpiredInDatabase() {
        when(repository.findExpired(any(LocalDateTime.class)))
                .thenReturn(List.of(
                        block(1, "a@x.com", LocalDateTime.now().minusMinutes(5)),
                        block(2, "b@x.com", LocalDateTime.now().minusMinutes(1))))
                .thenReturn(List.of());

        service.clearExpiredInDb();
        service.clearExpiredInDb();

        verify(repository).deleteById(1L);
        verify(repository).deleteById(2L);
    }

    private static BlockedCredentialsEntity withId(org.mockito.invocation.InvocationOnMock invocation) {
        BlockedCredentialsEntity saved = invocation.getArgument(0);
        saved.setId(1L);
        return saved;
    }
}
