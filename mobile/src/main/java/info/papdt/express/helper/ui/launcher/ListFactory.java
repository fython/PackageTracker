package info.papdt.express.helper.ui.launcher;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.text.style.ForegroundColorSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import info.papdt.express.helper.R;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.ColorGenerator;
import info.papdt.express.helper.support.ScreenUtils;
import info.papdt.express.helper.support.Spanny;

public class ListFactory implements RemoteViewsService.RemoteViewsFactory {

	private Context mContext;
	private int mAppWidgetId;

	private PackageDatabase mDatabase;

	private float DP_16_TO_PX;
	private int statusTitleColor, statusSubtextColor;
	private String[] STATUS_STRING_ARRAY;
	private String STATUS_ERROR;

	private static final String EXTRA_PACKAGE_JSON = "extra_package_json", EXTRA_STATE = "extra_state";

	public ListFactory(Context context, Intent intent) {
		this.mContext = context;
		this.mAppWidgetId = intent.getIntExtra(
				AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
		);
		this.mDatabase = PackageDatabase.Companion.getInstance(context.getApplicationContext());

		DP_16_TO_PX = ScreenUtils.dpToPx(context, 8);
		STATUS_STRING_ARRAY = context.getResources().getStringArray(R.array.item_status_description);
		STATUS_ERROR = context.getString(R.string.item_text_cannot_get_package_status);
		statusTitleColor = context.getResources().getColor(R.color.package_list_status_title_color);
		statusSubtextColor = context.getResources().getColor(R.color.package_list_status_subtext_color);
	}

	@Override
	public void onCreate() {

	}

	@Override
	public void onDataSetChanged() {

	}

	@Override
	public void onDestroy() {

	}

	@Override
	public int getCount() {
		return mDatabase.getDeliveringData().size();
	}

	@Override
	public RemoteViews getViewAt(int i) {
		final RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_list_package_for_widget);

		Package p = mDatabase.getDeliveringData().get(i);

		views.setTextViewText(R.id.tv_title, p.name);

		if (p.data != null && p.data.size() > 0) {
			Package.Status status = p.data.get(0);
			Spanny spanny = new Spanny(STATUS_STRING_ARRAY[p.getState()], new ForegroundColorSpan(statusTitleColor))
					.append(" - " + status.context, new ForegroundColorSpan(statusSubtextColor));
			views.setTextViewText(R.id.tv_other, spanny);
		} else {
			/** Set placeholder when cannot get data */
			views.setTextViewText(R.id.tv_other, STATUS_ERROR);
		}

		/** Set CircleImageView */
		views.setTextViewText(R.id.tv_first_char, p.name.substring(0, 1));
		Bitmap b = ScreenUtils.drawableToBitmap(new ColorDrawable(ColorGenerator.MATERIAL.getColor(p.name)));
		views.setImageViewBitmap(R.id.iv_logo, b);

		/** Add paddingTop/Bottom to the first or last item */
		if (i == 0) {
			views.setViewPadding(R.id.item_container, 0, (int) DP_16_TO_PX, 0, 0);
		} else if (i == getCount()) {
			views.setViewPadding(R.id.item_container, 0, 0, 0, (int) DP_16_TO_PX);
		}

		Intent intent = new Intent();
		intent.putExtra(EXTRA_PACKAGE_JSON, p.toJsonString());
		intent.putExtra(EXTRA_STATE, p.getState());
		views.setOnClickFillInIntent(R.id.item_container, intent);

		return views;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

}
