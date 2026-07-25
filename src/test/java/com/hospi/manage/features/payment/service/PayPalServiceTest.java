package com.hospi.manage.features.payment.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PayPalServiceTest {

    @Mock
    private PayPalProperties payPalProperties;

    @InjectMocks
    private PayPalService payPalService;

    @Test
    void shouldReturnSandboxUrl_whenModeIsSandbox() throws Exception {
        when(payPalProperties.mode()).thenReturn("sandbox");

        String baseUrl = invokeGetBaseUrl();

        assertEquals("https://api-m.sandbox.paypal.com", baseUrl);
    }

    @Test
    void shouldReturnLiveUrl_whenModeIsLive() throws Exception {
        when(payPalProperties.mode()).thenReturn("live");

        String baseUrl = invokeGetBaseUrl();

        assertEquals("https://api-m.paypal.com", baseUrl);
    }

    @Test
    void shouldReturnSandboxUrl_whenModeIsSandboxCaseInsensitive() throws Exception {
        when(payPalProperties.mode()).thenReturn("SANDBOX");

        String baseUrl = invokeGetBaseUrl();

        assertEquals("https://api-m.sandbox.paypal.com", baseUrl);
    }

    @Test
    void shouldReturnLiveUrl_whenModeIsUnknown() throws Exception {
        when(payPalProperties.mode()).thenReturn("production");

        String baseUrl = invokeGetBaseUrl();

        assertEquals("https://api-m.paypal.com", baseUrl);
    }

    @Test
    void shouldExtractJson_whenKeyExists() throws Exception {
        String payload = "{\"access_token\":\"A21.test_token_value.xyz\"}";

        String result = invokeExtractJson(payload, "access_token");

        assertEquals("A21.test_token_value.xyz", result);
    }

    @Test
    void shouldThrow_whenJsonKeyNotFound() {
        String payload = "{\"other_key\":\"value\"}";

        RuntimeException ex = assertThrowsRuntimeException(
                () -> invokeExtractJson(payload, "access_token"));

        assertTrue(ex.getMessage().contains("Unable to find key"));
    }

    @Test
    void shouldExtractApprovalUrl_whenPresent() throws Exception {
        String response = "{\"links\":[{\"href\":\"https://paypal.com/checkout/approve\",\"rel\":\"approve\"}]}";

        String url = invokeExtractApprovalUrl(response);

        assertEquals("https://paypal.com/checkout/approve", url);
    }

    @Test
    void shouldThrow_whenApprovalUrlNotFound() {
        String response = "{\"links\":[{\"href\":\"https://other.com\",\"rel\":\"self\"}]}";

        RuntimeException ex = assertThrowsRuntimeException(
                () -> invokeExtractApprovalUrl(response));

        assertTrue(ex.getMessage().contains("Approval URL not found"));
    }

    @Test
    void shouldExtractCaptureId_whenPresent() throws Exception {
        String response = "{\"purchase_units\":[{\"payments\":{\"captures\":[{\"id\":\"CAPTURE_123\"}]}}]}";

        String captureId = invokeExtractCaptureId(response);

        assertEquals("CAPTURE_123", captureId);
    }

    @Test
    void shouldThrow_whenCaptureIdNotFound() {
        String response = "{\"purchase_units\":[{\"payments\":{}}]}";

        RuntimeException ex = assertThrowsRuntimeException(
                () -> invokeExtractCaptureId(response));

        assertTrue(ex.getMessage().contains("Capture ID not found"));
    }

    // ========== Public method tests (via spy) ==========

    @Test
    void createOrder_shouldReturnApprovalUrl_whenPayPalApproves() throws Exception {
        when(payPalProperties.mode()).thenReturn("sandbox");
        when(payPalProperties.clientId()).thenReturn("test-client-id");
        when(payPalProperties.clientSecret()).thenReturn("test-secret");

        var spy = spy(new PayPalService(payPalProperties));
        String tokenResponse = "{\"access_token\":\"A21.fake_token\"}";
        String orderResponse = "{\"id\":\"ORDER123\",\"links\":[{\"href\":\"https://paypal.com/checkout/now\",\"rel\":\"approve\"}]}";
        doReturn(tokenResponse).doReturn(orderResponse).when(spy).readResponse(any());

        String result = spy.createOrder("50.00", "http://return", "http://cancel");

        assertEquals("https://paypal.com/checkout/now", result);
    }

    @Test
    void captureOrder_shouldReturnTrue_whenStatusCompleted() throws Exception {
        when(payPalProperties.mode()).thenReturn("sandbox");
        when(payPalProperties.clientId()).thenReturn("test-client-id");
        when(payPalProperties.clientSecret()).thenReturn("test-secret");

        var spy = spy(new PayPalService(payPalProperties));
        String tokenResponse = "{\"access_token\":\"A21.fake_token\"}";
        String captureResponse = "{\"status\":\"COMPLETED\"}";
        doReturn(tokenResponse).doReturn(captureResponse).when(spy).readResponse(any());

        boolean result = spy.captureOrder("ORDER123");

        assertTrue(result);
    }

    @Test
    void captureOrder_shouldReturnFalse_whenStatusNotCompleted() throws Exception {
        when(payPalProperties.mode()).thenReturn("sandbox");
        when(payPalProperties.clientId()).thenReturn("test-client-id");
        when(payPalProperties.clientSecret()).thenReturn("test-secret");

        var spy = spy(new PayPalService(payPalProperties));
        String tokenResponse = "{\"access_token\":\"A21.fake_token\"}";
        String captureResponse = "{\"status\":\"DECLINED\"}";
        doReturn(tokenResponse).doReturn(captureResponse).when(spy).readResponse(any());

        boolean result = spy.captureOrder("ORDER123");

        assertFalse(result);
    }

    @Test
    void refundOrder_shouldReturnTrue_whenRefundCompleted() throws Exception {
        when(payPalProperties.mode()).thenReturn("sandbox");
        when(payPalProperties.clientId()).thenReturn("test-client-id");
        when(payPalProperties.clientSecret()).thenReturn("test-secret");

        var spy = spy(new PayPalService(payPalProperties));
        String tokenResponse = "{\"access_token\":\"A21.fake_token\"}";
        String orderResponse = "{\"purchase_units\":[{\"payments\":{\"captures\":[{\"id\":\"CAPTURE_123\"}]}}]}";
        String refundResponse = "{\"status\":\"COMPLETED\"}";
        doReturn(tokenResponse).doReturn(orderResponse).doReturn(refundResponse).when(spy).readResponse(any());

        boolean result = spy.refundOrder("ORDER123");

        assertTrue(result);
    }

    @Test
    void refundOrder_shouldReturnFalse_whenRefundFails() throws Exception {
        when(payPalProperties.mode()).thenReturn("sandbox");
        when(payPalProperties.clientId()).thenReturn("test-client-id");
        when(payPalProperties.clientSecret()).thenReturn("test-secret");

        var spy = spy(new PayPalService(payPalProperties));
        String tokenResponse = "{\"access_token\":\"A21.fake_token\"}";
        String orderResponse = "{\"purchase_units\":[{\"payments\":{\"captures\":[{\"id\":\"CAPTURE_123\"}]}}]}";
        String refundResponse = "{\"status\":\"FAILED\"}";
        doReturn(tokenResponse).doReturn(orderResponse).doReturn(refundResponse).when(spy).readResponse(any());

        boolean result = spy.refundOrder("ORDER123");

        assertFalse(result);
    }

    @Test
    void refundOrder_shouldThrowException_whenCaptureIdNotFound() throws Exception {
        when(payPalProperties.mode()).thenReturn("sandbox");
        when(payPalProperties.clientId()).thenReturn("test-client-id");
        when(payPalProperties.clientSecret()).thenReturn("test-secret");

        var spy = spy(new PayPalService(payPalProperties));
        String tokenResponse = "{\"access_token\":\"A21.fake_token\"}";
        String orderResponse = "{\"purchase_units\":[{\"payments\":{}}]}";
        doReturn(tokenResponse).doReturn(orderResponse).when(spy).readResponse(any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> spy.refundOrder("ORDER123"));

        assertTrue(ex.getMessage().contains("Capture ID not found"));
    }

    // --- Reflection helpers ---

    /**
     * Invoke a reflective call and unwrap InvocationTargetException to get the
     * underlying RuntimeException thrown by the private method.
     */
    private RuntimeException assertThrowsRuntimeException(ReflectiveRunnable runnable) {
        try {
            runnable.run();
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) {
                return re;
            }
            throw new AssertionError("Expected RuntimeException wrapped in InvocationTargetException, got: " + cause, e);
        } catch (Exception e) {
            throw new AssertionError("Expected InvocationTargetException wrapping RuntimeException, got: " + e, e);
        }
        throw new AssertionError("Expected exception but none was thrown");
    }

    @FunctionalInterface
    private interface ReflectiveRunnable {
        void run() throws Exception;
    }

    private String invokeGetBaseUrl() throws Exception {
        Method method = PayPalService.class.getDeclaredMethod("getBaseUrl");
        method.setAccessible(true);
        return (String) method.invoke(payPalService);
    }

    private String invokeExtractJson(String json, String key) throws Exception {
        Method method = PayPalService.class.getDeclaredMethod("extractJson", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(payPalService, json, key);
    }

    private String invokeExtractApprovalUrl(String response) throws Exception {
        Method method = PayPalService.class.getDeclaredMethod("extractApprovalUrl", String.class);
        method.setAccessible(true);
        return (String) method.invoke(payPalService, response);
    }

    private String invokeExtractCaptureId(String orderResponse) throws Exception {
        Method method = PayPalService.class.getDeclaredMethod("extractCaptureId", String.class);
        method.setAccessible(true);
        return (String) method.invoke(payPalService, orderResponse);
    }
}
