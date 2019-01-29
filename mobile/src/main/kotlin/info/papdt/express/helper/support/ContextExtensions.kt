package info.papdt.express.helper.support

import android.content.Context
import androidx.localbroadcastmanager.content.LocalBroadcastManager

inline val Context.localBroadcastManager get() = LocalBroadcastManager.getInstance(this)