package info.papdt.express.helper.ui.fragment.settings

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import info.papdt.express.helper.R
import info.papdt.express.helper.receiver.ProcessTextReceiver
import info.papdt.express.helper.services.ClipboardDetectService
import info.papdt.express.helper.support.Settings
import moe.shizuku.preference.Preference
import moe.shizuku.preference.SwitchPreference

class SettingsAssistant : AbsPrefFragment(), Preference.OnPreferenceChangeListener {

    // Auto detect
    private val mPrefFromClipboard: SwitchPreference by PreferenceProperty("from_clipboard")
    private val mPrefSelectionAction: SwitchPreference by PreferenceProperty("selection_action")

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_assistant)

        mPrefFromClipboard.isChecked = settings.isClipboardDetectServiceEnabled()
        mPrefSelectionAction.isChecked = context?.packageManager
                ?.getComponentEnabledSetting(
                        ComponentName(context!!, ProcessTextReceiver::class.java.name)
                ) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED

        // Auto detect
        mPrefFromClipboard.onPreferenceChangeListener = this
        mPrefSelectionAction.onPreferenceChangeListener = this
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any?): Boolean {
        return when (pref) {
            // Auto detect
            mPrefFromClipboard -> {
                val isOpen = newValue as Boolean
                if (isOpen && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!android.provider.Settings.canDrawOverlays(activity)) {
                        val intent = Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + activity!!.packageName)
                        )
                        startActivity(intent)
                        return false
                    }
                }
                settings.putBoolean(Settings.KEY_DETECT_FROM_CLIPBOARD, isOpen)
                val intent = Intent(activity?.applicationContext, ClipboardDetectService::class.java)
                intent.run(if (!isOpen) activity!!::stopService else activity!!::startService)
                return true
            }
            mPrefSelectionAction -> {
                return context?.packageManager?.setComponentEnabledSetting(
                        ComponentName(context!!, ProcessTextReceiver::class.java.name),
                        if (newValue as Boolean) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                ) != null
            }
            else -> false
        }
    }

}