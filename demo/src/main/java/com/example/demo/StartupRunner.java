package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.example.demo.WebhookResponse;

@Component
public class StartupRunner implements CommandLineRunner {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(String... args) throws Exception {

        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        String json = """
        {
          "name": "John Doe",
          "regNo": "REG12347",
          "email": "john@example.com"
        }
        """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(json, headers);

        // 🔥 Step 1: Call API
        ResponseEntity<WebhookResponse> response =
                restTemplate.postForEntity(url, entity, WebhookResponse.class);

        // 🔥 Step 2: Null safety check
        if (response.getBody() == null) {
            System.out.println("❌ Failed to get response");
            return;
        }

        String webhookUrl = response.getBody().getWebhook();
        String token = response.getBody().getAccessToken();

        System.out.println("Webhook: " + webhookUrl);
        System.out.println("Token: " + token);

        // 🔥 Step 3: Final SQL Query
        String finalQuery = "SELECT p.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, d.DEPARTMENT_NAME FROM PAYMENTS p JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID WHERE DAY(p.PAYMENT_TIME) != 1 AND p.AMOUNT = (SELECT MAX(AMOUNT) FROM PAYMENTS WHERE DAY(p.PAYMENT_TIME) != 1);";

        // 🔥 Step 4: Send answer
        sendFinalQuery(webhookUrl, token, finalQuery);
    }

    // ✅ Method OUTSIDE run()
    private void sendFinalQuery(String webhookUrl, String token, String finalQuery) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", token);

        String body = "{ \"finalQuery\": \"" + finalQuery + "\" }";

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response =
                    restTemplate.postForEntity(webhookUrl, entity, String.class);

            System.out.println("✅ Submitted Successfully");
            System.out.println("Response: " + response.getBody());

        } catch (Exception e) {
            System.out.println("❌ Error submitting query");
            e.printStackTrace();
        }
    }
}