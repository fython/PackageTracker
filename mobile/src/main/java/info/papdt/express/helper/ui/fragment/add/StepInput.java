package info.papdt.express.helper.ui.fragment.add;

import android.content.Intent;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import info.papdt.express.helper.R;
import info.papdt.express.helper.api.PackageApi;
import info.papdt.express.helper.asynctask.CompanyFilterTask;
import info.papdt.express.helper.asynctask.GetPackageTask;
import info.papdt.express.helper.dao.PackageDatabase;
import info.papdt.express.helper.model.BaseMessage;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.receiver.ConnectivityReceiver;
import info.papdt.express.helper.ui.AddActivity;
import info.papdt.express.helper.ui.ScannerActivity;

import java.util.ArrayList;

public class StepInput extends AbsStepFragment {

	private MaterialEditText mEditText;

	private String number;

	@Override
	protected int getLayoutResId() {
		return R.layout.fragment_add_step_input;
	}

	@Override
	protected void doCreateView(View rootView) {
		mEditText = $(R.id.et_number);
		mEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
		$(R.id.btn_scan).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getAddActivity(), ScannerActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivityForResult(intent, ScannerActivity.REQUEST_CODE_SCAN);
			}
		});

		if (!TextUtils.isEmpty(getAddActivity().getPreNumber())) {
			mEditText.setText(getAddActivity().getPreNumber());
		}

		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (i == EditorInfo.IME_ACTION_DONE) {
					mButtonBar.onRightButtonClick();
				}
				return false;
			}
		});
		mButtonBar.setOnRightButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				mEditText.setText(mEditText.getText().toString().trim());
				if (!checkNumberInput()) {
					Toast.makeText(getContext(), R.string.toast_number_wrong, Toast.LENGTH_SHORT).show();
					return;
				}

				if (checkExistance()) {
					Toast.makeText(getContext(), R.string.toast_number_exist, Toast.LENGTH_SHORT).show();
					return;
				}

				// Pass check
				number = mEditText.getText().toString();
				if (ConnectivityReceiver.Companion.readNetworkState(getActivity())) {
					if (TextUtils.isEmpty(getAddActivity().getPreCompany())) {
						new FindPackageTask().execute(number);
					} else {
						new FindCompanyAndGetPackageTask().execute(getAddActivity().getPreCompany());
					}
				} else {
					getAddActivity().addStep(AddActivity.STEP_NO_INTERNET_CONNECTION);
				}
			}
		});
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == ScannerActivity.REQUEST_CODE_SCAN) {
			if (resultCode == ScannerActivity.RESULT_GET_RESULT) {
				String code = intent.getStringExtra(ScannerActivity.EXTRA_RESULT);
				mEditText.setText(code);
				mButtonBar.onRightButtonClick();
			}
		}
	}

	private boolean checkNumberInput() {
		return mEditText.getText().toString().trim().length() > 4;
	}

	private boolean checkExistance() {
		return PackageDatabase.Companion.getInstance(getContext()).indexOf(mEditText.getText().toString().trim()) != -1;
	}

	private class FindPackageTask extends GetPackageTask {

		@Override
		public void onPreExecute() {
			getAddActivity().showProgressBar();
			mEditText.setEnabled(false);
		}

		@Override
		public void onPostExecute(BaseMessage<Package> message) {
			if (getActivity() == null) return;
			getAddActivity().hideProgressBar();
			mEditText.setEnabled(true);
			if (message.getCode() == BaseMessage.CODE_OKAY) {
				Package p = message.getData();
				getAddActivity().setNumber(number);
				getAddActivity().setPackage(p);
				if (p.status.equals("200")) {
					getAddActivity().addStep(AddActivity.STEP_SUCCESS);
				} else {
					Toast.makeText(getContext(), p.message, Toast.LENGTH_SHORT).show();
					getAddActivity().addStep(AddActivity.STEP_NO_FOUND);
				}
			} else {
				getAddActivity().addStep(AddActivity.STEP_NO_FOUND);
			}
		}

	}

	private class FindCompanyAndGetPackageTask extends CompanyFilterTask {

		@Override
		public void onPostExecute(ArrayList<PackageApi.CompanyInfo.Company> lists) {
			if (lists.size() == 1) {
				new FindPackageTask().execute(number, lists.get(0).getCode());
			} else {
				new FindPackageTask().execute(number);
			}
		}

	}

}
