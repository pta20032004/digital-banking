package com.tony.demo.modules.transaction.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.testcontainers.containers.GenericContainer;
import com.tony.demo.modules.account.domain.Account;
import com.tony.demo.modules.account.domain.AccountRepository;
import com.tony.demo.modules.transaction.application.TransactionDto.TransactionRequest;

@SpringBootTest
@Testcontainers
public class TransactionServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.4.0"));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.elasticsearch.uris", () -> "localhost:9200"); // Mock ES port or disable it
    }

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    void setup() {
        accountRepository.deleteAll();

        Account source = Account.builder()
                .userId(1L)
                .accountNumber("SRC123")
                .balance(new BigDecimal("1000.00"))
                .build();

        Account target = Account.builder()
                .userId(2L)
                .accountNumber("TGT456")
                .balance(new BigDecimal("0.00"))
                .build();

        accountRepository.save(source);
        accountRepository.save(target);
    }

    @Test
    void transfer_ConcurrentRequests_ShouldPreventRaceCondition() throws InterruptedException {
        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        List<Callable<Void>> tasks = new ArrayList<>();

        // 10 threads trying to transfer 100 concurrently from SRC to TGT
        for (int i = 0; i < numberOfThreads; i++) {
            tasks.add(() -> {
                TransactionRequest request = new TransactionRequest();
                request.setRequestId(UUID.randomUUID().toString()); // unique for each thread to avoid idempotency check block
                request.setSourceAccountNumber("SRC123");
                request.setTargetAccountNumber("TGT456");
                request.setAmount(new BigDecimal("100.00"));
                request.setDescription("Concurrent Transfer");
                
                try {
                    transactionService.transfer(request);
                } catch (Exception e) {
                    // Ignore exceptions for lack of balance if any
                }
                return null;
            });
        }

        List<Future<Void>> futures = executorService.invokeAll(tasks);
        executorService.shutdown();

        // Validate final balances
        Account finalSource = accountRepository.findByAccountNumber("SRC123").orElseThrow();
        Account finalTarget = accountRepository.findByAccountNumber("TGT456").orElseThrow();

        // 10 transfers of 100 each = 1000 total transferred
        // Initial was 1000. Result should be exactly 0
        assertEquals(0, finalSource.getBalance().compareTo(new BigDecimal("0.00")));
        assertEquals(0, finalTarget.getBalance().compareTo(new BigDecimal("1000.00")));
    }
}
