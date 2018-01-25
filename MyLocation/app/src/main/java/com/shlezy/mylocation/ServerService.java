package com.shlezy.mylocation;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This class is a service for listening to distance from client
 *
 * @author Jeff
 */
public class ServerService extends IntentService
{
    private ServerSocket server = null;
    private static final int port = 5743;
    public static final String TAG = "com.shlezy.mylocation";
    FusedLocationProviderClient locationProvider = null;

    public ServerService()
    {
        super("com.shlezy.mylocation.server");
    }

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
        if (!MainActivity.checkLocationPermission(getApplicationContext()))
        {
            Log.e(TAG, "Location permission not granted. killing service");
            stopSelf();
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
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        Log.i(TAG, "Service started");
        if (!MainActivity.checkLocationPermission(getApplicationContext()))
        {
            Log.e(TAG, "no location permission");
            return;
        }
        try
        {
            server = new ServerSocket(port);
        } catch (IOException ioe)
        {
            Log.i(TAG, Log.getStackTraceString(ioe));
        }
        Log.i(TAG, "Server started");
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
    }
}

class Result
{
    private boolean waiting = true;
    private Location crntLocation = null;

    public boolean isWaiting()
    {
        return waiting;
    }

    public void setWaiting(boolean waiting)
    {
        this.waiting = waiting;
    }

    public Location getCrntLocation()
    {
        return crntLocation;
    }

    public void setCrntLocation(Location crntLocation)
    {
        this.crntLocation = crntLocation;
    }
}