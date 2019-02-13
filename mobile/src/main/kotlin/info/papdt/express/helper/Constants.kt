package info.papdt.express.helper

const val BUGLY_APP_ID = "a499a51858"
const val BUGLY_ENABLE_DEBUG = false

const val RESULT_EXTRA_PACKAGE_JSON = "package_json"
const val RESULT_EXTRA_COMPANY_CODE = "company_code"

const val REQUEST_ADD = 10001
const val REQUEST_DETAILS = 10002
const val REQUEST_CODE_CHOOSE_COMPANY = 10003
const val RESULT_NEW_PACKAGE = 2000
const val RESULT_DELETED = 2001
const val RESULT_RENAMED = 2002

const val ACTION_PREFIX = BuildConfig.APPLICATION_ID + ".action"
const val ACTION_SEARCH = "$ACTION_PREFIX.SEARCH"
const val ACTION_REQUEST_DELETE_PACK = "$ACTION_PREFIX.REQUEST_DELETE_PACK"

const val EXTRA_PREFIX = BuildConfig.APPLICATION_ID + ".extra"
const val EXTRA_DATA = "$EXTRA_PREFIX.DATA"
const val EXTRA_OLD_DATA = "$EXTRA_PREFIX.OLD_DATA"

const val CHANNEL_ID_PACKAGE_STATUS = "package_status"