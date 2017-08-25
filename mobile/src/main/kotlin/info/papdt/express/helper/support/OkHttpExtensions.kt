package info.papdt.express.helper.support

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody

const val APPLICATION_JSON = "application/json"
const val APPLICATION_FORM = "application/x-www-form-urlencoded"

fun Request.Builder.postJson(string: String)
		= this.post(RequestBody.create(APPLICATION_JSON.asMediaType(), string))

fun Request.Builder.postForm(form: Map<String, String>): Request.Builder
		= this.post(RequestBody.create(APPLICATION_FORM.asMediaType(), form.map { it.key + "=" + it.value }.reduce { acc, s -> "$acc&$s" }))

private fun String.asMediaType() = MediaType.parse(this)