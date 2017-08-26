package info.papdt.express.helper.view

import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController

class AnimatedRecyclerView : RecyclerView {

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

	override fun setLayoutManager(layoutManager: RecyclerView.LayoutManager) {
		if (layoutManager is LinearLayoutManager) {
			super.setLayoutManager(layoutManager)
		} else {
			throw ClassCastException("You should only use a LinearLayoutManager with this RecyclerView.")
		}
	}

	override fun attachLayoutAnimationParameters(child: View, params: ViewGroup.LayoutParams, index: Int, count: Int) {
		if (adapter != null && layoutManager is LinearLayoutManager) {
			var animationParameters: LayoutAnimationController.AnimationParameters? = params.layoutAnimationParameters
			if (animationParameters == null) {
				animationParameters = LayoutAnimationController.AnimationParameters()
				params.layoutAnimationParameters = animationParameters
			}
			animationParameters.index = index
			animationParameters.count = count
		} else {
			super.attachLayoutAnimationParameters(child, params, index, count)
		}
	}

}
