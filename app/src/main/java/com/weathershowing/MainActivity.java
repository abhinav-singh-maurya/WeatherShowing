package com.weathershowing;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.text.Html;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.weathershowing.permissionmanager.ActivityManagePermission;
import com.weathershowing.permissionmanager.PermissionResult;
import com.weathershowing.permissionmanager.PermissionUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by abhinav.maurya on 18-12-2017.
 */

public class MainActivity extends ActivityManagePermission {

    @BindView(R.id.city_field)
    TextView city_field;

    @BindView(R.id.updated_field)
    TextView updated_field;

    @BindView(R.id.details_field)
    TextView details_field;

    @BindView(R.id.current_temperature_field)
    TextView current_temperature_field;

    @BindView(R.id.humidity_field)
    TextView humidity_field;

    @BindView(R.id.pressure_field)
    TextView pressure_field;

    @BindView(R.id.weather_icon)
    TextView weather_icon;

    @BindView(R.id.main_root)
    RelativeLayout main_root;

    Typeface weatherFont;
    private Function.placeIdTask asyncTask = null;
    private ProgressDialog _progressDialog = null;
    private static final int REQUEST_PERMISSION_SETTING = 555;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        weatherFont = Typeface.createFromAsset(getApplicationContext().getAssets()
                , "fonts/weathericons-regular-webfont.ttf");

        weather_icon.setTypeface(weatherFont);

        asyncTask =new Function.placeIdTask(new Function.AsyncResponse() {
            public void processFinish(String weather_city, String weather_description,
                                      String weather_temperature, String weather_humidity,
                                      String weather_pressure, String weather_updatedOn,
                                      String weather_iconText, String sun_rise) {
                hideProgressDialog();
                city_field.setText(weather_city);
                updated_field.setText(weather_updatedOn);
                details_field.setText(weather_description);
                current_temperature_field.setText(weather_temperature);
                humidity_field.setText("Humidity: "+weather_humidity);
                pressure_field.setText("Pressure: "+weather_pressure);
                weather_icon.setText(Html.fromHtml(weather_iconText));

            }
        });

        getLocationPermission();

    }

    private void getLocationPermission() {
        askCompactPermissions(new String[]{PermissionUtils.Manifest_ACCESS_COARSE_LOCATION,
                PermissionUtils.Manifest_ACCESS_FINE_LOCATION}, new PermissionResult() {
            @SuppressLint("MissingPermission")
            @Override
            public void permissionGranted() {
                //permission granted
                getLocation();
            }

            @Override
            public void permissionDenied() {
                //permission denied
                Snackbar bar = Snackbar.make(main_root,
                        "Please allow location permission to get current location",
                        Snackbar.LENGTH_INDEFINITE)
                        .setAction("Allow", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getLocationPermission();
                            }
                        });

                bar.show();
            }

            @Override
            public void permissionForeverDenied() {
                // user has check 'never ask again'
                // you need to open setting manually
                Toast.makeText(MainActivity.this, "Manually provide permissions from settings."
                        , Toast.LENGTH_SHORT).show();
                  Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                  Uri uri = Uri.fromParts("package", getPackageName(), null);
                   intent.setData(uri);
                  startActivityForResult(intent, REQUEST_PERMISSION_SETTING);

            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            showProgressDialog(MainActivity.this, "Please wait...");
                            // Logic to handle location object
                            asyncTask.execute(location.getLatitude()+"", location.getLongitude()+"");
                        }
                    }
                });
    }

    public void showProgressDialog(Context context, String message){
        _progressDialog = ProgressDialog.show(context,"", message);
    }

    public void hideProgressDialog(){
        _progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_PERMISSION_SETTING){
                getLocation();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
