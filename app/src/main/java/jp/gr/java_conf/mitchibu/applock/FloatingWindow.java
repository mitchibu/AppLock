package jp.gr.java_conf.mitchibu.applock;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.view.View;
import android.view.WindowManager;

public class FloatingWindow {
	private static final WindowManager.LayoutParams LAYOUT_PARAMS = new WindowManager.LayoutParams();

	private final WindowManager wm;
	private final Context context;

	private View view;
	private boolean showing = false;

	public FloatingWindow(Context context) {
		this.context = context;
		wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);

		setWidth(WindowManager.LayoutParams.MATCH_PARENT);
		setHeight(WindowManager.LayoutParams.MATCH_PARENT);
		setType(WindowManager.LayoutParams.TYPE_APPLICATION);
		setFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_FULLSCREEN
				| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
		setFormat(PixelFormat.TRANSLUCENT);
		setScreenOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
	}

	public Context getContext() {
		return context;
	}

	public FloatingWindow setWidth(int width) {
		LAYOUT_PARAMS.width = width;
		return this;
	}

	public FloatingWindow setHeight(int height) {
		LAYOUT_PARAMS.height = height;
		return this;
	}

	public FloatingWindow setType(int type) {
		LAYOUT_PARAMS.type = type;
		return this;
	}

	public FloatingWindow setFlags(int flags) {
		LAYOUT_PARAMS.flags = flags;
		return this;
	}

	public FloatingWindow setFormat(int format) {
		LAYOUT_PARAMS.format = format;
		return this;
	}

	public FloatingWindow setScreenOrientation(int orientation) {
		LAYOUT_PARAMS.screenOrientation = orientation;
		return this;
	}

	public void setContentView(View view) {
		this.view = view;
	}

	public boolean isShowing() {
		return showing;
	}

	public void show() {
		wm.addView(view, LAYOUT_PARAMS);
		showing = true;
	}

	public void update() {
		wm.updateViewLayout(view, LAYOUT_PARAMS);
	}

	public void dismiss() {
		wm.removeView(view);
		showing = false;
	}
}
