package info.papdt.express.helper.support

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object LauncherIconUtils {

	private const val DEFAULT_ACTIVITY = "info.papdt.express.helper.EntryActivity"
	private const val DARK_ICON_ACTIVITY = "$DEFAULT_ACTIVITY.DarkLogo"

	fun isDarkLauncherIcon(context: Context): Boolean {
		return context.packageManager.getComponentEnabledSetting(ComponentName(context, DARK_ICON_ACTIVITY)) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
	}

	fun setDarkLauncherIcon(context: Context, enabled: Boolean) {
		val pm = context.packageManager
		pm.setComponentEnabledSetting(ComponentName(context, DEFAULT_ACTIVITY), !enabled)
		pm.setComponentEnabledSetting(ComponentName(context, DARK_ICON_ACTIVITY), enabled)
	}

	private fun PackageManager.setComponentEnabledSetting(componentName: ComponentName, enabled: Boolean) {
		setComponentEnabledSetting(
				componentName,
				if (enabled) {
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED
				} else {
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED
				},
				PackageManager.DONT_KILL_APP
		)
	}

}