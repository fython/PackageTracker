package info.papdt.express.helper.ui

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.app.ActionBar
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import info.papdt.express.helper.R
import info.papdt.express.helper.support.ResourcesUtils
import info.papdt.express.helper.support.Settings
import info.papdt.express.helper.ui.adapter.MaterialIconsGridAdapter
import info.papdt.express.helper.ui.common.AbsActivity
import moe.feng.kotlinyan.common.*

class ChooseIconActivity : AbsActivity() {

    private val mList: RecyclerView by lazyFindNonNullView(R.id.recycler_view)
    private val mSearchEdit: AppCompatEditText by lazyFindNonNullView(R.id.search_edit)
    private val rootLayout: View by lazyFindNonNullView(R.id.root_layout)

    private val mAdapter: MaterialIconsGridAdapter = MaterialIconsGridAdapter()

    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !isNightMode) {
                window.navigationBarColor = resources.color[R.color.lollipop_status_bar_grey]
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                if (!isNightMode) {
                    window.navigationBarColor = Color.WHITE
                    window.navigationBarDividerColor = Color.argb(30, 0, 0, 0)
                } else {
                    window.navigationBarColor = ResourcesUtils.getColorIntFromAttr(theme, android.R.attr.windowBackground)
                    window.navigationBarDividerColor = Color.argb(60, 255, 255, 255)
                }
            }
        }

        setContentView(R.layout.activity_icon_choose)

        if (savedInstanceState == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            rootLayout.makeInvisible()

            val viewTreeObserver = rootLayout.viewTreeObserver
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        Handler().postDelayed({
                            ifSupportSDK (Build.VERSION_CODES.LOLLIPOP) {
                                overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move)
                                var flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                if (!isNightMode) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        flag = flag or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && settings.getBoolean(Settings.KEY_NAVIGATION_TINT, true) && !isNightMode) {
                                        flag = flag or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                                    }
                                }
                                window.decorView.systemUiVisibility = flag
                                window.statusBarColor = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                    Color.TRANSPARENT else resources.color[R.color.lollipop_status_bar_grey]
                            }
                            circularRevealActivity()
                        }, 100)
                        rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                })
            }
        }
    }

    override fun setUpViews() {
        mActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM

        findViewById<View>(R.id.action_back).setOnClickListener { onBackPressed() }
        mSearchEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                mAdapter.update(charSequence.toString())
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

        /** Set up company list  */
        mList.setHasFixedSize(true)
        mList.layoutManager = GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false)

        mAdapter.callback = {
            setResult(RESULT_OK, Intent().apply { putExtra(EXTRA_RESULT_ICON_CODE, it) })
            finish()
        }
        mList.adapter = mAdapter
        mAdapter.update()
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdapter.destroy()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun circularRevealActivity() {
        val cx = (rootLayout.width / 2)
        val cy = (rootLayout.height / 2)

        val finalRadius = Math.max(rootLayout.width, rootLayout.height).toFloat()

        // create the animator for this view (the start radius is zero)
        val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, 0f, finalRadius)
        circularReveal.duration = 300
        circularReveal.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {}
            override fun onAnimationEnd(p0: Animator?) { mSearchEdit.showKeyboard() }
            override fun onAnimationCancel(p0: Animator?) {}
            override fun onAnimationStart(p0: Animator?) {}
        })

        // make the view visible and start the animation
        rootLayout.makeVisible()
        circularReveal.start()
    }

    override fun onBackPressed() {
        val cx = (rootLayout.width / 2)
        val cy = (rootLayout.height / 2)

        val finalRadius = Math.max(rootLayout.width, rootLayout.height).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, cy, finalRadius, 0f)

        circularReveal.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {}
            override fun onAnimationCancel(animator: Animator) {}
            override fun onAnimationRepeat(animator: Animator) {}
            override fun onAnimationEnd(animator: Animator) { rootLayout.makeInvisible(); finish() }
        })
        circularReveal.duration = 400
        circularReveal.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_search, menu)
        menu.tintItemsColor(resources.color[R.color.black_in_light])
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_clear) {
            mSearchEdit.setText("")
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {

        const val EXTRA_RESULT_ICON_CODE = "result_icon_code"

    }

}