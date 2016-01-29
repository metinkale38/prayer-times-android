package com.metinkale.prayerapp.vakit;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.metinkale.prayer.R;
import com.metinkale.prayerapp.vakit.times.MainHelper;
import com.metinkale.prayerapp.vakit.times.Times;
import com.mobeta.android.dslv.DragSortListView;

import java.util.ArrayList;
import java.util.List;

public class SortFragment extends Fragment implements DragSortListView.DropListener, MainHelper.MainHelperListener
{

    private MyAdapter mAdapter;
    private Main act;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bdl)
    {
        ViewGroup v = (ViewGroup) inflater.inflate(R.layout.vakit_sort_main, container, false);

        DragSortListView list = (DragSortListView) v.findViewById(android.R.id.list);
        list.setDropListener(this);
        mAdapter = new MyAdapter();

        list.setAdapter(mAdapter);
        list.setOnItemClickListener(act.mAdapter);
        return v;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MainHelper.addListener(this);

    }

    @Override
    public void onPause()
    {
        super.onPause();
        MainHelper.removeListener(this);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        if(activity instanceof Main)
        {
            act = (Main) activity;

        }
    }

    @Override
    public void drop(int from, int to)
    {
        act.mAdapter.drop(from, to);
    }

    public void notifyDataSetChanged()
    {
        if(mAdapter != null) mAdapter.notifyDataSetChanged();
    }


    private class MyAdapter extends BaseAdapter
    {
        private List<Long> ids = new ArrayList<Long>();

        private int hide = -1;

        public MyAdapter()
        {
            super();
            notifyDataSetChanged();
        }

        @Override
        public void notifyDataSetChanged()
        {
            ids.clear();
            ids.addAll(MainHelper.getIds());
            super.notifyDataSetChanged();
        }

        @Override
        public int getCount()
        {
            return ids.size();
        }

        @Override
        public Times getItem(int pos)
        {
            return MainHelper.getTimes(ids.get(pos));
        }

        @Override
        public long getItemId(int position)
        {
            return ids.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            if(convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.vakit_sort_item, parent, false);
                convertView.setTag(new ViewHolder(convertView));
            }
            final View v = convertView;
            ViewHolder vh = (ViewHolder) v.getTag();
            vh.pos = position;

            final Times c = getItem(position);
            if(c == null) return new View(getContext());
            vh.city.setText(c.getName());
            vh.source.setText(c.getSource().text);

            vh.delete.setOnClickListener(new OnClickListener()
            {

                @Override
                public void onClick(View arg0)
                {
                    act.mAdapter.remove(position);
                }
            });


            return convertView;
        }


        class ViewHolder
        {
            public TextView city;
            public TextView source;
            public ImageView drag, delete;
            public int pos;

            public ViewHolder(View v)
            {
                city = (TextView) v.findViewById(R.id.city);
                source = (TextView) v.findViewById(R.id.source);
                drag = (ImageView) v.findViewById(R.id.drag);
                delete = (ImageView) v.findViewById(R.id.delete);
            }
        }
    }


}