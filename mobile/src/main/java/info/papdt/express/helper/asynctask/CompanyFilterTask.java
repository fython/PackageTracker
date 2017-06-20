package info.papdt.express.helper.asynctask;

import android.os.AsyncTask;

import com.spreada.utils.chinese.ZHConverter;

import java.util.ArrayList;

import info.papdt.express.helper.api.PackageApi;

public class CompanyFilterTask extends AsyncTask<String, Void, ArrayList<PackageApi.CompanyInfo.Company>>{

	@Override
	protected ArrayList<PackageApi.CompanyInfo.Company> doInBackground(String... strings) {
		return doSync(strings[0]);
	}

	public static ArrayList<PackageApi.CompanyInfo.Company> doSync(String keyword) {
		keyword = ZHConverter.convert(keyword, ZHConverter.SIMPLIFIED).replaceAll("快递", "");
		ArrayList<PackageApi.CompanyInfo.Company> src = new ArrayList<>();
		if (keyword != null && keyword.trim().length() > 0) {
			for (int i = 0; i < PackageApi.CompanyInfo.info.size(); i++) {
				if (!PackageApi.CompanyInfo.names [i].toLowerCase().contains(keyword.toLowerCase())
						&& !PackageApi.CompanyInfo.pinyin[i].contains(keyword)) {
					continue;
				}

				src.add(PackageApi.CompanyInfo.info.get(i));
			}
		} else {
			return PackageApi.CompanyInfo.info;
		}
		return src;
	}

}
