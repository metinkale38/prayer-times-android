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
package com.metinkale.prayer.intro

import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import com.metinkale.prayer.utils.LocaleUtils
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet


/**
 * Created by metin on 25.07.17.
 */
class ChangelogFragment : IntroFragment() {
    public override fun shouldShow(): Boolean {
        return true
    }

    override fun allowTouch(): Boolean {
        return true
    }


    private val options = MutableDataSet()
    private val parser: Parser = Parser.builder(options).build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder(options).build()

    private fun WebView.loadMarkdownFile(md: String, css: String) {
        val mdContent = requireActivity().assets.open(md).bufferedReader().readText()
        val mdCss = requireActivity().assets.open(css).bufferedReader().readText()

        val document = parser.parse(mdContent)
        val html: String = renderer.render(document)
        val withStyle = "<html><head><style>$mdCss</style></head><body>$html</body></html>"
        val encodedHtml: String = Base64.encodeToString(withStyle.toByteArray(), Base64.NO_PADDING)
        loadData(encodedHtml, "text/html", "base64")
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.intro_changelog, container, false)
        val md = v.findViewById<WebView>(R.id.markdownview)
        val lang = LocaleUtils.getLanguage("en", "de", "tr")
        when (lang) {
            "en" -> md.loadMarkdownFile("english.md", "style.css")
            "de" -> md.loadMarkdownFile("german.md", "style.css")
            "tr" -> md.loadMarkdownFile("turkish.md", "style.css")
        }
        val color = MainActivity.blendColors(Color.WHITE, backgroundColor, 0.2f)
        md.setBackgroundColor(color)
        v.findViewById<View>(R.id.changelog).setBackgroundColor(color)
        return v
    }

    override fun onSelect() {}
    override fun onEnter() {}
    override fun onExit() {}
}
