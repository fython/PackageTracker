-ignorewarn

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.** {*;}
-optimizations !code/simplification/arithmetic
-optimizationpasses 5

-keepattributes *Annotation*
-keepattributes Signature

# Gson
-keep class sun.misc.Unsafe { *; }

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.view.View {*;}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}
-keepclassmembers class **.R$* {
    public static <fields>;
}
-keep class info.papdt.express.helper.ui.** {*;}
-keep class info.papdt.express.helper.view.** {*;}
-keep class info.papdt.express.helper.widget.** {*;}
-keep class info.papdt.express.helper.model.** {*;}
-keep class info.papdt.express.helper.dao.** {*;}
-keep class info.papdt.express.helper.asynctask.** {*;}
-keep class info.papdt.express.helper.api.** {*;}
-keep class info.papdt.express.helper.receiver.** {*;}
-keep class info.papdt.express.helper.services.** {*;}
-keep class android.support.** {*;}
-keep class me.dm7.** {*;}
-keep class com.roughlike.** {*;}
-keep class com.rengwuxian.** {*;}
-keep class okio.** {*;}
-keep class com.google.** {*;}