package info.papdt.express.helper.ui.fragment.settings

import android.app.AlertDialog
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import android.widget.TextView
import com.google.firebase.iid.FirebaseInstanceId
import info.papdt.express.helper.BuildConfig
import info.papdt.express.helper.R
import info.papdt.express.helper.api.PushApi
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.services.FCMService
import info.papdt.express.helper.support.ClipboardUtils
import info.papdt.express.helper.support.PushUtils
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.support.SettingsInstance
import moe.feng.kotlinyan.common.*
import moe.shizuku.preference.EditTextPreference
import moe.shizuku.preference.ListPreference
import moe.shizuku.preference.Preference
import moe.shizuku.preference.SwitchPreference

class SettingsPush : AbsPrefFragment(), Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    // Notification & push preference
    private val mPrefDontDisturb: SwitchPreference by PreferenceProperty("dont_disturb")
    private val mPrefIntervalTime: ListPreference by PreferenceProperty("interval")
    private val mPrefEnable: SwitchPreference by PreferenceProperty("enable_push")
    private val mPrefHttps: SwitchPreference by PreferenceProperty("enable_https")
    private val mPrefApiHost: EditTextPreference by PreferenceProperty("api_host")
    private val mPrefApiPort: EditTextPreference by PreferenceProperty("api_port")
    private val mPrefInstanceId: Preference by PreferenceProperty("firebase_instance_id")
    private val mPrefSync: Preference by PreferenceProperty("push_sync")
    private val mPrefReqPush: Preference by PreferenceProperty("request_push")
    private val mPrefWhatsThis: Preference by PreferenceProperty("push_intro")

    private var needRegister = false
    private var needFreeServer = false

    private val database by lazy { PackageDatabase.getInstance(activity!!) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_push)

        mPrefDontDisturb.isChecked = settings.getBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, true)

        val intervalTarget = settings.getInt(Settings.KEY_NOTIFICATION_INTERVAL, 1)
        if (mPrefIntervalTime.value == null) {
            mPrefIntervalTime.setValueIndex(intervalTarget)
        }

        mPrefApiHost.text = SettingsInstance.pushApiHost
        mPrefApiPort.text = SettingsInstance.pushApiPort.toString()
        mPrefHttps.isChecked = SettingsInstance.enableHttps
        ifSupportSDK(Build.VERSION_CODES.P) {
            mPrefHttps.summary = getString(R.string.pref_enable_https_force_reason)
            mPrefHttps.isEnabled = false
        }

        /** Hide development items */
        mPrefInstanceId.isVisible = BuildConfig.DEBUG
        mPrefReqPush.isVisible = BuildConfig.DEBUG

        // Notification & push
        mPrefDontDisturb.onPreferenceChangeListener = this
        mPrefIntervalTime.onPreferenceChangeListener = this
        mPrefInstanceId.onPreferenceClickListener = this
        mPrefSync.onPreferenceClickListener = this
        mPrefReqPush.onPreferenceClickListener = this
        mPrefEnable.onPreferenceChangeListener = this
        mPrefApiHost.onPreferenceChangeListener = this
        mPrefApiPort.onPreferenceChangeListener = this
        mPrefWhatsThis.onPreferenceClickListener = this
        mPrefHttps.onPreferenceChangeListener = this

        setEnablePush(SettingsInstance.enablePush)
    }

    override fun onStop() {
        super.onStop()
        if (needRegister) {
            ui {
                PushApi.register()
                PushApi.sync(database.getPackageIdList())
            }
        }
    }

    private fun setEnablePush(b: Boolean) {
        if (b) {
            mPrefIntervalTime.setValueIndex(4)
            mPrefIntervalTime.onPreferenceChangeListener.onPreferenceChange(mPrefIntervalTime, "4")
        }
        SettingsInstance.enablePush = b
        mPrefSync.isEnabled = b
        mPrefReqPush.isEnabled = b
        mPrefIntervalTime.isEnabled = !b
        activity?.packageManager?.setComponentEnabledSetting(
                ComponentName(activity!!.applicationContext, FCMService::class.java),
                if (b) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        )
    }

    private fun setFreeApiServer() {
        mPrefApiHost.text = "pt.api.rabi.coffee"
        mPrefApiPort.text = "3001"
        mPrefHttps.isChecked = true
        SettingsInstance.pushApiHost = "pt.api.rabi.coffee"
        SettingsInstance.pushApiPort = 3001
        SettingsInstance.enableHttps = true
    }

    override fun onPreferenceClick(pref: Preference): Boolean {
        return when (pref) {
            // Notification & push
            mPrefInstanceId -> {
                AlertDialog.Builder(activity).apply {
                    titleRes = R.string.pref_firebase_instance_id
                    message = FirebaseInstanceId.getInstance().token ?: "null"
                    okButton()
                    negativeButton(R.string.pref_copy_button) { _, _ ->
                        ClipboardUtils.putString(activity, FirebaseInstanceId.getInstance().token)
                        makeSnackbar(resources.string[R.string.toast_copied_successfully],
                                Snackbar.LENGTH_LONG)?.show()
                    }
                    neutralButton(R.string.pref_register_button) { _, _ ->
                        ui {
                            val res = PushApi.register(FirebaseInstanceId.getInstance().token!!)
                            makeSnackbar(
                                    if (res.code >= 0) "Succeed" else "Failed",
                                    Snackbar.LENGTH_LONG
                            )?.show()
                        }
                    }
                }.create().apply {
                    setOnShowListener {
                        findViewById<TextView>(android.R.id.message).setTextIsSelectable(true)
                    }
                }.show()
                true
            }
            mPrefSync -> {
                if (needRegister) {
                    ui {
                        PushApi.register()
                        val result = PushApi.sync(database.getPackageIdList())
                        makeSnackbar(
                                if (result.code >= 0) "Succeed" else "Failed",
                                Snackbar.LENGTH_LONG
                        )?.show()
                    }
                    needRegister = false
                } else {
                    ui {
                        val result = PushApi.sync(database.getPackageIdList())
                        makeSnackbar(
                                if (result.code >= 0) "Succeed" else "Failed",
                                Snackbar.LENGTH_LONG
                        )?.show()
                    }
                }
                true
            }
            mPrefReqPush -> {
                if (needRegister) {
                    ui {
                        PushApi.register()
                        val result = PushApi.requestPush()
                        makeSnackbar(result.message, Snackbar.LENGTH_LONG)?.show()
                    }
                    needRegister = false
                } else {
                    ui {
                        val result = PushApi.requestPush()
                        makeSnackbar(result.message, Snackbar.LENGTH_LONG)?.show()
                    }
                }
                true
            }
            mPrefWhatsThis -> {
                activity?.buildAlertDialog {
                    titleRes = R.string.fcm_push_intro_title
                    messageRes = R.string.fcm_push_intro_msg
                    okButton()
                    /*neutralButton(R.string.fcm_push_no_server_button) { _, _ ->
                        if (SettingsInstance.clickedDonate) {
                            setFreeApiServer()
                        } else {
                            needFreeServer = true
                            activity.buildAlertDialog {
                                messageRes = R.string.no_server_message
                                okButton()
                            }.show()
                        }
                    }*/
                }?.show()
                true
            }
            else -> false
        }
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any?): Boolean {
        return when (pref) {
            // Notification & push
            mPrefDontDisturb -> {
                val b = newValue as Boolean
                settings.putBoolean(Settings.KEY_NOTIFICATION_DO_NOT_DISTURB, b)
                true
            }
            mPrefIntervalTime -> {
                val value = Integer.parseInt(newValue as String)
                settings.putInt(Settings.KEY_NOTIFICATION_INTERVAL, value)
                activity?.applicationContext?.let(PushUtils::restartServices)
                true
            }
            mPrefEnable -> {
                val b = newValue as Boolean
                setEnablePush(b)
                if (b) needRegister = true
                database.size()
                true
            }
            mPrefHttps -> {
                val b = newValue as Boolean
                SettingsInstance.enableHttps = b
                needRegister = true
                true
            }
            mPrefApiHost -> {
                SettingsInstance.pushApiHost = newValue as String
                needRegister = true
                database.size()
                true
            }
            mPrefApiPort -> {
                SettingsInstance.pushApiPort = (newValue as? String)?.toIntOrNull() ?: 6000
                needRegister = true
                database.size()
                true
            }
            else -> false
        }
    }

}