val createAppNames by tasks.creating {
    doLast {
        val file = File(projectDir, "../features/base/src/main/res/values/languages.xml")
        file.readText().lines().filter { it.contains("<item>") }.map {
            it.substringAfter(">").substringBefore("<")
                .let { it.substringBefore("|") to it.substringAfter("|").toInt() }
        }.mapNotNull { (lang, percent) ->
            if (percent > 35) lang else null
        }.map { it.uppercase().take(2) }.map {
            it to File(
                projectDir,
                "../features/base/src/main/translations/values-${it.lowercase()}/strings.xml"
            ).readText()
                .lines().find { it.contains("<string name=\"appName\">") }!!.substringAfter(">")
                .substringBefore("<")
        }.also {
            File(projectDir, "src/main/res/values/app_names.xml").writeText(
                """<?xml version="1.0" encoding="utf-8"?><!--
  ~ Copyright (c) 2013-2023 Metin Kale
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
<resources>
""" + it.map { (cc, name) -> """    <string name="appName$cc" translatable="false">$name</string>""" }
                    .joinToString("\n") + "\n</resources>")

            var aliases = """
        <activity-alias
            android:name=".aliasDefault"
            android:label="@string/appName"
            android:logo="@mipmap/ic_launcher"
            android:targetActivity=".ShortcutActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
                <category android:name="android.intent.category.MULTIWINDOW_LAUNCHER" />
            </intent-filter>
        </activity-alias>
            """.trimIndent() + it.map { (it, _) ->
        """
        <activity-alias
            android:name=".alias$it"
            android:enabled="false"
            android:exported="true"
            android:label="@string/appName$it"
            android:targetActivity=".ShortcutActivity">
            <intent-filter>
                 <action android:name="android.intent.action.MAIN" />
                 
                 <category android:name="android.intent.category.LAUNCHER" />
                 <category android:name="android.intent.category.MULTIWINDOW_LAUNCHER" />
            </intent-filter>
        </activity-alias>
      """
            }.joinToString("\n")


            val manifest = File(projectDir, "../app/src/main/AndroidManifest.xml")

            manifest.readText()
                .replace("<activity-alias[\\s\\S]*?</activity-alias>\n".toRegex(), "")
                .replace("</application>", aliases + "\n    </application>").lines().filter{it.isNotBlank()}.joinToString("\n").let {
                    manifest.writeText(it)
                }

        }
    }
}