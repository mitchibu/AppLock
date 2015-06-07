package jp.gr.java_conf.mitchibu.applock;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import java.util.List;

public class MainService extends Service {
	private AlarmManager alarmManager;
	private ActivityManager activityManager;
	private PowerManager powerManager;
	private BroadcastReceiver receiver = null;

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
	@Override
	public void onCreate() {
		alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
		activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
		powerManager = (PowerManager)getSystemService(POWER_SERVICE);

		if(Prefs.hasLockedPackage(this)) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			registerReceiver(receiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action = intent.getAction();
					if(Intent.ACTION_SCREEN_ON.equals(action)) {
						startTimer();
					} else if(Intent.ACTION_SCREEN_OFF.equals(action)) {
						stopTimer();
					}
				}
			}, filter);

			//noinspection deprecation
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH ? powerManager.isScreenOn() : powerManager.isInteractive()) {
				startTimer();
			}
		} else {
			stopSelf();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		boolean resetLastPackageName = true;
		final String lastPackageName = Prefs.getLastPackageName(this);
		final List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
		if(list != null) {
			for(ActivityManager.RunningAppProcessInfo info : list) {
				if(info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && info.pkgList != null) {
					Boolean state = null;
					for(int i = 0; state == null && i < info.pkgList.length; ++i) {
						state = Prefs.isLockedPackage(this, info.pkgList[i]);
						if(state != null && state) {
							resetLastPackageName = false;
							if(!info.pkgList[i].equals(lastPackageName)) {
								Prefs.setLastPackageName(this, info.pkgList[i]);
								startService(new Intent(this, GuardService.class).putExtra(GuardService.EXTRA_PACKAGE_NAME, info.pkgList[i]));
							}
						}
					}
				}
			}
		}
		if(resetLastPackageName) Prefs.setLastPackageName(this, null);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		stopTimer();
		if(receiver != null) unregisterReceiver(receiver);
	}

	private void startTimer() {
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000, getOperation());
	}

	private void stopTimer() {
		alarmManager.cancel(getOperation());
	}

	private PendingIntent getOperation() {
		return PendingIntent.getService(this, 0, new Intent(this, getClass()), PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
