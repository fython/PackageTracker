package info.papdt.express.helper.ui.adapter;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import info.papdt.express.helper.R;
import info.papdt.express.helper.api.PackageApi;
import info.papdt.express.helper.model.Package;

public class DetailsInfoAdapter extends RecyclerView.Adapter {

	private Package data;
	private ArrayList<ItemType> items;

	private final String STRING_NUMBER_FORMAT;

	public DetailsInfoAdapter(Context context) {
		super();
		this.STRING_NUMBER_FORMAT = context.getString(R.string.list_package_number_format);
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
					NormalItemHolder h = (NormalItemHolder) holder;
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
						h.title.setText(status.context);
						h.time.setText(status.time);
						if (itemType.statusIndex == 0) {
							// TODO
						}
						if (itemType.statusIndex == data.data.size() - 1) {
							// TODO
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

		public StatusItemHolder(View itemView) {
			super(itemView);
			title = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
			time = (AppCompatTextView) itemView.findViewById(R.id.tv_time);
		}

	}

}
