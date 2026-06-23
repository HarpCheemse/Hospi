package com.hospi.manage.features.payment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PayPalService {

    private final PayPalProperties payPalProperties;

    private String getBaseUrl() {
        return "sandbox".equalsIgnoreCase(payPalProperties.mode())
                ? "https://api-m.sandbox.paypal.com"
                : "https://api-m.paypal.com";
    }

    private String getAccessToken() throws IOException {
        URL url = new URL(getBaseUrl() + "/v1/oauth2/token");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String credentials = payPalProperties.clientId() + ":" + payPalProperties.clientSecret();
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        conn.setRequestProperty("Authorization",
                "Basic " + encoded);
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");

        try (OutputStream os = conn.getOutputStream()) {
            os.write("grant_type=client_credentials"
                    .getBytes(StandardCharsets.UTF_8));
        }

        String response = readResponse(conn);

        return extractJson(response,
                "access_token");
    }

    public String createOrder(String amount,
                              String returnUrl,
                              String cancelUrl) throws IOException {

        String accessToken = getAccessToken();

        URL url = new URL(getBaseUrl() + "/v2/checkout/orders");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Authorization",
                "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type",
                "application/json");

        String json = "{"
                + "\"intent\":\"CAPTURE\","
                + "\"purchase_units\":[{"
                + "\"amount\":{"
                + "\"currency_code\":\"USD\","
                + "\"value\":\"" + amount + "\""
                + "}"
                + "}],"
                + "\"application_context\":{"
                + "\"return_url\":\"" + returnUrl + "\","
                + "\"cancel_url\":\"" + cancelUrl + "\""
                + "}"
                + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        String response = readResponse(conn);

        System.out.println("[PayPal] Create Order Response:");
        System.out.println(response);

        return extractApprovalUrl(response);
    }

    public boolean captureOrder(String orderId) throws IOException {
        String accessToken = getAccessToken();

        URL url = new URL(
                getBaseUrl()
                        + "/v2/checkout/orders/"
                        + orderId
                        + "/capture"
        );

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        conn.setRequestProperty("Authorization",
                "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type",
                "application/json");
        conn.setRequestProperty("Content-Length",
                "0");

        conn.getOutputStream().close();

        String response = readResponse(conn);

        System.out.println("[PayPal] Capture Response:");
        System.out.println(response);

        return response.contains("\"status\":\"COMPLETED\"");
    }

    private String readResponse(HttpURLConnection conn)
            throws IOException {

        InputStream is = conn.getResponseCode() >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();

        return new String(
                is.readAllBytes(),
                StandardCharsets.UTF_8
        );
    }

    private String extractJson(String json,
                               String key) {

        String marker = "\"" + key + "\":\"";

        int start = json.indexOf(marker);
        if (start < 0) {
            throw new RuntimeException(
                    "Unable to find key: " + key
            );
        }

        start += marker.length();

        int end = json.indexOf("\"",
                start);

        return json.substring(start,
                end);
    }

    private String extractApprovalUrl(String response) {
        String[] parts = response.split("\\{");

        for (String part : parts) {
            if (part.contains("\"rel\":\"approve\"")
                    && part.contains("\"href\"")) {

                String marker = "\"href\":\"";

                int start = part.indexOf(marker);
                if (start < 0) {
                    continue;
                }

                start += marker.length();

                int end = part.indexOf("\"",
                        start);

                if (end > start) {
                    return part.substring(start,
                            end);
                }
            }
        }

        throw new RuntimeException(
                "Approval URL not found. Response: " + response
        );
    }
}