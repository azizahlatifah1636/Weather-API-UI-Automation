package api;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class WeatherApiClient {

    private final String apiKey;

    public WeatherApiClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public Response getWeatherByCity(String city) {
        return RestAssured
                .given()
                .queryParam("q", city)
                .queryParam("appid", apiKey)
                .queryParam("units", "metric")
                .get("https://api.openweathermap.org/data/2.5/weather");
    }
}
