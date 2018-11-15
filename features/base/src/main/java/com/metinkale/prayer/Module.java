package com.metinkale.prayer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.play.core.splitinstall.SplitInstallManagerFactory;
import com.google.android.play.core.splitinstall.SplitInstallRequest;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.metinkale.prayer.base.R;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import lombok.Getter;

public enum Module {
    TIMES(R.drawable.ic_menu_times, R.string.appName), COMPASS(R.drawable.ic_menu_compass, R.string.compass),
    NAMES(R.drawable.ic_menu_names, R.string.names), CALENDAR(R.drawable.ic_menu_calendar, R.string.calendar),
    TESBIHAT(R.drawable.ic_menu_tesbihat, R.string.tesbihat), HADITH(R.drawable.ic_menu_hadith, R.string.hadith),
    MISSEDPRAYERS(R.drawable.ic_menu_missed, R.string.missedPrayers), DHIKR(R.drawable.ic_menu_dhikr, R.string.dhikr),
    SETTINGS(R.drawable.ic_menu_settings, R.string.settings), ABOUT(R.drawable.ic_menu_about, R.string.about), INTRO(0, 0), WIDGET(0, 0);
    
    @Getter
    final String key;
    @Getter
    final int iconRes;
    @Getter
    final int titleRes;
    
    Module(int iconRes, int titleRes) {
        this.key = name().toLowerCase(Locale.ENGLISH);
        this.iconRes = iconRes;
        this.titleRes = titleRes;
    }
    
    
    public Intent buildIntent(Context c) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://prayerapp.page.link/" + getKey()));
        intent.setPackage(c.getPackageName());
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        return intent;
    }
    
    public void launch(final Context c) {
        launch(c, null);
    }
    
    public void launch(@NonNull final Context c, @Nullable final Bundle extras) {
        
        if (installed(c)) {
            Intent intent = buildIntent(c);
            if (extras != null)
                intent.putExtras(extras);
            c.startActivity(intent);
            return;
        }
        
        install(c).addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                launch(c, extras);
            }
        });
    }
    
    private Task<Integer> install(@NonNull Context c) {
        SplitInstallRequest request = SplitInstallRequest.newBuilder().addModule(getKey()).build();
        return SplitInstallManagerFactory.create(c).startInstall(request);
    }
    
    public boolean installed(Context context) {
        return hasModuleRes() || SplitInstallManagerFactory.create(context).getInstalledModules().contains(getKey());
    }
    
    
    private boolean hasModuleRes() {
        try {
            Class.forName("com.metinkale.prayer." + key + ".R");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
}
