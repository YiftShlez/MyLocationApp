package com.shlezy.mylocation;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private String serverAddr = "";
    private int serverPort;
    private TextView clientAddrText = null;
    private TextView locationText = null;
    ConnectionThread connectionThread = new ConnectionThread();
    MapView mapView = null;
    GoogleMap map = null;
    FusedLocationProviderClient locationProvider = null;
    public static final int acces_location_request = 342;
    public static final String TAG = "com.shlezy.mylocation";
    EditText ipEdit = null;
    Button searchBtn = null;

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
        Intent service = new Intent(this, LocationClientService.class);
        startService(service);
        */
        mapView = findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        if (mapView != null)
            mapView.getMapAsync(this);
        locationText = findViewById(R.id.map_locationText);
        ipEdit = findViewById(R.id.map_ipEdit);
        searchBtn = findViewById(R.id.map_searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                connectionThread.searchIP(ipEdit.getText().toString());
            }
        });
        serverAddr = getIntent().getStringExtra(MainActivity.serverIPExtra);
        serverPort = getIntent().getIntExtra(MainActivity.serverPortExtra, 54326);
        clientAddrText = findViewById(R.id.map_ipText);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        clientAddrText.setText(ip);
        locationProvider = LocationServices.getFusedLocationProviderClient(this);
        if (checkLocationPermission(this))
        {
            locationProvider.requestLocationUpdates(new LocationRequest(), new LocationCallback()
            {
                @Override
                public void onLocationResult(LocationResult locationResult)
                {
                    Location location = locationResult.getLastLocation();
                    map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                            location.getLatitude(), location.getLongitude())));
                    map.animateCamera(CameraUpdateFactory.zoomTo(17));
                    locationText.setText(location.getLatitude() + ", " + location.getLongitude());
                }
            }, null);
            connectionThread.start();
        } else
            requestLocationPermission(this);
    }

    public static boolean checkLocationPermission(Context context)
    {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity)
    {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.ACCESS_FINE_LOCATION))
        {

            Toast.makeText(activity, "You disabled location. To enable, go to application settings", Toast.LENGTH_LONG).show();

        } else
        {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(activity,
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
            requestLocationPermission(this);
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
                        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(
                                location.getLatitude(), location.getLongitude())));
                        map.animateCamera(CameraUpdateFactory.zoomTo(17));
                        locationText.setText(location.getLatitude() + ", " + location.getLongitude());
                    }
                }, null);
                connectionThread.start();
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
        mapView.onDestroy();
        connectionThread.closeConnection();
        super.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    Handler mapMarkerHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            Bundle bundle = msg.getData();
            map.addMarker(new MarkerOptions().position
                    (new LatLng(bundle.getDouble("latitude"), bundle.getDouble("longitude")))
                    .title(bundle.getString("ip")));
        }
    };

    class ConnectionThread extends Thread
    {

        Socket socket = null; //socket for connecting to the server
        BufferedReader serverIn = null;
        PrintWriter serverOut = null;

        public void closeConnection()
        {
            new Thread ()
            {
                public void run()
                {
                    serverOut.println("exit");
                    try
                    {
                        serverIn.close();
                        serverOut.close();
                        socket.close();
                    } catch (IOException ioe)
                    {
                        Log.e(TAG, "Error closing connection");
                        Log.e(TAG, Log.getStackTraceString(ioe));
                    }
                }
            }.start();
        }

        public void searchIP(String ip)
        {
            if (!checkLocationPermission(getApplicationContext()))
            {
                requestLocationPermission(MapActivity.this);
                return;
            }
            FusedLocationProviderClient provider = LocationServices.getFusedLocationProviderClient(MapActivity.this);
            Task<Location> locationTask = provider.getLastLocation();
            while (!locationTask.isComplete())
                ;
            if (locationTask.isSuccessful())
            {
                final Location location = locationTask.getResult();
                final String finalIP = ip;
                new Thread()
                {
                    public void run()
                    {
                        serverOut.println("check:" + location.getLatitude() + ";" +
                                location.getLongitude() + ";" + finalIP);
                    }
                }.start();
            }
        }

        public void run()
        {
            Log.i(TAG, "Connecting to server in IP " + serverAddr + " in port " + serverPort);
            try
            {
                InetAddress inetAddress = InetAddress.getByName(serverAddr);
                socket = new Socket(inetAddress, serverPort);
                serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                serverOut = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ioe)
            {
                Log.e(TAG, "Error: Failed to connect to server in address " + serverAddr + ":" + serverPort);
                Log.e(TAG, Log.getStackTraceString(ioe));
                clientAddrText.setText("Error");
                locationText.setText("Error");
                return;
            }

            while (serverIn != null)
            {
                String line = "";
                try
                {
                    line = serverIn.readLine();
                } catch (IOException ioe)
                {
                    Log.e(TAG, "Failed listening to server");
                    Log.e(TAG, Log.getStackTraceString(ioe));
                    break;
                }
                if (line == null)
                {
                    Log.i(TAG, "connnection closed");
                    break;
                }
                Log.i(TAG, "received line from server: " + line);
                if (line.startsWith("check:"))
                {
                    Location serverLocation = new Location("");
                    serverLocation.setLatitude(Double.parseDouble(line.split(":")[1].split(";")[0]));
                    serverLocation.setLongitude(Double.parseDouble(line.split(":")[1].split(";")[1]));
                    if (checkLocationPermission(MapActivity.this))
                    {
                        FusedLocationProviderClient provider = LocationServices.getFusedLocationProviderClient(MapActivity.this);
                        Task<Location> locationTask = provider.getLastLocation();
                        while (!locationTask.isComplete())
                            ;
                        if (locationTask.isSuccessful())
                        {
                            Location myLocation = locationTask.getResult();
                            float distance = myLocation.distanceTo(serverLocation);
                            Log.i(TAG, "Distance: " + distance);
                            if (distance <= 300)
                            {
                                Log.i(TAG, "close");
                                serverOut.println("locResult:" + myLocation.getLatitude() + ";" +
                                        myLocation.getLongitude());
                            } else
                            {
                                Log.i(TAG, "far");
                                serverOut.println("locResult:far");
                            }
                        } else
                        {
                            Log.e(TAG, "Error getting location");
                            serverOut.println("locResult:Error0");
                        }
                    } else
                    {
                        requestLocationPermission(MapActivity.this);
                        serverOut.println("locResult:Error1");
                    }
                } else if (line.startsWith("locResult:"))
                {
                    String result = line.substring(10);
                    if (!(result.equals("far") || result.startsWith("Error")))
                    {
                        String[] params = result.split(";");
                        Bundle bundle = new Bundle();
                        bundle.putDouble("latitude", Double.parseDouble(params[1]));
                        bundle.putDouble("longitude", Double.parseDouble(params[2]));
                        bundle.putString("ip", params[0]);
                        Message message = new Message();
                        message.setData(bundle);
                        mapMarkerHandler.sendMessage(message);
                    }
                }
            }
            Log.i(TAG, "connection closed");
        }
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