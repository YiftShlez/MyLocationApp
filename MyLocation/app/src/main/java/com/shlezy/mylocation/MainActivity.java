package com.shlezy.mylocation;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
{
    private EditText ipEdit = null;
    private EditText portEdit = null;
    public static final String serverIPExtra = "ServerIP";
    public static final String serverPortExtra = "ServerPort";
    public static final String TAG = "com.shlezy.mylocation";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ipEdit = findViewById(R.id.main_edit_ip);
        portEdit = findViewById(R.id.main_edit_port);
    }

    public void connect (View view)
    {
        Intent intent = new Intent (this, ConnectionActivity.class);
        intent.putExtra(serverIPExtra, ipEdit.getText().toString());
        intent.putExtra(serverPortExtra, Integer.parseInt(portEdit.getText().toString()));
        startActivity(intent);
    }

    public void showMyLocation (View view)
    {
        Intent intent = new Intent (this, MapActivity.class);
        startActivity(intent);
    }
}