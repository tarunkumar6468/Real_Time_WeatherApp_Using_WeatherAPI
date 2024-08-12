package com.example.weatherapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {
    TextView cityName;
    Button Search;
    TextView show;
    TextView rainPrediction;
    String url;

    class getWeather extends AsyncTask<String, Void, String> {
        @Override
        public String doInBackground(String... urls) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.connect();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    result.append(line).append("\n");
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject mainObject = jsonObject.getJSONObject("main");

                // Convert temperatures from Kelvin to Fahrenheit and Celsius
                double tempKelvin = mainObject.getDouble("temp");
                double feelsLikeKelvin = mainObject.getDouble("feels_like");
                double tempMinKelvin = mainObject.getDouble("temp_min");
                double tempMaxKelvin = mainObject.getDouble("temp_max");

                String tempFahrenheit = formatTemperature(kelvinToFahrenheit(tempKelvin));
                String feelsLikeFahrenheit = formatTemperature(kelvinToFahrenheit(feelsLikeKelvin));
                String tempMinFahrenheit = formatTemperature(kelvinToFahrenheit(tempMinKelvin));
                String tempMaxFahrenheit = formatTemperature(kelvinToFahrenheit(tempMaxKelvin));

                String tempCelsius = formatTemperature(kelvinToCelsius(tempKelvin));
                String feelsLikeCelsius = formatTemperature(kelvinToCelsius(feelsLikeKelvin));
                String tempMinCelsius = formatTemperature(kelvinToCelsius(tempMinKelvin));
                String tempMaxCelsius = formatTemperature(kelvinToCelsius(tempMaxKelvin));

                // Check for rain
                String rainStatus = "No rain expected";
                int rainProbability = 0; // Default to 0% if no rain data is found

                if (jsonObject.has("rain")) {
                    JSONObject rainObject = jsonObject.getJSONObject("rain");
                    if (rainObject.has("1h")) {
                        double rainVolume = rainObject.getDouble("1h");
                        if (rainVolume > 0) {
                            // Approximate rain probability based on rain volume
                            rainProbability = (int) Math.min(rainVolume * 10, 100); // Just an example calculation
                            rainStatus = "Rain is expected: " + rainProbability + "% chance";
                        }
                    }
                }

                String weatherInfo = "Temperature: " + tempFahrenheit + "°F / " + tempCelsius + "°C\n" +
                        "Feels Like: " + feelsLikeFahrenheit + "°F / " + feelsLikeCelsius + "°C\n" +
                        "Temperature Min: " + tempMinFahrenheit + "°F / " + tempMinCelsius + "°C\n" +
                        "Temperature Max: " + tempMaxFahrenheit + "°F / " + tempMaxCelsius + "°C\n" +
                        "Pressure: " + mainObject.getString("pressure") + " hPa\n" +
                        "Humidity: " + mainObject.getString("humidity") + "%";

                show.setText(weatherInfo);
                rainPrediction.setText(rainStatus);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private double kelvinToFahrenheit(double kelvin) {
            return (kelvin - 273.15) * 9 / 5 + 32;
        }

        private double kelvinToCelsius(double kelvin) {
            return kelvin - 273.15;
        }

        private String formatTemperature(double temp) {
            return String.format("%.0f", temp);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Remove the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        cityName = findViewById(R.id.cityName);
        Search = findViewById(R.id.search);
        show = findViewById(R.id.weather);
        rainPrediction = findViewById(R.id.rainPrediction);

        cityName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    Search.performClick();
                    return true;
                }
                return false;
            }
        });

        Search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Button Clicked", Toast.LENGTH_SHORT).show();
                String city = cityName.getText().toString();
                if (!city.isEmpty()) {
                    url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=a681c4bf9dda825a594357b82253445f";
                    getWeather task = new getWeather();
                    task.execute(url);
                } else {
                    Toast.makeText(MainActivity.this, "Enter City", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
