package com.student.matthew.arcodeapp;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.util.Iterator;
import java.util.List;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;


public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, Scene.OnUpdateListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final double MIN_OPENGL_VERSION = 3.0;

    private AnchorNode anchorNode;
    private ArFragment arFragment;
    private ModelRenderable houseRenderable;

    public int count = 1;
    public Pose pose1;
    public Pose pose2;
    public Pose pose3;
    public float degree;
    public double latObject;
    public double lngObject;
    public double latUser;
    public double lngUser;
    public double dist;

    public  Anchor modelAnchor = null;
    public Plane AnchorPlane;
    public Frame AnchorFrame;

    public int MY_PERMISSIONS_REQUEST_FINE_LOCATION;
    public int MY_PERMISSIONS_REQUEST_COARSE_LOCATION;


    TextView distView;
    TextView locLng;
    TextView locLat;
    TextView locAcc;


    private SensorManager mSensorManager;

    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;

    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onResume() {
        super.onResume();
        if (true) {
            startLocationUpdates();
            Log.d(TAG, "Locatie updaten .....................");
        }
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, null);
    //    lngUser = mCurrentLocation.getLongitude();
        Log.d(TAG, "Locatie update started ..............: ");

    }

    @Override
    @SuppressWarnings({"AndroidApiChecker", "FutureReturnValueIgnored"})
    // CompletableFuture requires api level 24
    // FutureReturnValueIgnored is not valid
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        if (!checkIsSupportedDeviceOrFinish(this)) {
            return;
        }

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        setContentView(R.layout.activity_main);
        arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getArSceneView().getScene().addOnUpdateListener(this);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getPlaneDiscoveryController().setInstructionView(null);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        distView = findViewById(R.id.distanceText);
        locLng = findViewById(R.id.angle1Text);
        locLat = findViewById(R.id.angle2Text);
        locAcc = findViewById(R.id.angle3Text);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        latObject = 50.810559;
        lngObject = 4.227127;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }


        createLocationRequest();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

    }



    public void makeAr(Plane plane, Frame frame ) {

        for (int k = 0; k <10 ; k ++) {
            if (this.degree >= 160 && this.degree <= 170) {
                Toast.makeText(this, "walk", Toast.LENGTH_SHORT).show();
                List<HitResult> hitTest = frame.hitTest(screenCenter().x, screenCenter().y);

                Iterator hitTestIterator = hitTest.iterator();

                while (hitTestIterator.hasNext()) {
                    HitResult hitResult = (HitResult) hitTestIterator.next();


                    modelAnchor = plane.createAnchor(hitResult.getHitPose());

                    AnchorNode anchorNode = new AnchorNode(modelAnchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    TransformableNode transformableNode = new TransformableNode(arFragment.getTransformationSystem());
                    transformableNode.setParent(anchorNode);
                    transformableNode.setRenderable(MainActivity.this.houseRenderable);

                    float x = modelAnchor.getPose().tx();
                    float y = modelAnchor.getPose().compose(Pose.makeTranslation(0f, 0f, 0)).ty();

                    transformableNode.setWorldPosition(new Vector3(x, y, -k));

                }
            }
        }
    }
    private Vector3 screenCenter() {
        View vw = findViewById(android.R.id.content);
        return new Vector3(vw.getWidth() / 2f, vw.getHeight() / 2f, 0f);
    }

    private boolean updateUI() {
        Log.d(TAG, "UI update initiated .............");
        if (null != mCurrentLocation) {
            //https://stackoverflow.com/questions/51888308/render-3d-objects-in-arcore-using-gps-location


            double lat = mCurrentLocation.getLatitude();
            double lng = mCurrentLocation.getLongitude();
            double dx = lat - latObject;
            double dy = lng - lngObject;
            dist = Math.sqrt(dx*dx + dy*dy);

            latUser = lat/180*Math.PI;
            lngUser = lng/180*Math.PI;
            lngObject = lngObject/180*Math.PI;
            latObject= latObject/180*Math.PI;
            double y =Math.sin(lngObject-lngUser)*Math.cos(latObject);
            double x =Math.cos(latUser)*Math.sin(latObject)-Math.sin(latUser)*Math.cos(latObject)*Math.cos(lngObject-lngUser);
            double tan = Math.atan2(y,x);
            double graden = tan*180/Math.PI;
            if (graden < 0){
                graden = graden + 360;
            }
            double gradenGebruiker = degree;
            if(gradenGebruiker+90 > graden && gradenGebruiker-90 < graden){
                return true;
            }
            else{
                if(modelAnchor != null){
                return false;
            }
            }

        } else {
            Log.d(TAG, "location is null ...............");
            return false;
        }
        return false;
    }

    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            List<Location> locationList = locationResult.getLocations();
            if (locationList.size() > 0) {
                //The last location in the list is the newest
                Location location = locationList.get(locationList.size() - 1);
                Log.i("MainActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                mCurrentLocation = location;
            }
        }
    };


    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }
        String openGlVersionString =
                ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");
        }
    }
    private boolean isGooglePlayServicesAvailable() {
        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        return ConnectionResult.SUCCESS == status;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        degree = Math.round(event.values[0]);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //niet in gebruik
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onUpdate(FrameTime frameTime) {

        arFragment.onUpdate(frameTime);

        AnchorFrame = arFragment.getArSceneView().getArFrame();

        ModelRenderable.builder()
                .setSource(this, R.raw.bambo_house)
                .build()
                .thenAccept(renderable -> houseRenderable = renderable)
                .exceptionally(
                        throwable -> {
                            Toast toast =
                                    Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return null;
                        });

        if (updateUI()) {
            Session session = arFragment.getArSceneView().getSession();
            float[] pos = { 0,0,-1 };
            float[] rotation = {0,0,0,1};
            modelAnchor =  session.createAnchor(new Pose(pos, rotation));
            anchorNode = new AnchorNode(modelAnchor);
            anchorNode.setRenderable(houseRenderable);
            anchorNode.setParent(arFragment.getArSceneView().getScene());
        }

    }

}
