package info.papdt.express.helper.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import info.papdt.express.helper.R
import info.papdt.express.helper.support.ResourcesUtils
import info.papdt.express.helper.ui.common.AbsActivity
import info.papdt.express.helper.view.SwipeBackCoordinatorLayout
import moe.feng.kotlinyan.common.lazyFindNonNullView
import org.jetbrains.anko.withAlpha

class ManageCategoriesActivity : AbsActivity(), SwipeBackCoordinatorLayout.OnSwipeListener {

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, ManageCategoriesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            context.startActivity(intent)
        }

    }

    private val listView: RecyclerView by lazyFindNonNullView(android.R.id.list)
    private val rootLayout: View by lazyFindNonNullView(R.id.root_layout)

    private val rootViewBgColor: Int by lazy {
        ResourcesUtils.getColorIntFromAttr(theme, R.attr.rootViewBackgroundColor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)
    }

    override fun setUpViews() {
        findViewById<SwipeBackCoordinatorLayout>(R.id.swipe_back_coordinator_layout)
                .setOnSwipeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_manage_categories, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_new_category -> {

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun canSwipeBack(dir: Int): Boolean {
        return SwipeBackCoordinatorLayout.canSwipeBack(listView, SwipeBackCoordinatorLayout.DOWN_DIR)
    }

    override fun onSwipeProcess(percent: Float) {
        rootLayout.setBackgroundColor(
                rootViewBgColor.withAlpha(
                        (SwipeBackCoordinatorLayout.getBackgroundAlpha(percent) * 255)
                                .toInt()
                )
        )
    }

    override fun onSwipeFinish(dir: Int) {
        window.statusBarColor = Color.TRANSPARENT
        finish()
    }

}