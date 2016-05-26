package com.iv4.denicdamjan.silentskola;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaActivity extends FragmentActivity
        implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    public static final String TAG = MapaActivity.class.getSimpleName();
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_mapa);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 sekundi u milisekundama
                .setFastestInterval(1 * 1000); // 1 sekunda u milisekundama
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Servisi geolokacije konektovani.");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        Location lokacija = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (lokacija == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            handleNewLocation(lokacija);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Servisi geolokacije suspendovani. Molimo konektujte se ponovo.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Servisi geolokacije neuspesno konektovani. Kod greske je: " + connectionResult.getErrorCode());
        }
    }
    private void handleNewLocation(Location lokacija) {
        Log.d(TAG, lokacija.toString());

        double latituda_CSkole = 43.320772;  //Koordinati pocetak
        double longituda_CSkole = 21.902488; //Koordinati pocetak

        double delta_lat_skole = 0.000829;
        double delta_long_skole = 0.000481;

        double trenutna_latituda = lokacija.getLatitude();
        double trenutna_longituda = lokacija.getLongitude();

        LatLng latLng = new LatLng(trenutna_latituda, trenutna_longituda);
        LatLng llSkola = new LatLng(43.320772, 21.902488);

        double delta_lat_plus = trenutna_latituda+delta_lat_skole;
        double delta_lat_minus = trenutna_latituda-delta_lat_skole;

        double delta_long_plus = trenutna_longituda+delta_long_skole;
        double delta_long_minus = trenutna_longituda-delta_long_skole;

        boolean lok_lat_plus= trenutna_latituda==delta_lat_plus;
        boolean lok_lat_minus= trenutna_latituda==delta_lat_minus;

        boolean lok_long_plus= trenutna_longituda==delta_long_plus;
        boolean lok_long_minus= trenutna_longituda==delta_long_minus;

        MarkerOptions mLokacija = new MarkerOptions()
                .position(latLng)
                .title("Trenutna lokacija");
        mMap.addMarker(mLokacija);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        MarkerOptions mSkola = new MarkerOptions()
                .position(llSkola)
                .title("Gimnazija 'Bora Stankovic'");
        mMap.addMarker(mSkola);

        if (lok_lat_plus && lok_long_plus || lok_lat_minus && lok_long_minus) {
            AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
            amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
            amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            amanager.setStreamMute(AudioManager.STREAM_RING, true);
            amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);

        }
    }

    @Override
    public void onLocationChanged(Location lokacija) {
        handleNewLocation(lokacija);
    }
}
