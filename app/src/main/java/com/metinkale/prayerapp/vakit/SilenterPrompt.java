package com.metinkale.prayerapp.vakit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.NumberPicker;
import com.metinkale.prayer.R;

public class SilenterPrompt extends Activity {
    private SharedPreferences widgets;

    @Override
    public void onCreate(Bundle bdl) {
        super.onCreate(bdl);
        widgets = getSharedPreferences("widgets", 0);

        setContentView(R.layout.vakit_silenterprompt);

        final NumberPicker np = (NumberPicker) findViewById(R.id.numberPicker);
        np.setMinValue(1);
        np.setMaxValue(300);
        np.setValue(widgets.getInt("silenterWidget", 15));

        findViewById(R.id.cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.ok).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                widgets.edit().putInt("silenterWidget", np.getValue()).apply();
                AlarmReceiver.silenter(v.getContext(), np.getValue());
                finish();
            }
        });

    }
}
