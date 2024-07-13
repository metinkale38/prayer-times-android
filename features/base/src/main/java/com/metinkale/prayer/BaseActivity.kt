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
package com.metinkale.prayer

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.Intent.ShortcutIconResource
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.metinkale.prayer.base.BuildConfig
import com.metinkale.prayer.base.R
import com.metinkale.prayer.receiver.AppEventManager
import com.metinkale.prayer.utils.LocaleUtils
import com.metinkale.prayer.utils.PermissionUtils

open class BaseActivity(
    private val titleRes: Int,
    private val iconRes: Int,
    var defaultFragment: Fragment
) : AppCompatActivity(), FragmentManager.OnBackStackChangedListener, OnItemClickListener {
    private var navPos = 0
    private lateinit var nav: ListView
    private lateinit var drawerLayout: DrawerLayout

    //private long mStartTime;
    //private Fragment mFragment;
    private lateinit var toolbar: Toolbar
    private lateinit var progressDialog: View

    /*@Override
    public void onBackPressed() {
        if (!(mFragment instanceof MainFragment) || !((MainFragment) mFragment).onBackPressed())
            if (getSupportFragmentManager().getBackStackEntryCount() > 0
                    || !AppRatingDialog.showDialog(this, System.currentTimeMillis() - mStartTime))
                super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mStartTime = System.currentTimeMillis();
    }*/
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleUtils.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocaleUtils.init(this)
        //AppRatingDialog.increaseAppStarts();
        if (Preferences.SHOW_INTRO || Preferences.CHANGELOG_VERSION < BuildConfig.CHANGELOG_VERSION) {
            Module.INTRO.launch(this)
        }
        super.setContentView(R.layout.activity_base)
        toolbar = findViewById(R.id.toolbar)
        progressDialog = findViewById(R.id.progressDlg)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundResource(R.color.colorPrimary)
        toolbar.setNavigationIcon(R.drawable.ic_action_menu)
        drawerLayout = findViewById(R.id.drawer)
        nav = drawerLayout.findViewById(R.id.base_nav)
        val header = LayoutInflater.from(this).inflate(R.layout.drawer_header, nav, false)
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            (header.findViewById<View>(R.id.version) as TextView).text = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        nav.addHeaderView(header)
        val list = buildNavAdapter(this)
        nav.adapter = list
        nav.onItemClickListener = this
        nav.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    if (nav.height < (nav.parent as View).height) {
                        val diff = (nav.parent as View).height - nav.height
                        nav.dividerHeight = nav.dividerHeight + diff / nav.adapter.count + 1
                    } else {
                        nav.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                }
            })
        drawerLayout.post {
            toolbar.setTitle(titleRes)
        }
        supportFragmentManager.addOnBackStackChangedListener(this)
        if (savedInstanceState != null) navPos = savedInstanceState.getInt("navPos", 0)
        val comp = intent.component?.className
        for (i in Module.values().indices) {
            if (comp?.contains(Module.values()[i].getKey()) == true) {
                navPos = i
            }
        }
        if (Intent.ACTION_CREATE_SHORTCUT == intent.action) {
            val icon = ShortcutIconResource.fromContext(this, iconRes)
            val intent = Intent()
            val launchIntent = Intent(this, BaseActivity::class.java)
            launchIntent.component = getIntent().component
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            launchIntent.putExtra("duplicate", false)
            intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, launchIntent)
            intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(titleRes))
            intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon)
            setResult(RESULT_OK, intent)
            finish()
        }
        moveToFrag(defaultFragment)

        AppEventManager.sendOnStart()
    }

    fun setProgressDialogVisible(visible: Boolean) {
        progressDialog.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("navPos", navPos)
    }

    fun moveToFrag(frag: Fragment) {
        //mFragment = frag;
        val transaction = supportFragmentManager.beginTransaction().replace(R.id.basecontent, frag)
        if (frag !== defaultFragment) transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun buildNavAdapter(c: Context): ArrayAdapter<Module> {
        return object : ArrayAdapter<Module>(c, 0, Module.values()) {
            override fun getView(pos: Int, nullableView: View?, p: ViewGroup): View {
                val v: View = nullableView ?: LayoutInflater.from(c)
                    .inflate(R.layout.drawer_list_item, p, false)
                val item = getItem(pos)
                if (item!!.getIconRes() == 0 && item.getTitleRes() == 0) {
                    v.visibility = View.GONE
                    return v
                }
                v.visibility = View.VISIBLE
                (v as? TextView)?.setText(item.getTitleRes())
                if (pos == navPos) {
                    (v as? TextView?)?.setTypeface(null, Typeface.BOLD)
                } else (v as? TextView)?.setTypeface(null, Typeface.NORMAL)
                val icon = AppCompatResources.getDrawable(c, item.getIconRes())
                icon?.mutate()?.setTint(resources.getColor(R.color.foreground, null))
                if (c.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL) {
                    (v as? TextView)?.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        icon,
                        null
                    )
                } else {
                    (v as? TextView)?.setCompoundDrawablesWithIntrinsicBounds(
                        icon,
                        null,
                        null,
                        null
                    )
                }
                return v
            }
        }
    }


    override fun onResume() {
        super.onResume()
        nav.setSelection(navPos)
    }

    @SuppressLint("RtlHardcoded")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val fm = supportFragmentManager
            if (fm.backStackEntryCount > 0) {
                onBackPressedDispatcher.onBackPressed()
            } else {
                drawerLayout.openDrawer(if (isRTL) Gravity.RIGHT else Gravity.LEFT)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private val isRTL: Boolean
        get() {
            val config = resources.configuration
            return config.layoutDirection == View.LAYOUT_DIRECTION_RTL
        }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        PermissionUtils.get(this).onRequestPermissionResult(permissions, grantResults)
    }

    override fun onBackStackChanged() {
        val fm = supportFragmentManager
        if (fm.backStackEntryCount > 0) {
            toolbar.setNavigationIcon(R.drawable.ic_action_chevron_left)
        } else {
            toolbar.setNavigationIcon(R.drawable.ic_action_menu)
        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, pos: Int, id: Long) {
        @Suppress("NAME_SHADOWING") var pos = pos
        if (pos == 0) return
        pos-- // header
        if (pos == navPos && drawerLayout.isDrawerOpen(nav)) {
            drawerLayout.closeDrawers()
            return
        }
        Module.values()[pos].launch(this)
        drawerLayout.closeDrawers()
        //AppRatingDialog.addToOpenedMenus(ACTS[pos]);
    }

    open class MainFragment : Fragment() {
        init {
            @Suppress("DEPRECATION")
            setHasOptionsMenu(true)
        }

        open fun onBackPressed(): Boolean {
            return false
        }

        val baseActivity: BaseActivity?
            get() = activity as? BaseActivity

        fun backToMain() {
            val fm = requireActivity().supportFragmentManager
            val c = fm.backStackEntryCount
            for (i in 0 until c) {
                fm.popBackStack()
            }
        }

        fun back(): Boolean {
            val fm = requireActivity().supportFragmentManager
            if (fm.backStackEntryCount > 0) {
                fm.popBackStack()
                return true
            }
            return false
        }

        fun moveToFrag(frag: Fragment) {
            baseActivity?.moveToFrag(frag)
        }
    }
}