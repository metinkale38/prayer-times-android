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

package com.metinkale.prayer.times.times.sources;

import android.util.Log;

import androidx.annotation.NonNull;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.metinkale.prayer.App;
import com.metinkale.prayer.times.times.Source;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by metin on 12.02.2017.
 * <p>
 * Does not work anymore
 */
@Deprecated
public class IndonesiaTimes extends WebTimes {
    @SuppressWarnings({"unused", "WeakerAccess"})
    public IndonesiaTimes() {
        super();
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public IndonesiaTimes(long id) {
        super(id);
    }

    @NonNull
    @Override
    public Source getSource() {
        return Source.Indonesia;
    }


    protected boolean sync() throws InterruptedException {
        return true;
    }


}
