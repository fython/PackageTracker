package info.papdt.express.helper.ui

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Spinner
import info.papdt.express.helper.R
import info.papdt.express.helper.dao.PackageDatabase
import info.papdt.express.helper.ui.adapter.HomeToolbarSpinnerAdapter
import info.papdt.express.helper.ui.adapter.NewHomePackageListAdapter
import info.papdt.express.helper.ui.common.AbsActivity

class HomeActivity : AbsActivity() {

    private val coordinatorLayout by lazy<CoordinatorLayout> { findViewById(R.id.coordinator_layout) }
    private val appBarLayout by lazy<AppBarLayout> { findViewById(R.id.app_bar_layout) }
    private val listView by lazy<RecyclerView> { findViewById(android.R.id.list) }
    private val spinner by lazy<Spinner> { mToolbar!!.findViewById(R.id.spinner) }
    private val addButton by lazy<View> { findViewById(R.id.add_button) }

    private val listAdapter by lazy {
        NewHomePackageListAdapter()
    }

    private val homeListScrollListener = HomeListScrollListener()

    private val packageDatabase by lazy { PackageDatabase.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
    }

    override fun setUpViews() {
        listView.addOnScrollListener(homeListScrollListener)

        listView.adapter = listAdapter
        listAdapter.setPackages(packageDatabase.data)

        spinner.adapter = HomeToolbarSpinnerAdapter(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_new_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_search -> {

            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private inner class HomeListScrollListener : RecyclerView.OnScrollListener() {

        private val statedElevation by lazy {
            mapOf(true to resources.getDimension(R.dimen.app_bar_elevation), false to 0f)
        }

        private val duration by lazy {
            resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        }

        private var elevationAnimator: Animator? = null
        private var animatorDirection: Boolean? = null

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val layoutManager = recyclerView.layoutManager!! as LinearLayoutManager
            val shouldLift = layoutManager.findFirstCompletelyVisibleItemPosition() != 0

            if (animatorDirection != shouldLift) {
                if (elevationAnimator?.isRunning == true) {
                    elevationAnimator?.cancel()
                }
            }
            animatorDirection = shouldLift
            if (elevationAnimator?.isRunning != true
                    && appBarLayout.elevation != statedElevation[shouldLift]!!) {
                elevationAnimator = ObjectAnimator.ofFloat(
                        appBarLayout,
                        "elevation",
                        statedElevation[!shouldLift]!!,
                        statedElevation[shouldLift]!!
                )
                elevationAnimator!!.duration = duration
                elevationAnimator!!.start()
            }
        }

    }

}