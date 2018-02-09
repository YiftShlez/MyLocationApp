package com.shlezy.mylocation;

import android.location.Location;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import static com.shlezy.mylocation.MainActivity.serverIPExtra;
import static com.shlezy.mylocation.MainActivity.serverPortExtra;
import static com.shlezy.mylocation.MainActivity.TAG;
import static com.shlezy.mylocation.MapActivity.checkLocationPermission;
import static com.shlezy.mylocation.MapActivity.requestLocationPermission;

public class ConnectionActivity extends AppCompatActivity
{
    private Socket socket = null; //socket for connecting to the server
    private BufferedReader serverIn = null;
    private PrintWriter serverOut = null;
    private String serverAddr = "";
    private int serverPort;
    private TextView logView = null;
    private TextView serverAddrText = null;
    private TextView clientAddrText = null;
    private TextView locationText = null;

    public void log(String message, int status)
    {
        switch (status)
        {
            case 0:
                Log.i(TAG, message);
                break;
            case 1:
                Log.e(TAG, message);
        }
        logView.append(message + "\n");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void logErr(Throwable err)
    {
        log(Log.getStackTraceString(err), 1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);
        logView = findViewById(R.id.conn_log);
        serverAddr = getIntent().getStringExtra(serverIPExtra);
        serverPort = getIntent().getIntExtra(serverPortExtra, -1);
        serverAddrText = findViewById(R.id.conn_text_server);
        clientAddrText = findViewById(R.id.conn_text_ip);
        locationText = findViewById(R.id.conn_text_location);
        serverAddrText.setText(serverAddr + ":" + serverPort);
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        clientAddrText.setText(ip);
        if (checkLocationPermission(this))

            new Thread(new ConnectionThread()).start();
        //TODO: handle connection to server using intent extras from MainActivity
    }

    Handler setLocationHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            locationText.setText(msg.getData().getString("location"));
        }
    };

    Handler logHandler = new Handler()
    {
        public void handleMessage (Message msg)
        {
            Bundle bundle = msg.getData();
            log (bundle.getString("message"), bundle.getInt("status"));
        }
    };

    class ConnectionThread implements Runnable
    {

        private void threadLog (String message, int status)
        {
            Bundle bundle = new Bundle();
            bundle.putString("message", message);
            bundle.putInt("status", status);
            Message msg = new Message();
            msg.setData(bundle);
            logHandler.sendMessage(msg);
        }
        private void threadLogErr (Throwable error)
        {
            threadLog(Log.getStackTraceString(error), 1);
        }
        public void run ()
        {
            threadLog ("Connecting to server in IP " + serverAddr + " in port " + serverPort, 0);
            try
            {
                InetAddress inetAddress = InetAddress.getByName(serverAddr);
                socket = new Socket(inetAddress, serverPort);
                serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                serverOut = new PrintWriter (socket.getOutputStream(), true);
            }
            catch (IOException ioe)
            {
                threadLog("Error: Failed to connect to server in address " + serverAddr + ":" + serverPort, 1);
                threadLogErr (ioe);
                serverAddrText.setText("Error");
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
                }
                catch (IOException ioe)
                {
                    threadLog("Failed listening to server", 1);
                    threadLogErr(ioe);
                    break;
                }
                if (line == null)
                {
                    threadLog ("connnection closed", 0);
                    break;
                }
                threadLog ("received line from server: " + line, 0);
                if (line.startsWith("check:"))
                {
                    Location serverLocation = new Location ("");
                    serverLocation.setLatitude(Double.parseDouble(line.split(":")[1].split(";")[0]));
                    serverLocation.setLongitude(Double.parseDouble(line.split(":")[1].split(";")[1]));
                    if (checkLocationPermission(ConnectionActivity.this))
                    {
                        FusedLocationProviderClient provider = LocationServices.getFusedLocationProviderClient(ConnectionActivity.this);
                        Task <Location> locationTask = provider.getLastLocation();
                        while (!locationTask.isComplete());
                        if (locationTask.isSuccessful())
                        {
                            Location myLocation = locationTask.getResult();
                            Message message = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("location", myLocation.getLatitude() + ":" + myLocation.getLongitude());
                            message.setData(bundle);
                            setLocationHandler.sendMessage(message);
                            float distance = myLocation.distanceTo(serverLocation);
                            threadLog("Distance: " + distance, 0);
                            if (distance <= 300)
                            {
                                threadLog("close", 0);
                                serverOut.println(myLocation.getLatitude() + ";" + myLocation.getLongitude());
                            }
                        }
                        else
                        {
                            threadLog("Error getting location", 1);
                            serverOut.println("Error0");
                        }
                    }
                    else
                    {
                        requestLocationPermission(ConnectionActivity.this);
                        serverOut.println("Error0");
                    }
                }
            }
            threadLog("connection closed", 0);
        }
    }


}