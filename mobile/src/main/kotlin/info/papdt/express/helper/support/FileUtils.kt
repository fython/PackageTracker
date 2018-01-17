package info.papdt.express.helper.support

import android.content.Context
import android.net.Uri

import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader

object FileUtils {

    @Throws(IOException::class)
    fun saveFile(context: Context, name: String, text: String) {
        val fos = context.openFileOutput(name, Context.MODE_PRIVATE)
        fos.write(text.toByteArray())
        fos.close()
    }

    @Throws(IOException::class)
    fun readFile(context: Context, name: String): String {
        val file = context.getFileStreamPath(name)
        val `is` = FileInputStream(file)

        val b = ByteArray(file.length().toInt())

        `is`.read(b)
        `is`.close()

        return String(b)
    }

    @Throws(IOException::class)
    fun readTextFromUri(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(inputStream!!))
        val stringBuilder = StringBuilder()
        var line: String? = reader.readLine()
        while (line != null) {
            stringBuilder.append(line)
            line = reader.readLine()
        }
        inputStream.close()
        reader.close()
        return stringBuilder.toString()
    }

    @Throws(IOException::class)
    fun writeTextToUri(context: Context, uri: Uri, string: String) {
        val outputStream = context.contentResolver.openOutputStream(uri)
        outputStream!!.write(string.toByteArray())
        outputStream.close()
    }

}
