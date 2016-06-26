package info.papdt.express.helper.asynctask;

import android.os.AsyncTask;

import info.papdt.express.helper.api.PackageApi;
import info.papdt.express.helper.model.BaseMessage;
import info.papdt.express.helper.model.Package;

public abstract class GetPackageTask extends AsyncTask<String, Void, BaseMessage<Package>> {

	/**
	 * @param values Package number and company code
	 * @return target package
	 */
	@Override
	protected BaseMessage<Package> doInBackground(String... values) {
		String number = values[0];
		String company = values.length > 1 ? values[1] : null;
		return company == null ? PackageApi.getPackageByNumber(number) : PackageApi.getPackage(company, number);
	}

}
