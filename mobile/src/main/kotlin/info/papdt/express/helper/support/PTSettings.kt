package info.papdt.express.helper.support

import android.content.Context
import android.os.Build
import androidx.annotation.IntDef
import info.papdt.express.helper.ui.adapter.HomePackageListAdapter
import moe.feng.kotlinyan.common.SharedPreferencesProvider
import kotlin.properties.Delegates

class PTSettings(context: Context): SharedPreferencesProvider(context, "settings") {

	var firstRun by booleanValue(defValue = true)

	var enablePush by booleanValue(defValue = false)
	private var _enableHttps by booleanValue(key = "enableHttps", defValue = false)
	var enableHttps: Boolean
		get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || _enableHttps
		set(value) {
			_enableHttps = value
		}
	var pushApiHost by stringValue()
	var pushApiPort by intValue(defValue = 3000)

	var shouldShowTips by booleanValue(defValue = true)
	var clickedDonate by booleanValue(defValue = false)

    @ApiType
	var packageApiTypeInt by intValue(defValue = PackageApiType.KUAIDI100)

	var usingNewDatabase by booleanValue(defValue = false)

    var lastFilter by intValue(defValue = HomePackageListAdapter.FILTER_ON_THE_WAY)

    var lastSortBy by intValue(defValue = HomePackageListAdapter.SORT_BY_UPDATE_TIME)

    var forceUpdateAllPackages by booleanValue(defValue = false)

	var firstIntroCategory by booleanValue(defValue = true)

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