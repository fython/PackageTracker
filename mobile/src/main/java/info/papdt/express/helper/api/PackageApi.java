package info.papdt.express.helper.api;

import android.util.Log;

import com.google.gson.Gson;
import com.spreada.utils.chinese.ZHConverter;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.lang.reflect.Array;
import java.util.ArrayList;

import info.papdt.express.helper.model.BaseMessage;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.HttpUtils;

/**
 * @author Fung Go (fython@163.com)
 * @version 2.0
 */
public class PackageApi {

	/** API Url */
	private final static String API_HOST = "http://www.kuaidi100.com";
	private final static String QUERY_URL = API_HOST + "/query?type=%1$s&postid=%2$s";
	private final static String COMPANY_DETECT_URL = API_HOST + "/autonumber/autoComNum?text=%s";
	private final static String COMPANY_LIST_URL = API_HOST+"/js/share/company.js";

	private final static String TAG = PackageApi.class.getSimpleName();

	/**
	 * @param com Shipment company
	 * @param number Package number
	 * @return query url
	 */
	public static String getQueryUrl(String com, String number) {
		String url = String.format(QUERY_URL, com, number);
		Log.i(TAG, "query url: " + url);
		return url;
	}

	/**
	 * @param number The number of package which you want to get its company
	 * @return company code query url
	 */
	public static String getCompantDetectUrl(String number) {
		return String.format(COMPANY_DETECT_URL, number);
	}

	/**
	 * @return company list query url
	 */
	public static String getCompanyListUrl(){
		return COMPANY_LIST_URL;
	}

	/**
	 * @param number The number of package which you want to get its company
	 * @return company code
	 */
	public static String detectCompanyByNumber(String number) {
		BaseMessage<String> message = HttpUtils.getString(getCompantDetectUrl(number), false);
		if (message.getCode() == BaseMessage.CODE_OKAY) {
			DetectResult result = new Gson().fromJson(message.getData(), DetectResult.class);
			if (result.auto.size() > 0) {
				return result.auto.get(0).comCode;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private static ArrayList<CompanyInfo.Company> getCompanyList(){
		BaseMessage<String> message = HttpUtils.getString(getCompanyListUrl(), false);
		if (message.getCode() == BaseMessage.CODE_OKAY) {
			String strJson = message.getData().replace("var jsoncom=", "");
			strJson = strJson.replace("};", "}");
			strJson = strJson.replace("'", "\"");
			CompanyListResult result = new Gson().fromJson(strJson, CompanyListResult.class);
			if(result.company.size() > 0 && result.error_size < 0) {
				ArrayList<CompanyInfo.Company> info = new ArrayList<>();
				for(int i=0; i<result.company.size(); i++) {
					info.add(new CompanyInfo.Company(result.company.get(i).companyname,
							result.company.get(i).code, result.company.get(i).tel,
							result.company.get(i).comurl));
				}
				return info;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	/**
	 * @param number The number of package which you want to query
	 * @return Package and status code
	 */
	public static BaseMessage<Package> getPackageByNumber(String number) {
		String comcode = detectCompanyByNumber(number);
		return getPackage(comcode, number);
	}

	/**
	 * @param comcode The company code of package
	 * @param number The number of package
	 * @return Package and status code
	 */
	public static BaseMessage<Package> getPackage(String comcode, String number) {
		BaseMessage<String> message = HttpUtils.getString(getQueryUrl(comcode, number), false);
		if (message.getCode() == BaseMessage.CODE_OKAY) {
			Package pkg = Package.buildFromJson(message.getData());
			if(pkg.status.equals("200")) {
				return new BaseMessage<>(BaseMessage.CODE_OKAY, pkg);
			} else {
				pkg.number = number;
				pkg.companyType = comcode;
				pkg.companyChineseName = PackageApi.CompanyInfo.getNameByCode(pkg.companyType);
				pkg.data = new ArrayList<Package.Status>();
				return new BaseMessage<>(BaseMessage.CODE_OKAY, pkg);
			}
		} else {
			return new BaseMessage<>(BaseMessage.CODE_ERROR);
		}
	}

	/** Filter companies by keyword */
	public static ArrayList<CompanyInfo.Company> searchCompany(String keyword) {
		keyword = ZHConverter.convert(keyword, ZHConverter.SIMPLIFIED);
		ArrayList<CompanyInfo.Company> src = new ArrayList<>();
		if (keyword != null && keyword.trim().length() > 0) {
			for (int i = 0; i < CompanyInfo.info.size(); i++) {
				if (!CompanyInfo.names [i].contains(keyword) && !CompanyInfo.pinyin [i].contains(keyword)) {
					continue;
				}

				src.add(CompanyInfo.info.get(i));
			}
		} else {
			return CompanyInfo.info;
		}
		return src;
	}

	private class DetectResult {
		String comCode, num;
		ArrayList<AutoInfo> auto;

		class AutoInfo {
			String comCode, id, noPre, startTime;
			int noCount;
		}
	}

	private class CompanyListResult {
		int error_size;
		ArrayList<CompanyInfo> company;

		class CompanyInfo {
			String cid, id, companyname, shortname, tel, url, code, comurl, isavailable, promptinfo,
					testnu, freg, freginfo, telcomplaintnum, queryurl, serversite;
			int hasvali;
		}
	}

	public static class CompanyInfo {

		public static ArrayList<Company> info;
		public static String[] names, pinyin;

		public static class Company {

			public String name, code, phone, website;

			public Company(String name, String code, String phone, String website) {
				this.name = name;
				this.code = code;
				this.phone = phone;
				this.website = website;
			}

		}

		public static int findCompanyByCode(String code) {
			for (int i = 0; i < info.size(); i++) {
				if (info.get(i).code.equals(code)) {
					return i;
				}
			}
			return -1;
		}

		public static String getNameByCode(String code) {
			int index = findCompanyByCode(code);
			return index != -1 ? info.get(index).name : null;
		}

		static {
			info = getCompanyList();
			names = new String[info.size()];
			pinyin = new String[info.size()];

			HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
			format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

			for (int i = 0; i < info.size(); i++) {
				names [i] = info.get(i).name;

				StringBuffer sb = new StringBuffer();
				for (int j = 0; j < names [i].length(); j++) {
					try {
						String[] s = PinyinHelper.toHanyuPinyinStringArray(names[i].toCharArray() [j], format);
						if (s == null) continue;
						sb.append(s[0].toCharArray() [0]);
					} catch (BadHanyuPinyinOutputFormatCombination e) {
						e.printStackTrace();
					}
				}

				pinyin [i] = sb.toString();
			}
		}

	}

}
