package com.codeanalyzer.ai;

import com.codeanalyzer.util.AppConfig;
import com.codeanalyzer.util.HttpUtil;
import com.codeanalyzer.util.JsonUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GeminiService {

    private static final AppConfig CFG = AppConfig.getInstance();
    private static final int MAX_RETRIES = 2;

    public String generate(String prompt) throws Exception {
        String apiKey   = CFG.geminiApiKey();
        String model    = CFG.geminiModel();
        String baseUrl  = CFG.geminiApiUrl();

        if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
            throw new IllegalStateException(
                "Gemini API key chua duoc cau hinh. "
              + "Dat bien moi truong GEMINI_API_KEY hoac sua trong config.properties");
        }

        String url = baseUrl + model + ":generateContent?key=" + apiKey;

        JsonObject part = new JsonObject();
        part.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(part);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject requestBody = new JsonObject();
        requestBody.add("contents", contents);

        JsonObject genConfig = new JsonObject();
        genConfig.addProperty("temperature", 0.3);
        genConfig.addProperty("maxOutputTokens", 2048);
        requestBody.add("generationConfig", genConfig);

        Exception lastError = null;
        for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
            try {
                String responseJson = HttpUtil.post(url, requestBody.toString());
                return extractText(responseJson);
            } catch (Exception e) {
                lastError = e;
                String msg = e.getMessage() != null ? e.getMessage() : "";
                if (msg.contains("429") || msg.contains("503") || msg.contains("500")) {
                    long waitMs = (long) Math.pow(2, attempt) * 2000L;
                    System.out.println("[Gemini] Rate limit / server error, retry sau " + waitMs + "ms...");
                    Thread.sleep(waitMs);
                } else {
                    throw e;
                }
            }
        }
        throw lastError;
    }

    private String extractText(String responseJson) {
        try {
            JsonObject resp = JsonUtil.parseObject(responseJson);

            if (resp.has("error")) {
                JsonObject err = resp.getAsJsonObject("error");
                String message = err.has("message") ? err.get("message").getAsString() : responseJson;
                throw new RuntimeException("Gemini API error: " + message);
            }

            JsonArray candidates = resp.getAsJsonArray("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("No candidates in Gemini response: " + responseJson);
            }
            JsonObject candidate = candidates.get(0).getAsJsonObject();
            JsonObject respContent = candidate.getAsJsonObject("content");
            JsonArray respParts   = respContent.getAsJsonArray("parts");
            return respParts.get(0).getAsJsonObject().get("text").getAsString().trim();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + responseJson, e);
        }
    }
}
