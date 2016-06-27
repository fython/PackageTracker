package info.papdt.express.helper.ui.fragment.add;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import info.papdt.express.helper.R;
import info.papdt.express.helper.asynctask.GetPackageTask;
import info.papdt.express.helper.model.BaseMessage;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.ui.AddActivity;
import info.papdt.express.helper.ui.CompanyChooserActivity;

public class StepSuccess extends AbsStepFragment {

	private AppCompatTextView mMsgText, mDescText;
	private MaterialEditText mNameEdit;

	@Override
	protected int getLayoutResId() {
		return R.layout.fragment_add_step_success;
	}

	@Override
	protected void doCreateView(View rootView) {
		mMsgText = $(R.id.tv_message);
		mDescText = $(R.id.tv_desc);
		mNameEdit = $(R.id.et_name);

		mButtonBar.setOnLeftButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getAddActivity().step(AddActivity.STEP_INPUT);
			}
		});
		mButtonBar.setOnRightButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!TextUtils.isEmpty(mNameEdit.getText().toString())) {
					getAddActivity().getPackage().name = mNameEdit.getText().toString();
				} else {
					getAddActivity().getPackage().name = String.format(getString(R.string.package_name_unnamed), getAddActivity().getNumber().substring(0, 4));
				}
				getAddActivity().finishAdd();
			}
		});
		$(R.id.btn_choose_company).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(getActivity(), CompanyChooserActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
				startActivityForResult(intent, REQUEST_CODE_CHOOSE_COMPANY);
			}
		});

		Package p = getAddActivity().getPackage();
		if (p != null) {
			updateUIContent(p);
		} else {
			getAddActivity().step(AddActivity.STEP_NO_FOUND);
		}
	}

	private void updateUIContent(Package p) {
		mMsgText.setText(String.format(getString(R.string.message_successful_format), p.number));
		mDescText.setText(p.data.size() > 0 ? String.format(getString(R.string.description_successful_format), p.data.get(0).context, p.data.get(0).time) : p.message);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == REQUEST_CODE_CHOOSE_COMPANY) {
			if (resultCode == Activity.RESULT_OK) {
				String companyCode = intent.getStringExtra(RESULT_EXTRA_COMPANY_CODE);
				new FindPackageTask().execute(getAddActivity().getNumber(), companyCode);
			}
		}
	}

	private class FindPackageTask extends GetPackageTask {

		@Override
		public void onPreExecute() {
			getAddActivity().showProgressBar();
		}

		@Override
		public void onPostExecute(BaseMessage<Package> message) {
			getAddActivity().hideProgressBar();
			if (message.getCode() == BaseMessage.CODE_OKAY) {
				Package p = message.getData();
				if (p.status.equals("200")) {
					getAddActivity().setPackage(p);
					updateUIContent(p);
				} else {
					Toast.makeText(getContext(), p.message, Toast.LENGTH_SHORT).show();
					getAddActivity().step(AddActivity.STEP_NO_FOUND);
				}
			} else {
				getAddActivity().step(AddActivity.STEP_NO_INTERNET_CONNECTION);
			}
		}

	}

}
