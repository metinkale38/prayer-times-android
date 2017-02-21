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
 *
 */

package com.crashlytics.android.answers;

/**
 * Created by metin on 21.02.2017.
 */
public class ContentViewEvent {
    public ContentViewEvent putContentId(String s) {
        return this;
    }

    public ContentViewEvent putContentType(String baseActivity) {
        return this;
    }

    public ContentViewEvent putContentName(String act) {
        return this;
    }
}
