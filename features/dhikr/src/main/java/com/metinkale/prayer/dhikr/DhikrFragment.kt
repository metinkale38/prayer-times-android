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
package com.metinkale.prayer.dhikr

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Vibrator
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.SpinnerAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuItemCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.metinkale.prayer.BaseActivity
import com.metinkale.prayer.CrashReporter.recordException
import com.metinkale.prayer.dhikr.VibrationModeView.PrefsFunctions
import com.metinkale.prayer.dhikr.data.Dhikr
import com.metinkale.prayer.dhikr.data.DhikrViewModel

@Suppress("deprecation")
class DhikrFragment : BaseActivity.MainFragment(), View.OnClickListener, OnLongClickListener,
    AdapterView.OnItemSelectedListener, Observer<List<Dhikr>?> {
    private lateinit var viewModel: DhikrViewModel
    private lateinit var prefs: SharedPreferences
    private lateinit var dhikrView: DhikrView
    private lateinit var title: EditText
    private lateinit var vibrator: Vibrator
    private lateinit var reset: ImageView
    private var spinner: Spinner? = null
    private var dhikrs: List<Dhikr> = listOf()
    private var vibrate: Int = 0
    private var createdViews: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val v = inflater.inflate(R.layout.dhikr_main, container, false)
        prefs = requireActivity().getSharedPreferences("zikr", 0)
        dhikrView = v.findViewById(R.id.zikr)
        title = v.findViewById(R.id.title)
        dhikrView.setOnClickListener(this)
        dhikrView.setOnLongClickListener(this)
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        reset = v.findViewById(R.id.reset)
        reset.setOnClickListener(this)
        vibrate = PreferenceManager.getDefaultSharedPreferences(activity).getInt("zikrvibrate2", 0)
        (v.findViewById<View>(R.id.vibration) as VibrationModeView).setPrefFunctions(object :
            PrefsFunctions {
            override fun getValue(): Int {
                return vibrate
            }

            override fun setValue(obj: Int) {
                PreferenceManager.getDefaultSharedPreferences(activity).edit()
                    .putInt("zikrvibrate2", obj).apply()
                vibrate = obj
            }
        })
        val colorlist = View.OnClickListener { v: View -> changeColor(v) }
        v.findViewById<View>(R.id.color1).setOnClickListener(colorlist)
        v.findViewById<View>(R.id.color2).setOnClickListener(colorlist)
        v.findViewById<View>(R.id.color3).setOnClickListener(colorlist)
        v.findViewById<View>(R.id.color4).setOnClickListener(colorlist)
        v.findViewById<View>(R.id.color5).setOnClickListener(colorlist)
        v.findViewById<View>(R.id.color6).setOnClickListener(colorlist)
        v.findViewById<View>(R.id.color7).setOnClickListener(colorlist)
        v.findViewById<View>(R.id.color8).setOnClickListener(colorlist)
        viewModel = ViewModelProviders.of(this).get(DhikrViewModel::class.java)
        viewModel.dhikrs.observe(viewLifecycleOwner, this)
        title.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (dhikrs.isNotEmpty()) dhikrs[0].title = s.toString()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        createdViews = true
        return v
    }

    override fun onPause() {
        super.onPause()
        if (dhikrs.isEmpty()) return
        viewModel.saveDhikr(dhikrs[0])
    }

    fun changeColor(v: View) {
        val c = Color.parseColor(v.tag as String)
        dhikrs[0].color = c
        dhikrView.invalidate()
    }

    @SuppressLint("MissingPermission")
    override fun onClick(v: View) {
        if (v === dhikrView) {
            val dhikr = dhikrs[0]
            dhikr.value = dhikr.value + 1
            if (dhikr.value % dhikr.max == 0) {
                if (vibrate != -1) {
                    vibrator.vibrate(longArrayOf(0, 100, 100, 100, 100, 100), -1)
                }
            } else if (vibrate == 0) {
                vibrator.vibrate(10)
            }
            dhikrView.invalidate()
        } else if (v === reset) {
            val dialog = AlertDialog.Builder(
                requireActivity()
            ).create()
            dialog.setTitle(R.string.dhikr)
            dialog.setMessage(getString(R.string.resetConfirmDhikr, dhikrs[0].title))
            dialog.setCancelable(false)
            dialog.setButton(
                DialogInterface.BUTTON_POSITIVE, getString(R.string.yes)
            ) { dialogInterface: DialogInterface?, i: Int ->
                val dhikr = dhikrs[0]
                dhikr.value = 0
                dhikrView.invalidate()
            }
            dialog.setButton(
                DialogInterface.BUTTON_NEGATIVE, getString(R.string.no)
            ) { dialogInterface: DialogInterface?, i: Int -> }
            dialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.zikr, menu)
        val item = menu.findItem(R.id.menu_spinner)
        spinner = MenuItemCompat.getActionView(item) as Spinner
        onChanged(viewModel.dhikrs.value)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        dhikrs = viewModel.dhikrs.value ?: listOf()
        val i = item.itemId
        if (i == R.id.add) {
            addNewDhikr()
            return true
        } else if (i == R.id.del) {
            deleteDhikr()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteDhikr() {
        val dialog = AlertDialog.Builder(requireActivity()).create()
        dialog.setTitle(R.string.delete)
        dialog.setMessage(getString(R.string.delConfirmDhikr, dhikrs[0].title))
        dialog.setCancelable(false)
        dialog.setButton(
            DialogInterface.BUTTON_POSITIVE, getString(R.string.yes)
        ) { _: DialogInterface?, i: Int ->
            viewModel.deleteDhikr(dhikrs[0])
        }
        dialog.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            getString(R.string.no)
        ) { _: DialogInterface?, i: Int -> }
        dialog.show()
    }

    private fun addNewDhikr() {
        val builder = AlertDialog.Builder(
            requireActivity()
        )
        builder.setTitle(R.string.dhikr)
        val input = EditText(activity)
        input.setText(getString(R.string.newDhikr))
        input.setSelection(input.text.length)
        input.filters = arrayOf<InputFilter>(LengthFilter(20))
        builder.setView(input)
        builder.setPositiveButton(R.string.ok) { dialogInterface: DialogInterface?, i: Int ->
            val title = input.text.toString()
            val builder1 = AlertDialog.Builder(
                requireActivity()
            )
            builder1.setTitle(R.string.dhikrCount)
            val input1 = EditText(activity)
            input1.inputType = InputType.TYPE_CLASS_NUMBER
            input1.setText(33.toString())
            input1.setSelection(input1.text.length)
            builder1.setView(input1)
            builder1.setPositiveButton(R.string.ok) { dialogInterface1: DialogInterface?, i12: Int ->
                val count = input1.text.toString().toInt()
                viewModel.saveDhikr(dhikrs[0])
                val model = Dhikr()
                model.title = title
                model.max = count
                model.position = -1
                viewModel.addDhikr(model)
            }
            builder1.setNegativeButton(R.string.cancel) { dialog: DialogInterface, i1: Int -> dialog.cancel() }
            builder1.show()
        }
        builder.setNegativeButton(R.string.cancel) { dialog: DialogInterface, i: Int -> dialog.cancel() }
        builder.show()
    }

    override fun onLongClick(arg0: View): Boolean {
        if (dhikrs[0].id == 0) {
            return false
        }
        val builder = AlertDialog.Builder(
            requireActivity()
        )
        builder.setTitle(R.string.dhikrCount)
        val input = EditText(activity)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.setText(dhikrs[0].max.toString())
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton(R.string.ok) { dialogInterface: DialogInterface?, i: Int ->
            try {
                dhikrs[0].max = input.text.toString().toInt()
                dhikrView.invalidate()
            } catch (e: Exception) {
                recordException(e)
            }
        }
        builder.setNegativeButton(R.string.cancel) { dialog: DialogInterface, i: Int -> dialog.cancel() }
        builder.show()
        return false
    }

    override fun onItemSelected(adapterView: AdapterView<*>?, view: View, pos: Int, l: Long) {
        if (pos == 0) return
        dhikrs = viewModel!!.dhikrs.value ?: listOf()
        for (i in dhikrs.indices) {
            val model = dhikrs[i]
            if (i == pos) {
                model.position = 0
            } else if (i < pos) {
                model.position = i + 1
            } else {
                model.position = i
            }
        }
        viewModel.saveDhikr(*dhikrs.toTypedArray())
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {}
    override fun onChanged(dhikrs: List<Dhikr>?) {
        if (dhikrs == null || !createdViews) return

        this.dhikrs = dhikrs
        val itemList = ArrayList<String>()
        for (dhikr in dhikrs) {
            itemList.add(dhikr.title)
        }
        if (dhikrs.isEmpty()) {
            val model = Dhikr()
            model.title = getString(R.string.tasbih)
            model.max = 33
            viewModel.addDhikr(model)
        } else {
            val model = dhikrs[0]
            title.setText(model.title)
            title.setSelection(title.text.length)
            dhikrView.dhikr = model
            dhikrView.invalidate()
        }
        val c: Context = ContextThemeWrapper(activity, R.style.ToolbarTheme)
        val adap: SpinnerAdapter =
            ArrayAdapter(c, android.R.layout.simple_list_item_1, android.R.id.text1, itemList)

        spinner?.let {
            it.adapter = adap
            it.onItemSelectedListener = this
        }
    }
}