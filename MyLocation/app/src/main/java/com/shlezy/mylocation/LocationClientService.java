/*package com.shlezy.mylocation;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.LogRecord;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 *\/
public class LocationClientService extends IntentService
{
    Handler toastHandler = new Handler();
    Socket client = null;
    private static final String serverAddr = "10.0.0.13";
    private static final int serverPort = 54326;
    public static final String TAG = "com.shlezy.mylocation";

    public LocationClientService()
    {
        super("LocationClientService");
    }

    /**
     * The function running in background
     *\/
    private void handleClient()
    {
        Log.i(TAG, "handleClient()");
        try
        {
            client = new Socket(serverAddr, serverPort);
        } catch (IOException ioe)
        {
            Log.e(TAG, "Error connecting to server");
            Log.e(TAG, Log.getStackTraceString(ioe));
        }
        if (client == null)
            return;
        alert("Connected to server");
        BufferedReader in = null;
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(client.getOutputStream(), true);
        } catch (IOException ioe)
        {
            Log.e(TAG, "Error getting output stream");
            Log.e(TAG, Log.getStackTraceString(ioe));
        }
        try
        {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            String line = "";
            while ((line = in.readLine()) != null)
            {
                Log.i(TAG, "Received line: " + line);
                alert ("server line: " + line);
                if (line.split(";")[0].equals("location"))
                {
                    Location crntLocation = getLocation();
                    if (crntLocation == null)
                        out.println("LocationNotAvailable");
                    else
                    {
                        String locStr = line.split(";")[1];
                        Location srvrLocation = new Location("");
                        srvrLocation.setLatitude(Double.parseDouble(locStr.split(",")[0]));
                        srvrLocation.setLongitude(Double.parseDouble(locStr.split(",")[1]));
                        float distance = crntLocation.distanceTo(srvrLocation);
                        Log.i(TAG, "distance: " + distance);
                        alert ("distance " + distance);
                        if (distance <= 300)
                        {
                            out.println("location;" + crntLocation.getLatitude() + "," + crntLocation.getLongitude());
                        } else
                        {
                            out.println("far");
                        }
                    }
                }
            }
            Log.i(TAG, "server line null");
        } catch (IOException ioe)
        {
            Log.e(TAG, "Error listening to server");
            Log.e(TAG, Log.getStackTraceString(ioe));
            return;
        }
        try
        {
            in.close();
            client.close();
        } catch (IOException ioe)
        {
            Log.e(TAG, "Error closing socket");
            Log.e(TAG, Log.getStackTraceString(ioe));
        }
    }


    /**
     * Show a simple alert message using Toast
     *
     * @param msg the message to show in the alert
     *\/
    public void alert(String msg)
    {
        toastHandler.post(new DisplayToast(getApplicationContext(), msg));
    }

    /**
     * Get the current location of the device using Google Play Location Service
     *
     * @return the location of the user. When no permission - return null
     *\/
    private Location getLocation()
    {
        FusedLocationProviderClient providerClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (!MainActivity.checkLocationPermission(getApplicationContext()))
        {
            Log.e(TAG, "Error: no location permission");
            return null;
        }
        final Result result = new Result();
        result.setWaiting(true);
        providerClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>()
        {
            @Override
            public void onComplete(@NonNull Task<Location> task)
            {
                if (task.isSuccessful())
                    result.setCrntLocation(task.getResult());
                else
                    result.setCrntLocation(null);
                result.setWaiting(false);
            }
        });
        while (result.isWaiting())
            ;
        return result.getCrntLocation();
    }
    public class DisplayToast implements Runnable {
        private final Context mContext;
        String mText;

        public DisplayToast(Context mContext, String text){
            this.mContext = mContext;
            mText = text;
        }

        public void run(){
            Toast.makeText(mContext, mText, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent)
    {
        Log.i(TAG, "Service started");
        alert("Service started");
        Thread thread = new Thread()
        {
            public void run()
            {
                handleClient();
            }
        };
        thread.start();
    }
}
*/