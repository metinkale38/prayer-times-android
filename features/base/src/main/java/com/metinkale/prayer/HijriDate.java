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

package com.metinkale.prayer;

import com.crashlytics.android.Crashlytics;
import com.metinkale.prayer.base.R;
import com.metinkale.prayer.utils.FastTokenizer;

import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.chrono.GregorianChronology;
import org.joda.time.chrono.ISOChronology;
import org.joda.time.chrono.IslamicChronology;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Cleanup;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class HijriDate {
    public static final int MUHARRAM = 1;
    public static final int SAFAR = 2;
    public static final int RABIAL_AWWAL = 3;
    public static final int RABIAL_AKHIR = 4;
    public static final int JUMADAAL_AWWAL = 5;
    public static final int JUMADAAL_AKHIR = 6;
    public static final int RAJAB = 7;
    public static final int SHABAN = 8;
    public static final int RAMADAN = 9;
    public static final int SHAWWAL = 10;
    public static final int DHUL_QADA = 11;
    public static final int DHUL_HIJJA = 12;


    public static final int ISLAMIC_NEW_YEAR = 1;
    public static final int ASHURA = 2;
    public static final int MAWLID_AL_NABI = 3;
    public static final int THREE_MONTHS = 4;
    public static final int RAGAIB = 5;
    public static final int MIRAJ = 6;
    public static final int BARAAH = 7;
    public static final int RAMADAN_BEGIN = 8;
    public static final int LAYLATALQADR = 9;
    public static final int LAST_RAMADAN = 10;
    public static final int EID_AL_FITR_DAY1 = 11;
    public static final int EID_AL_FITR_DAY2 = 12;
    public static final int EID_AL_FITR_DAY3 = 13;
    public static final int ARAFAT = 14;
    public static final int EID_AL_ADHA_DAY1 = 15;
    public static final int EID_AL_ADHA_DAY2 = 16;
    public static final int EID_AL_ADHA_DAY3 = 17;
    public static final int EID_AL_ADHA_DAY4 = 18;

    @Getter
    private static int MIN_YEAR = 2012;
    @Getter
    private static int MAX_YEAR = 2022;

    private static Greg MAX_GREG_DIYANET;

    private static TreeMap<Hijri, HijriDate> fromHijri = new TreeMap<>();
    private static TreeMap<Greg, HijriDate> fromGreg = new TreeMap<>();

    private Hijri hijri;
    private Greg greg;


    public int getDay() {
        return hijri.day;
    }

    public int getMonth() {
        return hijri.month;
    }

    public int getYear() {
        return hijri.year;
    }

    private static void init() {

        if (!fromHijri.isEmpty()) return;
        try {
            @Cleanup BufferedReader is = new BufferedReader(new InputStreamReader(App.get().getResources().openRawResource(R.raw.hijri)));
            String line;
            while (true) {
                line = is.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains("HD")) continue;//first line
                FastTokenizer ft = new FastTokenizer(line, "\t");

                int d = ft.nextInt();
                int m = ft.nextInt();
                int y = ft.nextInt();
                Hijri hijri = new Hijri(y, m, d);
                d = ft.nextInt();
                m = ft.nextInt();
                y = ft.nextInt();
                Greg greg = new Greg(y, m, d);

                create(hijri, greg);
            }
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
    }


    private static HijriDate create(Hijri hijri, Greg greg) {
        HijriDate bundle = new HijriDate(hijri, greg);
        if (hijri.day == 1) {
            fromHijri.put(hijri, bundle);
            fromGreg.put(greg, bundle);
            if (greg.year < MIN_YEAR) MIN_YEAR = greg.year;
            if (greg.year > MAX_YEAR && hijri.month == 1) MAX_YEAR = greg.year;
        }
        return bundle;
    }


    public static HijriDate fromGreg(int y, int m, int d) {
        return fromGreg(new LocalDate(y, m, d));
    }

    public static HijriDate fromHijri(int y, int m, int d) {
        return fromHijri(new LocalDate(y, m, d, IslamicChronology.getInstanceUTC()));
    }


    public static HijriDate fromHijri(LocalDate ld) {
        if (!(ld.getChronology() instanceof IslamicChronology)) {
            throw new RuntimeException("fromHijri can only be used with a IslamicChronology");
        }
        init();

        Hijri hijri = new Hijri(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfMonth());

        HijriDate date = fromHijri.get(hijri);
        if (date != null) {
            return date;
        }

        HijriDate last = fromHijri.floorEntry(hijri).getValue();
        if (last == null || fromHijri.ceilingKey(hijri) == null) {
            LocalDate gregorian = ld.toDateTimeAtStartOfDay().withChronology(ISOChronology.getInstanceUTC()).toLocalDate();
            int hfix = Prefs.getHijriFix();
            if (hfix != 0) {
                gregorian = gregorian.plusDays(hfix);
            }
            Greg greg = new Greg(gregorian.getYear(), gregorian.getMonthOfYear(), gregorian.getDayOfMonth());
            return create(hijri, greg);
        } else {
            LocalDate gregLd = last.getLocalDate().plusDays(hijri.day - 1);
            int hfix = Prefs.getHijriFix();
            if (hfix != 0) {
                gregLd = gregLd.plusDays(hfix);
            }
            Greg greg = new Greg(gregLd.getYear(), gregLd.getMonthOfYear(), gregLd.getDayOfMonth());
            return create(hijri, greg);
        }
    }


    public static HijriDate fromGreg(LocalDate ld) {
        if (!(ld.getChronology() instanceof GregorianChronology || ld.getChronology() instanceof ISOChronology)) {
            throw new RuntimeException("fromGreg can only be used with a GregorianChronology");
        }
        init();

        int hfix = Prefs.getHijriFix();
        if (hfix != 0) {
            ld = ld.plusDays(hfix);
        }


        Greg greg = new Greg(ld.getYear(), ld.getMonthOfYear(), ld.getDayOfMonth());

        HijriDate date = fromGreg.get(greg);
        if (date != null) {
            return date;
        }

        HijriDate last = fromGreg.floorEntry(greg).getValue();
        if (last == null || fromGreg.ceilingKey(greg) == null) {
            LocalDate islamic = ld.toDateTimeAtStartOfDay().withChronology(IslamicChronology.getInstanceUTC()).toLocalDate();
            Hijri hijri = new Hijri(islamic.getYear(), islamic.getMonthOfYear(), islamic.getDayOfMonth());
            return create(hijri, greg);
        } else {
            int y = last.hijri.year;
            int m = last.hijri.month;
            int d = last.hijri.day;
            LocalDate lastLD = last.getLocalDate();
            int diff = Days.daysBetween(lastLD, ld).getDays();

            d += diff;

            Hijri hijri = new Hijri(y, m, d);
            return create(hijri, greg);
        }
    }

    public LocalDate getLocalDate() {
        return new LocalDate(greg.year, greg.month, greg.day);
    }

    public HijriDate plusDays(int days) {
        return HijriDate.fromGreg(getLocalDate().plusDays(days));
    }


    public HijriDate plusYears(int years) {
        return HijriDate.fromGreg(getLocalDate().plusYears(years));
    }


    public HijriDate plusMonths(int month) {
        return HijriDate.fromGreg(getLocalDate().plusMonths(month));
    }

    @NonNull
    public static ArrayMap<HijriDate, Integer> getHolydaysForGregYear(int year) {
        int hijriYear = fromGreg(year, 1, 1).getYear();
        ArrayMap<HijriDate, Integer> holydays = new ArrayMap<>();
        for (Map.Entry<HijriDate, Integer> entry : getHolydaysForHijriYear(hijriYear).entrySet()) {
            if (entry.getKey().greg.year == year) {
                holydays.put(entry.getKey(), entry.getValue());
            }
        }

        for (Map.Entry<HijriDate, Integer> entry : getHolydaysForHijriYear(hijriYear + 1).entrySet()) {
            if (entry.getKey().greg.year == year) {
                holydays.put(entry.getKey(), entry.getValue());
            }
        }

        return holydays;


    }

    @NonNull
    public static ArrayMap<HijriDate, Integer> getHolydaysForHijriYear(int year) {
        ArrayMap<HijriDate, Integer> dates = new ArrayMap<>(18);
        dates.put(HijriDate.fromHijri(year, MUHARRAM, 1), ISLAMIC_NEW_YEAR);
        dates.put(HijriDate.fromHijri(year, MUHARRAM, 10), ASHURA);
        dates.put(HijriDate.fromHijri(year, RABIAL_AWWAL, 11), MAWLID_AL_NABI);
        dates.put(HijriDate.fromHijri(year, RAJAB, 1), THREE_MONTHS);

        HijriDate ragaib = HijriDate.fromGreg(
                HijriDate.fromHijri(year, RAJAB, 1).getLocalDate()
                        .withDayOfWeek(DateTimeConstants.FRIDAY));
        if (ragaib.getMonth() < RAJAB) ragaib = ragaib.plusDays(7);
        dates.put(ragaib.plusDays(-1), RAGAIB);
        dates.put(HijriDate.fromHijri(year, RAJAB, 26), MIRAJ);
        dates.put(HijriDate.fromHijri(year, SHABAN, 14), BARAAH);
        dates.put(HijriDate.fromHijri(year, RAMADAN, 1), RAMADAN_BEGIN);
        dates.put(HijriDate.fromHijri(year, RAMADAN, 26), LAYLATALQADR);
        HijriDate tmp = null;
        dates.put((tmp = HijriDate.fromHijri(year, SHAWWAL, 1)).plusDays(-1), LAST_RAMADAN);
        dates.put(tmp, EID_AL_FITR_DAY1);
        dates.put(HijriDate.fromHijri(year, SHAWWAL, 2), EID_AL_FITR_DAY2);
        dates.put(HijriDate.fromHijri(year, SHAWWAL, 3), EID_AL_FITR_DAY3);
        dates.put(HijriDate.fromHijri(year, DHUL_HIJJA, 9), ARAFAT);
        dates.put(HijriDate.fromHijri(year, DHUL_HIJJA, 10), EID_AL_ADHA_DAY1);
        dates.put(HijriDate.fromHijri(year, DHUL_HIJJA, 11), EID_AL_ADHA_DAY2);
        dates.put(HijriDate.fromHijri(year, DHUL_HIJJA, 12), EID_AL_ADHA_DAY3);
        dates.put(HijriDate.fromHijri(year, DHUL_HIJJA, 13), EID_AL_ADHA_DAY4);
        return dates;
    }


    public static int isHolyday() {
        return fromGreg(LocalDate.now()).getHolyday();
    }

    public int getHolyday() {
        HijriDate tmp;
        if (hijri.day == 1 && hijri.month == MUHARRAM) {
            return ISLAMIC_NEW_YEAR;
        } else if (hijri.day == 10 && hijri.month == MUHARRAM) {
            return ASHURA;
        } else if (hijri.day == 11 && hijri.month == RABIAL_AWWAL) {
            return MAWLID_AL_NABI;
        } else if (hijri.day == 1 && hijri.month == RAJAB) {
            return THREE_MONTHS;
        } else if ((tmp = fromGreg(getLocalDate().plusDays(1))).getLocalDate().getWeekyear() == DateTimeConstants.FRIDAY
                && tmp.hijri.day <= 7 && tmp.hijri.month == RAJAB) {//we need this, because it might be also the last night of the previous night


            return RAGAIB;
        } else if (hijri.day == 26 && hijri.month == RAJAB) {
            return MIRAJ;
        } else if (hijri.day == 14 && hijri.month == SHABAN) {
            return BARAAH;
        } else if (hijri.day == 1 && hijri.month == RAMADAN) {
            return RAMADAN_BEGIN;
        } else if (hijri.day == 26 && hijri.month == RAMADAN) {
            return LAYLATALQADR;
        } else if (fromGreg(getLocalDate().plusDays(1)).getHolyday() == EID_AL_FITR_DAY1) {
            return LAST_RAMADAN;
        } else if (hijri.day == 1 && hijri.month == SHAWWAL) {
            return EID_AL_FITR_DAY1;
        } else if (hijri.day == 2 && hijri.month == SHAWWAL) {
            return EID_AL_FITR_DAY2;
        } else if (hijri.day == 3 && hijri.month == SHAWWAL) {
            return EID_AL_FITR_DAY3;
        } else if (hijri.day == 9 && hijri.month == DHUL_HIJJA) {
            return ARAFAT;
        } else if (hijri.day == 10 && hijri.month == DHUL_HIJJA) {
            return EID_AL_ADHA_DAY1;
        } else if (hijri.day == 11 && hijri.month == DHUL_HIJJA) {
            return EID_AL_ADHA_DAY2;
        } else if (hijri.day == 12 && hijri.month == DHUL_HIJJA) {
            return EID_AL_ADHA_DAY3;
        } else if (hijri.day == 13 && hijri.month == DHUL_HIJJA) {
            return EID_AL_ADHA_DAY4;
        }

        return 0;
    }

    public static HijriDate now() {
        return fromGreg(LocalDate.now());
    }


    private static class Hijri extends DateIntern<Hijri> {
        Hijri(int year, int month, int day) {
            super(year, month, day);
        }
    }


    private static class Greg extends DateIntern<Hijri> {
        Greg(int year, int month, int day) {
            super(year, month, day);
        }
    }

    @AllArgsConstructor
    private abstract static class DateIntern<K extends DateIntern> implements Comparable<K> {
        final int year;
        final int month;
        final int day;


        @Override
        public int compareTo(@NonNull K o) {
            int x = hashCode();
            int y = o.hashCode();
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof DateIntern)) return false;
            if (!getClass().equals(o.getClass())) return false;
            final DateIntern other = (DateIntern) o;

            return other.hashCode() == hashCode();
        }

        public int hashCode() {
            return year * 10000 + month * 100 + day;
        }

    }


    @Override
    public int hashCode() {
        return hijri.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof HijriDate)) return false;
        return hijri.hashCode() == ((HijriDate) obj).hijri.hashCode();
    }
}

