package com.mobileapp.a4cast.ui.home;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.AddressComponents;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.mobileapp.a4cast.DatabaseItem;
import com.mobileapp.a4cast.GlobalData;
import com.mobileapp.a4cast.MainActivity;
import com.mobileapp.a4cast.R;
import com.mobileapp.a4cast.SQLiteManager;
import com.mobileapp.a4cast.databinding.FragmentHomeBinding;
import com.mobileapp.a4cast.ui.home.HomeViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class HomeFragment extends Fragment {
    //---BINDING---
    private FragmentHomeBinding binding;
    //---API WEATHER INFO---
    private static final String API_KEY = "dec0f72ce23604612032a38b00466f12";
    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String ONE_CALL_API_URL = "https://api.openweathermap.org/data/2.5";
    private static final String UNITS = "imperial";
    private static final String EXCLUDE = "minutely,daily,alerts";
    //---DATABASE---
    private SQLiteManager dbManager;
    //---LOCATION---
    private static final int REQUEST_LOCATION_PERMISSION = 1;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    public LocationManager locationManager;
    public LocationListener locationListener;
    //---WEATHER DISPLAY VARS---
    double temperature = 0, feelsLike = 0, celsius = 0;
    int humidity;
    DecimalFormat df = new DecimalFormat("#");
    String mainDescription = "", description = "", cityName = "", latitude, longitude, tempString = "";
    List<DatabaseItem> conditions, temps;
    List<DatabaseItem> tempRecommendations, activityReco, foodReco, clothingReco;
    //---NAV VAR---
    BottomNavigationView bottomNavigationView;
    //---MANUAL VARS---
    AutocompleteSupportFragment autocompleteFragment;
    SwitchCompat manualCitySwitch;
    TextView selectedCityText, manualCityText;
    Boolean manual = false;
    CardView cardView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        bottomNavigationView = getActivity().findViewById(R.id.nav_view);
        //Boolean manual = GlobalData.getInstance().getManualCity();

        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        selectedCityText = binding.currentSelectedCity;
        manualCitySwitch = binding.CitySwitch;
        manualCityText = binding.manualCityText;

        autocompleteFragment.getView().setVisibility(View.GONE);
        selectedCityText.setVisibility(View.GONE);
        manualCitySwitch.setVisibility(View.GONE);
        manualCityText.setVisibility(View.GONE);

        cardView = binding.cardView;
        //cardView.setRadius(100);
        //cardView.setBackgroundColor(66000000);
        //cardView.setCardBackgroundColor(66000000);
        //cardView.setback
        //cardView.setRadius(2);
        manualCitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    autocompleteFragment.getView().setVisibility(View.VISIBLE);
                    selectedCityText.setVisibility(View.VISIBLE);
                    cityName = null;
                    GlobalData.getInstance().setManualSwitch(true);
                    selectedCityText.setText("Current Selected City: ");
                    Log.d("DEBUG", "HOME FRAGMENT: Manual Location On");
                    manual = true;
                } else {
                    autocompleteFragment.getView().setVisibility(View.GONE);
                    selectedCityText.setVisibility(View.GONE);
                    Log.d("DEBUG", "SETTINGS FRAGMENT: Manual Off");
                    manual = false;
                    GlobalData.getInstance().setManualSwitch(false);
                    locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                    String regularURL = BASE_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;
                    String hourlyURL = ONE_CALL_API_URL + "/onecall?lat=" + latitude + "&lon=" + longitude + "&exclude=" + EXCLUDE + "&units=" + UNITS + "&appid=" + API_KEY;
                    //ADDED
                    getWeatherData(regularURL);
                    getHourlyForecastData(hourlyURL);

                }
            }
        });
        if (autocompleteFragment != null) {
            autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS));
            autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
                @Override
                public void onPlaceSelected(@NonNull Place place) {
                    //Log.d("DEBUG", "SETTINGS FRAGMENT: PLACE: " + place.getLatLng().latitude);
                    AddressComponents addressComponents = place.getAddressComponents();

                    String regularURL = BASE_URL + "?lat=" + place.getLatLng().latitude + "&lon=" + place.getLatLng().longitude + "&appid=" + API_KEY;
                    String hourlyURL = ONE_CALL_API_URL + "/onecall?lat=" + place.getLatLng().latitude + "&lon=" + place.getLatLng().longitude + "&exclude=" + EXCLUDE + "&units=" + UNITS + "&appid=" + API_KEY;
                    getWeatherData(regularURL);
                    getHourlyForecastData(hourlyURL);
                    List<String> urls = new ArrayList<String>();
                    urls.add(regularURL);
                    urls.add(hourlyURL);
                    GlobalData.getInstance().setManualURLs(urls);

                    cityName = null;
                    if (addressComponents != null) {
                        for (AddressComponent component : addressComponents.asList()) {
                            List<String> types = component.getTypes();
                            if (types.contains("locality")) {
                                cityName = component.getName();
                                selectedCityText.setText("Current Selected City: " + component.getName());
                                break;
                            }
                        }
                    }
                }

                @Override
                public void onError(@NonNull Status status) {
                    // Handle error
                }
            });
        } else {
            Log.d("DEBUG", "AutocompleteSupportFragment not found");
        }
        /*
        // START --- MANUAL LOCATION ---
        binding.getWeatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cityName = binding.enterCityTextEdit.getText().toString().trim();
                if (cityName.isEmpty()) {
                    Toast.makeText(getActivity(), "Please enter a city name", Toast.LENGTH_SHORT).show();
                    return;
                }

                String url = BASE_URL + "?q=" + cityName + "&appid=" + API_KEY;

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject main = response.getJSONObject("main");
                                    double temperature = main.getDouble("temp");
                                    double feelsLike = main.getDouble("feels_like");
                                    temperature = ((temperature - 273.15) * 9 / 5 + 32);
                                    feelsLike = ((feelsLike - 273.15) * 9 / 5 + 32);
                                    int humidity = main.getInt("humidity");
                                    JSONArray weather = response.getJSONArray("weather");
                                    String description = weather.getJSONObject(0).getString("description");
                                    String mainDescription = weather.getJSONObject(0).getString("main");

                                    String output =
                                            "Temperature: " + df.format(temperature) + "°F" + "\n"
                                            + "Feels like: " + df.format(feelsLike) + "°F" + "\n"
                                            + "Humidity: " + humidity + "%" + "\n"
                                            + "Description: " + description + "\n"
                                            + "Main condition: " + mainDescription;                                            ;

                                    binding.showWeatherData.setText(output);

                                    activityReco = new ArrayList<>();
                                    clothingReco = new ArrayList<>();
                                    foodReco = new ArrayList<>();
                                    tempRecommendations = dbManager.getItemsByTemp((int) temperature);
                                    Log.d("DEBUG", "SETTINGS FRAGMENT: LENGTH: " + tempRecommendations.size());
                                    for (int i = 0; i < tempRecommendations.size(); i++) {
                                        if (tempRecommendations.get(i).getType().equals("ACTIVITY")) {
                                            activityReco.add(tempRecommendations.get(i));
                                        } else if (tempRecommendations.get(i).getType().equals("CLOTHING")) {
                                            clothingReco.add(tempRecommendations.get(i));
                                        } else if (tempRecommendations.get(i).getType().equals("FOOD")) {
                                            foodReco.add(tempRecommendations.get(i));
                                        }
                                    }

                                    String temp = "-----ACTIVITIES-----\n";
                                    for (int i = 0; i < activityReco.size(); i++) {
                                        temp = temp + "\t" + activityReco.get(i).getName() + "\n";
                                    }
                                    temp += "-----FOOD-----\n";
                                    for (int i = 0; i < foodReco.size(); i++) {
                                        temp = temp + "\t" + foodReco.get(i).getName() + "\n";
                                    }
                                    temp += "-----CLOTHING-----\n";
                                    for (int i = 0; i < clothingReco.size(); i++) {
                                        temp = temp + "\t" + clothingReco.get(i).getName() + "\n";
                                    }
                                    binding.showRecom.setText(temp);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(binding.getRoot().getContext());
                requestQueue.add(jsonObjectRequest);
            }
        });
        // END --- MANUAL LOCATION ---
         */

        // START --- SETUP DATABASE ---
        dbManager = new SQLiteManager(getContext());
        try { dbManager.createDataBase(); } catch (Exception e) { Log.d("DEBUG", "EXCEPTION: " + e); }
        try { dbManager.openDataBase(); } catch (SQLException e) { Log.d("DEBUG", "EXCEPTION: " + e); }
        SQLiteDatabase db1 = dbManager.getReadableDatabase();
        // END --- SETUP DATABASE ---

        //autocompleteFragment.getView().setVisibility(View.GONE);
        //selectedCityText.setVisibility(View.GONE);
        //manualCitySwitch.setVisibility(View.GONE);
        //manualCityText.setVisibility(View.GONE);

        // START --- SETUP LOCATION ---
        if(!GlobalData.getInstance().getManualSwitch()) {
            locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allPermissionsGranted = true;
                for (Map.Entry<String, Boolean> entry : result.entrySet()) {
                    if (!entry.getValue()) {
                        allPermissionsGranted = false;
                        break;
                    }
                }
                if (allPermissionsGranted) {
                    getLocation();
                } else {
                    Toast.makeText(getActivity(), "Location permission is required for this app", Toast.LENGTH_SHORT).show();
                }
            });
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
            } else {
                getLocation();
            }
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if(!GlobalData.getInstance().getManualSwitch()) {
                        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                        //ADDED
                        latitude = String.valueOf(location.getLatitude());
                        longitude = String.valueOf(location.getLongitude());
                        //Create the URL for the weather data
                        String regularURL = BASE_URL + "?lat=" + latitude + "&lon=" + longitude + "&appid=" + API_KEY;
                        String hourlyURL = ONE_CALL_API_URL + "/onecall?lat=" + latitude + "&lon=" + longitude + "&exclude=" + EXCLUDE + "&units=" + UNITS + "&appid=" + API_KEY;
                        //ADDED
                        getWeatherData(regularURL);
                        getHourlyForecastData(hourlyURL);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) { /*Nothing*/ }

                @Override
                public void onProviderEnabled(String provider) { /*Nothing*/ }

                @Override
                public void onProviderDisabled(String provider) { /*Nothing*/ }
            };
        } else {
            List<String> urls = GlobalData.getInstance().getManualURsL();
            getWeatherData(urls.get(0));
            getHourlyForecastData(urls.get(1));
            selectedCityText.setText("Current Selected City: " + GlobalData.getInstance().getLocationCity());
        }
        return root;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try { locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                bottomNavigationView = getActivity().findViewById(R.id.nav_view);
                bottomNavigationView.setVisibility(View.GONE);
            } catch (Exception e) { Log.d("DEBUG", "EXCEPTION: " + e); }
        }
    }

    // START --- GET WEATHER DATA ---
    //https://openweathermap.org/weather-conditions <-- List of conditions
    //SNOW, RAIN, DRIZZLE, THUNDERSTORM, CLEAR, CLOUDS <-- Main conditions
    private void getWeatherData(String url) { //Location location

        autocompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        selectedCityText = binding.currentSelectedCity;
        manualCitySwitch = binding.CitySwitch;
        manualCityText = binding.manualCityText;

        manualCitySwitch.setVisibility(View.GONE);
        manualCityText.setVisibility(View.GONE);

        //Make nav bar invisible while weather data is being gathered
        bottomNavigationView = getActivity().findViewById(R.id.nav_view);
        bottomNavigationView.setVisibility(View.GONE);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            cityName = response.getString("name");
                            JSONObject main = response.getJSONObject("main");
                            temperature = main.getDouble("temp");
                            feelsLike = main.getDouble("feels_like");
                            humidity = main.getInt("humidity");
                            JSONArray weather = response.getJSONArray("weather");
                            description = weather.getJSONObject(0).getString("description");
                            mainDescription = weather.getJSONObject(0).getString("main");

                            binding.textCityName.setText(cityName);
                            //TEMPS MATH
                            temperature = ((temperature - 273.15) * 9 / 5 + 32);
                            feelsLike = ((feelsLike - 273.15) * 9 / 5 + 32);
                            GlobalData.getInstance().setLocationTemp(temperature);
                            GlobalData.getInstance().setLocationCity(cityName);
                            if (!(GlobalData.getInstance().getFahrenheit())) {
                                celsius = ((temperature - 32) * (0.55556));
                                feelsLike = ((feelsLike - 32) * (0.55556));
                                binding.textTemperature.setText(String.format(Locale.getDefault(), "%.0f°C", celsius));
                                binding.textFeelslike.setText(String.format(Locale.getDefault(), "%.0f°C", feelsLike));
                            } else {
                                binding.textTemperature.setText(String.format(Locale.getDefault(), "%.0f°F", temperature));
                                binding.textFeelslike.setText(String.format(Locale.getDefault(), "%.0f°F", feelsLike));
                            }
                            binding.textDescription.setText(description);
                            binding.textHumidity.setText(String.format(Locale.getDefault(), "%d%%", humidity));
                            conditions = dbManager.getItemsByConditions(mainDescription.toUpperCase());
                            GlobalData.getInstance().setCurrentTemp(temperature);
                            GlobalData.getInstance().setCurrentConditions(mainDescription.toUpperCase());
                            temps = dbManager.getItemsByTemp((int) temperature + GlobalData.getInstance().getPersonalTemp());
                            GlobalData.getInstance().setTemps(temps);
                            GlobalData.getInstance().setConditions(conditions);
                            // weather icon change
                            binding.descriptionImage.setImageResource(getImageConditions(mainDescription));
                            bottomNavigationView = getActivity().findViewById(R.id.nav_view);
                            bottomNavigationView.setVisibility(View.VISIBLE);

                            //autocompleteFragment.getView().setVisibility(View.VISIBLE);
                            //selectedCityText.setVisibility(View.VISIBLE);
                            manualCitySwitch.setVisibility(View.VISIBLE);
                            manualCityText.setVisibility(View.VISIBLE);


                        } catch (JSONException e) { e.printStackTrace(); }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                bottomNavigationView = getActivity().findViewById(R.id.nav_view);
                bottomNavigationView.setVisibility(View.VISIBLE);
                //autocompleteFragment.getView().setVisibility(View.VISIBLE);
                //selectedCityText.setVisibility(View.VISIBLE);
                manualCitySwitch.setVisibility(View.VISIBLE);
                manualCityText.setVisibility(View.VISIBLE);

                Toast.makeText(getActivity(), "Error retrieving weather data", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(binding.getRoot().getContext());
        requestQueue.add(jsonObjectRequest);
    }
    //END --- GET WEATHER DATA ---

    //START --- HOURLY FORECAST ---
    private void getHourlyForecastData(String url) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray hourlyForecast = response.getJSONArray("hourly");
                            StringBuilder sb = new StringBuilder();
                            StringBuilder sb1 = new StringBuilder();
                            for (int i = 0; i < hourlyForecast.length() - 43; i++) {
                                JSONObject hourlyData = hourlyForecast.getJSONObject(i);
                                long timestamp = hourlyData.getLong("dt");
                                Date date = new Date(timestamp * 1000);
                                DateFormat format = new SimpleDateFormat("h:mm a", Locale.getDefault());
                                format.setTimeZone(TimeZone.getDefault());
                                String formattedDate = format.format(date);
                                double temp = hourlyData.getDouble("temp");
                                String main = hourlyData.getJSONArray("weather").getJSONObject(0).getString("main");
                                //sb.append(formattedDate).append(" ").append(main).append("                  ").append(temp).append("°F").append("\n\n");
                                sb.append(formattedDate).append("\n\n");
                                if (!(GlobalData.getInstance().getFahrenheit())) {
                                    double celsius = ((temp - 32) * (0.55556));
                                    sb1.append(df.format(celsius)).append("°C").append("\n\n");
                                } else {
                                    sb1.append(df.format(temp)).append("°F").append("\n\n");
                                }
                               //Set Hourly Images
                                binding.hourlyImage1.setImageResource(getImageConditions(main));
                                binding.hourlyImage2.setImageResource(getImageConditions(main));
                                binding.hourlyImage3.setImageResource(getImageConditions(main));
                                binding.hourlyImage4.setImageResource(getImageConditions(main));
                                binding.hourlyImage5.setImageResource(getImageConditions(main));

                            }
                            binding.hourlyHour.setText(sb.toString());
                            binding.hourlyTemp.setText(sb1.toString());
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing JSON response for hourly forecast", e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Error getting hourly forecast data", error);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }
    //END --- HOURLY FORECAST ---

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public int getImageConditions(String cond) {
        switch (cond) {
            case "Clear":
                return R.drawable.sun;
            case "Clouds":
                return R.drawable.fewcloud;
            case "Drizzle":
                return R.drawable.shower;
            case "Rain":
                return R.drawable.rain;
            case "Thunderstorm":
                return R.drawable.storm;
            case "Snow":
                return R.drawable.snow;
            case "Mist":
                return R.drawable.mist;
        }
        return 0;
    }

    @Override
    public void onStart() {
        super.onStart();
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getLocation();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}

