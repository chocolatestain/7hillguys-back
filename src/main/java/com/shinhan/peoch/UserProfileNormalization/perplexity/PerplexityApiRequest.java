package com.shinhan.peoch.UserProfileNormalization.perplexity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Service
public class PerplexityApiRequest {

    private static final String API_URL = "https://api.perplexity.ai/chat/completions";

    @Value("${perplexity.api.key}")
    private String apiKey;

    /**
     * 모델명과 프롬프트를 받아 API 요청을 보내는 메서드
     */
    public String sendRequestToApi(String model, String prompt) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("model", model);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        messages.put(message);

        payload.put("messages", messages);

        return postRequestAndExtractResponse(API_URL, payload);
    }

    /**
     * 직접 JSONObject 페이로드를 받아 API 요청을 보내는 메서드
     */
    public String sendRequestToApi(JSONObject payload) throws JSONException {
        return postRequestAndExtractResponse(API_URL, payload);
    }

    private String postRequestAndExtractResponse(String apiUrl, JSONObject payload) {
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);

            // 요청 본문 전송
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 응답 코드 확인
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.err.println("API 요청 실패. 응답 코드: " + responseCode);

                // 에러 응답 읽기
                StringBuilder errorResponse = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        errorResponse.append(responseLine.trim());
                    }
                }
                System.err.println("에러 응답: " + errorResponse);

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
            String processedResponse = processPerplexityResponse(response.toString());
            System.out.println("API 응답: " + processedResponse);
            return processedResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "Request failed: " + e.getMessage();
        }
    }

    private String processPerplexityResponse(String rawResponse) {
        try {
            JSONObject responseObj = new JSONObject(rawResponse);

            // JSON 응답 형식이 있는 경우 content 필드에서 직접 JSON 데이터를 추출
            JSONObject choice = responseObj.getJSONArray("choices").getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            String content = message.getString("content");

            // content가 이미 JSON 형식인지 확인
            if (content.trim().startsWith("{") && content.trim().endsWith("}")) {
                try {
                    // JSON 파싱 시도
                    return content;
                } catch (Exception e) {
                    // JSON 파싱 실패 시 원본 content 반환
                    return content;
                }
            }

            return content;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error processing response: " + e.getMessage();
        }
    }
}
