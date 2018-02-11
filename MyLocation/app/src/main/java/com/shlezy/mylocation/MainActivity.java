package com.shlezy.mylocation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity
{
    private EditText ipEdit = null;
    private EditText portEdit = null;
    public static final String serverIPExtra = "ServerIP";
    public static final String serverPortExtra = "ServerPort";
    public static final String TAG = "com.shlezy.mylocation";
    public static final String ipPrefKey = "serverIP";
    public static final String portPrefKey = "serverPort";
    public static final String boxChecKey = "check";
    private SharedPreferences sharedPref = null;
    private CheckBox rememberBox = null;
    private Toolbar toolbar = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        ipEdit = findViewById(R.id.main_edit_ip);
        portEdit = findViewById(R.id.main_edit_port);
        rememberBox = findViewById(R.id.main_check_remember);
        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String serverIP = sharedPref.getString(ipPrefKey, "");
        int serverPort = sharedPref.getInt(portPrefKey, 0);
        rememberBox.setChecked(sharedPref.getBoolean(boxChecKey, false));
        if (serverIP.length() > 0 && serverPort > 0)
        {
            ipEdit.setText(serverIP);
            portEdit.setText("" + serverPort);
        }
    }

    public void connect(View view)
    {
        if (rememberBox.isChecked())
            sharedPref.edit().putString(ipPrefKey, ipEdit.getText().toString())
                    .putInt(portPrefKey, Integer.parseInt(portEdit.getText().toString()))
                    .putBoolean(boxChecKey, true)
                    .commit();
        else
            sharedPref.edit().putString(ipPrefKey, "").putInt(portPrefKey, 0)
                    .putBoolean(boxChecKey, false).commit();
        Intent intent = new Intent(this, MapActivity.class)
                .putExtra(serverIPExtra, ipEdit.getText().toString())
                .putExtra(serverPortExtra, Integer.parseInt(portEdit.getText().toString()));
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.menu_settings:
                return true;
            case R.id.menu_help:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}