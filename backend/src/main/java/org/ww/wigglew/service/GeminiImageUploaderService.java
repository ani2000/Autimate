package org.ww.wigglew.service;

import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;


@Service
public class GeminiImageUploaderService {
//    @Value("${GEMINI_API_KEY}")
    private static String API_KEY = "AIzaSyBn8zt_nQqt_5R9YHLqHHkvN2DRbXf6NW8";
    private static String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;

    public String getImageFeedback(String imagePath){
        try {
            String base64Image = encodeImageToBase64(imagePath);
            return sendImageToGemini(base64Image);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    private static String encodeImageToBase64(String imagePath) throws IOException {
        File file = new File(imagePath);
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] bytes = new byte[(int) file.length()];
        fileInputStream.read(bytes);
        fileInputStream.close();
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static String sendImageToGemini(String base64Image) throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String promptText = Files.readString(Paths.get("src/main/resources/static/uploads/prompt_lib/drawing_therapy_guide_prompt.txt"));
        promptText = promptText.replaceAll("[\n\r]+", " ").replace("\"", "\\\"");

        String jsonRequest = String.format(
                "{ \"contents\": [ { \"parts\": [ { \"text\": \"%s\" }, { \"inline_data\": { \"mime_type\": \"image/jpeg\", \"data\": \"%s\" } } ] } ] }",
                promptText, base64Image
        );


        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = jsonRequest.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
        StringBuilder response = new StringBuilder();
        String responseLine;
        while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
        }
        // Parse JSON Response
        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray candidates = jsonResponse.getJSONArray("candidates");
        if (candidates.length() > 0) {
            JSONObject content = candidates.getJSONObject(0).getJSONObject("content");
            JSONArray parts = content.getJSONArray("parts");
            if (parts.length() > 0) {
                String modelResponse = parts.getJSONObject(0).getString("text");
                return modelResponse;
            }
        }

        return "No valid response from Gemini.";
    }
}
