package jp.gr.java_conf.mitchibu.applock;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.IBinder;
import android.view.WindowManager;

public class GuardService extends Service implements GuardWindow.OnPasscodeListener, GuardWindow.OnCancelListener {
	private GuardWindow guardWindow;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		guardWindow = new GuardWindow(this);
		guardWindow.setType(WindowManager.LayoutParams.TYPE_PHONE);
		guardWindow.setOnPasscodeListener(this);
		guardWindow.setOnCancelListener(this);
		guardWindow.show();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		guardWindow.setPackageName(intent.getData().getSchemeSpecificPart());
		return START_NOT_STICKY;
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
	public void onPasscode(GuardWindow window, String pass) {
		startService(new Intent(MainService.ACTION_DISMISS, Uri.fromParts("package", guardWindow.getPackageName(), null), this, MainService.class).putExtra(MainService.EXTRA_PASSCODE, pass.toString()));
	}

	@Override
	public void onCancel(GuardWindow window) {
		startService(new Intent(MainService.ACTION_DISMISS, Uri.fromParts("package", guardWindow.getPackageName(), null), this, MainService.class));
	}
}
