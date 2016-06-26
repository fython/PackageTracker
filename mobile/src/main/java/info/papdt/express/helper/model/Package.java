package info.papdt.express.helper.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Package {

	/** Query data */
	@SerializedName("message") public String message;
	@SerializedName("nu") public String number;
	@SerializedName("companyname") public String companyType;
	@SerializedName("ischeck") public String isCheck;
	@SerializedName("updatetime") public String updateTime;
	@SerializedName("status") public String status;
	@SerializedName("condition") public String condition;
	@SerializedName("codenumber") public String codeNumber;
	@SerializedName("data") public ArrayList<Status> data;
	@SerializedName("state") private String state;

	/** Local data */
	public boolean shouldPush = false;

	public static final int STATUS_FAILED = 2, STATUS_NORMAL = 0, STATUS_ON_THE_WAY = 5,
			STATUS_DELIVERED = 3, STATUS_RETURNED = 4 /* RETURNING 6 */, STATUS_OTHER = 1;

	public int getState() {
		return Integer.parseInt(state);
	}

	public void setState(int status) {
		this.state = String.valueOf(status);
	}

	public static Package buildFromJson(String json) {
		return new Gson().fromJson(json, Package.class);
	}

	public String toJsonString() {
		return new Gson().toJson(this);
	}

	public class Status {

		public String time, location, context, ftime;

	}

}
