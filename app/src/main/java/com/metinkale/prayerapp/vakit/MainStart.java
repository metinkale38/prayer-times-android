package com.metinkale.prayerapp.vakit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

public class MainStart extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(new LinearLayout(this));
        startActivity(new Intent(this, Main.class));
        finish();
    }
}
