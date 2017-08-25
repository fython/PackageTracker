package info.papdt.express.helper.support

import android.content.Context
import moe.feng.kotlinyan.common.SharedPreferencesProvider
import kotlin.properties.Delegates

class PTSettings(context: Context): SharedPreferencesProvider(context, "settings") {

	var pushApiHost by stringValue(defValue = "192.168.1.108")
	var pushApiPort by intValue(defValue = 3000)

}

var SettingsInstance: PTSettings by Delegates.notNull()