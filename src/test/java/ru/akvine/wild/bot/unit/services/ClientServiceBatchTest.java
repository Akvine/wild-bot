package ru.akvine.wild.bot.unit.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import ru.akvine.wild.bot.entities.ClientEntity;
import ru.akvine.wild.bot.repositories.ClientRepository;
import ru.akvine.wild.bot.services.ClientBlockingService;
import ru.akvine.wild.bot.services.ClientService;
import ru.akvine.wild.bot.services.domain.ClientModel;
import ru.akvine.wild.bot.services.encryption.EncryptionService;

class ClientServiceBatchTest {
    private static final int BATCH_SIZE = 2;
    private static final Pageable PAGE = PageRequest.ofSize(BATCH_SIZE);

    private final ClientRepository clientRepository = mock(ClientRepository.class);
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientService =
                new ClientService(clientRepository, mock(ClientBlockingService.class), mock(EncryptionService.class));
        ReflectionTestUtils.setField(clientService, "batchSize", BATCH_SIZE);
    }

    private static ClientEntity client(long id) {
        return new ClientEntity().setId(id).setChatId("chat-" + id);
    }

    @Test
    @DisplayName("Пачка запрашивается по id, после которого идти клиентам, с размером из настройки")
    void batchIsRequestedAfterGivenIdWithConfiguredSize() {
        when(clientRepository.findBatchAfterId(5, PAGE)).thenReturn(List.of(client(7), client(9)));

        List<ClientModel> batch = clientService.getBatchAfterId(5);

        assertThat(batch).extracting(ClientModel::getId).containsExactly(7L, 9L);
        verify(clientRepository).findBatchAfterId(5, PAGE);
    }

    @Test
    @DisplayName("Обход всех клиентов циклом: каждая следующая пачка запрашивается после id последнего клиента предыдущей")
    void loopWalksThroughAllClients() {
        when(clientRepository.findBatchAfterId(0, PAGE)).thenReturn(List.of(client(1), client(5)));
        when(clientRepository.findBatchAfterId(5, PAGE)).thenReturn(List.of(client(7), client(9)));
        when(clientRepository.findBatchAfterId(9, PAGE)).thenReturn(List.of(client(12)));
        when(clientRepository.findBatchAfterId(12, PAGE)).thenReturn(List.of());

        // тот же цикл, что в ClientAdminService.sendMessage
        List<Long> processed = new java.util.ArrayList<>();
        long lastId = 0;
        while (true) {
            List<ClientModel> batch = clientService.getBatchAfterId(lastId);
            if (batch.isEmpty()) {
                break;
            }
            batch.forEach(client -> processed.add(client.getId()));
            lastId = batch.getLast().getId();
        }

        assertThat(processed).containsExactly(1L, 5L, 7L, 9L, 12L);
    }

    @Test
    @DisplayName("Если клиентов после id нет, возвращается пустой список")
    void emptyBatchWhenNoMoreClients() {
        when(clientRepository.findBatchAfterId(100, PAGE)).thenReturn(List.of());

        assertThat(clientService.getBatchAfterId(100)).isEmpty();
    }
}
