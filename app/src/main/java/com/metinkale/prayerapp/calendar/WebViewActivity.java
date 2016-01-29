package com.metinkale.prayerapp.calendar;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;

public class WebViewActivity extends BaseActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        WebView v = (WebView) findViewById(R.id.webview);
        v.loadUrl("file:///android_asset/" + getIntent().getStringExtra("asset"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:

                onBackPressed();

                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean setNavBar()
    {
        return false;
    }
}
