package com.metinkale.prayerapp.vakit.times.other;

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

public enum Method {

    MWL("Muslim World League", "Europe, Far East, parts of US",
            new double[]{18, 1, 0, 0, 17}),
    ISNA("Islamic Society of North America", "North America (US and Canada)",
            new double[]{15, 1, 0, 0, 15}),
    Egypt("Egyptian General Authority of Survey", "Africa, Syria, Lebanon, Malaysia",
            new double[]{19.5, 1, 0, 0, 17.5}),
    Makkah("Umm al-Qura University, Makkah", "Arabian Peninsula",
            new double[]{18.5, 1, 0, 1, 90}),
    Karachi("University of Islamic Sciences, Karachi", "Pakistan, Afganistan, Bangladesh, India",
            new double[]{18, 1, 0, 0, 18}),
    Tehran("Institute of Geophysics, University of Tehran", "Iran, Some Shia communities",
            new double[]{17.7, 0, 4.5, 0, 14}),
    Jafari("Shia Ithna Ashari, Leva Research Institute, Qum", "Some Shia communities worldwide",
            new double[]{16, 0, 4, 0, 14}),
    Custom("Custom", "Custom calculation parameters",
            new double[]{0, 0, 0, 0, 0});
    public String title;
    public String desc;
    /*
     * ==Calc Method Parameters:
     * fa : fajr angle
     * ms : maghrib selector (0 = angle; 1 = minutes after sunset)
     * mv : maghrib parameter value (in angle or minutes)
     * is : isha selector (0 = angle; 1 = minutes after maghrib)
     * iv : isha parameter value (in angle or minutes)
     */
    public double[] params;
    double[] offsets = {0, 0, 0, 0, 0, 0, 0, 0};

    Method(String title, String desc, double[] params) {
        this.params = params;
        this.title = title;
        this.desc = desc;
    }

}
