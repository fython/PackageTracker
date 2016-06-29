package info.papdt.express.helper.ui.adapter;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemDrawableTypes;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemResults;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.ColorGenerator;
import info.papdt.express.helper.ui.callback.OnDataRemovedCallback;

public class HomePackageListAdapter extends RecyclerView.Adapter<HomePackageListAdapter.MyViewHolder> implements SwipeableItemAdapter<HomePackageListAdapter.MyViewHolder>{

	private PackageDatabase db;
	private int type;

	private OnDataRemovedCallback mDataRemovedCallback;

	public static final int TYPE_ALL = 0, TYPE_DELIVERED = 1, TYPE_DELIVERING = 2;

	public HomePackageListAdapter(PackageDatabase db, int type) {
		/** This is required for swiping feature. */
		setHasStableIds(true);

		this.db = db;
		this.type = type;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_package_for_home, parent, false);
		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		Package p = getItemData(position);

		holder.titleText.setText(p.name);
		if (p.data.size() > 0) {
			Package.Status status = p.data.get(0);
			holder.descText.setText(status.context);
			holder.timeText.setText(status.ftime);
			holder.timeText.setVisibility(View.VISIBLE);
		} else {
			/** Set placeholder when cannot get data */
			holder.descText.setText(R.string.item_text_cannot_get_package_status);
			holder.timeText.setVisibility(View.GONE);
		}

		/** Set bold text when unread */
		holder.descText.getPaint().setFakeBoldText(p.unreadNew);
		holder.titleText.getPaint().setFakeBoldText(p.unreadNew);

		/** Set CircleImageView */
		holder.bigCharView.setText(p.name.substring(0, 1));
		holder.logoView.setImageDrawable(new ColorDrawable(ColorGenerator.MATERIAL.getColor(p.name)));
	}

	public Package getItemData(int pos) {
		switch (type) {
			case TYPE_DELIVERED:
				return db.getDeliveredData().get(pos);
			case TYPE_DELIVERING:
				return db.getDeliveringData().get(pos);
			case TYPE_ALL:
			default:
				return db.get(pos);
		}
	}

	public void setDatabase(PackageDatabase db) {
		this.db = db;
		notifyDataSetChanged();
	}

	@Override
	public long getItemId(int pos) {
		/** Use package number (digits only) as id */
		return getItemData(pos).getId();
	}

	@Override
	public int getItemCount() {
		switch (type) {
			case TYPE_DELIVERED:
				return db.getDeliveredData().size();
			case TYPE_DELIVERING:
				return db.getDeliveringData().size();
			case TYPE_ALL:
			default:
				return db.size();
		}
	}

	@Override
	public SwipeResultAction onSwipeItem(MyViewHolder holder, int position, @SwipeableItemResults int result) {
		if (result == SwipeableItemConstants.RESULT_CANCELED) {
			return new SwipeResultActionDefault();
		} else {
			return new MySwipeResultActionRemoveItem(this, position);
		}
	}

	@Override
	public int onGetSwipeReactionType(MyViewHolder holder, int position, int x, int y) {
		return SwipeableItemConstants.REACTION_CAN_SWIPE_BOTH_H;
	}

	@Override
	public void onSetSwipeBackground(MyViewHolder holder, int position, @SwipeableItemDrawableTypes int type) {

	}

	public void setOnDataRemovedCallback(OnDataRemovedCallback callback) {
		this.mDataRemovedCallback = callback;
	}

	public class MyViewHolder extends AbstractSwipeableItemViewHolder {

		CircleImageView logoView;
		AppCompatTextView titleText, descText, timeText;
		TextView bigCharView;

		private View containerView;

		public MyViewHolder(View itemView) {
			super(itemView);
			logoView = (CircleImageView) itemView.findViewById(R.id.iv_logo);
			titleText = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
			descText = (AppCompatTextView) itemView.findViewById(R.id.tv_other);
			timeText = (AppCompatTextView) itemView.findViewById(R.id.tv_time);
			bigCharView = (TextView) itemView.findViewById(R.id.tv_first_char);
			containerView = itemView.findViewById(R.id.item_container);
		}

		@Override
		public View getSwipeableContainerView() {
			return containerView;
		}
	}

	class MySwipeResultActionRemoveItem extends SwipeResultActionRemoveItem {

		private HomePackageListAdapter adapter;
		private int position;

		public MySwipeResultActionRemoveItem(HomePackageListAdapter adapter, int position) {
			this.adapter = adapter;
			this.position = position;
		}

		@Override
		protected void onPerformAction() {
			final String title = adapter.db.get(position).name;
			adapter.db.remove(position);
			adapter.notifyItemRemoved(position);
			if (mDataRemovedCallback != null) mDataRemovedCallback.onDataRemoved(position, title);
		}

	}

}
