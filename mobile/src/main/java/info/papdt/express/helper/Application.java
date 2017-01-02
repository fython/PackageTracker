package info.papdt.express.helper;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tencent.bugly.crashreport.CrashReport;

public class Application extends android.app.Application {

	@Override
	public void onCreate() {
		super.onCreate();

		/** Init CrashReport */
		CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(getApplicationContext());
		strategy.setAppPackageName(getPackageName());

		String versionName = null;
		int versionCode = 0;
		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			versionName = packageInfo.versionName;
			versionCode = packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		strategy.setAppVersion(versionName + "(" + versionCode + ")");
		CrashReport.initCrashReport(getApplicationContext(), Constants.BUGLY_APP_ID, Constants.BUGLY_ENABLE_DEBUG, strategy);
	}

}
