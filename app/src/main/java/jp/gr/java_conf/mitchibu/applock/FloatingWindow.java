package jp.gr.java_conf.mitchibu.applock;

import android.content.Context;
import android.view.View;
import android.view.WindowManager;

public class FloatingWindow {
	private final Context context;

	public FloatingWindow(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

	public void addView(View view, WindowManager.LayoutParams params) {
		((WindowManager)view.getContext().getSystemService(Context.WINDOW_SERVICE)).addView(view, params);
	}

	public void removeView(View view) {
		((WindowManager)view.getContext().getSystemService(Context.WINDOW_SERVICE)).removeView(view);
	}
}
