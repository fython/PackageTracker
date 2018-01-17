package info.papdt.express.helper.dao

import android.content.Context
import android.util.Log

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose

import java.io.IOException
import java.util.ArrayList

import info.papdt.express.helper.api.Kuaidi100PackageApi
import info.papdt.express.helper.api.PushApi
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Package
import info.papdt.express.helper.support.FileUtils
import info.papdt.express.helper.support.SettingsInstance

class PackageDatabase private constructor(private val mContext: Context) {

	@Expose @Volatile
	lateinit var data: ArrayList<Package>
		private set
	@Volatile
	lateinit var deliveredData: ArrayList<Package>
		private set
	@Volatile
	lateinit var deliveringData: ArrayList<Package>
		private set
	@Expose
	var dataVersion: String? = "2.5.0"
		private set

	private var lastRemovedData: Package? = null
	private var lastRemovedPosition = -1

	init {
		this.load()
	}

	fun load() {
		var json: String
		try {
			json = FileUtils.readFile(mContext, FILE_NAME)
		} catch (e: IOException) {
			json = "{\"data\":[]}"
			e.printStackTrace()
		}

		this.data = Gson().fromJson(json, PackageDatabase::class.java).data
		refreshList()
	}

	fun restoreData(json: String) {
		this.data = Gson().fromJson(json, PackageDatabase::class.java).data
		PushApi.sync(getPackageIdList()).subscribe()
		refreshList()
	}

	val backupData: String
		get() {
			if (dataVersion == null) {
				dataVersion = "2.5.0"
			}
			return GsonBuilder().excludeFieldsWithoutExposeAnnotation().create().toJson(this)
		}

	fun save(): Boolean {
		return try {
			val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
			FileUtils.saveFile(mContext, FILE_NAME, gson.toJson(this))
			true
		} catch (e: IOException) {
			e.printStackTrace()
			false
		}
	}

	fun refreshList() {
		deliveredData = ArrayList()
		deliveringData = ArrayList()
		for (p in data) {
			if (p.getState() == Package.STATUS_DELIVERED) {
				deliveredData.add(p)
			} else {
				deliveringData.add(p)
			}
		}
	}

	fun add(pack: Package) {
		data.add(pack)
		PushApi.add(pack.number!!, pack.companyType).subscribe()
		refreshList()
	}

	fun add(index: Int, pack: Package) {
		data.add(index, pack)
		PushApi.add(pack.number!!, pack.companyType).subscribe()
		refreshList()
	}

	operator fun set(index: Int, pack: Package) {
		data[index] = pack
		refreshList()
	}

	fun remove(index: Int) {
		val removedItem = data.removeAt(index)

		PushApi.remove(removedItem.number!!).subscribe()

		lastRemovedData = removedItem
		lastRemovedPosition = index

		refreshList()
	}

	fun remove(pack: Package) {
		val nowPos = indexOf(pack)
		remove(nowPos)
	}

	fun undoLastRemoval(): Int {
		if (lastRemovedData != null) {
			val insertedPosition: Int = if (lastRemovedPosition >= 0 && lastRemovedPosition < data.size) {
				lastRemovedPosition
			} else {
				data.size
			}

			data.add(insertedPosition, lastRemovedData!!)
			refreshList()

			lastRemovedData = null
			lastRemovedPosition = -1

			return insertedPosition
		} else {
			return -1
		}
	}

	fun indexOf(p: Package): Int {
		return indexOf(p.number!!)
	}

	fun indexOf(number: String): Int {
		return (0 until size()).firstOrNull { get(it).number == number } ?: -1
	}

	fun clear() {
		data.clear()
		refreshList()
	}

	fun size(): Int {
		return data.size
	}

	operator fun get(index: Int): Package {
		return data[index]
	}

	fun getPackageIdList(): List<String> = data.map { "${it.number}+${it.companyType}" }

	fun pullDataFromNetwork(shouldRefreshDelivered: Boolean) {
		if (SettingsInstance.enablePush) PushApi.sync(getPackageIdList()).subscribe()
		for (i in 0 until size()) {
			val pack = this[i]
			if (!shouldRefreshDelivered && pack.getState() == Package.STATUS_DELIVERED) {
				continue
			}
			val newPack = Kuaidi100PackageApi.getPackage(pack.companyType, pack.number!!)
			if (newPack.code == BaseMessage.CODE_OKAY && newPack.data?.data != null) {
				pack.applyNewData(newPack.data)
				this[i] = pack
			} else {
				Log.e(TAG, "Package " + pack.codeNumber + " couldn\'t get new info.")
			}
		}
		refreshList()
	}

	companion object {

		@Volatile private var sInstance: PackageDatabase? = null

		private const val FILE_NAME = "packages.json"

		private val TAG = PackageDatabase::class.java.simpleName

		fun getInstance(context: Context): PackageDatabase {
			if (sInstance == null) {
				synchronized(PackageDatabase::class.java) {
					if (sInstance == null) {
						sInstance = PackageDatabase(context)
					}
				}
			}
			return sInstance!!
		}
	}

}
