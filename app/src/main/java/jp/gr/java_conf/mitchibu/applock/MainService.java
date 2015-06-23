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
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import java.util.List;

public class MainService extends Service {
	public static final String ACTION_DISMISS = MainService.class.getName() + ".action.DISMISS";
	public static final String EXTRA_PASSCODE = MainService.class.getName() + ".extra.PASSCODE";

	public static void start(Context context) {
		context.startService(new Intent(context, MainService.class));
	}

	public static void stop(Context context) {
		context.stopService(new Intent(context, MainService.class));
	}

	private AlarmManager alarmManager;
	private ActivityManager activityManager;
	private Vibrator vibrator;
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
		vibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

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
						hideGuard(containsLockedPackage() != null);
					}
				}
			}, filter);

			PowerManager powerManager = (PowerManager)getSystemService(POWER_SERVICE);
			//noinspection deprecation
			if(Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT_WATCH ? powerManager.isScreenOn() : powerManager.isInteractive()) {
				startTimer();
			}
			startForeground();
		} else {
			stopSelf();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = intent.getAction();
		if(ACTION_DISMISS.equals(action)) {
			String pass = intent.getStringExtra(EXTRA_PASSCODE);
			if(pass == null) {
				hideGuard(true);
			} else if(pass.equals(Prefs.getPasscode(this))) {
				Prefs.setAllowedPackageName(this, intent.getData().getSchemeSpecificPart());
				hideGuard(false);
			} else {
				vibrator.vibrate(100);
			}
		} else {
			Intent guardIntent = new Intent(this, GuardService.class);
			String packageName = containsLockedPackage();
			if(packageName != null) {
				if(!packageName.equals(Prefs.getAllowedPackageName(this))) {
					guardIntent.setData(Uri.fromParts("package", packageName, null));
					guardIntent.putExtra(GuardService.EXTRA_LOCK_TYPE, Prefs.getLockType(this));
					startService(guardIntent);
				}
			} else {
				Prefs.setAllowedPackageName(this, null);
				stopService(guardIntent);
			}
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		if(receiver != null) unregisterReceiver(receiver);
		stopTimer();
		stopForeground(true);
	}

	private void hideGuard(boolean finish) {
		if(finish) {
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			startActivity(intent);
		}
		stopService(new Intent(this, GuardService.class));
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

	private String containsLockedPackage() {
		final List<ActivityManager.RunningAppProcessInfo> list = activityManager.getRunningAppProcesses();
		if(list == null) return null;

		for(ActivityManager.RunningAppProcessInfo info : list) {
			if(info.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && info.pkgList != null) {
				Boolean state = null;
				for(int i = 0; state == null && i < info.pkgList.length; ++i) {
					state = Prefs.isLockedPackage(this, info.pkgList[i]);
					if(state != null && state) return info.pkgList[i];
				}
			}
		}
		return null;
	}

	private void startForeground() {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle(getString(R.string.app_name));
		builder.setContentText(getString(R.string.message));
		builder.setSmallIcon(R.mipmap.ic_launcher);
		builder.setWhen(0);
		builder.setOngoing(true);
		builder.setAutoCancel(false);
		startForeground(hashCode(), builder.build());
	}
}
