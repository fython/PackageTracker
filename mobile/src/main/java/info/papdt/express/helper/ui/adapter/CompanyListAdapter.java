package info.papdt.express.helper.ui.adapter;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import info.papdt.express.helper.R;
import info.papdt.express.helper.api.PackageApi;
import info.papdt.express.helper.support.ColorGenerator;
import info.papdt.express.helper.ui.common.SimpleRecyclerViewAdapter;

public class CompanyListAdapter extends SimpleRecyclerViewAdapter {

	private ArrayList<PackageApi.CompanyInfo.Company> list;

	public CompanyListAdapter(RecyclerView recyclerView, ArrayList<PackageApi.CompanyInfo.Company> list) {
		super(recyclerView);
		this.list = list;
	}

	public void setList(ArrayList<PackageApi.CompanyInfo.Company> list) {
		this.list = list;
	}

	@Override
	public ClickableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		bindContext(parent.getContext());
		return new ItemHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_company, parent, false));
	}

	@Override
	public void onBindViewHolder(ClickableViewHolder holder, int pos) {
		super.onBindViewHolder(holder, pos);
		if (holder instanceof ItemHolder) {
			ItemHolder itemHolder = (ItemHolder) holder;
			itemHolder.titleText.setText(getItem(pos).getName());
			itemHolder.otherText.setText(getItem(pos).getPhone() != null ? getItem(pos).getPhone() : getItem(pos).getWebsite());
			itemHolder.otherText.setVisibility(itemHolder.otherText.getText() != null ? View.VISIBLE : View.INVISIBLE);

			/** Set up the logo */
			itemHolder.logoView.setImageDrawable(new ColorDrawable(ColorGenerator.MATERIAL.getColor(getItem(pos).getName())));
			itemHolder.firstCharText.setText(getItem(pos).getName().substring(0, 1));
		}
	}

	@Override
	public int getItemCount() {
		return list == null ? 0 : list.size();
	}

	public PackageApi.CompanyInfo.Company getItem(int pos) {
		return list.get(pos);
	}

	public class ItemHolder extends ClickableViewHolder {

		AppCompatTextView titleText, otherText;
		CircleImageView logoView;
		TextView firstCharText;

		public ItemHolder(View itemView) {
			super(itemView);
			titleText = (AppCompatTextView) itemView.findViewById(R.id.tv_title);
			otherText = (AppCompatTextView) itemView.findViewById(R.id.tv_other);
			logoView = (CircleImageView) itemView.findViewById(R.id.iv_logo);
			firstCharText = (TextView) itemView.findViewById(R.id.tv_first_char);
		}

	}

}
