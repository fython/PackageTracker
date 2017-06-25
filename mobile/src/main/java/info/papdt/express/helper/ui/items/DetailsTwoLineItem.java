package info.papdt.express.helper.ui.items;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class DetailsTwoLineItem {

	private String type, content, optional;

	public static final String TYPE_NAME = "name", TYPE_NUMBER = "number";

	@StringDef({TYPE_NAME, TYPE_NUMBER})
	@Retention(RetentionPolicy.SOURCE)
	public @interface ItemType {}

	public DetailsTwoLineItem(@ItemType String type, @NonNull String content) {
		this.type = type;
		this.content = content;
	}

	public DetailsTwoLineItem(@ItemType String type, @NonNull String content, @Nullable String optional) {
		this.type = type;
		this.content = content;
		this.optional = optional;
	}

	public @ItemType String getType() {
		return this.type;
	}

	public @NonNull String getContent() {
		return this.content;
	}

	public String getOptional() {
		return this.optional;
	}

}
