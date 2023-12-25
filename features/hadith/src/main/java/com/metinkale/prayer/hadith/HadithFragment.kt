/*
 * Copyright (c) 2013-2023 Metin Kale
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
package com.metinkale.prayer.hadith

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.text.Html
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.core.content.edit
import androidx.core.view.MenuItemCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.metinkale.prayer.App.Companion.get
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.Module
import com.metinkale.prayer.hadith.utils.NumberDialog
import com.metinkale.prayer.utils.LocaleUtils.getLanguage
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

class HadithFragment : BaseActivity.MainFragment(), View.OnClickListener,
    SearchView.OnQueryTextListener {
    private var state = 0
    private lateinit var pager: ViewPager
    private lateinit var left: ImageView
    private lateinit var right: ImageView
    private lateinit var number: TextView
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: MyAdapter
    private lateinit var switch: MenuItem
    private lateinit var fav: MenuItem

    private val favs: MutableSet<Int> = mutableSetOf()
    private var list: MutableList<Int> = mutableListOf()
    private var remFav = -1
    private var task: SearchTask? = null
    private var query: String? = null
    private var shareText: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.hadis_main, container, false)
        prefs = requireActivity().getSharedPreferences("hadis", 0)
        number = v.findViewById(R.id.number)
        left = v.findViewById(R.id.left)
        right = v.findViewById(R.id.right)
        adapter = MyAdapter(childFragmentManager)
        pager = v.findViewById(R.id.pager)
        pager.adapter = adapter
        left.setOnClickListener(this)
        right.setOnClickListener(this)
        number.setOnClickListener(this)
        loadFavs()
        try {
            setState(prefs.getInt("state", STATE_SHUFFLED))
        } catch (e: RuntimeException) {
            recordException(e)
            val lang = getLanguage("en", "de", "tr")
            File(
                get().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                "$lang/hadis.db"
            ).delete()
            Module.TIMES.launch(activity)
        }
        return v
    }

    private fun setState(state: Int): Boolean {
        list.clear()
        query = null
        when (state) {
            STATE_ORDER -> {
                var i = 1
                while (i <= Shuffled.getList().size) {
                    list.add(i)
                    i++
                }
            }

            STATE_SHUFFLED -> list.addAll(Shuffled.getList())
            STATE_FAVORITE -> list.addAll(favs)
            else -> list.addAll(SqliteHelper.get()[SqliteHelper.get().categories[state - 3]])
        }
        if (list.isEmpty()) {
            setState(this.state)
            return false
        }
        this.state = state
        requireActivity().runOnUiThread {
            adapter.notifyDataSetChanged()
            pager.currentItem = 9999
            pager.adapter = adapter
            pager.currentItem = prefs.getInt(last(), 0)
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        pager.currentItem = prefs.getInt(last(), 0)
        loadFavs()
    }

    override fun onPause() {
        super.onPause()
        prefs.edit().putInt(last(), pager.currentItem).putInt("state", state).apply()
        storeFavs()
    }

    private fun last(): String {
        return if (state == STATE_FAVORITE || state == STATE_SHUFFLED || state == STATE_ORDER) {
            "last_nr" + (state == STATE_FAVORITE) + (state == STATE_SHUFFLED)
        } else {
            "last_nr$state"
        }
    }

    override fun onClick(v: View) {
        if (v === left) {
            pager.currentItem = pager.currentItem - 1
        } else if (v === right) {
            pager.currentItem = pager.currentItem + 1
        } else if (v === number) {
            val nd = NumberDialog.create(1, adapter.count + 1, pager.currentItem + 1)
            nd.setOnNumberChangeListener { nr: Int -> pager.setCurrentItem(nr - 1, false) }
            nd.show(childFragmentManager, null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == fav.itemId) {
            val i = adapter.getItemId(pager.currentItem).toInt()
            if (state == STATE_FAVORITE) {
                if (remFav == -1) {
                    fav.setIcon(R.drawable.ic_action_star_outline)
                    remFav = i
                    number.text = String.format(
                        Locale.getDefault(),
                        "%d/%d",
                        pager.currentItem + 1,
                        adapter.count - 1
                    )
                } else {
                    fav.setIcon(R.drawable.ic_action_star)
                    remFav = -1
                    number.text = String.format(
                        Locale.getDefault(),
                        "%d/%d",
                        pager.currentItem + 1,
                        adapter.count
                    )
                }
            } else {
                if (favs.contains(i)) {
                    favs.remove(i)
                } else {
                    favs.add(i)
                }
                adapter.notifyDataSetChanged()
                setCurrentPage(pager.currentItem)
            }
        } else if (item.itemId == switch.itemId) {
            val builder = AlertDialog.Builder(requireActivity())
            val cats = SqliteHelper.get().categories
            val items: MutableList<String> = ArrayList()
            items.add(getString(R.string.mixed))
            items.add(getString(R.string.sorted))
            items.add(getString(R.string.favorite))
            for (cat in cats) {
                items.add(Html.fromHtml(cat).toString())
            }
            builder.setTitle(items[state]).setItems(
                items.toTypedArray<CharSequence>()
            ) { dialog: DialogInterface?, which: Int ->
                if (!setState(which)) {
                    Toast.makeText(activity, R.string.noFavs, Toast.LENGTH_LONG).show()
                }
            }
            builder.show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.hadis, menu)
        switch = menu.findItem(R.id.favswitch)
        fav = menu.findItem(R.id.fav)
        setCurrentPage(pager.currentItem)
        var item = menu.findItem(R.id.menu_item_share)
        item.setOnMenuItemClickListener { item1: MenuItem? ->
            val sendIntent = Intent()
            sendIntent.action = Intent.ACTION_SEND
            sendIntent.putExtra(Intent.EXTRA_TEXT, shareText)
            sendIntent.type = "text/plain"
            startActivity(Intent.createChooser(sendIntent, resources.getText(R.string.share)))
            true
        }
        item = menu.findItem(R.id.menu_search)
        val searchView = MenuItemCompat.getActionView(item) as SearchView
        searchView.setOnQueryTextListener(this)
    }

    override fun onBackPressed(): Boolean {
        onQueryTextSubmit("")
        return super.onBackPressed()
    }

    @SuppressLint("SetTextI18n")
    fun setCurrentPage(page: Int) {
        var i = page
        if (i >= adapter.count) {
            i = adapter.count - 1
        }
        number.text = "${i + 1}/${adapter.count}"
        if (!this::fav.isInitialized) {
            return
        }
        if (adapter.count == 0) {
            return
        }
        if (favs.contains(adapter.getItemId(pager.currentItem).toInt())) {
            fav.setIcon(R.drawable.ic_action_star)
        } else {
            fav.setIcon(R.drawable.ic_action_star_outline)
        }
        if (remFav != -1 && favs.contains(remFav)) {
            favs.remove(remFav)
            adapter.notifyDataSetChanged()
            setCurrentPage(pager.currentItem)
            remFav = -1
        }
    }

    fun storeFavs() {
        prefs.edit {
            putString("favs", Json.encodeToString(SetSerializer(Int.serializer()), favs))
        }
    }

    fun loadFavs() {
        val count = prefs.getInt("Count", 0)
        if (count != 0) {
            for (i in 0 until count) {
                favs.add(prefs.getInt("fav_$i", i) + 1)
            }
            storeFavs()
        }
        favs.addAll(
            Json.decodeFromString(
                SetSerializer(Int.serializer()),
                prefs.getString("favs", null) ?: "[]"
            )
        )
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (task != null && task!!.status == AsyncTask.Status.RUNNING) {
            return false
        }
        this.query = query
        task = SearchTask(requireContext())
        task!!.execute(query)
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }

    private inner class MyAdapter(fm: FragmentManager?) : FragmentPagerAdapter(
        fm!!
    ) {
        override fun getItemId(pos: Int): Long {
            return list[pos].toLong()
        }

        override fun getItem(pos: Int): Fragment {
            return Frag.create(getItemId(pos).toInt())
        }

        override fun getCount(): Int {
            return list.size
        }

        override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
            super.setPrimaryItem(container, position, obj)
            setCurrentPage(position)
            if (obj is Frag) {
                obj.setQuery(query)
                val hadis = (obj as Fragment).requireArguments().getString("hadis", "")
                val kaynak = (obj as Fragment).requireArguments().getString("kaynak", "")
                setShareText(hadis + if (kaynak.length <= 3) "" else "\n\n$kaynak")
            }
        }

        private fun setShareText(txt: String) {
            var txt = txt
            txt = txt.replace("\n", "|")
            shareText = Html.fromHtml(txt).toString().replace("|", "\n")
        }
    }

    private inner class SearchTask(c: Context) : AsyncTask<String, String, Boolean>() {
        private val dialog: ProgressDialog

        init {
            dialog = ProgressDialog(c)
        }

        override fun onPreExecute() {
            dialog.show()
        }

        override fun onPostExecute(success: Boolean) {
            val inputManager =requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            val v = requireActivity().currentFocus
            if (v != null) {
                inputManager.hideSoftInputFromWindow(
                    v.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
            v!!.clearFocus()
            if (dialog.isShowing) {
                dialog.dismiss()
            }
            if (!isCancelled) {
                adapter.notifyDataSetChanged()
                pager.currentItem = 9999
                pager.adapter = adapter
                pager.currentItem = 0
            }
            val s = list.size
            if (query != "") Toast.makeText(
                activity, getString(
                    R.string.foundXHadis,
                    if (s == SqliteHelper.get().count) 0.toString() + "" else s.toString() + ""
                ), Toast.LENGTH_LONG
            ).show()
        }

        override fun onProgressUpdate(vararg arg: String) {
            dialog.setMessage(arg[0])
        }

        override fun doInBackground(vararg args: String): Boolean {
            if ("" == args[0]) {
                return false
            }
            val result = SqliteHelper.get().search(args[0])
            if (result.isNotEmpty()) {
                list = result.toMutableList()
            }
            return result.isNotEmpty()
        }
    }

    companion object {
        private const val STATE_SHUFFLED = 0
        private const val STATE_ORDER = 1
        private const val STATE_FAVORITE = 2
    }
}