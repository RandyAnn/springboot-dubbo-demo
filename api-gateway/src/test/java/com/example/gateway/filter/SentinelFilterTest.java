package com.example.gateway.filter;

import com.alibaba.csp.sentinel.slots.block.authority.AuthorityException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowException;

import com.example.shared.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SentinelFilter单元测试类
 * 测试不同类型的BlockException异常处理逻辑
 */
@ExtendWith(MockitoExtension.class)
class SentinelFilterTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletResponse response;

    @Mock
    private PrintWriter writer;

    @InjectMocks
    private SentinelFilter sentinelFilter;

    private StringWriter stringWriter;

    @BeforeEach
    void setUp() throws IOException {
        // 设置PrintWriter模拟
        stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        // 设置RequestContextHolder用于getCurrentRequestUri()方法
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/test");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // 设置ObjectMapper模拟返回
        when(objectMapper.writeValueAsString(any(ApiResponse.class)))
            .thenReturn("{\"code\":429,\"message\":\"test message\",\"data\":null}");
    }

    @Test
    void testFlowExceptionReturns429() throws Exception {
        // Given
        FlowException flowException = new FlowException("flow limit");

        // When
        invokeHandleBlockException(flowException);

        // Then
        verify(response).setStatus(429);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");

        // 验证ApiResponse.error调用
        ArgumentCaptor<ApiResponse> captor = ArgumentCaptor.forClass(ApiResponse.class);
        verify(objectMapper).writeValueAsString(captor.capture());

        ApiResponse<?> apiResponse = captor.getValue();
        assertEquals(429, apiResponse.getCode());
        assertEquals("请求过于频繁，请稍后再试", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void testDegradeExceptionReturns503() throws Exception {
        // Given
        DegradeException degradeException = new DegradeException("degrade");

        // When
        invokeHandleBlockException(degradeException);

        // Then
        verify(response).setStatus(503);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");

        // 验证ApiResponse.error调用
        ArgumentCaptor<ApiResponse> captor = ArgumentCaptor.forClass(ApiResponse.class);
        verify(objectMapper).writeValueAsString(captor.capture());

        ApiResponse<?> apiResponse = captor.getValue();
        assertEquals(503, apiResponse.getCode());
        assertEquals("服务暂时不可用，请稍后再试", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }



    @Test
    void testParamFlowExceptionReturns429() throws Exception {
        // Given
        ParamFlowException paramException = new ParamFlowException("param", "param flow");

        // When
        invokeHandleBlockException(paramException);

        // Then
        verify(response).setStatus(429);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");

        // 验证ApiResponse.error调用
        ArgumentCaptor<ApiResponse> captor = ArgumentCaptor.forClass(ApiResponse.class);
        verify(objectMapper).writeValueAsString(captor.capture());

        ApiResponse<?> apiResponse = captor.getValue();
        assertEquals(429, apiResponse.getCode());
        assertEquals("参数访问过于频繁，请稍后再试", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void testAuthorityExceptionReturns403() throws Exception {
        // Given
        AuthorityException authorityException = new AuthorityException("authority");

        // When
        invokeHandleBlockException(authorityException);

        // Then
        verify(response).setStatus(403);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");

        // 验证ApiResponse.error调用
        ArgumentCaptor<ApiResponse> captor = ArgumentCaptor.forClass(ApiResponse.class);
        verify(objectMapper).writeValueAsString(captor.capture());

        ApiResponse<?> apiResponse = captor.getValue();
        assertEquals(403, apiResponse.getCode());
        assertEquals("访问权限不足", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void testUnknownBlockExceptionReturns429() throws Exception {
        // Given - 创建一个未知的BlockException子类
        com.alibaba.csp.sentinel.slots.block.BlockException unknownException =
            new com.alibaba.csp.sentinel.slots.block.BlockException("unknown", "unknown type") {};

        // When
        invokeHandleBlockException(unknownException);

        // Then
        verify(response).setStatus(429);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");

        // 验证ApiResponse.error调用
        ArgumentCaptor<ApiResponse> captor = ArgumentCaptor.forClass(ApiResponse.class);
        verify(objectMapper).writeValueAsString(captor.capture());

        ApiResponse<?> apiResponse = captor.getValue();
        assertEquals(429, apiResponse.getCode());
        assertEquals("请求被限制，请稍后再试", apiResponse.getMessage());
        assertNull(apiResponse.getData());
    }

    @Test
    void testResponseWriterFlushIsCalled() throws Exception {
        // Given
        FlowException flowException = new FlowException("flow limit");

        // When
        invokeHandleBlockException(flowException);

        // Then - 验证response.getWriter()被调用了
        verify(response, times(2)).getWriter(); // 一次用于write，一次用于flush

        // 验证内容被写入StringWriter
        String writtenContent = stringWriter.toString();
        assertTrue(writtenContent.contains("test message"));
    }

    @Test
    void testObjectMapperSerializationIsCalled() throws Exception {
        // Given
        DegradeException degradeException = new DegradeException("degrade");

        // When
        invokeHandleBlockException(degradeException);

        // Then
        verify(objectMapper, times(1)).writeValueAsString(any(ApiResponse.class));
    }

    @Test
    void testGetCurrentRequestUriWithValidRequest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users/profile");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        FlowException flowException = new FlowException("flow limit");

        // When
        invokeHandleBlockException(flowException);

        // Then - 验证方法执行成功，URI信息会在日志中使用
        verify(response).setStatus(429);
    }

    @Test
    void testGetCurrentRequestUriWithNoRequest() throws Exception {
        // Given - 清除RequestContextHolder
        RequestContextHolder.resetRequestAttributes();

        FlowException flowException = new FlowException("flow limit");

        // When
        invokeHandleBlockException(flowException);

        // Then - 即使没有请求上下文，方法也应该正常执行
        verify(response).setStatus(429);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testAllExceptionTypesHaveDifferentStatusCodes() throws Exception {
        // 测试确保不同异常类型返回不同的状态码

        // FlowException -> 429
        resetResponseMock();
        invokeHandleBlockException(new FlowException("flow"));
        verify(response).setStatus(429);

        // DegradeException -> 503
        resetResponseMock();
        invokeHandleBlockException(new DegradeException("degrade"));
        verify(response).setStatus(503);

        // ParamFlowException -> 429
        resetResponseMock();
        invokeHandleBlockException(new ParamFlowException("param", "param flow"));
        verify(response).setStatus(429);

        // AuthorityException -> 403
        resetResponseMock();
        invokeHandleBlockException(new AuthorityException("authority"));
        verify(response).setStatus(403);
    }

    /**
     * 重置response mock并重新设置getWriter()
     */
    private void resetResponseMock() throws IOException {
        reset(response);
        PrintWriter printWriter = new PrintWriter(new StringWriter());
        when(response.getWriter()).thenReturn(printWriter);
    }

    /**
     * 使用反射调用私有的handleBlockException方法
     */
    private void invokeHandleBlockException(Exception exception) throws Exception {
        java.lang.reflect.Method method = SentinelFilter.class.getDeclaredMethod(
            "handleBlockException", HttpServletResponse.class, com.alibaba.csp.sentinel.slots.block.BlockException.class);
        method.setAccessible(true);
        method.invoke(sentinelFilter, response, exception);
    }
}
