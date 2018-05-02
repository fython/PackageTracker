package info.papdt.express.helper.support

import android.content.Context
import android.support.annotation.IntDef
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

    @ApiType var packageApiType by intValue(defValue = PackageApiType.KUAIDI100)

	var usingNewDatabase by booleanValue(defValue = false)

}


@IntDef(PackageApiType.KUAIDI100, PackageApiType.BAIDU)
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class ApiType

object PackageApiType {
    const val KUAIDI100 = 0
    const val BAIDU = 1
}

var SettingsInstance: PTSettings by Delegates.notNull()

var isFontProviderEnabled: Boolean = false