package info.papdt.express.helper.support

import android.content.Context
import android.content.SharedPreferences
import android.os.Build

class Settings private constructor(context: Context) {

    private val mPrefs: SharedPreferences =
            context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE)

    fun putBoolean(key: String, value: Boolean): Settings {
        mPrefs.edit().putBoolean(key, value).apply()
        return this
    }

    fun getBoolean(key: String, def: Boolean): Boolean {
        return mPrefs.getBoolean(key, def)
    }

    fun putInt(key: String, value: Int): Settings {
        mPrefs.edit().putInt(key, value).apply()
        return this
    }

    fun getInt(key: String, defValue: Int): Int {
        return mPrefs.getInt(key, defValue)
    }


    fun putString(key: String, value: String): Settings {
        mPrefs.edit().putString(key, value).apply()
        return this
    }

    fun getString(key: String, defValue: String): String? {
        return mPrefs.getString(key, defValue)
    }

    fun isClipboardDetectServiceEnabled(): Boolean {
        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
                && mPrefs.getBoolean(Settings.KEY_DETECT_FROM_CLIPBOARD, false)
    }

    companion object {

        const val XML_NAME = "settings"
        const val KEY_NOTIFICATION_SOUND = "noti_sound"
        const val KEY_NOTIFICATION_VIBRATE = "noti_vibrate"
        const val KEY_NOTIFICATION_INTERVAL = "noti_interval"
        const val KEY_NOTIFICATION_DO_NOT_DISTURB = "noti_do_not_disturb"
        const val KEY_NIGHT_MODE = "night_mode"
        const val KEY_DETECT_FROM_CLIPBOARD = "from_clipboard"
        const val KEY_FIREBASE_INSTANCE_ID = "firebase_instance_id"

        const val KEY_NAVIGATION_TINT = "navi_tint"

        const val KEY_FIRST_RUN = "first_run"

        private var sInstance: Settings? = null

        fun getInstance(context: Context): Settings {
            if (sInstance == null) {
                sInstance = Settings(context)
            }
            return sInstance ?: getInstance(context)
        }
    }

}