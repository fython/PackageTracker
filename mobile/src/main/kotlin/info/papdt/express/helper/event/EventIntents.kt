package info.papdt.express.helper.event

import android.content.Intent
import info.papdt.express.helper.ACTION_REQUEST_DELETE_PACK
import info.papdt.express.helper.EXTRA_DATA
import info.papdt.express.helper.model.Kuaidi100Package

object EventIntents {

    fun requestDeletePackage(data: Kuaidi100Package): Intent {
        val intent = Intent(ACTION_REQUEST_DELETE_PACK)
        intent.putExtra(EXTRA_DATA, data)
        return intent
    }

}