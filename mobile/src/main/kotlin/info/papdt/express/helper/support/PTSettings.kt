package info.papdt.express.helper.support

import android.content.Context
import moe.feng.kotlinyan.common.SharedPreferencesProvider
import kotlin.properties.Delegates

class PTSettings(context: Context): SharedPreferencesProvider(context, "settings") {

	var firstRun by booleanValue(defValue = true)

	var enablePush by booleanValue(defValue = false)
	var enableHttps by booleanValue(defValue = false)
	var pushApiHost by stringValue()
	var pushApiPort by intValue(defValue = 3000)

	var shouldShowTips by booleanValue(defValue = true)
	var clickedDonate by booleanValue(defValue = false)

	var usingNewDatabase by booleanValue(defValue = false)

}

var SettingsInstance: PTSettings by Delegates.notNull()

var isFontProviderEnabled: Boolean = false