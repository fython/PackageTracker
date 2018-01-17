package info.papdt.express.helper.model

data class CommonStatus(
		val content: String,
		val time: String,
		val location: String
) {

	fun toOldPackageStatus(): Package.Status {
		val status = Package.Status()
		status.context = content
		status.time = time
		status._location = location
		return status
	}
}