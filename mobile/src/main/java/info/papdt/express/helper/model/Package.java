package info.papdt.express.helper.model;

import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		try {
			Package p = new Gson().fromJson(json, Package.class);
			if (p.companyChineseName == null && p.companyType != null) {
				p.companyChineseName = PackageApi.CompanyInfo.getNameByCode(p.companyType);
			}
			if (p.companyType == null) {
				p.companyType = p.companyType1;
			}
			return p;
		}catch (Exception e){
			//may not be a json string
			e.printStackTrace();
			return new Package();
		}
	}

	public void applyNewData(Package newData) {
		if (newData == null) return;

		try {
			this.unreadNew |= this.shouldPush = !this.data.get(0).time.equalsIgnoreCase(newData.data.get(0).time);
		} catch (Exception e) {
			if (newData.data != null && this.data == null) {
				this.unreadNew |= this.shouldPush = true;
			} else {
				this.unreadNew |= this.shouldPush = false;
			}
		}
		this.status = newData.status;
		this.state = newData.state;
		this.updateTime = newData.updateTime;
		this.isCheck = newData.isCheck;
		this.condition = newData.condition;
		this.message = newData.message;
		if (newData.data != null && !newData.data.isEmpty()) {
			this.data = newData.data;
		} else {
			this.unreadNew = this.shouldPush = false;
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

	public static class Status {

		@Expose public String time, location = null, context, ftime;
		@Expose public String phone;

		private void processOldData() {
			String qszp = "签收照片,";
			if (context.startsWith(qszp)) {
				context = context
						.substring(context.indexOf(qszp) + qszp.length(), context.length())
						.trim();
			}
		}

		public String getLocation() {
			processOldData(); // dirty method
			if (location != null && location.trim().length() > 0)	return location;

			if (context.contains("【")) {
				location = context.substring(context.indexOf("【") + 1, context.indexOf("】")).trim();
			}
			if (context.contains("[")) {
				location = context.substring(context.indexOf("[") + 1, context.indexOf("]")).trim();
			}
			return location;
		}

		public String getPhone() {
			if (phone != null) return phone;
			return phone = Status.findContact(context);
		}

		public static String findContact(String s) {
			String number = checkNum(s);
			if (number == null || number.length() < 8) return null;
			if (number.contains(",")) number = number.substring(0, number.indexOf(","));
			return number;
		}

		private static String checkNum(String num){
			if (num == null || num.length() == 0) return "";
			Pattern pattern = Pattern.compile("(?<!\\d)(?:(?:1[3578]\\d{9})|(?:861[3578]\\d{9}))(?!\\d)");
			Matcher matcher = pattern.matcher(num);
			StringBuffer bf = new StringBuffer(64);
			while (matcher.find()) {
				bf.append(matcher.group()).append(",");
			}
			int len = bf.length();
			if (len > 0) {
				bf.deleteCharAt(len - 1);
			}
			return bf.toString();
		}

	}

}
