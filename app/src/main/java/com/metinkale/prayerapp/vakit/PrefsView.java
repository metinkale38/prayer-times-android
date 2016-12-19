/*
 * Copyright (c) 2016 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metinkale.prayerapp.vakit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.*;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.NumberPicker;
import android.widget.RadioGroup;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.utils.NumberPickerDialog;
import com.metinkale.prayerapp.utils.NumberPickerDialog.OnNumberSetListener;
import com.metinkale.prayerapp.utils.PermissionUtils;
import com.metinkale.prayerapp.vakit.sounds.SoundChooser;
import com.metinkale.prayerapp.vakit.sounds.SoundChooser.Callback;
import com.metinkale.prayerapp.vakit.sounds.Sounds;
import com.metinkale.prayerapp.vakit.sounds.Sounds.Sound;
import com.metinkale.prayerapp.vakit.times.other.Vakit;

import java.util.List;

public class PrefsView extends View implements OnClickListener {
    private static final ColorFilter sCFActive = new PorterDuffColorFilter(0xffffffff, Mode.SRC_ATOP);
    private static final ColorFilter sCFInactive = new PorterDuffColorFilter(0xffa0a0a0, Mode.SRC_ATOP);

    private final Paint mPaint;
    private Pref mPref;
    private Drawable mDrawable;
    private PrefsFunctions mFunc;
    private Vakit mVakit;

    public PrefsView(Context c) {
        this(c, null, 0);
    }

    public PrefsView(Context c, Pref pref) {
        this(c, null, 0, pref);
    }

    public PrefsView(Context c, AttributeSet attrs) {
        this(c, attrs, 0);
    }

    public PrefsView(Context c, AttributeSet attrs, int defStyle) {
        this(c, attrs, defStyle, null);
    }

    public PrefsView(Context c, AttributeSet attrs, int defStyle, Pref pref) {
        super(c, attrs, defStyle);
        if (pref != null) {
            mPref = pref;
        } else {
            mPref = Pref.valueOf((String) getTag());
        }

        if (mPref == null) {
            mPref = Pref.Sound;
        }
        mDrawable = c.getResources().getDrawable(mPref.resId);
        setOnClickListener(this);

        ViewCompat.setTranslationY(this, getResources().getDimension(R.dimen.dimen4dp));

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
    }

    public void setPrefFunctions(PrefsFunctions func) {
        mFunc = func;
    }

    public void setVakit(Vakit vakit) {
        mVakit = vakit;
    }

    @Override
    public void setTag(Object obj) {
        super.setTag(obj);
        mPref = Pref.valueOf((String) getTag());
    }

    public void setPrefType(Pref pref) {
        mPref = pref;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.scale(0.8f, 0.8f, canvas.getWidth() / 2, canvas.getHeight() / 2);
        Object o = getValue();
        boolean active = ((o instanceof Boolean) && o.equals(true)) || ((o instanceof Integer) && !o.equals(0)) || ((o instanceof String) && !((String) o).startsWith("silent"));
        if (mPref == Pref.Vibration2) {
            active = !o.equals(-1);
        }
        mPaint.setColor(active ? 0xff03A9F4 : 0xffe0e0e0);
        int w = getHeight();
        canvas.drawCircle(w / 2, w / 2, w / 2, mPaint);

        int p = w / 7;
        mDrawable.setBounds(p, p, w - p, w - p);
        mDrawable.setColorFilter(active ? sCFActive : sCFInactive);
        mDrawable.draw(canvas);

        if ((mPref == Pref.Time) || (mPref == Pref.SabahTime) || (mPref == Pref.Silenter)) {
            int s = (Integer) getValue();

            if (s != 0) {
                mPaint.setColor(getResources().getColor(R.color.colorPrimaryDark));
                canvas.drawCircle(getWidth() * 0.78f, getHeight() * 0.78f, getWidth() / 4, mPaint);
                mPaint.setColor(Color.WHITE);
                mPaint.setTextAlign(Align.CENTER);
                mPaint.setTextSize(w / 5);
                mPaint.setTypeface(Typeface.DEFAULT_BOLD);
                canvas.drawText(s + "", w * 0.78f, w * 0.87f, mPaint);
            }
        } else if (mPref == Pref.Vibration2)

        {
            int s = (Integer) getValue();
            String txt = "";
            if (s == 0) {
                txt = "8";
            } else if (s == 1) {
                txt = "1";
            }
            mPaint.setColor(Color.BLACK);
            mPaint.setTextAlign(Align.CENTER);
            mPaint.setTextSize(w / 2);
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);
            if (s == 0) {
                canvas.rotate(90, canvas.getWidth() / 2, canvas.getHeight() / 2);
                canvas.drawText(txt, w / 2, (w * 2) / 3, mPaint);
                canvas.rotate(-90, canvas.getWidth() / 2, canvas.getHeight() / 2);
            } else {
                canvas.drawText(txt, w / 2, w * 2 / 3, mPaint);
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = (width > height) ? height : width;
        setMeasuredDimension(size, size);
    }

    public Object getValue() {
        if (mFunc == null) {
            switch (mPref) {
                case Dua:
                case Sela:
                case Sound:
                    return "silent";
                case Time:
                case Silenter:
                    return 0;
                case Vibration:
                    return false;
                case Vibration2:
                    return 0;
                default:
                    break;
            }
        } else {
            return mFunc.getValue();
        }

        return null;
    }

    public void setValue(Object obj) {
        if (mFunc != null) {
            mFunc.setValue(obj);
        }
        invalidate();
    }

    @Override
    public void onClick(View v) {
        Object o = getValue();
        if ((mPref == Pref.Sound) || (mPref == Pref.Dua) || (mPref == Pref.Sela)) {
            new SoundChooser().showExpanded(((Activity) getContext()).getFragmentManager(), new Callback() {

                @Override
                public String getCurrent() {
                    return (String) getValue();
                }

                @Override
                public void setCurrent(String current) {
                    setValue(current);

                }

                @Override
                public Vakit getVakit() {
                    return mVakit;
                }

                @Override
                public List<Sound> getSounds() {
                    if (mPref == Pref.Sound) {
                        return Sounds.getSounds(mVakit);
                    } else if (mPref == Pref.Dua) {
                        return Sounds.getSounds("dua", "extra");
                    } else if (mPref == Pref.Sela) {
                        return Sounds.getSounds("sela", "extra");
                    }

                    return Sounds.getSounds(mVakit);
                }

            });

        } else if (mPref == Pref.SabahTime) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.sabahtime_dialog, null);
            final NumberPicker np = (NumberPicker) view.findViewById(R.id.number_picker);
            final RadioGroup rg = (RadioGroup) view.findViewById(R.id.rg);

            int val = (Integer) getValue();
            np.setMinValue(0);
            np.setMaxValue(300);
            np.setValue(Math.abs(val));

            rg.check((val < 0) ? R.id.afterImsak : R.id.beforeGunes);
            builder.setView(view).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    setValue(np.getValue() * ((rg.getCheckedRadioButtonId() == R.id.beforeGunes) ? 1 : -1));
                }
            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.show();
        } else if (mPref == Pref.Vibration2) {
            int i = (Integer) o;
            i++;
            if ((i < -1) || (i > 1)) {
                i = -1;
            }
            setValue(i);
            performHapticFeedback(HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING | HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        } else if (o instanceof Boolean) {
            setValue(!(Boolean) o);
            performHapticFeedback(HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING | HapticFeedbackConstants.FLAG_IGNORE_VIEW_SETTING);
        } else if (o instanceof Integer) {
            int titleId = 0;
            switch (mPref) {
                case Silenter:
                    PermissionUtils.get(getContext())
                            .needNotificationPolicy((Activity) getContext());
                    titleId = R.string.silenterDuration;
                    break;
                case Time:
                    titleId = R.string.time;
                    break;
                default:
                    break;
            }
            NumberPickerDialog npd = new NumberPickerDialog(getContext(), new OnNumberSetListener() {

                @Override
                public void onNumberSet(int dialogId, int number) {
                    setValue(number);

                }
            }, (Integer) o, 0, 300, titleId, 0, 0);
            npd.show();
        }

    }

    public enum Pref {
        Silenter(R.drawable.ic_volume_off_white_24dp), Sound(R.drawable.ic_volume_up_white_24dp), Sela(R.drawable.ic_volume_up_white_24dp), Dua(R.drawable.ic_dua), Vibration2(R.drawable.ic_vibration_white_24dp), Vibration(R.drawable.ic_vibration_white_24dp), Time(R.drawable.ic_access_time_white_24dp), SabahTime(R.drawable.ic_access_time_white_24dp);
        int resId;

        Pref(int resId) {
            this.resId = resId;
        }

    }

    public interface PrefsFunctions {
        Object getValue();

        void setValue(Object obj);
    }
}
