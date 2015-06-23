package jp.gr.java_conf.mitchibu.applock;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.WindowManager;

public class PasscodePreference extends Preference {
	private final GuardWindow guardWindow;

	private String lastCode = null;

	public PasscodePreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		guardWindow = new GuardWindow(context, 1);
		guardWindow.setType(WindowManager.LayoutParams.TYPE_PHONE);
		guardWindow.setPackageName(getContext().getPackageName());
		guardWindow.setOnPasscodeListener(new GuardWindow.OnPasscodeListener() {
			@Override
			public void onPasscode(GuardWindow window, String pass) {
				if(lastCode == null) {
					lastCode = pass;
				} else {
					if(lastCode.equals(pass)) {
						persistString(pass);
						guardWindow.dismiss();
					}
				}
			}
		});
		guardWindow.setOnCancelListener(new GuardWindow.OnCancelListener() {
			@Override
			public void onCancel(GuardWindow window) {
				guardWindow.dismiss();
			}
		});
	}

	@Override
	public void onClick() {
		guardWindow.show();
	}
}
