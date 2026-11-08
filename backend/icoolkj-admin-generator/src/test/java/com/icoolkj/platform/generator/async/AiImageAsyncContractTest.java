package com.icoolkj.platform.generator.async;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoolkj.platform.common.exception.BusinessException;
import com.icoolkj.platform.generator.dto.AiImageGenerateRequest;
import com.icoolkj.platform.generator.vo.AiAsyncImageResultVO;
import com.icoolkj.platform.generator.vo.AiImageGenerateResultVO;
import com.icoolkj.platform.infra.async.AsyncTaskRuntime;
import com.icoolkj.platform.infra.vo.AsyncTaskSubmitVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiImageAsyncContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void toAsyncResultKeepsFileIdContract() {
        AiImageGenerateResultVO.AiImageItemVO item = new AiImageGenerateResultVO.AiImageItemVO();
        item.setFileId("f1");
        item.setUrl("/infra/file/f1/download?inline=1");
        AiImageGenerateResultVO generated = new AiImageGenerateResultVO();
        generated.setInvokeId("inv1");
        generated.setPrompt("cat");
        generated.setModel("m1");
        generated.setProviderName("p1");
        generated.setSize("1024x1024");
        generated.setN(1);
        generated.setFileId("f1");
        generated.setItems(List.of(item));

        AiAsyncImageResultVO result = AiImageGenerateAsyncHandler.toAsyncResult(generated);
        assertEquals("f1", result.getFileId());
        assertEquals("ai-image-f1.png", result.getFileName());
        assertEquals("inv1", result.getInvokeId());
        assertEquals(1, result.getItems().size());
        assertEquals("/infra/file/f1/download?inline=1", result.getItems().get(0).getUrl());
    }

    @Test
    void handlerTaskTypeIsPlatformConstant() {
        AiImageGenerateAsyncHandler handler = new AiImageGenerateAsyncHandler(objectMapper, null);
        assertEquals(AiAsyncConstants.TASK_IMAGE_GENERATE, handler.taskType());
        assertEquals("ai.image.generate", handler.taskType());
    }

    @Test
    void facadeRejectsBlankPrompt() {
        AiImageAsyncSubmitFacade facade = new AiImageAsyncSubmitFacade(new RecordingRuntime(), objectMapper);
        AiImageGenerateRequest request = new AiImageGenerateRequest();
        request.setPrompt("  ");
        BusinessException ex = assertThrows(BusinessException.class, () -> facade.submit(request));
        assertEquals("error.ai.image_prompt_required", ex.getMessage());
    }

    @Test
    void facadeSubmitsWithPlatformTaskType() throws Exception {
        RecordingRuntime runtime = new RecordingRuntime();
        AiImageAsyncSubmitFacade facade = new AiImageAsyncSubmitFacade(runtime, objectMapper);
        AiImageGenerateRequest request = new AiImageGenerateRequest();
        request.setPrompt("a dog");
        AsyncTaskSubmitVO vo = facade.submit(request);
        assertNotNull(vo.taskId());
        assertEquals(vo.taskId(), runtime.taskId);
        assertEquals(vo.taskId(), runtime.bizId);
        assertEquals(AiAsyncConstants.TASK_IMAGE_GENERATE, runtime.taskType);
        assertEquals(AiAsyncConstants.BIZ_TYPE_IMAGE, runtime.bizType);
        assertTrue(runtime.requestParam.contains("a dog"));
    }

    private static final class RecordingRuntime implements AsyncTaskRuntime {
        String taskId;
        String taskType;
        String bizType;
        String bizId;
        String requestParam;

        @Override
        public String submit(String taskType, String taskName, String bizType, String bizId, String requestParam) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String submitWithId(
                String taskId,
                String taskType,
                String taskName,
                String bizType,
                String bizId,
                String requestParam) {
            this.taskId = taskId;
            this.taskType = taskType;
            this.bizType = bizType;
            this.bizId = bizId;
            this.requestParam = requestParam;
            return taskId;
        }
    }
}
