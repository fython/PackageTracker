package info.papdt.express.helper.support;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class FileUtils {

	public static void saveFile(Context context, String name, String text) throws IOException {
		FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE);
		fos.write(text.getBytes());
		fos.close();
	}

	public static String readFile(Context context, String name) throws IOException{
		File file = context.getFileStreamPath(name);
		InputStream is = new FileInputStream(file);

		byte b[] = new byte[(int) file.length()];

		is.read(b);
		is.close();

		String string = new String(b);

		return string;
	}

	public static String readTextFromUri(Context context, Uri uri) throws IOException {
		InputStream inputStream = context.getContentResolver().openInputStream(uri);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
		}
		inputStream.close();
		reader.close();
		return stringBuilder.toString();
	}

	public static void writeTextToUri(Context context, Uri uri, String string) throws IOException {
		OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
		outputStream.write(string.getBytes());
		outputStream.close();
	}

}
