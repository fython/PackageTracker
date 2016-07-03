package info.papdt.express.helper.dao;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.io.IOException;
import java.util.ArrayList;

import info.papdt.express.helper.api.PackageApi;
import info.papdt.express.helper.model.BaseMessage;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.support.FileUtils;

public class PackageDatabase {

	@Expose private ArrayList<Package> data;
	private ArrayList<Package> delivered, delivering;
	private Context mContext;

	private static PackageDatabase sInstance;

	private Package lastRemovedData = null;
	private int lastRemovedPosition = -1;

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
		refreshList();
	}

	public boolean save() {
		try {
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
			FileUtils.saveFile(mContext, FILE_NAME, gson.toJson(this));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void refreshList() {
		delivered = new ArrayList<>();
		delivering = new ArrayList<>();
		for (Package p : data) {
			if (p.getState() == Package.STATUS_DELIVERED) {
				delivered.add(p);
			} else {
				delivering.add(p);
			}
		}
	}

	public void add(Package pack) {
		data.add(pack);
		refreshList();
	}

	public void add(int index, Package pack) {
		data.add(index, pack);
		refreshList();
	}

	public void set(int index, Package pack) {
		data.set(index, pack);
		refreshList();
	}

	public void remove(int index) {
		final Package removedItem = data.remove(index);

		lastRemovedData = removedItem;
		lastRemovedPosition = index;

		refreshList();
	}

	public void remove(Package pack) {
		int nowPos = indexOf(pack);
		remove(nowPos);
	}

	public int undoLastRemoval() {
		if (lastRemovedData != null) {
			int insertedPosition;
			if (lastRemovedPosition >= 0 && lastRemovedPosition < data.size()) {
				insertedPosition = lastRemovedPosition;
			} else {
				insertedPosition = data.size();
			}

			data.add(insertedPosition, lastRemovedData);
			refreshList();

			lastRemovedData = null;
			lastRemovedPosition = -1;

			return insertedPosition;
		} else {
			return -1;
		}
	}

	public ArrayList<Package> getData() {
		return data;
	}

	public ArrayList<Package> getDeliveredData() {
		return delivered;
	}

	public ArrayList<Package> getDeliveringData() {
		return delivering;
	}

	public int indexOf(Package p) {
		return indexOf(p.number);
	}

	public int indexOf(String number) {
		for (int index = 0; index < size(); index++) {
			if (get(index).number.equals(number)) return index;
		}
		return -1;
	}

	public void clear() {
		data.clear();
		refreshList();
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
			if (newPack.getCode() == BaseMessage.CODE_OKAY && newPack.getData().data != null) {
				newPack.getData().shouldPush = newPack.getData().data.size() > pack.data.size();
				this.set(i, newPack.getData());
			} else {
				Log.e(TAG, "Package " + pack.codeNumber + " couldn\'t get new info.");
			}
		}
		refreshList();
	}

}
