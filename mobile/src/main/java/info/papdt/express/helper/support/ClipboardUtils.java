package info.papdt.express.helper.support;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

public class ClipboardUtils {

	public static void putString(Context context, String string) {
		ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
		manager.setPrimaryClip(ClipData.newPlainText("text" , string));
	}

}
