package info.papdt.express.helper.ui.items;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import info.papdt.express.helper.R;
import me.drakeet.multitype.ItemViewBinder;

public class SubheaderItemBinder extends ItemViewBinder<String, SubheaderItemBinder.ItemHolder> {

	@NonNull
	@Override
	protected ItemHolder onCreateViewHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
		return new ItemHolder(inflater.inflate(R.layout.item_list_details_info_subheader, parent, false));
	}

	@Override
	protected void onBindViewHolder(@NonNull ItemHolder holder, @NonNull String item) {
		holder.title.setText(item);
	}

	class ItemHolder extends RecyclerView.ViewHolder {

		AppCompatTextView title;

		ItemHolder(View itemView) {
			super(itemView);
			title = itemView.findViewById(R.id.tv_title);
		}

	}

}
