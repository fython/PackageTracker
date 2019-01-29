package info.papdt.express.helper.ui.fragment.settings

import android.os.Bundle
import info.papdt.express.helper.R
import info.papdt.express.helper.support.SettingsInstance
import moe.shizuku.preference.CheckBoxPreference
import moe.shizuku.preference.ListPreference
import moe.shizuku.preference.Preference

class SettingsApi : AbsPrefFragment(), Preference.OnPreferenceChangeListener {

    // Query api settings
    private val mPrefApiType: ListPreference by PreferenceProperty("api_type")
    private val mPrefForceUpdate: CheckBoxPreference by PreferenceProperty("force_update_all_packages")

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_api)

        mPrefApiType.value = SettingsInstance.packageApiTypeInt.toString()
        mPrefForceUpdate.isChecked = SettingsInstance.forceUpdateAllPackages

        // Query api type
        mPrefApiType.onPreferenceChangeListener = this
        mPrefForceUpdate.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any?): Boolean {
        return when (pref) {
            // Query Api
            mPrefApiType -> {
                SettingsInstance.packageApiTypeInt = (newValue as String).toInt()
                true
            }
            mPrefForceUpdate -> {
                SettingsInstance.forceUpdateAllPackages = newValue as Boolean
                true
            }
            else -> false
        }
    }

}