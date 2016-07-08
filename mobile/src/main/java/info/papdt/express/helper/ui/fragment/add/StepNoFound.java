package info.papdt.express.helper.ui.fragment.add;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import info.papdt.express.helper.R;
import info.papdt.express.helper.asynctask.GetPackageTask;
import info.papdt.express.helper.model.BaseMessage;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.ui.AddActivity;
import info.papdt.express.helper.ui.CompanyChooserActivity;

public class StepNoFound extends AbsStepFragment {

	@Override
	protected int getLayoutResId() {
		return R.layout.fragment_add_step_no_found;
	}

	@Override
	protected void doCreateView(View rootView) {
		mButtonBar.setOnLeftButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getAddActivity().step(AddActivity.STEP_INPUT);
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
			if (getActivity() == null) return;
			getAddActivity().hideProgressBar();
			if (message.getCode() == BaseMessage.CODE_OKAY) {
				Package p = message.getData();
				if (p.status.equals("200")) {
					getAddActivity().setPackage(p);
					getAddActivity().setNumber(p.number);
					getAddActivity().step(AddActivity.STEP_SUCCESS);
				} else {
					Toast.makeText(getContext(), p.message, Toast.LENGTH_SHORT).show();
				}
			} else {
				getAddActivity().step(AddActivity.STEP_NO_INTERNET_CONNECTION);
			}
		}

	}

}
