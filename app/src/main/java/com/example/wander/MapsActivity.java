package com.example.wander;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;

import org.json.JSONObject;

import java.util.Locale;

public class MapsActivity
        extends AppCompatActivity
        implements OnMapReadyCallback {

    private static String TAG = MapsActivity.class.getSimpleName();
    private static int REQUEST_LOCATION_PERMISSION = 1;
    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is
        // ready to be used.
        SupportMapFragment mapFragment =
                (SupportMapFragment) this.getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://jsonplaceholder.typicode.com/posts/1";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i("test", "Response is: " + response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("test", "That didn't work!");
            }
        });

        queue.add(request);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the
     * camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will
     * be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered
     * once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        double latitude = 42.350957;
        double longitude = -71.148327;
        float zoomLevel = 15.0f;
        float overlaySize = 100f;

        LatLng homeLatLng = new LatLng(latitude,
                longitude);
        this.googleMap.addMarker(new MarkerOptions().position(homeLatLng));
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(homeLatLng,
                zoomLevel));

        this.googleMap.addGroundOverlay(new GroundOverlayOptions().image(BitmapDescriptorFactory.fromResource(R.drawable.android))
                .position(homeLatLng,
                        overlaySize));

        this.setMapLongClick();
        this.setPoiClick();
        this.setMapStyle();
        this.enableMyLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater()
                .inflate(R.menu.map_options,
                        menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean result;
        switch (item.getItemId()) {
            case (R.id.normal_map):
                this.googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                result = true;
                break;
            case (R.id.hybrid_map):
                this.googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                result = true;
                break;
            case (R.id.satellite_map):
                this.googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                result = true;
                break;
            case (R.id.terrrain_map):
                this.googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 &&
                    (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                this.enableMyLocation();
            }
        }
    }

    private void setMapLongClick() {
        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String snippet = String.format(Locale.getDefault(),
                        MapsActivity.this.getString(R.string.lat_long_snippet),
                        latLng.latitude,
                        latLng.longitude);
                MapsActivity.this.googleMap.addMarker(new MarkerOptions().position(latLng)
                        .title(MapsActivity.this.getString(R.string.dropped_pin))
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            }
        });
    }

    private void setPoiClick() {
        this.googleMap.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest pointOfInterest) {
                MapsActivity.this.googleMap.addMarker(new MarkerOptions().position(pointOfInterest.latLng)
                        .title(pointOfInterest.name))
                        .showInfoWindow();
            }
        });
    }

    private void setMapStyle() {
        try {
            boolean success =
                    this.googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                            R.raw.map_style));

            if (!success) {
                Log.e(TAG,
                        "Style parsing failed");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG,
                    "Can't find style. Error: ",
                    e);
        }
    }

    private boolean isPermissionGranted() {
        return this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED;
    }

    private void enableMyLocation() {
        if (this.isPermissionGranted()) {
            this.googleMap.setMyLocationEnabled(true);
        } else {
            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }
    }
}
