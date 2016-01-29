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
import com.metinkale.prayerapp.Date;
import com.metinkale.prayerapp.settings.Prefs;

import java.util.Map;

public class Adapter extends ArrayAdapter<Date>
{
    private final Context context;
    private Map<Date, String> days;
    private boolean hasInfo;

    public Adapter(Context context, int year)
    {
        super(context, R.layout.names_item);
        this.context = context;
        days = Date.getHolydays(year, false);
        hasInfo = !"en".equals(Prefs.getLanguage());
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {

        ViewHolder vh;
        if(convertView == null)
        {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.calendar_item, parent, false);

            vh = new ViewHolder();
            vh.name = (TextView) convertView.findViewById(R.id.name);
            vh.date = (TextView) convertView.findViewById(R.id.date);
            vh.hicri = (TextView) convertView.findViewById(R.id.hicri);
            vh.next = (ImageView) convertView.findViewById(R.id.next);
            vh.next.setVisibility(hasInfo ? View.VISIBLE : View.GONE);
            convertView.setTag(vh);
        } else
        {
            vh = (ViewHolder) convertView.getTag();
        }

        convertView.setPadding(0, 0, 0, pos == getCount() - 1 ? ((BaseActivity) context).getBottomMargin() : 0);
        Date h = days.keySet().toArray(new Date[days.size()])[pos];

        vh.hicri.setText(h.format(true));
        vh.date.setText(h.format(false));
        vh.name.setText(days.get(h));

        return convertView;
    }

    @Override
    public int getCount()
    {
        return days.size();
    }

    static class ViewHolder
    {
        TextView name;
        TextView date;
        TextView hicri;
        ImageView next;
    }

}