package info.papdt.express.helper.support;

import android.util.Log;

import java.io.IOException;

import info.papdt.express.helper.model.BaseMessage;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtils {

	private static OkHttpClient client = new OkHttpClient();

	private static final String UA_CHROME = "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.84 Safari/537.36";

	private static final String TAG = HttpUtils.class.getSimpleName();

	public static BaseMessage<String> getString(String url, String ua) {
		BaseMessage<String> result = new BaseMessage<>();

		Request request = new Request.Builder().url(url).addHeader("User-Agent", ua).build();
		try {
			Response response = client.newCall(request).execute();
			result.setCode(response.code());
			result.setData(response.body().string());
			Log.i(TAG, result.getData());
		} catch (IOException e) {
			result.setCode(BaseMessage.CODE_ERROR);
			e.printStackTrace();
		}

		return result;
	}

	public static BaseMessage<String> getString(String url, boolean useLocalUA) {
		return getString(
				url,
				useLocalUA ? System.getProperty("http.agent") : UA_CHROME
		);
	}

}
