/**
 * Get phone's geolocation (lat, long) for borrow / return locations / user address
 */

package ca.ualberta.cmput301f20t04.bookatmenow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Set Geolocation of user address / book pickup or return.
 */
public class GeoLocation extends AppCompatActivity implements OnMapReadyCallback {

    private Button setGeoLocPickup;
    private Button cancelPickupLocSet;
    private TextView locationMessage;

    private GoogleMap map;
    private LatLng selectedLocation;
    private boolean viewingMap;
    private boolean setAddress;

    private Intent mapType;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private CameraPosition cameraPosition;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);//for testing
    private static final int DEFAULT_ZOOM = 15;
    private static final String TAG = GeoLocation.class.getSimpleName();

    private LatLng pickupLocationViewing;

    /*
    We should have a formula, I was thinking <activity_type_name> so as an example: <setNewUser_button_saveAndExit>
    */

    public void setPickupLocation(View view) {//return selectedLocation via intent listener
        if (selectedLocation == null){//no location was selected
            Toast toast = Toast.makeText(this, "Select a location first", Toast.LENGTH_SHORT);
            toast.show();//tell user improper format
        } else {//location was selected. Return values
            Log.i("AppInfo", "Before intent send: " + String.valueOf(selectedLocation.latitude) + ", " + String.valueOf(selectedLocation.longitude));
            Intent returnData = new Intent();
            returnData.putExtra("lat", String.valueOf(selectedLocation.latitude));
            returnData.putExtra("lng", String.valueOf(selectedLocation.longitude));
            setResult(Activity.RESULT_OK, returnData);
            this.finish();//close activity
        }
    }

    public void cancel(View view) {//cancel setting pickup location, or pressing back on location view screen
        setResult(RESULT_CANCELED);
        this.finish();//close activity
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }*/

        setContentView(R.layout.activity_geo_location);

        setGeoLocPickup = findViewById(R.id.GeoLocation_button_setPickupLoc);
        cancelPickupLocSet = findViewById(R.id.GeoLocation_button_cancel);
        locationMessage = findViewById(R.id.GeoLocation_message);
        locationMessage.setVisibility(View.INVISIBLE);

        mapType = getIntent();
        viewingMap = false;
        setAddress = false;

        if(mapType.getStringExtra(ProgramTags.LOCATION_MESSAGE) != null) {
            switch (mapType.getStringExtra(ProgramTags.LOCATION_MESSAGE)) {
                case "UserLocSelect": {
                    locationMessage.setVisibility(View.VISIBLE);
                    String locSelect = "Please select your location.";
                    SpannableString messageString = new SpannableString(locSelect);
                    messageString.setSpan(new StyleSpan(Typeface.BOLD), 0,
                            locSelect.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    locationMessage.setText(messageString);

                    break;
                }
                case "SelectHandover": {
                    locationMessage.setVisibility(View.VISIBLE);
                    String locView = "Select location to handover: ";
                    String bookName = mapType.getStringExtra(ProgramTags.PASSED_BOOKNAME);
                    SpannableString messageString = new SpannableString(locView + bookName);
                    messageString.setSpan(new StyleSpan(Typeface.BOLD), 0,
                            locView.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    messageString.setSpan(new StyleSpan(Typeface.ITALIC), locView.length() - 1,
                            locView.length() + bookName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    locationMessage.setText(messageString);

                    break;
                }
                case "ViewHandover": {
                    locationMessage.setVisibility(View.VISIBLE);
                    String locView = "Location to handover: ";
                    String bookName = mapType.getStringExtra(ProgramTags.PASSED_BOOKNAME);
                    SpannableString messageString = new SpannableString(locView + bookName);
                    messageString.setSpan(new StyleSpan(Typeface.BOLD), 0,
                            locView.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    messageString.setSpan(new StyleSpan(Typeface.ITALIC), locView.length() - 1,
                            locView.length() + bookName.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    locationMessage.setText(messageString);
                    break;
                }
            }
        }


        if (mapType.getStringExtra(ProgramTags.LOCATION_PURPOSE).equals("view") ){//we are viewing the map, not setting a location
            setGeoLocPickup.setVisibility(View.GONE);
            cancelPickupLocSet.setText("BACK");
            viewingMap = true;
            pickupLocationViewing = new LatLng(Double.valueOf(mapType.getStringExtra("lat")), Double.valueOf(mapType.getStringExtra("lng")));
        } else if(mapType.getStringExtra(ProgramTags.LOCATION_PURPOSE).equals("getLocation")) {//selecting address for profile or pickup
            setGeoLocPickup.setText("SET ADDRESS");
            setAddress = true;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.GeoLocation_fragment_map);
        mapFragment.getMapAsync(this);


        // Prompt the user for permission.
        getLocationPermission();


    }//onCreate end
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles the result of the request for location permissions.
     */
    // [START maps_current_place_on_request_permissions_result]
    @Override
    public void onRequestPermissionsResult(int requestCode,//needs permission to get users location
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    updateLocationUI();
                    getDeviceLocation();
                } else {
                    Toast toast = Toast.makeText(this, "Location permissions are needed to access your location", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {//after map loads
        this.map = googleMap;
        
        if(locationPermissionGranted){
            updateLocationUI();
            getDeviceLocation();
        }

        if (viewingMap == false){//we are wanting to select a location
            this.map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {//user clicks on map
                @Override
                public void onMapClick(LatLng latLng) {
                    map.clear();//clears map of markers
                    selectedLocation = latLng;
                    if(setAddress == true){
                        map.addMarker(new MarkerOptions()//set marker
                                .position(latLng)
                                .title("My Address"));
                    } else {
                        map.addMarker(new MarkerOptions()//set marker
                                .position(latLng)
                                .title("Pickup Location"));
                    }
                }
            });
        }  else {//viewing book pickup location. Set marker there and zoom in on it
            Log.i("AppInfo", "should place marker at: " + String.valueOf(pickupLocationViewing));
            map.addMarker(new MarkerOptions()//set marker
                    .position(pickupLocationViewing)
                    .title("Pickup Location"));

            if (pickupLocationViewing != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLocationViewing, DEFAULT_ZOOM));
            }

        }


    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                            if (lastKnownLocation != null && !viewingMap) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
            updateLocationUI();
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

}