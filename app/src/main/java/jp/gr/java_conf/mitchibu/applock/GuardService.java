package jp.gr.java_conf.mitchibu.applock;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.IBinder;

public class GuardService extends Service implements GuardWindow.OnPasscodeListener, GuardWindow.OnCancelListener {
	private GuardWindow guardWindow;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		guardWindow = new GuardWindow(this);
		guardWindow.setOnPasscodeListener(this);
		guardWindow.setOnCancelListener(this);
		guardWindow.show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		guardWindow.setPackageName(intent.getData().getSchemeSpecificPart());
		return START_REDELIVER_INTENT;
	}

	@Override
	public void onDestroy() {
		guardWindow.dismiss();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		guardWindow.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onPasscode(GuardWindow window, CharSequence pass) {
		startService(new Intent(MainService.ACTION_DISMISS, Uri.fromParts("package", guardWindow.getPackageName(), null), this, MainService.class).putExtra(MainService.EXTRA_PASSCODE, pass.toString()));
		return false;
	}

	@Override
	public void onCancel(GuardWindow window) {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(intent);
	}
}
