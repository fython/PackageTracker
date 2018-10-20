package info.papdt.express.helper.support

import android.content.Context
import android.support.v4.content.LocalBroadcastManager

inline val Context.localBroadcastManager get() = LocalBroadcastManager.getInstance(this)