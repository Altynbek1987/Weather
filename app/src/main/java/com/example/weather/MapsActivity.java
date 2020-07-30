package com.example.weather;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.example.weather.data.WeatherData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{

    private GoogleMap mMap;
    private Api mApi = Api.Instance.getApi();
    private EditText editText;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        editText = findViewById(R.id.editText);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = editText.getText().toString().trim();
                if (TextUtils.isEmpty(search)){
                    editText.setError("Введите город");
                    return;
                }
                else {
                    performSearch(search);
                }
            }

        });
    }
    @SuppressLint("CheckResult")
    private void performSearch(String search) {
        mApi.getWeatherDataByCity(search,"c773fb0ce7a8107622734d8f0fe977ba","metric")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherData>() {
                    @Override
                    public void accept(WeatherData weatherData) throws Exception {
                        showWeatherMarker(weatherData);
                        Toast.makeText(MapsActivity.this, weatherData.getName() + " " + weatherData.getMain().getTemp(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showWeatherMarker(WeatherData weatherData) {
        LatLng latLng = new LatLng(weatherData.getCoord().getLat(),weatherData.getCoord().getLon());
        mMap.addMarker(new MarkerOptions().position(latLng).title(weatherData.getName() + " " + weatherData.getMain().getTemp())).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//        LatLng bishkek = new LatLng(42.8667, 74.5667);
//        mMap.addMarker(new MarkerOptions().position(bishkek).title("Marker in Bishkek"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(bishkek));
    }
}
