package com.icoolkj.platform.infra.async.redis;

import com.icoolkj.platform.infra.async.AsyncProperties;
import com.icoolkj.platform.infra.async.AsyncTaskMessage;
import com.icoolkj.platform.infra.async.AsyncTaskPrincipalResolver;
import com.icoolkj.platform.infra.async.AsyncTaskRetrySupport;
import com.icoolkj.platform.infra.async.AsyncTaskStatus;
import com.icoolkj.platform.infra.async.handler.DefaultAsyncTaskExecutor;
import com.icoolkj.platform.infra.domain.AsyncTaskEntity;
import com.icoolkj.platform.infra.mapper.AsyncTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedisStreamAsyncTaskTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private StreamOperations<String, Object, Object> streamOps;
    @Mock
    private AsyncTaskMapper asyncTaskMapper;
    @Mock
    private DefaultAsyncTaskExecutor asyncTaskExecutor;

    private AsyncProperties props;
    private RedisStreamTaskPublisher publisher;

    @BeforeEach
    void setUp() {
        props = new AsyncProperties();
        props.setProvider("redis-stream");
        props.setQueueName("test:async:stream");
        props.setConsumerGroup("test-group");
        props.setConsumerName("test-consumer");
        props.setPollBatchSize(5);
        when(stringRedisTemplate.opsForStream()).thenReturn(streamOps);
        when(streamOps.createGroup(anyString(), anyString())).thenReturn("OK");
        when(streamOps.add(any())).thenReturn(RecordId.of("1-0"));
        publisher = new RedisStreamTaskPublisher(stringRedisTemplate, props);
    }

    private ObjectProvider<AsyncTaskPrincipalResolver> emptyPrincipalResolver() {
        @SuppressWarnings("unchecked")
        ObjectProvider<AsyncTaskPrincipalResolver> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(null);
        return provider;
    }

    @Test
    void publisherAddsStreamRecord() {
        publisher.publish(AsyncTaskMessage.of("tid-1", "t1", "u1"));
        ArgumentCaptor<MapRecord<String, String, String>> captor = ArgumentCaptor.forClass(MapRecord.class);
        verify(streamOps).add(captor.capture());
        MapRecord<String, String, String> record = captor.getValue();
        assertEquals("test:async:stream", record.getStream());
        assertEquals("tid-1", record.getValue().get(AsyncTaskMessage.FIELD_TASK_ID));
        assertEquals("t1", record.getValue().get(AsyncTaskMessage.FIELD_TENANT_ID));
        assertEquals("u1", record.getValue().get(AsyncTaskMessage.FIELD_USER_ID));
    }

    @Test
    void consumerAcksTerminalTaskWithoutExecuting() {
        AsyncTaskEntity entity = new AsyncTaskEntity();
        entity.setId("tid-done");
        entity.setTaskStatus(AsyncTaskStatus.SUCCESS);
        when(asyncTaskMapper.selectById("tid-done")).thenReturn(entity);

        Map<Object, Object> fields = Map.of(
                AsyncTaskMessage.FIELD_TASK_ID, "tid-done",
                AsyncTaskMessage.FIELD_TENANT_ID, "t1",
                AsyncTaskMessage.FIELD_USER_ID, "u1");
        MapRecord<String, Object, Object> record = StreamRecords.mapBacked(fields)
                .withStreamKey("test:async:stream")
                .withId(RecordId.of("2-0"));

        when(streamOps.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .thenReturn(List.of(record))
                .thenReturn(List.of());

        RedisStreamTaskConsumer consumer = new RedisStreamTaskConsumer(
                stringRedisTemplate, props, asyncTaskMapper, asyncTaskExecutor,
                new AsyncTaskRetrySupport(asyncTaskMapper, props), publisher,
                emptyPrincipalResolver());
        int n = consumer.poll();
        assertEquals(1, n);
        verify(asyncTaskExecutor, never()).execute(anyString());
        verify(streamOps, atLeastOnce()).acknowledge(eq("test:async:stream"), eq("test-group"), eq(RecordId.of("2-0")));
    }

    @Test
    void consumerExecutesPendingThenAcksSuccess() {
        AsyncTaskEntity pending = new AsyncTaskEntity();
        pending.setId("tid-run");
        pending.setTenantId("t1");
        pending.setUserId("u1");
        pending.setTaskStatus(AsyncTaskStatus.PENDING);

        AsyncTaskEntity success = new AsyncTaskEntity();
        success.setId("tid-run");
        success.setTaskStatus(AsyncTaskStatus.SUCCESS);

        when(asyncTaskMapper.selectById("tid-run")).thenReturn(pending, success);

        Map<Object, Object> fields = Map.of(
                AsyncTaskMessage.FIELD_TASK_ID, "tid-run",
                AsyncTaskMessage.FIELD_TENANT_ID, "t1",
                AsyncTaskMessage.FIELD_USER_ID, "u1");
        MapRecord<String, Object, Object> record = StreamRecords.mapBacked(fields)
                .withStreamKey("test:async:stream")
                .withId(RecordId.of("3-0"));

        when(streamOps.read(any(Consumer.class), any(StreamReadOptions.class), any(StreamOffset.class)))
                .thenReturn(List.of(record))
                .thenReturn(List.of());

        RedisStreamTaskConsumer consumer = new RedisStreamTaskConsumer(
                stringRedisTemplate, props, asyncTaskMapper, asyncTaskExecutor,
                new AsyncTaskRetrySupport(asyncTaskMapper, props), publisher,
                emptyPrincipalResolver());
        consumer.poll();
        verify(asyncTaskExecutor).execute("tid-run");
        verify(streamOps).acknowledge(eq("test:async:stream"), eq("test-group"), eq(RecordId.of("3-0")));
    }
}
