package info.papdt.express.helper.asynctask;

import android.os.AsyncTask;

import com.spreada.utils.chinese.ZHConverter;

import java.util.ArrayList;

import info.papdt.express.helper.api.PackageApi;

public class CompanyFilterTask extends AsyncTask<String, Void, ArrayList<PackageApi.CompanyInfo.Company>>{

	@Override
	protected ArrayList<PackageApi.CompanyInfo.Company> doInBackground(String... strings) {
		String keyword = ZHConverter.convert(strings[0], ZHConverter.SIMPLIFIED);
		ArrayList<PackageApi.CompanyInfo.Company> src = new ArrayList<>();
		if (keyword != null && keyword.trim().length() > 0) {
			for (int i = 0; i < PackageApi.CompanyInfo.info.size(); i++) {
				if (!PackageApi.CompanyInfo.names [i].contains(keyword) && !PackageApi.CompanyInfo.pinyin [i].contains(keyword)) {
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
