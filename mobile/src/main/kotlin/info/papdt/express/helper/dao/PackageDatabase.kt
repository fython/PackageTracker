package info.papdt.express.helper.dao

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose

import java.io.IOException
import java.util.ArrayList

import info.papdt.express.helper.api.PackageApi
import info.papdt.express.helper.api.PushApi
import info.papdt.express.helper.model.BaseMessage
import info.papdt.express.helper.model.Kuaidi100Package
import info.papdt.express.helper.support.FileUtils
import info.papdt.express.helper.support.SettingsInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PackageDatabase private constructor(private val mContext: Context) {

	@Expose @Volatile
	lateinit var data: ArrayList<Kuaidi100Package>
		private set
	@Expose
	var dataVersion: String? = "5.0.0"
		private set
	@Expose @Volatile
	var lastUpdateTime: Long = 0

	private var lastRemovedData: Kuaidi100Package? = null
	private var lastRemovedPosition = -1

	init {
		this.load()
	}

	fun load() {
		var json: String? = null
		try {
			json = FileUtils.readFile(mContext, FILE_NAME)
		} catch (e: IOException) {
			e.printStackTrace()
		}
        json = json ?: "{\"data\":[], \"lastUpdateTime\":0}"

		this.data = Gson().fromJson(json, PackageDatabase::class.java).data
	}

	fun restoreData(json: String) {
		this.data = Gson().fromJson(json, PackageDatabase::class.java).data
		CoroutineScope(Dispatchers.IO).launch { PushApi.sync(getPackageIdList()) }
	}

	val backupData: String
		get() {
			if (dataVersion == null) {
				dataVersion = "5.0.0"
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

    @Synchronized
	fun add(pack: Kuaidi100Package) {
		data.add(pack)
		CoroutineScope(Dispatchers.IO).launch { PushApi.add(pack.number!!, pack.companyType) }
	}

    @Synchronized
	fun add(index: Int, pack: Kuaidi100Package) {
		data.add(index, pack)
		CoroutineScope(Dispatchers.IO).launch { PushApi.add(pack.number!!, pack.companyType) }
	}

    @Synchronized
	operator fun set(index: Int, pack: Kuaidi100Package) {
		data[index] = pack
	}

    @Synchronized
	fun remove(index: Int) {
		val removedItem = data.removeAt(index)

		CoroutineScope(Dispatchers.IO).launch { PushApi.remove(removedItem.number!!) }

		lastRemovedData = removedItem
		lastRemovedPosition = index
	}

    @Synchronized
	fun remove(pack: Kuaidi100Package) {
		val nowPos = indexOf(pack)
		remove(nowPos)
	}

    @Synchronized
	fun undoLastRemoval(): Int {
		if (lastRemovedData != null) {
			val insertedPosition: Int = if (lastRemovedPosition >= 0 && lastRemovedPosition < data.size) {
				lastRemovedPosition
			} else {
				data.size
			}

			data.add(insertedPosition, lastRemovedData!!)

			lastRemovedData = null
			lastRemovedPosition = -1

			return insertedPosition
		} else {
			return -1
		}
	}

	fun indexOf(p: Kuaidi100Package): Int {
		return indexOf(p.number!!)
	}

	fun indexOf(number: String): Int {
		return (0 until size()).firstOrNull { get(it).number == number } ?: -1
	}

	fun clear() {
		data.clear()
	}

	fun size(): Int {
		return data.size
	}

	operator fun get(index: Int): Kuaidi100Package {
		return data[index]
	}

	fun getByNumber(number: String): Kuaidi100Package? {
		return data.find { it.number == number }
	}

	fun getPackageIdList(): List<String> = data.map { "${it.number}+${it.companyType}" }

	fun pullDataFromNetwork(shouldRefreshDelivered: Boolean) {
		if (SettingsInstance.enablePush) {
			CoroutineScope(Dispatchers.IO).launch { PushApi.sync(getPackageIdList()) }
		}
		for (i in 0 until size()) {
			val pack = this[i]
			if (!shouldRefreshDelivered && pack.getState() == Kuaidi100Package.STATUS_DELIVERED) {
				continue
			}
			val newPack = PackageApi.getPackage(pack.number!!, pack.companyType)
			if (newPack.code == BaseMessage.CODE_OKAY && newPack.data?.data != null) {
				pack.applyNewData(newPack.data)
				this[i] = pack
			} else {
				Log.e(TAG, "Kuaidi100Package " + pack.codeNumber + " couldn\'t get new info.")
			}
		}
	}

    fun readAll(): Int {
        var count = 0
		for (i in 0 until size()) {
			if (data[i].unreadNew) {
				count++
                data[i] = Kuaidi100Package(data[i])
				data[i].unreadNew = false
			}
		}
        return count
    }

    companion object {

		@SuppressLint("StaticFieldLeak")
        @Volatile
        private var sInstance: PackageDatabase? = null

		private const val FILE_NAME = "packages.json"

		private val TAG = PackageDatabase::class.java.simpleName

		fun getInstance(context: Context): PackageDatabase {
			if (sInstance == null) {
				synchronized(PackageDatabase::class.java) {
					if (sInstance == null) {
						sInstance = PackageDatabase(
                                if (context is Application) context else context.applicationContext
                        )
					}
				}
			}
			return sInstance!!
		}
	}

}
