package info.papdt.express.helper.model;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.papdt.express.helper.api.PackageApi;

public class Package {

	/** Query data */
	@Expose @SerializedName("message") public String message;
	@Expose @SerializedName("nu") public String number;
	@Expose @SerializedName("com") public String companyType;
	@Expose @SerializedName("companytype") private String companyType1;
	@Expose @SerializedName("ischeck") public String isCheck;
	@Expose @SerializedName("updatetime") public String updateTime;
	@Expose @SerializedName("status") public String status;
	@Expose @SerializedName("condition") public String condition;
	@Expose @SerializedName("codenumber") public String codeNumber;
	@Expose @SerializedName("data") public ArrayList<Status> data;
	@Expose @SerializedName("state") private String state;

	/** Local data */
	@Expose public boolean shouldPush = false;
	@Expose public boolean unreadNew = false;
	@Expose public String name;
	@Expose public String companyChineseName;

	public static final int STATUS_FAILED = 2, STATUS_NORMAL = 0, STATUS_ON_THE_WAY = 5,
			STATUS_DELIVERED = 3, STATUS_RETURNED = 4, STATUS_RETURNING = 6 , STATUS_OTHER = 1;

	public int getState() {
		return state != null ? Integer.parseInt(state) : STATUS_FAILED;
	}

	public void setState(int status) {
		this.state = String.valueOf(status);
	}

	public static Package buildFromJson(String json) {
		Package p = new Gson().fromJson(json, Package.class);
		if (p.companyChineseName == null && p.companyType != null) {
			p.companyChineseName = PackageApi.CompanyInfo.getNameByCode(p.companyType);
		}
		if (p.companyType == null) {
			p.companyType = p.companyType1;
		}
		return p;
	}

	public void applyNewData(Package newData) {
		if (newData == null) return;

		this.unreadNew |= this.shouldPush |= newData.data.size() != this.data.size() || newData.getState() != this.getState();
		this.status = newData.status;
		this.state = newData.state;
		this.updateTime = newData.updateTime;
		this.isCheck = newData.isCheck;
		this.condition = newData.condition;
		this.message = newData.message;
		if (newData.data != null) {
			this.data = newData.data;
		}
	}

	public String toJsonString() {
		return new Gson().toJson(this);
	}

	public long getId() {
		if (TextUtils.isDigitsOnly(number)) {
			return Long.parseLong(number);
		} else {
			StringBuffer sb = new StringBuffer();
			List<String> DIGITS = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9");
			for (int i = 0; i < number.length(); i++) {
				String s;
				if (DIGITS.contains(s = number.substring(i, i+1))) {
					sb.append(s);
				}
			}
			return Long.parseLong(sb.toString());
		}
	}

	public class Status {

		@Expose public String time, location, context, ftime;

	}

}
