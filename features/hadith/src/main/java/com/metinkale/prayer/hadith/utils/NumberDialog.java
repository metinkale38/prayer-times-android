/*
 * Copyright (c) 2013-2017 Metin Kale
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

package com.metinkale.prayer.hadith.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.metinkale.prayer.base.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;




public class NumberDialog extends DialogFragment implements TextWatcher {
    private int mMin;
    private int mMax;
    private int mNr;
    private EditText mEdit;
    private OnNumberChangeListener mList;

    @NonNull
    public static NumberDialog create(int min, int max, int current) {
        Bundle bdl = new Bundle();
        bdl.putInt("min", min);
        bdl.putInt("max", max);
        bdl.putInt("current", current);
        NumberDialog nd = new NumberDialog();
        nd.setArguments(bdl);
        return nd;

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bdl = getArguments();
        mMin = bdl.getInt("min");
        mMax = bdl.getInt("max");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View v = View.inflate(getActivity(), R.layout.number_dialog, null);
        builder.setView(v).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (mList != null) {
                    mList.onNumberChange(mNr);
                }
            }
        }).setNegativeButton(R.string.cancel, null);

        ((TextView) v.findViewById(R.id.max)).setText("/" + (mMax - 1));
        mEdit = v.findViewById(R.id.nr);
        mEdit.addTextChangedListener(this);
        mEdit.setText(bdl.getInt("current") + "");
        int[] ids = {R.id.k0, R.id.k1, R.id.k2, R.id.k3, R.id.k4, R.id.k5, R.id.k6, R.id.k7, R.id.k8, R.id.k9};
        for (int id : ids) {
            Button btn = v.findViewById(id);
            btn.setTag(btn.getText());
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String txt = mEdit.getText().toString();
                    mEdit.setText(txt + view.getTag());
                }
            });
        }

        v.findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = mEdit.getText().toString();
                if (!txt.isEmpty()) {
                    mEdit.setText(txt.substring(0, txt.length() - 1));
                }
            }
        });

        v.findViewById(R.id.prev).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String txt = mEdit.getText().toString();
                mEdit.setText((Integer.parseInt("0" + txt) - 1) + "");
            }
        });
        v.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
    public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
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