package com.example.multi_lang_weather_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Appcompact implements SCDF.SingleChoiceListener {

    public String locate;
    private int PERMISSION_CODE= 1;
    private LocationManager locationManager;
    private Criteria criteria;
    private Location location;
    double lo= 77.1024902;
    double la= 28.7040592;
    String TAG = "Main activity";
    Dialog myDialog,myDialogset;
    FusedLocationProviderClient fusedLocationProviderClient;
    String temp,wind,humidity,pressure,name,city,country;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_CODE);
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
        String provider = locationManager.getBestProvider(criteria, false);
        if (false){
            location = locationManager.getLastKnownLocation(provider);
            locate = getcityname(location.getLatitude(), location.getLongitude());
        }else{
            locate=getLastLocation();
        }
        getweatherinfo(locate);

        SharedPreferences sharedPreferences=getSharedPreferences("pref",MODE_PRIVATE);
        boolean first=sharedPreferences.getBoolean("first",true);
        if(first){
            showstartdig();
        }

        ImageButton LocButton= findViewById(R.id.Locationbtn);
        ImageButton menuBtn = findViewById(R.id.MenuButton);

        LocButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (provider==null){
                    locate = getcityname(location.getLatitude(), location.getLongitude());
                }else{
                    locate=getLastLocation();//default vale when provider = null
                }
                getweatherinfo(locate);
            }
        });

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuBtn);
                popupMenu.getMenuInflater().inflate(R.menu.menu_item, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.search) {
//                          search
                            myDialog = new Dialog(MainActivity.this);
                            myDialog.setContentView(R.layout.custompopupsc);
                            EditText cityEdit = myDialog.findViewById(R.id.cityedit);
                            cityEdit.setOnKeyListener(new View.OnKeyListener() {
                                @Override
                                public boolean onKey(View v, int keyCode, KeyEvent event) {
                                    String city= cityEdit.getText().toString();
                                    if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                            (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                        if(city.isEmpty()){
                                            Toast.makeText(MainActivity.this, "Please Enter Location", Toast.LENGTH_SHORT).show();
                                        }else{
                                            myDialog.dismiss();
                                            getweatherinfo(city);

                                        }
                                        return true;
                                    }
                                    return false;
                                }
                            });
                            myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                            myDialog.setCanceledOnTouchOutside(true);
                            myDialog.show();

                        } else if (item.getItemId() == R.id.language) {
                            DialogFragment singleChoiceDialog = new SCDF();
                            singleChoiceDialog.setCancelable(false);
                            singleChoiceDialog.show(getSupportFragmentManager(), "Language");
                        }
                        return true;
                    }
                });
                try {
                    Object menuHelper;
                    Class[] argTypes;
                    Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
                    fMenuHelper.setAccessible(true);
                    menuHelper = fMenuHelper.get(popupMenu);
                    argTypes = new Class[]{boolean.class};
                    menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
                } catch (Exception e) {

                }
                popupMenu.show();
            }
        });
        if(savedInstanceState!=null){
            locate= savedInstanceState.getString("name vale",name);
            getweatherinfo(locate);
        }
    }
    private void showstartdig(){

        myDialogset = new Dialog(MainActivity.this);
        myDialogset.setContentView(R.layout.cutomfirstpop);
        TextView loc_det= myDialogset.findViewById(R.id.loc_det);
        RadioGroup radioGroup= myDialogset.findViewById(R.id.radioGroup);
        Button cancel= myDialogset.findViewById(R.id.button2);
        Button ok= myDialogset.findViewById(R.id.button);

        String addr=getLocation();
        loc_det.setText("Your Current location is :"+addr);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialogset.dismiss();
            }
        });
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedId = radioGroup.getCheckedRadioButtonId();
                RadioButton lang = myDialogset.findViewById(selectedId);
                if(selectedId==-1){
                    Toast.makeText(MainActivity.this,"Nothing selected", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this,lang.getText(), Toast.LENGTH_SHORT).show();
                }
                myDialogset.dismiss();
            }
        });

        myDialogset.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialogset.setCanceledOnTouchOutside(true);
        myDialogset.show();


        SharedPreferences sharedPreferences= getSharedPreferences("pref",MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean("first",false);//false
        editor.apply();

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("name vale",name);
    }
    private String getLastLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null){
                                try {
                                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    city=addresses.get(0).getLocality();
                                    country=addresses.get(0).getCountryName();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
        }else {
            askPermission();
        }
        return city;
    }
    private String getLocation(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null){
                                try {
                                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    city=addresses.get(0).getLocality();
                                    country=addresses.get(0).getCountryName();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    });
        }else {
            askPermission();
        }
        return (city+","+country);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==PERMISSION_CODE){
            if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permissions granted..", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this, "Please provide the permission ", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getcityname(double latitude, double longitude ){
        String cityname = "not found";
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses!=null){
                city = addresses.get(0).getLocality();
                if(city!=null&&!city.equals("")){
                    cityname=city;
                }else{
                    Log.d("city name", "City name not found" );
                    Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityname;
    }
    private String getaddressname(double latitude, double longitude ){
        String addressname = "not found";
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if(addresses!=null){
                city = addresses.get(0).getLocality();
                country=addresses.get(0).getCountryName();
                if(city!=null&&!city.equals("")){
                    addressname=city+","+country;
                }else{
                    Log.d("Address name", "Address not found" );
                    Toast.makeText(this, "Not Found", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addressname;
    }

    private void getweatherinfo(String cityname){

        TextView textView = findViewById(R.id.temp);
        TextView hum = findViewById(R.id.hum_val);
        TextView ps = findViewById(R.id.pres_val);
        TextView win = findViewById(R.id.wind_val);
        TextView loc_name = findViewById(R.id.loc_name);

        String url ="http://api.weatherapi.com/v1/current.json?key=6576d1c37e394754b4b51008231302&q="+cityname+"&aqi=yes";

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                //Log.e(TAG, "onResponse:"+response.toString() );
                try {
                    JSONObject obj, res = new JSONObject();
                    obj= response.getJSONObject("current");
                    temp = obj.getString("temp_c");
                    wind = obj.getString("wind_kph");
                    pressure = obj.getString("pressure_in");
                    humidity = obj.getString("humidity");

                    res= response.getJSONObject("location");
                    name = res.getString("name");

                    loc_name.setText(name);
                    textView.setText(temp);
                    win.setText(wind);
                    hum.setText(humidity);
                    ps.setText(pressure);


                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: "+e );
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: "+ error);
            }
        });
        queue.add(jsonObjectRequest);

    }

    @Override
    public void onPositiveButtonClicked(String[] list, int position) {
        LanguageManager lang = new LanguageManager(this);
        switch (position) {
            case 0:
                lang.updateResource("en");
                break;
            case 1:
                lang.updateResource("hi");
                break;
        }

        TextView humt = findViewById(R.id.text_hum);
        TextView pst = findViewById(R.id.text_pres);
        TextView wint = findViewById(R.id.text_wind);

        humt.setText(R.string.humidity);
        pst.setText(R.string.pressure);
        wint.setText(R.string.wind_speed);

    }

    @Override
    public void onNegativeButtonClicked() {
        //no action
    }
    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_CODE);
    }
}