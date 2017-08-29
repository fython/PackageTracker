package info.papdt.express.helper.support

import android.content.Context
import moe.feng.kotlinyan.common.SharedPreferencesProvider
import kotlin.properties.Delegates

class PTSettings(context: Context): SharedPreferencesProvider(context, "settings") {

	var enablePush by booleanValue(defValue = false)
	var pushApiHost by stringValue(defValue = "pt.api.rabi.coffee")
	var pushApiPort by intValue(defValue = 3000)

	var shouldShowTips by booleanValue(defValue = true)

}

var SettingsInstance: PTSettings by Delegates.notNull()