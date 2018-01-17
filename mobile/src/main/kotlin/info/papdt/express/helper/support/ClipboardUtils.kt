package info.papdt.express.helper.support

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

object ClipboardUtils {

    fun putString(context: Context?, string: String?) {
        (context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
                ?.primaryClip = string?.let { ClipData.newPlainText("text", it) }
    }

}
