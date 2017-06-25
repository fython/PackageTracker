package info.papdt.express.helper.ui.items;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import info.papdt.express.helper.R;
import info.papdt.express.helper.support.ClipboardUtils;
import info.papdt.express.helper.ui.DetailsActivity;
import me.drakeet.multitype.ItemViewBinder;

public class DetailsTwoLineItemBinder extends ItemViewBinder<DetailsTwoLineItem, DetailsTwoLineItemBinder.ItemHolder> {

	private static final String STRING_NUMBER_FORMAT = "%1$s (%2$s)";

	@NonNull
	@Override
	protected ItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
		return new ItemHolder(inflater.inflate(R.layout.item_list_details_info_normal, parent, false));
	}

	@Override
	protected void onBindViewHolder(@NonNull ItemHolder holder, @NonNull DetailsTwoLineItem item) {
		holder.setData(item);
	}

	class ItemHolder extends RecyclerView.ViewHolder {

		private DetailsTwoLineItem data;

		AppCompatTextView title, summary;
		AppCompatImageButton button;

		ItemHolder(View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.tv_title);
			summary = itemView.findViewById(R.id.tv_summary);
			button = itemView.findViewById(R.id.btn_action);
		}

		void setData(DetailsTwoLineItem newData) {
			this.data = newData;
			if (DetailsTwoLineItem.TYPE_NAME.equals(data.getType())) {
				title.setText(R.string.list_package_name);
				summary.setText(data.getContent());
				button.setVisibility(View.GONE);
				itemView.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						if (v.getContext() instanceof DetailsActivity) {
							((DetailsActivity) v.getContext()).showNameEditDialog();
						}
						return true;
					}
				});
			} else if (DetailsTwoLineItem.TYPE_NUMBER.equals(data.getType())) {
				title.setText(R.string.list_package_number);
				summary.setText(String.format(
						STRING_NUMBER_FORMAT,
						data.getContent(),
						data.getOptional()
				));
				if (button.getTag() != null && ((Boolean) button.getTag())) {
					button.setImageResource(R.drawable.ic_visibility_off_black_24dp);
					summary.setText(String.format(STRING_NUMBER_FORMAT, data.getContent(), data.getOptional()));
				} else {
					int length = data.getContent().length();
					String str = data.getContent().substring(0, 4);
					for (int i = 4; i < length; i++) str += "*";
					summary.setText(String.format(STRING_NUMBER_FORMAT, str, data.getOptional()));
					button.setImageResource(R.drawable.ic_visibility_black_24dp);
					button.setContentDescription(
							itemView.getResources()
									.getString(R.string.list_package_show_toggle_desc)
					);
				}
				button.setVisibility(View.VISIBLE);
				button.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (button.getTag() != null && ((Boolean) button.getTag())) {
							int length = data.getContent().length();
							String str = data.getContent().substring(0, 4);
							for (int i = 4; i < length; i++) str += "*";
							summary.setText(String.format(STRING_NUMBER_FORMAT, str, data.getOptional()));
							button.setImageResource(R.drawable.ic_visibility_black_24dp);
							button.setTag(false);
						} else {
							button.setImageResource(R.drawable.ic_visibility_off_black_24dp);
							summary.setText(String.format(STRING_NUMBER_FORMAT, data.getContent(), data.getOptional()));
							button.setTag(true);
						}
					}
				});
				itemView.setOnLongClickListener(new View.OnLongClickListener() {
					@Override
					public boolean onLongClick(View v) {
						ClipboardUtils.putString(v.getContext(), data.getContent());
						Snackbar.make(((Activity) v.getContext()).findViewById(R.id.coordinator_layout),
								R.string.toast_copied_code,
								Snackbar.LENGTH_LONG
						).show();
						return true;
					}
				});
			}
		}

	}

}
