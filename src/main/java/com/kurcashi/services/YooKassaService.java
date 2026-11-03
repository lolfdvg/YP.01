package com.kurcashi.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YooKassaService {

    private static final String SHOP_ID = "1515255";
    private static final String SECRET_KEY = "test_hsGxCDFkcmJJvnn2F2oEgrS_YOtIjCoPxvdUZ_Qmr1E";
    private static final String API_URL = "https://api.yookassa.ru/v3/payments";

    /**
     * Создает платеж и возвращает массив: [0] - ссылка на оплату, [1] - ID платежа в ЮKassa.
     */
    public String[] createPayment(String orderId, double amount) throws Exception {
        String auth = SHOP_ID.trim() + ":" + SECRET_KEY.trim();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        String jsonBody = String.format(Locale.US, """
            {
                "amount": { "value": "%.2f", "currency": "RUB" },
                "confirmation": { "type": "redirect", "return_url": "https://www.example.com/return" },
                "capture": true,
                "description": "Заказ №%s"
            }
            """, amount, orderId);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Basic " + encodedAuth)
                .header("Idempotence-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            String responseBody = response.body();

            String paymentId = null;
            Pattern idPattern = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
            Matcher idMatcher = idPattern.matcher(responseBody);
            if (idMatcher.find()) paymentId = idMatcher.group(1);

            String confirmationUrl = null;
            Pattern urlPattern = Pattern.compile("\"confirmation_url\"\\s*:\\s*\"([^\"]+)\"");
            Matcher urlMatcher = urlPattern.matcher(responseBody);
            if (urlMatcher.find()) confirmationUrl = urlMatcher.group(1);

            if (paymentId != null && confirmationUrl != null) {
                return new String[]{confirmationUrl, paymentId};
            }
        }

        throw new RuntimeException("Ошибка API ЮKassa: " + response.body());
    }

    /**
     * Проверяет статус платежа. Возвращает true, если статус succeeded.
     */
    public boolean checkPaymentStatus(String paymentId) throws Exception {
        String auth = SHOP_ID.trim() + ":" + SECRET_KEY.trim();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        String url = API_URL + "/" + paymentId;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Basic " + encodedAuth)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            Pattern pattern = Pattern.compile("\"status\"\\s*:\\s*\"([^\"]+)\"");
            Matcher matcher = pattern.matcher(response.body());
            if (matcher.find()) {
                String status = matcher.group(1);
                System.out.println("Статус платежа ЮKassa: " + status);
                return "succeeded".equals(status);
            }
        }
        return false;
    }
}