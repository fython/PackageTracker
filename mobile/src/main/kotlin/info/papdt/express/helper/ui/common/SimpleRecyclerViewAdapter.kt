package info.papdt.express.helper.ui.common

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View

import java.util.ArrayList

abstract class SimpleRecyclerViewAdapter(protected var mRecyclerView: RecyclerView)
	: RecyclerView.Adapter<SimpleRecyclerViewAdapter.ClickableViewHolder>() {

	var context: Context? = null
		private set
	protected var mListeners: MutableList<RecyclerView.OnScrollListener> = ArrayList()

	init {
		this.mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
			override fun onScrollStateChanged(rv: RecyclerView, newState: Int) {
				for (listener in mListeners) {
					listener.onScrollStateChanged(rv, newState)
				}
			}

			override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
				for (listener in mListeners) {
					listener.onScrolled(rv, dx, dy)
				}
			}
		})
	}

	fun addOnScrollListener(listener: RecyclerView.OnScrollListener) {
		mListeners.add(listener)
	}

	interface OnItemClickListener {
		fun onItemClick(position: Int, holder: ClickableViewHolder)
	}

	interface OnItemLongClickListener {
		fun onItemLongClick(position: Int, holder: ClickableViewHolder): Boolean
	}

	private var itemClickListener: OnItemClickListener? = null
	private var itemLongClickListener: OnItemLongClickListener? = null

	fun setOnItemClickListener(listener: OnItemClickListener) {
		this.itemClickListener = listener
	}

	fun setOnItemLongClickListener(listener: OnItemLongClickListener) {
		this.itemLongClickListener = listener
	}

	fun bindContext(context: Context) {
		this.context = context
	}

	override fun onBindViewHolder(holder: ClickableViewHolder, position: Int) {
		holder.parentView.setOnClickListener {
			if (itemClickListener != null) {
				itemClickListener!!.onItemClick(position, holder)
			}
		}
		holder.parentView.setOnLongClickListener {
			if (itemLongClickListener != null) {
				itemLongClickListener!!.onItemLongClick(position, holder)
			} else {
				false
			}
		}
	}

	open inner class ClickableViewHolder(val parentView: View) : RecyclerView.ViewHolder(parentView)

}