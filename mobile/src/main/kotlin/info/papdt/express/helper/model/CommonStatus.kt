package info.papdt.express.helper.model

import android.arch.persistence.room.Entity

@Entity
data class CommonStatus(
		val content: String?,
		val time: String?,
		val location: String?
) {

	fun toOldPackageStatus(): Kuaidi100Package.Status {
		val status = Kuaidi100Package.Status()
		status.context = content
		status.time = time
		status._location = location
		return status
	}

    companion object {

        @JvmStatic fun Kuaidi100Package.Status.toNewPackageStatus(): CommonStatus {
            return CommonStatus(this.context, this.time, this._location)
        }

        @JvmStatic fun toNewPackageStatusList(oldData: List<Kuaidi100Package.Status>): MutableList<CommonStatus> {
            return oldData.map { it.toNewPackageStatus() }.toMutableList()
        }

    }
}