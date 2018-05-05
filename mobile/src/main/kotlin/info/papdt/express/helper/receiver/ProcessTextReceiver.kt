package info.papdt.express.helper.receiver

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.text.TextUtils
import android.widget.Toast
import info.papdt.express.helper.R
import info.papdt.express.helper.services.ClipboardDetectService
import info.papdt.express.helper.ui.MainActivity
import moe.feng.kotlinyan.common.get

/**
 * 文本处理接收器
 *
 * 受限于接口，需要用 Activity 代替 BroadcastReceiver 来接收事件。
 */
@RequiresApi(Build.VERSION_CODES.M)
class ProcessTextReceiver : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receivedText = intent[Intent.EXTRA_PROCESS_TEXT]?.asString()

        val lastNumber = receivedText?.let(ClipboardDetectService.Companion::getPackageNumber)

        if (TextUtils.isEmpty(lastNumber)) {
            Toast.makeText(this, R.string.toast_process_text_failed, Toast.LENGTH_LONG).show()
        } else {
            MainActivity.search(this, lastNumber)
        }

        finish()
    }

}