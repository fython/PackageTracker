package info.papdt.express.helper.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;

public class AnimatedRecyclerView extends RecyclerView {

	public AnimatedRecyclerView(Context context) {
		super(context);
	}

	public AnimatedRecyclerView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public AnimatedRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setLayoutManager(LayoutManager layoutManager) {
		if (layoutManager instanceof LinearLayoutManager) {
			super.setLayoutManager(layoutManager);
		} else {
			throw new ClassCastException("You should only use a LinearLayoutManager with this RecyclerView.");
		}
	}

	@Override
	protected void attachLayoutAnimationParameters(View child, ViewGroup.LayoutParams params, int index, int count) {
		if (getAdapter() != null && getLayoutManager() instanceof LinearLayoutManager) {
			LayoutAnimationController.AnimationParameters animationParameters = params.layoutAnimationParameters;
			if (animationParameters == null) {
				animationParameters = new LayoutAnimationController.AnimationParameters();
				params.layoutAnimationParameters = animationParameters;
			}
			animationParameters.index = index;
			animationParameters.count = count;
		} else {
			super.attachLayoutAnimationParameters(child, params, index, count);
		}
	}

}
