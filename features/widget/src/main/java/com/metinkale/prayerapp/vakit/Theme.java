/*
 * Copyright (c) 2013-2019 Metin Kale
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

import android.graphics.Color;

import androidx.annotation.DrawableRes;

import com.metinkale.prayer.widgets.R;


public enum Theme {
    Trans(0xFFFFFFFF, 0x55FFFFFF, 0x00000000, 0x00000000, R.drawable.widget_trans),
    LightTrans(0xFFFFFFFF, 0x55FFFFFF, 0x33FFFFFF, 0x55FFFFFF, R.drawable.widget_lighttrans),
    Light(Color.BLACK, Color.WHITE, 0xAAFFFFFF, 0xFF3F9BBF, R.drawable.widget_light),
    Dark(Color.WHITE, 0x55FFFFFF, 0x77000000, 0xAAFFFFFF, R.drawable.widget_dark);

    public final int background;
    public final int textcolor;
    public final int hovercolor;
    public final int bgcolor;
    public final int strokecolor;

    Theme(int textcolor, int hovercolor, int bgcolor, int strokecolor, @DrawableRes int background) {
        this.textcolor = textcolor;
        this.hovercolor = hovercolor;
        this.bgcolor = bgcolor;
        this.strokecolor = strokecolor;
        this.background = background;
    }

}