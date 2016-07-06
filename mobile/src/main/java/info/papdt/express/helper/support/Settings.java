package info.papdt.express.helper.support;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

	public static final String XML_NAME = "settings";
	public static final String KEY_NOTIFICATION_SOUND = "noti_sound",
			KEY_NOTIFICATION_VIBRATE = "noti_vibrate",
			KEY_NOTIFICATION_INTERVAL = "noti_interval",
			KEY_NOTIFICATION_DO_NOT_DISTURB = "noti_do_not_disturb";

	public static final String KEY_NAVIGATION_TINT = "navi_tint";

	private static Settings sInstance;

	private SharedPreferences mPrefs;

	public static Settings getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new Settings(context);
		}
		return sInstance;
	}

	private Settings(Context context) {
		mPrefs = context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE);
	}

	public Settings putBoolean(String key, boolean value) {
		mPrefs.edit().putBoolean(key, value).commit();
		return this;
	}

	public boolean getBoolean(String key, boolean def) {
		return mPrefs.getBoolean(key, def);
	}

	public Settings putInt(String key, int value) {
		mPrefs.edit().putInt(key, value).commit();
		return this;
	}

	public int getInt(String key, int defValue) {
		return mPrefs.getInt(key, defValue);
	}


	public Settings putString(String key, String value) {
		mPrefs.edit().putString(key, value).commit();
		return this;
	}

	public String getString(String key, String defValue) {
		return mPrefs.getString(key, defValue);
	}

}