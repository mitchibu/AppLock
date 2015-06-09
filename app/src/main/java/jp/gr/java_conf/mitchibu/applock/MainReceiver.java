package jp.gr.java_conf.mitchibu.applock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MainReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		MainService.start(context);
	}
}
