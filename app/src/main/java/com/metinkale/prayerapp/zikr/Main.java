package com.metinkale.prayerapp.zikr;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.vakit.PrefsView;
import com.metinkale.prayerapp.vakit.PrefsView.PrefsFunctions;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class Main extends BaseActivity implements OnClickListener, OnNavigationListener, OnLongClickListener
{

    private ZikrView mZikr;
    private EditText mTitle;
    private Vibrator mVibrator;
    private Zikr mCurrent;
    private List<Zikr> mZikrList = new ArrayList<>();
    private ImageView mReset;
    private int mVibrate;

    @Override
    public void onCreate(Bundle bdl)
    {
        super.onCreate(bdl);
        setContentView(R.layout.zikr_main);
        mZikr = (ZikrView) findViewById(R.id.zikr);
        mTitle = (EditText) findViewById(R.id.title);
        mZikr.setOnClickListener(this);
        mZikr.setOnLongClickListener(this);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mReset = (ImageView) findViewById(R.id.reset);
        mReset.setOnClickListener(this);
        mVibrate = PreferenceManager.getDefaultSharedPreferences(this).getInt("zikrvibrate2", 0);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

        ((PrefsView) findViewById(R.id.vibration)).setPrefFunctions(new PrefsFunctions()
        {

            @Override
            public Object getValue()
            {
                return mVibrate;
            }

            @Override
            public void setValue(Object obj)
            {
                PreferenceManager.getDefaultSharedPreferences(Main.this).edit().putInt("zikrvibrate2", (Integer) obj).apply();
                mVibrate = (Integer) obj;

            }

        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Gson gson = new Gson();
        mCurrent.title = mTitle.getText().toString();
        mCurrent.color = mZikr.getColor();
        mCurrent.count = mZikr.getCount();
        mCurrent.max = mZikr.getMax();
        mCurrent.count2 = mZikr.getCount2();
        try
        {
            FileWriter fileWriter = new FileWriter(new File(getFilesDir(), "zikr.json"));
            gson.toJson(mZikrList, fileWriter);
            fileWriter.close();
        } catch(JsonIOException | IOException e)
        {
            Crashlytics.logException(e);
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();

        Gson gson = new Gson();

        try
        {
            FileReader reader = new FileReader(new File(getFilesDir(), "zikr.json"));
            mZikrList = gson.fromJson(reader, new TypeToken<List<Zikr>>()
            {
            }.getType());
            reader.close();
            mCurrent = mZikrList.get(0);
        } catch(Exception e)
        {
            Crashlytics.logException(e);
            mZikrList = new ArrayList<>();
            mCurrent = new Zikr();
            mCurrent.title = getString(R.string.tesbih);
            mCurrent.max = 33;

        }

        load(mCurrent);

    }

    private void load(Zikr z)
    {
        if(!mZikrList.contains(z))
        {
            mZikrList.add(z);
        }

        if(mCurrent != z)
        {
            mCurrent.title = mTitle.getText().toString();
            mCurrent.color = mZikr.getColor();
            mCurrent.count = mZikr.getCount();
            mCurrent.max = mZikr.getMax();
            mCurrent.count2 = mZikr.getCount2();
            mCurrent = z;
        }
        mTitle.setText(z.title);
        mZikr.setColor(z.color);
        setNavBarColor(z.color);
        mZikr.setCount(z.count);
        mZikr.setCount2(z.count2);
        mZikr.setMax(z.max);

        if(mZikrList.indexOf(z) == 0)
        {
            mTitle.setEnabled(false);
            mTitle.setText(getString(R.string.tesbih));
        } else
        {
            mTitle.setEnabled(true);
        }
        ArrayList<String> itemList = new ArrayList<>();
        for(Zikr zi : mZikrList)
        {
            itemList.add(zi.title);
        }
        Context c = new ContextThemeWrapper(this, R.style.ToolbarTheme);
        ArrayAdapter<String> aAdpt = new ArrayAdapter<>(c, android.R.layout.simple_list_item_1, android.R.id.text1, itemList);
        getSupportActionBar().setListNavigationCallbacks(aAdpt, this);
        getSupportActionBar().setSelectedNavigationItem(mZikrList.indexOf(z));

    }

    public void changeColor(View v)
    {
        int c = Color.parseColor((String) v.getTag());
        setNavBarColor(c);
        mZikr.setColor(c);
    }

    @Override
    public void onClick(View v)
    {
        if(v == mZikr)
        {
            mZikr.setCount(mZikr.getCount() + 1);

            if(mZikr.getCount() == mZikr.getMax())
            {
                mZikr.counter();
                if(mVibrate != -1) mVibrator.vibrate(new long[]{0, 100, 100, 100, 100, 100}, -1);
                mZikr.setCount(0);
            } else if(mVibrate == 0)
            {
                mVibrator.vibrate(10);
            }

        } else if(v == mReset)
        {
            AlertDialog dialog = new AlertDialog.Builder(Main.this).create();
            dialog.setTitle(R.string.zikr);
            dialog.setMessage(getString(R.string.resetConfirmZikr, mCurrent.title));
            dialog.setCancelable(false);
            dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int buttonId)
                {
                    mZikr.setCount(0);
                }
            });
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int buttonId)
                {
                }
            });
            dialog.setIcon(R.drawable.ic_delete);
            dialog.show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(int itemPosition, long itemId)
    {
        if(mZikrList.indexOf(mCurrent) != itemPosition)
        {
            load(mZikrList.get(itemPosition));
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

        getMenuInflater().inflate(R.menu.zikr, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.add:
                Zikr z = new Zikr();
                z.title = getString(R.string.new_zikr);
                mZikrList.add(z);
                load(z);
                onLongClick(null);
                return true;

            case R.id.del:
                if(mZikrList.indexOf(mCurrent) == 0)
                {
                    return false;
                }
                AlertDialog dialog = new AlertDialog.Builder(Main.this).create();
                dialog.setTitle(R.string.delete);
                dialog.setMessage(getString(R.string.delConfirmZikr, mCurrent.title));
                dialog.setCancelable(false);
                dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.yes), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int buttonId)
                    {
                        mZikrList.remove(mCurrent);
                        load(mZikrList.get(0));
                    }
                });
                dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.no), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int buttonId)
                    {
                    }
                });
                dialog.setIcon(R.drawable.ic_delete);
                dialog.show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onLongClick(View arg0)
    {
        if(mZikrList.indexOf(mCurrent) == 0)
        {
            return false;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.zikr_count);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(mZikr.getMax() + "");

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try
                {
                    mZikr.setMax(Integer.parseInt(input.getText().toString()));
                } catch(Exception e)
                {
                    Crashlytics.logException(e);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();
        return false;
    }

    @Override
    public boolean setNavBar()
    {
        return true;
    }

    private class Zikr
    {
        String title;
        int max;
        int count;
        int color;
        int count2;
    }

}
