package info.papdt.express.helper.ui.fragment.settings

import android.os.Bundle
import info.papdt.express.helper.R
import info.papdt.express.helper.support.LauncherIconUtils
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.support.SettingsInstance
import moe.shizuku.preference.ListPreference
import moe.shizuku.preference.Preference
import moe.shizuku.preference.SwitchPreference

class SettingsUi : AbsPrefFragment(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    // User interface preference
    private val mPrefNightMode: ListPreference by PreferenceProperty("night_mode")
    private val mPrefShowTipsAgain: Preference by PreferenceProperty("show_tips_again")
    private val mPrefDarkIcon: SwitchPreference? by NullablePreferenceProperty("dark_launcher_icon")

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_ui)

        val target = settings.getInt(Settings.KEY_NIGHT_MODE, 0)
        if (mPrefNightMode.value == null) {
            mPrefNightMode.setValueIndex(target)
        }

        mPrefDarkIcon?.isChecked = LauncherIconUtils.isDarkLauncherIcon(activity!!)

        // UI
        mPrefNightMode.onPreferenceChangeListener = this
        mPrefShowTipsAgain.onPreferenceClickListener = this
        mPrefDarkIcon?.onPreferenceChangeListener = this
    }

    override fun onPreferenceClick(pref: Preference): Boolean {
        return when (pref) {
            // UI
            mPrefShowTipsAgain -> {
                SettingsInstance.shouldShowTips = true
                makeRestartTips()
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any?): Boolean {
        return when (pref) {
            // UI
            mPrefNightMode -> {
                val value = Integer.parseInt(newValue as String)
                settings.putInt(Settings.KEY_NIGHT_MODE, value)
                makeRestartTips()
                true
            }
            mPrefDarkIcon -> {
                val value = newValue as Boolean
                LauncherIconUtils.setDarkLauncherIcon(activity!!, value)
                true
            }
            else -> false
        }
    }

}