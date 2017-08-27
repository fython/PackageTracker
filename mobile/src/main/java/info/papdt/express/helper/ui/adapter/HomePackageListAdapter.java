package info.papdt.express.helper.ui.adapter;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.style.ForegroundColorSpan;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
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

import de.hdodenhof.circleimageview.CircleImageView;
import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.ColorGenerator;
import info.papdt.express.helper.support.ScreenUtils;
import info.papdt.express.helper.support.Spanny;
import info.papdt.express.helper.ui.DetailsActivity;
import info.papdt.express.helper.ui.MainActivity;
import info.papdt.express.helper.ui.callback.OnDataRemovedCallback;

public class HomePackageListAdapter extends RecyclerView.Adapter<HomePackageListAdapter.MyViewHolder> implements SwipeableItemAdapter<HomePackageListAdapter.MyViewHolder>{

	private PackageDatabase db;
	private int type;
	private AppCompatActivity parentActivity;

	private OnDataRemovedCallback mDataRemovedCallback;

	private float DP_16_TO_PX = -1;
	private int statusTitleColor, statusSubtextColor = -1;
	private String[] STATUS_STRING_ARRAY;

	public static final int TYPE_ALL = 0, TYPE_DELIVERED = 1, TYPE_DELIVERING = 2;

	public HomePackageListAdapter(PackageDatabase db, int type, AppCompatActivity parentActivity) {
		/** This is required for swiping feature. */
		setHasStableIds(true);

		this.db = db;
		this.type = type;
		this.parentActivity = parentActivity;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (DP_16_TO_PX == -1) DP_16_TO_PX = ScreenUtils.dpToPx(parent.getContext(), 8);
		if (STATUS_STRING_ARRAY == null) STATUS_STRING_ARRAY = parent.getContext().getResources().getStringArray(R.array.item_status_description);
		if (statusSubtextColor == -1) {
			statusTitleColor = parent.getContext().getResources().getColor(R.color.package_list_status_title_color);
			statusSubtextColor = parent.getContext().getResources().getColor(R.color.package_list_status_subtext_color);
		}

		View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_package_for_home, parent, false);
		return new MyViewHolder(itemView);
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		Package p = getItemData(position);

		if (p.name.length() > 0) {
			holder.titleText.setText(p.name);
		}else {
			holder.titleText.setText(p.companyChineseName);
		}
		if (p.data != null && p.data.size() > 0) {
			Package.Status status = p.data.get(0);
			Spanny spanny = new Spanny(STATUS_STRING_ARRAY[p.getState()], new ForegroundColorSpan(statusTitleColor))
					.append(" - " + status.context, new ForegroundColorSpan(statusSubtextColor));
			holder.descText.setText(spanny);
			holder.timeText.setText(status.ftime);
			holder.timeText.setVisibility(View.VISIBLE);
		} else {
			/** Set placeholder when cannot get data */
			holder.descText.setText(R.string.item_text_cannot_get_package_status);
			holder.timeText.setVisibility(View.GONE);
		}

		/** Set bold text when unread */
		holder.descText.getPaint().setFakeBoldText(p.unreadNew);
		holder.titleText.getPaint().setFakeBoldText(true);

		/** Set CircleImageView */
		if (p.name.length() > 0){
			holder.bigCharView.setText(p.name.substring(0, 1).toUpperCase());
			holder.logoView.setImageDrawable(new ColorDrawable(ColorGenerator.MATERIAL.getColor(p.name)));
		} else if (p.companyChineseName.length() > 0){
			holder.bigCharView.setText(p.companyChineseName.substring(0,1).toUpperCase());
			holder.logoView.setImageDrawable(new ColorDrawable(ColorGenerator.MATERIAL.getColor(p.companyChineseName)));
		}

		/** Add paddingTop/Bottom to the first or last item */
		if (position == 0) {
			holder.getSwipeableContainerView().setPadding(0, (int) DP_16_TO_PX, 0, 0);
		} else if (position == getItemCount()) {
			holder.getSwipeableContainerView().setPadding(0, 0, 0, (int) DP_16_TO_PX);
		}
	}

	public Package getItemData(int pos) {
		switch (type) {
			case TYPE_DELIVERED:
				return db.getDeliveredData().get(db.getDeliveredData().size() - pos - 1);
			case TYPE_DELIVERING:
				return db.getDeliveringData().get(db.getDeliveringData().size() - pos - 1);
			case TYPE_ALL:
			default:
				return db.get(db.size() - pos - 1);
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

	public class MyViewHolder extends AbstractSwipeableItemViewHolder
			implements View.OnCreateContextMenuListener {

		CircleImageView logoView;
		AppCompatTextView titleText, descText, timeText;
		TextView bigCharView;

		private View containerView;

		public MyViewHolder(View itemView) {
			super(itemView);
			logoView = itemView.findViewById(R.id.iv_logo);
			titleText = itemView.findViewById(R.id.tv_title);
			descText = itemView.findViewById(R.id.tv_other);
			timeText = itemView.findViewById(R.id.tv_time);
			bigCharView = itemView.findViewById(R.id.tv_first_char);
			containerView = itemView.findViewById(R.id.item_container);

			containerView.setOnCreateContextMenuListener(this);

			getSwipeableContainerView().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					DetailsActivity.Companion.launch(parentActivity, getItemData(getAdapterPosition()));
				}
			});
		}

		@Override
		public View getSwipeableContainerView() {
			return containerView;
		}

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo info) {
			if (parentActivity instanceof MainActivity) {
				((MainActivity) parentActivity).onContextMenuCreate(getItemData(getAdapterPosition()));
			}

			menu.setHeaderTitle(getItemData(getAdapterPosition()).name);
			if (!getItemData(getAdapterPosition()).unreadNew) {
				menu.add(Menu.NONE, R.id.action_set_unread, 0, R.string.action_set_unread);
			}
			menu.add(Menu.NONE, R.id.action_share, 0, R.string.action_share);
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
			final String title = getItemData(position).name;
			adapter.db.remove(getItemData(position));
			adapter.notifyItemRemoved(position);
			if (mDataRemovedCallback != null) mDataRemovedCallback.onDataRemoved(position, title);
		}

	}

}
