package mobile.HW1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;

public class SecondActivity extends AppCompatActivity {

    static String TAG = "TAG";

    TextView cityField;
    TextView updatedField;
    TextView detailsField;
    TextView currentTemperatureField;
    TextView weatherIcon;


    private static final String DARK_SKY = "https://api.forecast.io/forecast/a6b8c7b90a261e493e22279291026462/%s";

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        cityField = findViewById(R.id.cityField);
        updatedField = findViewById(R.id.update);
        detailsField = findViewById(R.id.detailsField);
        currentTemperatureField = findViewById(R.id.temperature);
        weatherIcon = findViewById(R.id.weatherIcon);


        // getting pressed city ...
        Intent intent = getIntent();
        String cityName = intent.getStringExtra("CityName");


        // setting up views
        Typeface weatherFont = ResourcesCompat.getFont(this, R.font.weather);
        weatherIcon.setTypeface(weatherFont);
        weatherIcon.setText(getString(R.string.weather_clear_night));
        cityField.setText(cityName);

        // loading data ...

        String coordinates = chooseCity(cityName);

        // running networking in main thread requesting
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        final JSONObject json = getJSON(coordinates);
        renderWeather(json);

    }

    private String chooseCity(String cityName) {

        String coordinates;

        switch (cityName) {

            case ("New York"):

                coordinates = "42.3482,-75.1890";

                break;

            case ("London"):

                coordinates = "51.5072,-0.1275";

                break;

            case ("Los Angles"):

                coordinates = "34.0500,-118.2500";

                break;

            case ("Paris"):

                coordinates = "48.8567,2.3508";

                break;

            case ("Tokyo"):

                coordinates = "35.6833,139.6833";

                break;

            default:

                coordinates = "42.3482,-75.1890";

                break;

        }
        return coordinates;
    }

    public static JSONObject getJSON(String coordintate) {

        try {
            URL url = new URL(String.format((DARK_SKY), coordintate));

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuilder json = new StringBuilder(1024);
            String tmp;

            while ((tmp = reader.readLine()) != null) {
                Log.v(TAG, tmp);
                json.append(tmp).append("\n");
            }
            reader.close();

            return new JSONObject(json.toString());

        } catch (
                Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    private void renderWeather(JSONObject json) {
        try {

            Calendar calendar = Calendar.getInstance();
            int today = calendar.get(Calendar.DAY_OF_WEEK);

            String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
            JSONArray data_array = json.getJSONObject("daily").getJSONArray("data");

            for (int i = 0; i < 7; i++) {

                JSONObject item = data_array.getJSONObject(i);

                String temperatureMax = item.getString("temperatureMax");
                String temperatureMin = item.getString("temperatureMin");
                String w_summary = item.getString("summary");
                temperatureMax = temperatureMax.substring(0, 2);
                temperatureMin = temperatureMin.substring(0, 2);

                detailsField.setText(detailsField.getText() + days[(today + i) % 7] + ": " + temperatureMin + " - " + temperatureMax + " " + w_summary + "\n");
            }


            if (json.getString("timezone").contains("York"))
                cityField.setText("New York");
            if (json.getString("timezone").contains("London"))
                cityField.setText("London");
            if (json.getString("timezone").contains("Los"))
                cityField.setText("Los Angles");
            if (json.getString("timezone").contains("Paris"))
                cityField.setText("Paris");
            if (json.getString("timezone").contains("Tokyo"))
                cityField.setText("Tokyo");


            currentTemperatureField.setText(json.getJSONObject("currently").getString("temperature") + " \u00b0 F");
            updatedField.setText(

                    // "SUMMARY OF WEEK  : " +
                    json.getJSONObject("daily").getString("summary")
                    // +      "\nTIME ZONE  : " + json.getString("timezone")
            );

        } catch (Exception e) {
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }

}