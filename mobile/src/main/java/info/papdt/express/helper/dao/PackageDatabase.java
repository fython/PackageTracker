package info.papdt.express.helper.dao;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import info.papdt.express.helper.api.PackageApi;
import info.papdt.express.helper.model.BaseMessage;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.FileUtils;

public class PackageDatabase {

	private ArrayList<Package> data;
	private Context mContext;

	private static PackageDatabase sInstance;

	private static final String FILE_NAME = "packages.json";

	private static final String TAG = PackageDatabase.class.getSimpleName();

	public static PackageDatabase getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new PackageDatabase(context);
		}
		return sInstance;
	}

	private PackageDatabase(Context context) {
		this.mContext = context;
		this.load();
	}

	public void load() {
		String json;
		try {
			json = FileUtils.readFile(mContext, FILE_NAME);
		} catch (IOException e) {
			json = "{\"data\":[]}";
			e.printStackTrace();
		}
		this.data = new Gson().fromJson(json, PackageDatabase.class).data;
	}

	public boolean save() {
		try {
			FileUtils.saveFile(mContext, FILE_NAME, new Gson().toJson(this));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void add(Package pack) {
		data.add(pack);
	}

	public void add(int index, Package pack) {
		data.add(index, pack);
	}

	public void set(int index, Package pack) {
		data.set(index, pack);
	}

	public void remove(int index) {
		data.remove(index);
	}

	public void remove(Package pack) {
		data.remove(pack);
	}

	public void clear() {
		data.clear();
	}

	public int size() {
		return data.size();
	}

	public Package get(int index) {
		return data.get(index);
	}

	public void pullDataFromNetwork(boolean shouldRefreshDelivered) {
		for (int i = 0; i < size(); i++) {
			Package pack = this.get(i);
			if (!shouldRefreshDelivered && pack.getState() == Package.STATUS_DELIVERED) {
				continue;
			}
			BaseMessage<Package> newPack = PackageApi.getPackage(pack.companyType, pack.number);
			if (newPack.getCode() == BaseMessage.CODE_OKAY) {
				newPack.getData().shouldPush = newPack.getData().data.size() > pack.data.size();
				this.set(i, newPack.getData());
			} else {
				Log.e(TAG, "Package " + pack.codeNumber + " couldn\'t get new info.");
			}
		}
	}

}
