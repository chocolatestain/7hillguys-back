package com.shinhan.peoch.lifecycleincome.service;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class NgrokApiRequest {

    private static final String API_URL = "https://0cdf-58-122-202-23.ngrok-free.app/api/generate";

    public String sendRequestToApi(String model, String prompt) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("model", model);
        payload.put("prompt", prompt);

        return postRequestAndExtractResponse(API_URL, payload);
    }

    private String postRequestAndExtractResponse(String apiUrl, JSONObject payload) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            // 요청 본문 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return "Request failed with HTTP code: " + responseCode;
            }

            // 응답 읽기
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            // 응답 데이터 처리
            return processRawResponse(response.toString());

        } catch (Exception e) {
            return "Request failed: " + e.getMessage();
        }
    }

    private String processRawResponse(String rawResponse) {
        StringBuilder finalResponse = new StringBuilder();

        try {
            // 응답 데이터를 줄 단위로 분리
            String[] lines = rawResponse.split("(?<=\\})(?=\\{)"); // `}{` 기준으로 분리

            for (String line : lines) {
                // 각 줄을 JSON 객체로 파싱
                JSONObject obj = new JSONObject(line.trim());
                if (obj.has("response")) {
                    finalResponse.append(obj.getString("response"));
                }
            }
        } catch (Exception e) {
            return "Error processing response: " + e.getMessage();
        }

        return finalResponse.toString().trim();
    }
}
