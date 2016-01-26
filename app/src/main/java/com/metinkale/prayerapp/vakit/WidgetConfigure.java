package com.metinkale.prayerapp.vakit;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.vakit.times.MainHelper;
import com.metinkale.prayerapp.vakit.times.Times;

public class WidgetConfigure extends Activity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        cityDialog();

    }

    void cityDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.cities);

        String[] array = new String[MainHelper.getCount()];
        for (int i = 0; i < array.length; i++) {
            Times t = MainHelper.getTimes(MainHelper.getIds().get(i));
            if (t == null)
                array[i] = "DELETED";
            else
                array[i] = t.getName() + " (" + t.getSource() + ")";
        }
        builder.setItems(array, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                getSharedPreferences("widgets", 0).edit().putLong("" + mAppWidgetId, MainHelper.getIds().get(which)).apply();

                themeDialog();

            }
        });
        builder.show();
    }

    void themeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.widgetDesign);
        builder.setItems(new String[]{getString(R.string.whiteWidget), getString(R.string.blackWidget), getString(R.string.transWhiteWidget), getString(R.string.transWidget)}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                getSharedPreferences("widgets", 0).edit().putInt(mAppWidgetId + "_theme", which).apply();

                result();

            }
        });
        builder.show();
    }

    void result() {
        WidgetProvider.updateWidgets(WidgetConfigure.this);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        WidgetConfigure.this.setResult(RESULT_OK, resultValue);
        WidgetConfigure.this.finish();
    }

}
