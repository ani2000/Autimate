package org.ww.wigglew.service.geocoding;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IpGeolocation {
    public static Map<String, String> getGeolocationFromIP(String ipAddress) {
        String apiUrl = "http://ip-api.com/json/" + ipAddress;
        Map<String, String> geolocation = new HashMap<String, String>();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(apiUrl);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                JSONObject json = new JSONObject(result);

                if ("success".equals(json.getString("status"))) {
                    double latitude = json.getDouble("lat");
                    double longitude = json.getDouble("lon");

                    geolocation.put("latitude", String.valueOf(latitude));
                    geolocation.put("longitude", String.valueOf(longitude));
                } else {
                    geolocation.put("error", json.getString("message"));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            geolocation.put("exception", e.getMessage());
        }
        return geolocation;
    }

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371 * 1000; // Radius of the Earth in kilometers

        // Convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // Distance in kilometers
    }
}
