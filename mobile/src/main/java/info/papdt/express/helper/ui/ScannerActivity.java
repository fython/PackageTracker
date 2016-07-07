package info.papdt.express.helper.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;

import com.google.zxing.Result;

import info.papdt.express.helper.R;
import info.papdt.express.helper.ui.common.AbsActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AbsActivity implements ZXingScannerView.ResultHandler {

	private ZXingScannerView mScannerView;

	public static final int REQUEST_CODE_SCAN = 10001, RESULT_GET_RESULT = 1000;
	public static final String EXTRA_RESULT = "extra_result";

	private static final int REQUEST_PERMISSION = 20001;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
			);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			getWindow().setStatusBarColor(Color.TRANSPARENT);
		}
		setContentView(R.layout.activity_scanner);

		if (!isCameraPermissionGranted()) {
			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
				$(R.id.iv_error).setVisibility(View.VISIBLE);
				new AlertDialog.Builder(this)
						.setTitle(R.string.dialog_explanation_permission_title)
						.setMessage(R.string.dialog_explanation_permission_message)
						.setPositiveButton(R.string.dialog_explanation_permission_pos_btn, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int i) {
								jumpToSettings();
							}
						})
						.setNegativeButton(android.R.string.cancel, null)
						.show();
			} else {
				ActivityCompat.requestPermissions(
						this,
						new String[]{Manifest.permission.CAMERA},
						REQUEST_PERMISSION
				);
			}
		}
	}

	@Override
	protected void setUpViews() {
		mScannerView = $(R.id.scanner_view);
		mActionBar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isCameraPermissionGranted()) {
			startScan();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mScannerView.stopCamera();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		if (requestCode == REQUEST_PERMISSION) {
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				startScan();
			} else {
				Snackbar.make(mScannerView, R.string.toast_permission_denied, Snackbar.LENGTH_INDEFINITE)
						.setAction(R.string.toast_permission_denied_action, new View.OnClickListener() {
							@Override
							public void onClick(View view) {
								jumpToSettings();
							}
						})
				.show();
			}
		}
	}

	private void startScan() {
		$(R.id.iv_error).setVisibility(View.INVISIBLE);
		mScannerView.setResultHandler(this);
		mScannerView.startCamera();
	}

	private boolean isCameraPermissionGranted() {
		return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
	}

	private void jumpToSettings() {
		Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getApplication().getPackageName()));
		startActivity(intent);
	}

	@Override
	public void handleResult(Result result) {
		Intent intent = new Intent();
		intent.putExtra(EXTRA_RESULT, result.getText());
		setResult(RESULT_GET_RESULT, intent);
		finish();
	}

}
