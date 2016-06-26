package info.papdt.express.helper.ui.fragment.add;

import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import info.papdt.express.helper.R;
import info.papdt.express.helper.asynctask.GetPackageTask;
import info.papdt.express.helper.model.BaseMessage;
import info.papdt.express.helper.model.Package;
import info.papdt.express.helper.ui.AddActivity;
import info.papdt.express.helper.widget.ButtonBar;

public class StepInput extends AbsStepFragment {

	private MaterialEditText mEditText;
	private ButtonBar mButtonBar;

	private String number;

	@Override
	protected int getLayoutResId() {
		return R.layout.fragment_add_step_input;
	}

	@Override
	protected void doCreateView(View rootView) {
		mEditText = $(R.id.et_number);
		mButtonBar = $(R.id.button_bar);

		mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					mButtonBar.onRightButtonClick();
				}
				return false;
			}
		});
		mButtonBar.setOnRightButtonClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (!checkNumberInput()) {
					Toast.makeText(getContext(), R.string.toast_number_wrong, Toast.LENGTH_SHORT).show();
				} else {
					number = mEditText.getText().toString();
					new FindPackageTask().execute(number);
				}
			}
		});
	}

	private boolean checkNumberInput() {
		return mEditText.getText().toString().trim().length() > 4;
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
					getAddActivity().step(AddActivity.STEP_SUCCESS);
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
