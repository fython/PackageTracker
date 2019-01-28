package info.papdt.express.helper.ui.fragment.settings

import android.os.Bundle
import info.papdt.express.helper.R
import info.papdt.express.helper.support.SettingsInstance
import moe.shizuku.preference.ListPreference
import moe.shizuku.preference.Preference

class SettingsApi : AbsPrefFragment(), Preference.OnPreferenceChangeListener {

    // Query api settings
    private val mPrefApiType: ListPreference by PreferenceProperty("api_type")

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_api)

        mPrefApiType.value = SettingsInstance.packageApiTypeInt.toString()

        // Query api type
        mPrefApiType.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any?): Boolean {
        return when (pref) {
            // Query Api
            mPrefApiType -> {
                SettingsInstance.packageApiTypeInt = (newValue as String).toInt()
                true
            }
            else -> false
        }
    }

}