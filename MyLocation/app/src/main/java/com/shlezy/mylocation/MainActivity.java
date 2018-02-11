package com.shlezy.mylocation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    private EditText ipEdit = null;
    private EditText portEdit = null;
    public static final String serverIPExtra = "ServerIP";
    public static final String serverPortExtra = "ServerPort";
    public static final String TAG = "com.shlezy.mylocation";
    public static final String ipPrefKey = "serverIP";
    public static final String portPrefKey = "serverPort";
    private SharedPreferences sharedPref = null;
    private CheckBox rememberBox = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipEdit = findViewById(R.id.main_edit_ip);
        portEdit = findViewById(R.id.main_edit_port);
        rememberBox = findViewById(R.id.main_check_remember);
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String serverIP = sharedPref.getString(ipPrefKey, "");
        int serverPort = sharedPref.getInt(portPrefKey, 0);
        if (serverIP.length() > 0 && serverPort > 0)
        {
            ipEdit.setText(serverIP);
            ipEdit.setText("" + serverPort);
        }
    }

    public void connect(View view)
    {
        if (rememberBox.isChecked())
            sharedPref.edit().putString(ipPrefKey, ipEdit.getText().toString())
                    .putInt(portPrefKey, Integer.parseInt(portEdit.getText().toString()));
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(serverIPExtra, ipEdit.getText().toString());
        intent.putExtra(serverPortExtra, Integer.parseInt(portEdit.getText().toString()));
        startActivity(intent);
    }

}