package com.example.weather;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.weather.data.WeatherData;
import com.example.weather.save.Config;
import com.example.weather.save.PreferencesUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Api mApi = Api.Instance.getApi();
    private Button buttonSave, buttonDelete;
    private List<LatLng> coordinates = new ArrayList<>();
    private Polygon polygon;
    private SearchView searchView;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (getSharedPreferences(Config.PREF, Context.MODE_PRIVATE) != null && coordinates != null) {
            coordinates = PreferencesUtils.getLocation(coordinates);
        }
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        buttonSave = findViewById(R.id.btn_save);
        buttonDelete = findViewById(R.id.btn_delete);
        searchView = findViewById(R.id.action_search);

    }

    @SuppressLint("CheckResult")
    private void performSearch(String search) {
        mApi.getWeatherDataByCity(search, "c773fb0ce7a8107622734d8f0fe977ba", "metric")
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
        LatLng latLng = new LatLng(weatherData.getCoord().getLat(), weatherData.getCoord().getLon());
        mMap.addMarker(new MarkerOptions().position(latLng).title(weatherData.getName() + " " + weatherData.getMain().getTemp() + "\u2103" + " Влажность " + weatherData.getMain().getHumidity() + "%")).showInfoWindow();
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        final CameraPosition cameraPosition = CameraPosition.builder().target(new LatLng(42.8667, 74.5667)).zoom(10).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        if (coordinates != null && getSharedPreferences(Config.PREF, Context.MODE_PRIVATE) != null) {
            addPolygonOptions();
        }

//        LatLng bishkek = new LatLng(42.8667, 74.5667);
//        mMap.addMarker(new MarkerOptions().position(bishkek).title("Marker in Bishkek"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(bishkek));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker()));
                coordinates.add(latLng);
            }
        });
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPolygonOptions();
                PreferencesUtils.saveLocation(coordinates);
            }
        });
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Данные удалены", Toast.LENGTH_SHORT).show();
//                PreferencesUtils.clear();
//                coordinates.clear();
            }
        });
    }

    public void addPolygonOptions() {
        if (coordinates != null) {
            PolygonOptions polygonOptions = new PolygonOptions();
            polygonOptions.strokeWidth(5f);
            polygonOptions.strokeColor(getResources().getColor(R.color.colorAccent));
            for (LatLng latLng : coordinates) {
                polygonOptions.add(latLng);
                mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker()));
            }
            if (polygonOptions != null) {
                mMap.addPolygon(polygonOptions);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem.OnActionExpandListener onActionExpandListener = new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem menuItem) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                return true;
            }
        };
        menu.findItem(R.id.action_search).setOnActionExpandListener(onActionExpandListener);
        final SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();
        search.setQueryHint("Поиск");
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        return true;
    }
}