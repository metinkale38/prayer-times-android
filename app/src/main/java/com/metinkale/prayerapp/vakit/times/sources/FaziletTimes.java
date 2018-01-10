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

package com.metinkale.prayerapp.vakit.times.sources;

import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.builder.Builders;
import com.metinkale.prayerapp.App;
import com.metinkale.prayerapp.vakit.times.Source;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.metinkale.prayerapp.utils.Utils.az;

@Deprecated
public class FaziletTimes extends WebTimes {

    @SuppressWarnings({"unused", "WeakerAccess"})
    public FaziletTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public FaziletTimes(long id) {
        super(id);
    }

    public static String getDeprecatedText() {
        return "Namaz Vakti Android Uygulaması ile tek Uygulamada değişik Takvimlerin Vakitlerine bakabiliyorsunuz.\n\n" +
                "Vakitler için Fazilet Takvimini kullanan birçok kullanıcı var. Fazilet Takvimi Vakitleri için www.FaziletTakvimi.com sayfasındaki bilgiler kullanılıyordu. " +
                "Bu sayfa malesef artık Namaz Vakitleri göstermiyor.\n\n" +
                "Fazilet Takvimin ücretsiz Uygulaması'da artık çalısmıyor, sadece bir ücretli Uygulamaları var.\n\n" +
                "Dolayısıyla bu uygulamadada artık Fazilet Takvimi Vakitleri indiremiyeceksiniz.\n\n" +
                "Önceden indirilmiş Vakitler varsa bunları kullanabilirsiniz, fakat yeni Vakitler güncellenmiyecektir.\n\n" +
                "Bu engelleme Fazilet Takvimi tarafından, benim yapabileceğim birşey yok malesef.\n" +
                "İsterseniz Namaz Vakti Android Uygulamasında başka bir Takvim seçebilirsiniz. Fazilet Takvimi için ücretli Uygulamalarını satın almanız gerekiyor.";
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Fazilet;
    }


    protected boolean sync() {
        return true;
    }


}