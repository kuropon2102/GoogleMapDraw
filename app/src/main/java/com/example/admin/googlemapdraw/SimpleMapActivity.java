package com.example.admin.googlemapdraw;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class SimpleMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void requestAccessFineLocation() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        double lat = 34.65;
        double lon = 135;

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // GPSで測位
        Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // 測位できない場合
        if (loc == null) {
            // Wi-Fiで測位
            loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        // 測位できた場合
        if (loc != null) {
            lat = loc.getLatitude();
            lon = loc.getLongitude();
        }

        LatLng myLoc = new LatLng(lat, lon);
        CameraPosition.Builder builder = new CameraPosition.Builder();
        // 方向
        builder.bearing(0);
        // 表示角度
        builder.tilt(0);
        // 倍率
        builder.zoom(18);
        // 位置
        builder.target(myLoc);
        CameraUpdate cUpdate = CameraUpdateFactory.newCameraPosition(builder.build());
        mMap.moveCamera(cUpdate);

        // DangerousなPermissionはリクエストして許可をもらわないと使えない
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)){
                // 一度拒否されたとき、Rationale(理論的根拠)を説明して、再度許可ダイアログを出すようにする
                new AlertDialog.Builder(this)
                        .setTitle("許可が必要です")
                        .setMessage("移動に合わせて地図を動かすためには、ACCESS_FINE_LOCATIONを" +
                                "許可してください")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // OK button pressed
                                requestAccessFineLocation();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showToast("GPS機能が使えないので、地図は動きません");
                            }
                        })
                        .show();
            } else{
                // まだ許可を求める前のとき、許可を求めるダイアログを表示する
                requestAccessFineLocation();
            }
        } else {
            // 許可されている場合
            myLocationEnable();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // ユーザーが許可したとき
                // 許可が必要な機能を改めて実行する
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 現在地
                    myLocationEnable();

//                    // 位置測定を始める
//                    locationStart();
                }
                else {
                    // ユーザーが許可しなかった時
                    // 許可されなかったため機能が実行できないことを表示する
                    showToast("GPS機能が使えないので、地図は動きません");
                    // 以下を実行すると、java.lang.RuntimeExceptionになる
                    // mMap.setMyLocationEnable(true);
                }
                return;
            }
        }
    }

    private void myLocationEnable(){
        Log.d("debug", "myLocationEnable()");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }
    private void showToast(String msg) {
        Toast error = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        error.show();
    }
}
