package com.icoolkj.platform.framework.audit;

import com.icoolkj.platform.framework.cache.CacheTtlProperties;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditIntegrityServiceReanchorTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private final ConcurrentHashMap<String, String> redis = new ConcurrentHashMap<>();
    private AuditIntegrityService service;

    @BeforeEach
    void setUp() {
        CacheTtlProperties ttl = new CacheTtlProperties();
        ttl.setKeyPrefix("icoolkj");
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        org.mockito.Mockito.doAnswer(inv -> {
            redis.put(inv.getArgument(0), inv.getArgument(1));
            return null;
        }).when(valueOperations).set(anyString(), anyString(), any(Duration.class));
        service = new AuditIntegrityService(stringRedisTemplate, ttl);
    }

    @Test
    void reanchorRebuildsFromGenesis() {
        record Row(String id, String payload) {
        }
        List<Row> rows = List.of(new Row("1", "a"), new Row("2", "b"));
        List<AuditIntegrityService.ChainHashes> written = new ArrayList<>();

        service.reanchor(
                AuditIntegrityService.CHAIN_LOGIN,
                "t1",
                rows,
                Row::payload,
                (row, hashes) -> written.add(hashes));

        assertEquals(2, written.size());
        assertEquals(AuditIntegrityService.GENESIS, written.get(0).prevHash());
        assertEquals(written.get(0).rowHash(), written.get(1).prevHash());
        String expectedFirst = AuditIntegrityService.sha256Hex(AuditIntegrityService.GENESIS + "|a");
        assertEquals(expectedFirst, written.get(0).rowHash());
        assertTrue(redis.values().contains(written.get(1).rowHash()));
    }
}
