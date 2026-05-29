package utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

public final class PayPalUtils {

    private static final String BASE_URL;
    private static final String CLIENT_ID;
    private static final String CLIENT_SECRET;

    static {
        try {
            Properties props = new Properties();
            props.load(PayPalUtils.class
                    .getClassLoader()
                    .getResourceAsStream("config/config.properties"));

            String mode = props.getProperty("paypal.mode", "sandbox");
            CLIENT_ID = props.getProperty("paypal.client.id");
            CLIENT_SECRET = props.getProperty("paypal.client.secret");
            BASE_URL = "sandbox".equals(mode)
                    ? "https://api-m.sandbox.paypal.com"
                    : "https://api-m.paypal.com";

        } catch (IOException e) {
            throw new RuntimeException("Failed to load PayPal config", e);
        }
    }

    private PayPalUtils() {
    }

    private static String getAccessToken() throws IOException {
        URL url = new URL(BASE_URL + "/v1/oauth2/token");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        conn.setRequestProperty("Authorization", "Basic " + encoded);
        conn.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded");

        byte[] body = "grant_type=client_credentials"
                .getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(body);
        }

        String response = readResponse(conn);
        return extractJson(response, "access_token");
    }

    public static String createOrder(String amount, String returnUrl,
            String cancelUrl) throws IOException {

        String token = getAccessToken();

        URL url = new URL(BASE_URL + "/v2/checkout/orders");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");

        String json = "{"
                + "\"intent\":\"CAPTURE\","
                + "\"purchase_units\":[{"
                + "  \"amount\":{"
                + "    \"currency_code\":\"USD\","
                + "    \"value\":\"" + amount + "\""
                + "  }"
                + "}],"
                + "\"application_context\":{"
                + "  \"return_url\":\"" + returnUrl + "\","
                + "  \"cancel_url\":\"" + cancelUrl + "\""
                + "}"
                + "}";

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        String response = readResponse(conn);
        System.out.println("[PayPal] Create order response: " + response);

        // extract approval URL from links array
        String marker = "\"rel\":\"approve\",\"href\":\"";
        // paypal returns href before rel in some responses — handle both
        return extractApprovalUrl(response);
    }

    public static boolean captureOrder(String orderId) throws IOException {
        String token = getAccessToken();

        URL url = new URL(BASE_URL + "/v2/checkout/orders/"
                + orderId + "/capture");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Content-Length", "0");
        conn.getOutputStream().close();

        String response = readResponse(conn);
        System.out.println("[PayPal] Capture response: " + response);

        return response.contains("\"status\":\"COMPLETED\"");
    }

    private static String readResponse(HttpURLConnection conn)
            throws IOException {
        InputStream is = conn.getResponseCode() >= 400
                ? conn.getErrorStream()
                : conn.getInputStream();
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static String extractJson(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search) + search.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private static String extractApprovalUrl(String response) {
        // find href that contains "approve" context
        String[] parts = response.split("\\{");
        for (String part : parts) {
            if (part.contains("approve") && part.contains("href")) {
                String hrefMarker = "\"href\":\"";
                int start = part.indexOf(hrefMarker) + hrefMarker.length();
                int end = part.indexOf("\"", start);
                if (start > hrefMarker.length() - 1 && end > start) {
                    return part.substring(start, end);
                }
            }
        }
        throw new RuntimeException("Approval URL not found in response: "
                + response);
    }
}
