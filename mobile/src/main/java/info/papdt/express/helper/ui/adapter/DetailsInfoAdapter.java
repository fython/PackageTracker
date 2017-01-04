package info.papdt.express.helper.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import info.papdt.express.helper.R;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.view.VerticalStepView;

public class DetailsInfoAdapter extends RecyclerView.Adapter {

	private Package data;
	private ArrayList<ItemType> items;

	private final String STRING_NUMBER_FORMAT;

	private Activity parentActivity;

	public DetailsInfoAdapter(Context context) {
		super();
		this.STRING_NUMBER_FORMAT = context.getString(R.string.list_package_number_format);
		if (context instanceof Activity) {
			parentActivity = (Activity) context;
		}
	}

	public void setData(Package newData, ArrayList<ItemType> items) {
		this.data = newData;
		this.items = items;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder result = null;
		switch (viewType) {
			case ItemType.TYPE_NORMAL:
				result = new NormalItemHolder(
						LayoutInflater.from(parent.getContext())
						.inflate(R.layout.item_list_details_info_normal, parent, false)
				);
				break;
			case ItemType.TYPE_SUBHEADER:
				result = new SubheaderItemHolder(
						LayoutInflater.from(parent.getContext())
						.inflate(R.layout.item_list_details_info_subheader, parent, false)
				);
				break;
			case ItemType.TYPE_PACK_STATUS:
				result = new StatusItemHolder(
						LayoutInflater.from(parent.getContext())
						.inflate(R.layout.item_list_details_info_status, parent, false)
				);
				break;
		}
		return result;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int index) {
		ItemType itemType = items.get(index);
		switch (items.get(index).viewType) {
			case ItemType.TYPE_NORMAL:
				if (holder instanceof NormalItemHolder) {
					final NormalItemHolder h = (NormalItemHolder) holder;
					if (itemType.id == ItemType.ID_NAME) {
						h.title.setText(R.string.list_package_name);
						h.summary.setText(data.name);
					} else if (itemType.id == ItemType.ID_NUMBER) {
						h.title.setText(R.string.list_package_number);
						h.summary.setText(String.format(
								STRING_NUMBER_FORMAT,
								data.number,
								data.companyChineseName
						));
						if (h.button.getTag() != null && ((Boolean) h.button.getTag())) {
							h.button.setImageResource(R.drawable.ic_visibility_off_black_24dp);
							h.summary.setText(String.format(STRING_NUMBER_FORMAT, data.number, data.companyChineseName));
						} else {
							int length = data.number.length();
							String str = data.number.substring(0, 4);
							for (int i = 4; i < length; i++) str += "*";
							h.summary.setText(String.format(STRING_NUMBER_FORMAT, str, data.companyChineseName));
							h.button.setImageResource(R.drawable.ic_visibility_black_24dp);
						}
						h.button.setVisibility(View.VISIBLE);
						h.button.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								if (h.button.getTag() != null && ((Boolean) h.button.getTag())) {
									int length = data.number.length();
									String str = data.number.substring(0, 4);
									for (int i = 4; i < length; i++) str += "*";
									h.summary.setText(String.format(STRING_NUMBER_FORMAT, str, data.companyChineseName));
									h.button.setImageResource(R.drawable.ic_visibility_black_24dp);
									h.button.setTag(false);
								} else {
									h.button.setImageResource(R.drawable.ic_visibility_off_black_24dp);
									h.summary.setText(String.format(STRING_NUMBER_FORMAT, data.number, data.companyChineseName));
									h.button.setTag(true);
								}
							}
						});
					}
				}
				break;
			case ItemType.TYPE_SUBHEADER:
				if (holder instanceof SubheaderItemHolder) {
					SubheaderItemHolder h = (SubheaderItemHolder) holder;
					if (itemType.id == ItemType.ID_STATUS_HEADER) {
						h.title.setText(R.string.list_status_subheader);
					}
				}
				break;
			case ItemType.TYPE_PACK_STATUS:
				if (holder instanceof StatusItemHolder) {
					StatusItemHolder h = (StatusItemHolder) holder;
					if (itemType.id == ItemType.ID_STATUS) {
						Package.Status status = data.data.get(itemType.statusIndex);

						/** Show time and location (if available) */
						String timeText = status.time;
						String location = status.getLocation();
						if (location != null) {
							timeText += "  ·  " + location;
						}
						h.time.setText(timeText);
						/** Show status context*/
						String context = status.context;
						h.title.setText(context);

						/** Show contact card if available */
						String phone = status.getPhone();
						if (phone != null) h.phone.setText(phone);
						h.contactCard.setVisibility(phone != null ? View.VISIBLE : View.GONE);

						/** Set up step view style */
						if (itemType.statusIndex == 0) {
							h.stepView.setIsMini(false);
							h.stepView.setLineShouldDraw(false, data.data.size() > 1);
							if (data.data.size() > 1) {
								h.stepView.setPointOffsetY(-h.time.getTextSize());
							}
							int pointColorResId, pointIconResId;
							switch (data.getState()) {
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
							h.stepView.setPointColorResource(pointColorResId);
							h.stepView.setCenterIcon(pointIconResId);
						} else {
							h.stepView.setIsMini(true);
							h.stepView.setPointColorResource(R.color.blue_grey_500);
							h.stepView.setPointOffsetY(-h.time.getTextSize());
							h.stepView.setCenterIcon(null);
							h.stepView.setLineShouldDraw(true, true);
						}
						if (itemType.statusIndex == data.data.size() - 1 && data.data.size() > 1) {
							h.stepView.setLineShouldDraw(true, false);
							h.stepView.setPointOffsetY(0);
							h.stepView.setCenterIcon(null);
						}
					}
				}
				break;
		}
	}

	@Override
	public int getItemCount() {
		return items != null ? items.size() : 0;
	}

	@Override
	public int getItemViewType(int index) {
		return items.get(index).viewType;
	}

	public static class ItemType {

		public final static int TYPE_NORMAL = 0, TYPE_PACK_STATUS = 1, TYPE_SUBHEADER = 2;
		public final static int ID_NAME = 1000, ID_NUMBER = 1001, ID_STATUS = 1002, ID_STATUS_HEADER = 1003;

		public int viewType, id;

		// Optional
		public int statusIndex;

		public ItemType(int viewType, int id) {
			this.viewType = viewType;
			this.id = id;
		}

	}

	public class NormalItemHolder extends RecyclerView.ViewHolder {

		AppCompatTextView title, summary;
		AppCompatImageButton button;

		public NormalItemHolder(View itemView) {
			super(itemView);
			title = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
			summary = (AppCompatTextView) itemView.findViewById(R.id.tv_summary);
			button = (AppCompatImageButton) itemView.findViewById(R.id.btn_action);
		}

	}

	public class SubheaderItemHolder extends RecyclerView.ViewHolder {

		AppCompatTextView title;

		public SubheaderItemHolder(View itemView) {
			super(itemView);
			title = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
		}

	}

	public class StatusItemHolder extends RecyclerView.ViewHolder {

		AppCompatTextView title, time;
		VerticalStepView stepView;

		CardView contactCard;
		AppCompatTextView phone;

		public StatusItemHolder(View itemView) {
			super(itemView);
			title = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
			time = (AppCompatTextView) itemView.findViewById(R.id.tv_time);
			stepView = (VerticalStepView) itemView.findViewById(R.id.step_view);
			contactCard = (CardView) itemView.findViewById(R.id.contact_card);
			phone = (AppCompatTextView) itemView.findViewById(R.id.contact_number);

			View.OnClickListener callPhone = new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (!TextUtils.isEmpty(phone.getText().toString())
							&& parentActivity != null) {
						Intent intent = new Intent(Intent.ACTION_DIAL);
						intent.setData(Uri.parse("tel:" + phone.getText().toString()));
						parentActivity.startActivity(intent);
					}
				}
			};
			contactCard.setOnClickListener(callPhone);
			itemView.findViewById(R.id.btn_call_contact).setOnClickListener(callPhone);
		}

	}

}
