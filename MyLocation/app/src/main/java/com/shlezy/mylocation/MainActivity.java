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
    EditText IPEdit = null;
    EditText PortEdit = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        IPEdit = findViewById(R.id.main_edit_ip);
        PortEdit = findViewById(R.id.main_edit_port);
    }

    public void connect (View view)
    {
        Intent intent = new Intent (this, ConnectionActvity.class);
        intent.putExtra("ServerIP", IPEdit.getText().toString());
        intent.putExtra("ServerPort", PortEdit.getText().toString());
    }

    public void showMyLocation (View view)
    {
        Intent intent = new Intent (this, MapActivity.class);
        startActivity(intent);
    }
}