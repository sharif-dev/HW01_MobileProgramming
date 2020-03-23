package mobile.HW1.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;


import java.util.ArrayList;
import java.util.List;

import mobile.HW1.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    TextView textView;
    Button button;
    RecyclerView recyclerView;

    private static String MAPBOX_ACCESS_TOKEN = "pk.eyJ1IjoiYWhyZWNjZXNlIiwiYSI6ImNrN28yZjE1ZzA0bnIzZG0zb2kxNzlrcHkifQ.J5-vlsAJMMenyqXyRoq32A";
    private static String TAG = "TAG";
    private static final int START_SEARCHING = 1;
    private static final int SEARCH_DONE = 0;
    private static ArrayList<String> results = new ArrayList<>();
    private static MyAdapter mAdapter = new MyAdapter(results);

    private static Handler mHandlerThread;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.first_page);


        textView = findViewById(R.id.input_string);
        button = findViewById(R.id.search_button);
        recyclerView = findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);


        // specify an adapter (see also next example)

        recyclerView.setAdapter(mAdapter);

        // handler part
        mHandlerThread = new geocodeSearcherHandler(results);

    }

    static class geocodeSearcherHandler extends Handler {
        private final ArrayList<String> results;

        geocodeSearcherHandler(ArrayList<String> results) {
            this.results = results;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == START_SEARCHING) {
                // start Searching
                gecode(msg.obj.toString());
            } else {
                if (results.size() != 0) {
                    mAdapter.reset();
                }
                results.addAll((ArrayList<String>) msg.obj);
                Log.v(TAG, "workDone");
            }
        }

    }


    public void SearchClick(View view) {

        String location = textView.getText().toString();

        Message message = new Message();
        message.what = START_SEARCHING;
        message.obj = location;
        mHandlerThread.sendMessage(message);

    }


    public static void gecode(String name) {

        ArrayList<String> cityResults = new ArrayList<>();

        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(MAPBOX_ACCESS_TOKEN)
                .query(name)
                .build();

        mapboxGeocoding.enqueueCall(new Callback<GeocodingResponse>() {
            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                List<CarmenFeature> results = response.body().features();

                if (results.size() > 0) {

                    for (int i = 0; i < results.size(); i++) {

                        cityResults.add(results.get(i).placeName());
                        // Log the first results Point.
                        String firstResultPoint = results.get(i).placeName();
                        Log.v(TAG, "onResponse: " + firstResultPoint);

                    }

                    Message message = new Message();
                    message.what = SEARCH_DONE;
                    message.obj = cityResults;
                    mHandlerThread.sendMessage(message);

                } else {

                    // No result for your request were found.
                    Log.v(TAG, "onResponse: No result found");

                }

            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                throwable.printStackTrace();
            }
        });

    }


}