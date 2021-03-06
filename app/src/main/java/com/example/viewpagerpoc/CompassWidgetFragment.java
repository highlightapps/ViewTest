package com.example.viewpagerpoc;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.viewpagerpoc.views.CompassView;
import com.example.viewpagerpoc.views.GlobalData;

import static android.content.ContentValues.TAG;
import static android.content.Context.SENSOR_SERVICE;

import com.example.viewpagerpoc.views.LowPassFilter;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CompassWidgetFragment extends Fragment implements SensorEventListener, LocationListener {

    private static PowerManager.WakeLock wakeLock = null;

    private static TextView textTop = null;
    private static TextView textBottom = null;

    private static CompassView compassView = null;
    Context context;

    private static final AtomicBoolean computing = new AtomicBoolean(false);

    private static final int MIN_TIME = 30 * 1000;
    private static final int MIN_DISTANCE = 10;

    private static final float grav[] = new float[3]; // Gravity (a.k.a
    // accelerometer data)
    private static final float mag[] = new float[3]; // Magnetic
    private static final float rotation[] = new float[9]; // Rotation matrix in
    // Android format
    private static final float orientation[] = new float[3]; // azimuth, pitch,
    // roll
    private static float smoothed[] = new float[3];

    private static SensorManager sensorMgr = null;
    private static List<Sensor> sensors = null;
    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;

    private static LocationManager locationMgr = null;
    private static Location currentLocation = null;
    private static GeomagneticField gmf = null;

    private static double floatBearing = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.compass_widget_fragment_layout, null);
        context = getActivity();
        onStart();
        textTop = (TextView) view.findViewById(R.id.textTop);
        compassView = (CompassView) view.findViewById(R.id.compass);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");

        try {
            sensorMgr = (SensorManager) context.getSystemService(SENSOR_SERVICE);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (sensors.size() > 0) sensorGrav = sensors.get(0);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (sensors.size() > 0) sensorMag = sensors.get(0);

            sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_NORMAL);
            sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);

            locationMgr = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

            try {
                /* defaulting to our place */
                Location hardFix = new Location("ATL");
                hardFix.setLatitude(39.931261);
                hardFix.setLongitude(-75.051267);
                hardFix.setAltitude(1);

                try {
                    Location gps = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location network = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (gps != null) currentLocation = (gps);
                    else if (network != null) currentLocation = (network);
                    else currentLocation = (hardFix);
                } catch (Exception ex2) {
                    currentLocation = (hardFix);
                }
                onLocationChanged(currentLocation);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex1) {
            try {
                if (sensorMgr != null) {
                    sensorMgr.unregisterListener(this, sensorGrav);
                    sensorMgr.unregisterListener(this, sensorMag);
                    sensorMgr = null;
                }
                if (locationMgr != null) {
                    locationMgr.removeUpdates(this);
                    locationMgr = null;
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {
        super.onStop();

        try {
            try {
                sensorMgr.unregisterListener(this, sensorGrav);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sensorMgr = null;

            try {
                locationMgr.removeUpdates(this);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            locationMgr = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");

        wakeLock.acquire();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");

        wakeLock.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!computing.compareAndSet(false, true)) return;

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            smoothed = LowPassFilter.filter(event.values, grav);
            grav[0] = smoothed[0];
            grav[1] = smoothed[1];
            grav[2] = smoothed[2];
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smoothed = LowPassFilter.filter(event.values, mag);
            mag[0] = smoothed[0];
            mag[1] = smoothed[1];
            mag[2] = smoothed[2];
        }

        // Get rotation matrix given the gravity and geomagnetic matrices
        SensorManager.getRotationMatrix(rotation, null, grav, mag);
        SensorManager.getOrientation(rotation, orientation);
        floatBearing = orientation[0];

        // Convert from radians to degrees
        floatBearing = Math.toDegrees(floatBearing); // degrees east of true
        // north (180 to -180)

        // Compensate for the difference between true north and magnetic north
        if (gmf != null) floatBearing += gmf.getDeclination();

        // adjust to 0-360
        if (floatBearing < 0) floatBearing += 360;

        GlobalData.setBearing((int) floatBearing);

        computing.set(false);
        //context.super(onSensorChanged(evt));
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER
                || event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // Tell the compass to update it's graphics
            if (compassView != null)
                compassView.postInvalidate();
        }

        // Update the direction text
        updateText(GlobalData.getBearing());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.w(TAG, "Compass data unreliable");
        }
    }

    private static void updateText(float bearing) {
        int range = (int) (bearing / (360f / 16f));
        String dirTxt = "";
        if (range == 15 || range == 0)
            dirTxt = "N";
        else if (range == 1 || range == 2)
            dirTxt = "NE";
        else if (range == 3 || range == 4)
            dirTxt = "E";
        else if (range == 5 || range == 6)
            dirTxt = "SE";
        else if (range == 7 || range == 8)
            dirTxt = "S";
        else if (range == 9 || range == 10)
            dirTxt = "SW";
        else if (range == 11 || range == 12)
            dirTxt = "W";
        else if (range == 13 || range == 14)
            dirTxt = "NW";
        textTop.setText("" + ((int) bearing) + ((char) 176) + " " + dirTxt);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLocationChanged(Location location) {
        if (location == null) throw new NullPointerException();
        currentLocation = (location);
        gmf = new GeomagneticField((float) currentLocation.getLatitude(), (float) currentLocation.getLongitude(), (float) currentLocation.getAltitude(),
                System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderDisabled(String provider) {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onProviderEnabled(String provider) {
        // Ignore
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Ignore
    }

}