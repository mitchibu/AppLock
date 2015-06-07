package jp.gr.java_conf.mitchibu.applock;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.Vibrator;

public class GuardService extends Service implements GuardWindow.OnPasswordListener, GuardWindow.OnCancelListener {
	public static final String EXTRA_PACKAGE_NAME = GuardService.class.getName() + ".extra.PACKAGE_NAME";

	private Vibrator vibrator;
	private GuardWindow guardWindow;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
		guardWindow = new GuardWindow(this);
		guardWindow.setOnPasswordListener(this);
		guardWindow.setOnCancelListener(this);
		guardWindow.show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		guardWindow.setPackageName(intent.getStringExtra(EXTRA_PACKAGE_NAME));
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
	public boolean onPassword(CharSequence pass) {
		boolean rc = pass.toString().equals("1116");
		if(rc) {
			stopSelf();
		} else {
			vibrator.vibrate(100);
		}
		return rc;
	}

	@Override
	public void onCancel() {
		finish();
		stopSelf();
	}

	private void finish() {
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		startActivity(intent);
	}
}