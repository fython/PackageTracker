package info.papdt.express.helper.ui.adapter;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import info.papdt.express.helper.R;
import info.papdt.express.helper.api.PackageApi;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.ColorGenerator;
import info.papdt.express.helper.support.ScreenUtils;
import info.papdt.express.helper.support.Spanny;
import info.papdt.express.helper.ui.DetailsActivity;

public class SearchResultAdapter extends RecyclerView.Adapter {

	private ArrayList<Package> packages;
	private ArrayList<PackageApi.CompanyInfo.Company> companies;

	private ArrayList<ItemType> items;

	private AppCompatActivity parentActivity;

	private float DP_16_TO_PX = -1;
	private int statusTitleColor, statusSubtextColor = -1;
	private String[] STATUS_STRING_ARRAY;

	public SearchResultAdapter(AppCompatActivity parentActivity) {
		this.parentActivity = parentActivity;
	}

	public void setPackages(ArrayList<Package> packages) {
		this.packages = packages;
	}

	public void setCompanies(ArrayList<PackageApi.CompanyInfo.Company> companies) {
		this.companies = companies;
	}

	public void setItems(ArrayList<ItemType> items) {
		this.items = items;
		Log.i("test", new Gson().toJson(items));
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (DP_16_TO_PX == -1) DP_16_TO_PX = ScreenUtils.dpToPx(parent.getContext(), 8);
		if (STATUS_STRING_ARRAY == null) STATUS_STRING_ARRAY = parent.getContext().getResources().getStringArray(R.array.item_status_description);
		if (statusSubtextColor == -1) {
			statusTitleColor = parent.getContext().getResources().getColor(R.color.package_list_status_title_color);
			statusSubtextColor = parent.getContext().getResources().getColor(R.color.package_list_status_subtext_color);
		}

		RecyclerView.ViewHolder result = null;
		switch (viewType) {
			case ItemType.TYPE_EMPTY:
				result = new EmptyHolder(
						LayoutInflater.from(parent.getContext())
								.inflate(R.layout.item_search_result_empty, parent, false)
				);
				break;
			case ItemType.TYPE_SUBHEADER:
				result = new SubheaderItemHolder(
						LayoutInflater.from(parent.getContext())
								.inflate(R.layout.item_list_details_info_subheader, parent, false)
				);
				break;
			case ItemType.TYPE_COMPANY:
				result = new CompanyHolder(
						LayoutInflater.from(parent.getContext())
								.inflate(R.layout.item_list_company, parent, false)
				);
				break;
			case ItemType.TYPE_PACKAGE:
				result = new PackageHolder(
						LayoutInflater.from(parent.getContext())
								.inflate(R.layout.item_list_package_for_home, parent, false)
				);
				break;
		}
		return result;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		final ItemType itemType = items.get(position);
		switch (items.get(position).viewType) {
			case ItemType.TYPE_EMPTY:
				EmptyHolder eh = (EmptyHolder) holder;
				eh.title.setText(
						(position == 1 ? packages == null : companies == null) ?
								R.string.item_title_please_wait :
								R.string.search_no_result
				);
				break;
			case ItemType.TYPE_SUBHEADER:
				SubheaderItemHolder h0 = (SubheaderItemHolder) holder;
				h0.title.setText(position > 0 ? R.string.subheader_company : R.string.subheader_package);
				break;
			case ItemType.TYPE_COMPANY:
				CompanyHolder h1 = (CompanyHolder) holder;
				h1.titleText.setText(companies.get(itemType.index).name);
				h1.otherText.setText(companies.get(itemType.index).phone != null ? companies.get(itemType.index).phone : companies.get(itemType.index).website);
				h1.otherText.setVisibility(h1.otherText.getText() != null ? View.VISIBLE : View.INVISIBLE);

				/** Set up the logo */
				h1.logoView.setImageDrawable(new ColorDrawable(ColorGenerator.MATERIAL.getColor(companies.get(itemType.index).name)));
				h1.firstCharText.setText(companies.get(itemType.index).name.substring(0, 1));

				h1.rootView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String phone = companies.get(itemType.index).phone;
						if (phone != null && !TextUtils.isEmpty(phone)) {
							Intent intent = new Intent(Intent.ACTION_DIAL);
							intent.setData(Uri.parse("tel:" + phone));
							parentActivity.startActivity(intent);
						}
					}
				});
				break;
			case ItemType.TYPE_PACKAGE:
				PackageHolder h2 = (PackageHolder) holder;
				final Package p = packages.get(itemType.index);

				h2.titleText.setText(p.name);
				if (p.data.size() > 0) {
					Package.Status status = p.data.get(0);
					Spanny spanny = new Spanny(STATUS_STRING_ARRAY[p.getState()], new ForegroundColorSpan(statusTitleColor))
							.append(" - " + status.context, new ForegroundColorSpan(statusSubtextColor));
					h2.descText.setText(spanny);
					h2.timeText.setText(status.ftime);
					h2.timeText.setVisibility(View.VISIBLE);
				} else {
					/** Set placeholder when cannot get data */
					h2.descText.setText(R.string.item_text_cannot_get_package_status);
					h2.timeText.setVisibility(View.GONE);
				}
				
				/** Set CircleImageView */
				h2.bigCharView.setText(p.name.substring(0, 1));
				h2.logoView.setImageDrawable(new ColorDrawable(ColorGenerator.MATERIAL.getColor(p.name)));

				/** Add paddingTop/Bottom to the first or last item */
				if (position == 0) {
					h2.getContainerView().setPadding(0, (int) DP_16_TO_PX, 0, 0);
				} else if (position == getItemCount()) {
					h2.getContainerView().setPadding(0, 0, 0, (int) DP_16_TO_PX);
				}

				h2.getContainerView().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						DetailsActivity.launch(parentActivity, p);
					}
				});
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

		public final static int TYPE_PACKAGE = 0, TYPE_COMPANY = 1, TYPE_SUBHEADER = 2, TYPE_EMPTY = 3;

		public int viewType;

		// Optional
		public int index;

		public ItemType(int viewType) {
			this.viewType = viewType;
		}

	}

	private class PackageHolder extends RecyclerView.ViewHolder {

		CircleImageView logoView;
		AppCompatTextView titleText, descText, timeText;
		TextView bigCharView;

		private View containerView;

		PackageHolder(View itemView) {
			super(itemView);
			logoView = (CircleImageView) itemView.findViewById(R.id.iv_logo);
			titleText = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
			descText = (AppCompatTextView) itemView.findViewById(R.id.tv_other);
			timeText = (AppCompatTextView) itemView.findViewById(R.id.tv_time);
			bigCharView = (TextView) itemView.findViewById(R.id.tv_first_char);
			containerView = itemView.findViewById(R.id.item_container);
		}

		View getContainerView() {
			return containerView;
		}

	}

	private class SubheaderItemHolder extends RecyclerView.ViewHolder {

		AppCompatTextView title;

		SubheaderItemHolder(View itemView) {
			super(itemView);
			title = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
		}

	}

	private class EmptyHolder extends RecyclerView.ViewHolder {

		AppCompatTextView title;

		EmptyHolder(View itemView) {
			super(itemView);
			title = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
		}

	}

	private class CompanyHolder extends RecyclerView.ViewHolder {

		AppCompatTextView titleText, otherText;
		CircleImageView logoView;
		TextView firstCharText;

		View rootView;

		CompanyHolder(View itemView) {
			super(itemView);
			rootView = itemView;
			titleText = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
			otherText = (AppCompatTextView) itemView.findViewById(R.id.tv_other);
			logoView = (CircleImageView) itemView.findViewById(R.id.iv_logo);
			firstCharText = (TextView) itemView.findViewById(R.id.tv_first_char);
		}

	}

}
