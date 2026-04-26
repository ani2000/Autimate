package org.ww.wigglew.service.geocoding;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class ForwardGeoCoding {

    private static final OkHttpClient client = new OkHttpClient();  // Use a single OkHttpClient instance
    private static final String API_KEY = "pk.5a0d8206eb2001ba4d8e413f34e15308";

    public static Map<String, String> getLocationData(String locationString) throws IOException {
        String encodedLocation = locationString.replaceAll(" ", "%20").replaceAll(",", "%2C");

        try {
            String url = "https://us1.locationiq.com/v1/search?q=" + encodedLocation + "&format=json&key=" + API_KEY;
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("accept", "application/json")
                    .build();

            Response response = client.newCall(request).execute();

            if (response.code() == 200) {
                String responseBody = response.body().string();
                System.out.println("responseBody: " + responseBody);

                JSONArray jsonArray = new JSONArray(responseBody);

                if (jsonArray.length() > 0) {
                    JSONObject firstPlace = jsonArray.getJSONObject(0);
                    String displayName = firstPlace.getString("display_name");
                    String latitude = firstPlace.getString("lat");
                    String longitude = firstPlace.getString("lon");

                    Map<String, String> result = new HashMap<>();
                    result.put("latitude", latitude);
                    result.put("longitude", longitude);
                    result.put("displayName", displayName);
                    result.put("error", "");
                    return result;
                } else {
                    return Map.of("error", "No location found");
                }
            } else {
                return Map.of("error", "Request failed with status: " + response.code());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", e.getMessage());
        }
    }

    // public static void main(String[] args) {
    //     try {
    //         Map<String, String> locationData = getLocationData("15 Moylapota Main Road, 2nd Floor, Khulna 9100, Bangladesh");
    //
    //         if (!locationData.get("error").isEmpty()) {
    //             System.out.println(locationData.get("error"));
    //         } else {
    //             System.out.println("Latitude: " + locationData.get("latitude"));
    //             System.out.println("Longitude: " + locationData.get("longitude"));
    //             System.out.println("Display Name: " + locationData.get("displayName"));
    //         }
    //     } catch (IOException e) {
    //         e.printStackTrace();
    //     }
    // }
}
