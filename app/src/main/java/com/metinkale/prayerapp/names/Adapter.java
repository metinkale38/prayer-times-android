package com.metinkale.prayerapp.names;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.names.Adapter.Item;
import com.metinkale.prayerapp.settings.Prefs;

public class Adapter extends ArrayAdapter<Item> {

    private int lang;

    public Adapter(Context context, Item[] objects) {
        super(context, 0, objects);
        lang = Language.valueOf(Prefs.getLanguage()).ordinal();
    }

    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);

    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.names_item, parent, false);
            vh = new ViewHolder();
            vh.name = (TextView) convertView.findViewById(R.id.name);
            vh.arabicImg = (ImageView) convertView.findViewById(R.id.arabicImg);
            vh.arabic = (TextView) convertView.findViewById(R.id.arabicTxt);
            vh.meaning = (TextView) convertView.findViewById(R.id.meaning);

            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }

        convertView.setPadding(0, 0, 0, pos == getCount() - 1 ? ((BaseActivity) getContext()).getBottomMargin() : 0);

        Item i = getItem(pos);
        if (pos == 0) {
            vh.arabicImg.setVisibility(View.VISIBLE);
            vh.arabicImg.setImageResource(R.drawable.allah);
            vh.name.setVisibility(View.GONE);
            vh.arabic.setVisibility(View.GONE);
        } else {
            vh.arabicImg.setImageDrawable(null);
            vh.arabicImg.setVisibility(View.GONE);
            vh.arabic.setText(i.arabic);
            vh.name.setText(i.name[lang]);
            vh.name.setVisibility(View.VISIBLE);
            vh.arabic.setVisibility(View.VISIBLE);
        }

        vh.meaning.setText(i.meaning[lang]);

        return convertView;
    }

    enum Language {
        tr, de, en
    }

    static class Item {
        String arabic;
        String name[] = new String[3];
        String meaning[] = new String[3];

        @Override
        public String toString() {
            return arabic + " " + name[0] + " " + name[1] + " " + name[2] + " " + meaning[0] + " " + meaning[1] + " " + meaning[2];
        }
    }

    static class ViewHolder {
        TextView name, meaning, arabic;
        ImageView arabicImg;
    }
}