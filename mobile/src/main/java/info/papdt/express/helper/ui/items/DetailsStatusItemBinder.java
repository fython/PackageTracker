package info.papdt.express.helper.ui.items;

import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import info.papdt.express.helper.R;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.view.VerticalStepIconView;
import info.papdt.express.helper.view.VerticalStepLineView;
import me.drakeet.multitype.ItemViewBinder;

public class DetailsStatusItemBinder extends ItemViewBinder<Package.Status, DetailsStatusItemBinder.ItemHolder> {

	private Package mPackage;
	private boolean isShowed[] = new boolean[1000];

	public void setData(Package src) {
		mPackage = src;
	}

	@NonNull
	@Override
	protected ItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
		return new ItemHolder(inflater.inflate(R.layout.item_list_details_info_status, parent, false));
	}

	@Override
	protected void onBindViewHolder(@NonNull ItemHolder holder, @NonNull Package.Status item) {
		holder.setData(item);
	}

	class ItemHolder extends RecyclerView.ViewHolder {

		private Package.Status data;

		AppCompatTextView title, time;
		VerticalStepIconView stepIcon;
		VerticalStepLineView stepLine;

		CardView contactCard;
		AppCompatTextView phoneView;

		ItemHolder(View itemView) {
			super(itemView);

			title = itemView.findViewById(R.id.tv_title);
			time = itemView.findViewById(R.id.tv_time);
			stepIcon = itemView.findViewById(R.id.step_icon_view);
			stepLine = itemView.findViewById(R.id.step_line_view);
			contactCard = itemView.findViewById(R.id.contact_card);
			phoneView = itemView.findViewById(R.id.contact_number);

			View.OnClickListener callPhone = new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (!TextUtils.isEmpty(phoneView.getText().toString())) {
						Intent intent = new Intent(Intent.ACTION_DIAL);
						intent.setData(Uri.parse("tel:" + phoneView.getText().toString()));
						view.getContext().startActivity(intent);
					}
				}
			};
			contactCard.setOnClickListener(callPhone);
			itemView.findViewById(R.id.btn_call_contact).setOnClickListener(callPhone);
		}

		void setData(Package.Status newData) {
			this.data = newData;

			/** Show time and location (if available) */
			String timeText = data.time;
			String location = data.getLocation();
			if (location != null) {
				timeText += "  ·  " + location;
			}
			time.setText(timeText);
			/** Show status context*/
			String context = data.context;
			title.setText(context);

			/** Show contact card if available */
			String phone = data.getPhone();
			if (phone != null) phoneView.setText(phone);
			contactCard.setVisibility(phone != null ? View.VISIBLE : View.GONE);

			/** Set up step view style */
			int indexInStatus = getAdapterPosition() - 3;
			if (indexInStatus == 0) {
				stepIcon.setIsMini(false);
				stepLine.setLineShouldDraw(false, mPackage.data.size() > 1);
				if (mPackage.data.size() > 1) {
					stepIcon.setPointOffsetY(-time.getTextSize());
					stepLine.setPointOffsetY(-time.getTextSize());
				}
				int pointColorResId, pointIconResId;
				switch (mPackage.getState()) {
					case Package.STATUS_DELIVERED:
						pointColorResId = R.color.green_500;
						pointIconResId = R.drawable.ic_done_white_24dp;
						break;
					case Package.STATUS_FAILED:
					case Package.STATUS_OTHER:
						pointColorResId = R.color.red_500;
						pointIconResId = R.drawable.ic_close_white_24dp;
						break;
					case Package.STATUS_RETURNED:
						pointColorResId = R.color.brown_500;
						pointIconResId = R.drawable.ic_assignment_return_white_24dp;
						break;
					case Package.STATUS_ON_THE_WAY:
						pointColorResId = R.color.blue_700;
						pointIconResId = R.drawable.ic_local_shipping_white_24dp;
						break;
					case Package.STATUS_NORMAL:
					default:
						pointColorResId = R.color.blue_500;
						pointIconResId = R.drawable.ic_flight_white_24dp;
						break;
				}
				stepIcon.setPointColorResource(pointColorResId);
				stepIcon.setCenterIcon(pointIconResId);
			} else {
				stepIcon.setIsMini(true);
				stepIcon.setPointColorResource(R.color.blue_grey_500);
				stepLine.setPointOffsetY(-time.getTextSize());
				stepIcon.setPointOffsetY(-time.getTextSize());
				stepIcon.setCenterIcon(null);
				stepLine.setLineShouldDraw(true, true);
			}
			if (indexInStatus == mPackage.data.size() - 1 && mPackage.data.size() > 1) {
				stepLine.setLineShouldDraw(true, false);
				stepLine.setPointOffsetY(0);
				stepIcon.setPointOffsetY(0);
				stepIcon.setCenterIcon(null);
			}
			synchronized (this) {
				if  (!isShowed[indexInStatus]) {
					synchronized (this) {
						isShowed[indexInStatus] = true;
					}
					stepIcon.setScaleX(0f);
					stepIcon.setScaleY(0f);
					stepIcon.animate().scaleX(1f).scaleY(1f)
							.setStartDelay(150 * (indexInStatus + 1))
							.setDuration(500).setInterpolator(new OvershootInterpolator()).start();
					
				}
			}
		}

	}

}
