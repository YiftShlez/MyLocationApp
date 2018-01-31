package com.shlezy.mylocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.FusedLocationProviderClient;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
{
    MapView mapView = null;
    GoogleMap map = null;
    TextView locationText = null;
    FusedLocationProviderClient locationProvider = null;
    private static final int acces_location_request = 342;
    public static final String TAG = "com.shlezy.mylocation";

    //private ServerSocket server = null;
    //private static final int port = 8463;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        /*
        if (!MapActivity.checkLocationPermission(getApplicationContext()))
        {
            Log.e(TAG, "no location permission");
            return;
        }
        try
        {
            server = ServerSocketFactory.getDefault().createServerSocket(port);
            Log.i(TAG, "Server started. IP: " + server.getInetAddress().getHostAddress() + " Port: " + server.getLocalPort());
            Socket socket = server.accept();
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println ("Hello world!");
            out.close();
            socket.close();
            Log.i(TAG, "connected");
        } catch (IOException ioe)
        {
            Log.e(TAG, Log.getStackTraceString(ioe));
            return;
        }
        Thread accept = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "Started accepting clients");
                while (true)
                {
                    Socket client = null;
                    try
                    {
                        client = server.accept();
                    } catch (IOException ioe)
                    {
                        Log.e(TAG, "Error accepting client");
                        Log.e(TAG, Log.getStackTraceString(ioe));
                    }
                    if (client != null)
                    {
                        final Socket clientCopy = client;
                        Thread handleClient = new Thread()
                        {
                            public void run()
                            {
                                handleClient(clientCopy);
                            }
                        };
                        handleClient.start();
                    }
                }

            }

        };
        accept.start();
        */
        Intent service = new Intent(this, LocationClientService.class);
        startService(service);
        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        if (mapView != null)
            mapView.getMapAsync(this);
        locationText = findViewById(R.id.locationText);
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission(this))
            locationProvider.requestLocationUpdates(new LocationRequest(), new LocationCallback()
            {
                @Override
                public void onLocationResult(LocationResult locationResult)
                {
                    Location location = locationResult.getLastLocation();
                    locationText.setText(getResources().getString(R.string.my_location) + " " + location.getLatitude() + ", " + location.getLongitude());
                }
            }, null);
        else
            requestLocationPermission();
    }

    public static boolean checkLocationPermission(Context context)
    {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission()
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION))
        {

            Toast.makeText(this, "You disabled location. To enable, go to application settings", Toast.LENGTH_LONG).show();

        } else
        {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, acces_location_request);
            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        if (checkLocationPermission(this))
        {
            googleMap.setMyLocationEnabled(true);
        } else
            requestLocationPermission();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == acces_location_request)
        {
            if (checkLocationPermission(this))
            {
                map.setMyLocationEnabled(true);
                locationProvider.requestLocationUpdates(new LocationRequest(), new LocationCallback()
                {
                    @Override
                    public void onLocationResult(LocationResult locationResult)
                    {
                        Location location = locationResult.getLastLocation();
                        locationText.setText(getResources().getString(R.string.my_location) + " " + location.getLatitude() + ", " + location.getLongitude());
                    }
                }, null);
            }
        } else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void onResume()
    {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy()
    {/*
        try
        {
            server.close();
        }
        catch (IOException ioe)
        {
            Log.e(TAG, Log.getStackTraceString(ioe));
        }*/
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

/*
    private void handleClient(Socket client)
    {
        try
        {
            client = server.accept();
        } catch (IOException ioe)
        {
            Log.e(TAG, "Error receiving client");
            Log.e(TAG, Log.getStackTraceString(ioe));
            return;
        }
        Log.i(TAG, "received client");
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException ioe)
        {
            Log.e(TAG, "Error getting client output stream");
            Log.e(TAG, Log.getStackTraceString(ioe));
            return;
        }
        out.println("Hello");
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        } catch (IOException ioe)
        {
            Log.e(TAG, "Error listening to client");
            Log.e(TAG, Log.getStackTraceString(ioe));
            return;
        }
        locationProvider = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (!MapActivity.checkLocationPermission(getApplicationContext()))
        {
            Log.e(TAG, "Location permission not granted. killing service");
            return;
        }
        final Result result = new Result();
        result.setWaiting(true);
        locationProvider.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>()
        {
            @Override
            public void onSuccess(Location location)
            {
                result.setCrntLocation(location);
            }
        });
        Log.i(TAG, "Location listener set");
        while (result.isWaiting())
            ;
        Log.i(TAG, "Location received");
        if (result.getCrntLocation() == null)
            out.println("LocationUnAvailable");
        else
        {
            String locStr = null;
            try
            {
                locStr = in.readLine();
            } catch (IOException ioe)
            {
                Log.e(TAG, "Error reading client line");
                Log.e(TAG, Log.getStackTraceString(ioe));
                return;
            }
            Location clntLocation = new Location("");
            clntLocation.setLatitude(Double.parseDouble(locStr.split(";")[0]));
            clntLocation.setLongitude(Double.parseDouble(locStr.split(";")[1]));
            Location crntLocation = result.getCrntLocation();
            if (crntLocation.distanceTo(clntLocation) <= 300)
                out.println("Close:" + crntLocation.getLatitude() + ";" + crntLocation.getLongitude());
            else
                out.println("NotClose");
        }
        out.close();
        try
        {
            in.close();
            out.close();
            client.close();
        } catch (IOException ioe)
        {
            Log.e(TAG, "Error closing client socket");
            Log.e(TAG, Log.getStackTraceString(ioe));
        }
    }*/
}