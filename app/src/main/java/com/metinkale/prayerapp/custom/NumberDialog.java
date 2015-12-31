package com.metinkale.prayerapp.custom;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.metinkale.prayer.R;

public class NumberDialog extends DialogFragment implements TextWatcher {
    private int mMin, mMax;
    private int mNr = 0;
    private EditText mEdit;
    private OnNumberChangeListener mList;

    public NumberDialog() {

    }

    public static NumberDialog create(int min, int max, int current) {
        Bundle bdl = new Bundle();
        bdl.putInt("min", min);
        bdl.putInt("max", max);
        bdl.putInt("current", current);
        NumberDialog nd = new NumberDialog();
        nd.setArguments(bdl);
        return nd;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bdl = getArguments();
        mMin = bdl.getInt("min");
        mMax = bdl.getInt("max");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = View.inflate(getActivity(), R.layout.number_dialog, null);
        builder.setView(v).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mList != null) {
                    mList.onNumberChange(mNr);
                }
            }
        }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }

        });

        ((TextView) v.findViewById(R.id.max)).setText("/" + (mMax - 1));
        mEdit = (EditText) v.findViewById(R.id.nr);
        mEdit.addTextChangedListener(this);
        mEdit.setText(bdl.getInt("current") + "");
        int ids[] = new int[]{R.id.k0, R.id.k1, R.id.k2, R.id.k3, R.id.k4, R.id.k5, R.id.k6, R.id.k7, R.id.k8, R.id.k9};
        for (int id : ids) {
            Button btn = (Button) v.findViewById(id);
            btn.setTag(btn.getText());
            btn.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String txt = mEdit.getText().toString();
                    mEdit.setText(txt + v.getTag());
                }
            });
        }

        v.findViewById(R.id.back).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String txt = mEdit.getText().toString();
                if (txt.length() != 0) {
                    mEdit.setText(txt.substring(0, txt.length() - 1));
                }

            }
        });

        v.findViewById(R.id.prev).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String txt = mEdit.getText().toString();
                mEdit.setText(Integer.parseInt("0" + txt) - 1 + "");

            }
        });
        v.findViewById(R.id.next).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String txt = mEdit.getText().toString();
                mEdit.setText(Integer.parseInt(txt) + 1 + "");

            }
        });

        return builder.create();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        try {
            if (s.length() != 0) {
                int i = Integer.parseInt(mEdit.getText().toString());
                if (i < mMin) {
                    mEdit.setText(mMin + "");
                } else if (i > mMax) {
                    mEdit.setText(mNr + "");
                } else {
                    mNr = i;
                }

            }
        } catch (Exception e) {
            mEdit.setText(mNr + "");
        }

    }

    public void setOnNumberChangeListener(OnNumberChangeListener list) {
        mList = list;
    }

    public interface OnNumberChangeListener {
        void onNumberChange(int nr);
    }
}