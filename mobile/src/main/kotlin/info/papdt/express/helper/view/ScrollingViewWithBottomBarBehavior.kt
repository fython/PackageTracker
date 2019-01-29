package info.papdt.express.helper.view

import android.content.Context
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import info.papdt.express.helper.R

class ScrollingViewWithBottomBarBehavior(context: Context, attrs: AttributeSet?)
    : AppBarLayout.ScrollingViewBehavior(context, attrs) {

    private var bottomMargin = 0

    private fun isBottomBar(view: View): Boolean {
        return view.id == R.id.bottom_bar && view is LinearLayout
    }

    override fun layoutDependsOn(parent: CoordinatorLayout,
                                 child: View,
                                 dependency: View): Boolean {
        return super.layoutDependsOn(parent, child, dependency) || isBottomBar(dependency)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout,
                                        child: View,
                                        dependency: View): Boolean {
        val result = super.onDependentViewChanged(parent, child, dependency)

        if (isBottomBar(dependency) && dependency.height != bottomMargin) {
            bottomMargin = dependency.height
            var targetChild = child
            if (child is SmartRefreshLayout) {
                for (index in 0 until child.childCount) {
                    val view = child.getChildAt(index)
                    if (view is RecyclerView) {
                        targetChild = view
                        break
                    }
                }
            }
            val layout = targetChild.layoutParams as ViewGroup.MarginLayoutParams
            layout.bottomMargin = bottomMargin
            targetChild.requestLayout()
            return true
        } else {
            return result
        }
    }

}