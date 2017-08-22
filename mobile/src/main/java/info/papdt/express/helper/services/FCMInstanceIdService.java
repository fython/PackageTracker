package info.papdt.express.helper.services;

import android.util.Log;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FCMInstanceIdService extends FirebaseInstanceIdService {

	@Override
	public void onTokenRefresh() {
		// Get updated InstanceID token.
		String refreshedToken = FirebaseInstanceId.getInstance().getToken();
		Log.d(FCMInstanceIdService.class.getSimpleName(), "Refreshed token: " + refreshedToken);

		// sendRegistrationToServer(refreshedToken);
	}

}
