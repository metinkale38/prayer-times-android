package com.metinkale.prayerapp.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.BaseActivity;
import com.metinkale.prayerapp.HicriDate;
import com.metinkale.prayerapp.Utils;
import com.metinkale.prayerapp.settings.Prefs;
import org.joda.time.LocalDate;

import java.util.List;

public class Adapter extends ArrayAdapter<int[]> {
    private final Context context;
    private List<int[]> days;
    private boolean hasInfo;

    public Adapter(Context context, int year) {
        super(context, R.layout.names_item);
        this.context = context;
        days = HicriDate.getHolydays(year);

        hasInfo = !"en".equals(Prefs.getLanguage());
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {

        ViewHolder vh;
        if (convertView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.calendar_item, parent, false);

            vh = new ViewHolder();
            vh.name = (TextView) convertView.findViewById(R.id.name);
            vh.date = (TextView) convertView.findViewById(R.id.date);
            vh.hicri = (TextView) convertView.findViewById(R.id.hicri);
            vh.next = (ImageView) convertView.findViewById(R.id.next);
            vh.next.setVisibility(hasInfo ? View.VISIBLE : View.GONE);
            convertView.setTag(R.id.viewholder, vh);
        } else {
            vh = (ViewHolder) convertView.getTag(R.id.viewholder);
        }

        convertView.setPadding(0, 0, 0, pos == getCount() - 1 ? ((BaseActivity) context).getBottomMargin() : 0);
        int[] h = days.get(pos);

        vh.hicri.setText(Utils.format(new HicriDate(h[HicriDate.HY], h[HicriDate.HM], h[HicriDate.HD])));
        vh.date.setText(Utils.format(new LocalDate(h[HicriDate.GY], h[HicriDate.GM], h[HicriDate.GD])));
        vh.name.setText(Utils.getHolyday(h[HicriDate.DAY] - 1));
        convertView.setTag(h[HicriDate.DAY]);
        return convertView;
    }

    @Override
    public int getCount() {
        return days.size();
    }

    static class ViewHolder {
        TextView name;
        TextView date;
        TextView hicri;
        ImageView next;
    }

}