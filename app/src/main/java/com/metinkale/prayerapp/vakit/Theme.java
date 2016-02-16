package com.metinkale.prayerapp.vakit;

import android.graphics.Color;

public enum Theme {
    Trans(0xFFFFFFFF, 0x55FFFFFF, 0x00000000, 0x00000000), LightTrans(0xFFFFFFFF, 0x55FFFFFF, 0x33FFFFFF, 0x55FFFFFF), Light(Color.BLACK, Color.WHITE, 0xAAFFFFFF, 0xFF3F9BBF), Dark(Color.WHITE, 0x55FFFFFF, 0x77000000, 0xAAFFFFFF);

    public int bg;
    public int textcolor;
    public int hovercolor;
    public int bgcolor;
    public int strokecolor;

    Theme(int textcolor, int hovercolor, int bgcolor, int strokecolor) {
        this.textcolor = textcolor;
        this.hovercolor = hovercolor;
        this.bgcolor = bgcolor;
        this.strokecolor = strokecolor;
    }

}