package jp.gr.java_conf.mitchibu.applock;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

public class GuardWindow extends FloatingWindow implements View.OnKeyListener, View.OnClickListener {
	private static final WindowManager.LayoutParams LAYOUT_PARAMS = new WindowManager.LayoutParams(
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.MATCH_PARENT,
			WindowManager.LayoutParams.TYPE_PHONE,
			WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
					| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
					| WindowManager.LayoutParams.FLAG_FULLSCREEN
					| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
			PixelFormat.TRANSLUCENT);

	private View view = null;
	private String packageName = null;
	private boolean initialized = false;
	private OnPasswordListener onPasswordListener = null;
	private OnCancelListener onCancelListener = null;

	public GuardWindow(Context context) {
		super(context);
		LAYOUT_PARAMS.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR;
	}

	public void onConfigurationChanged(Configuration newConfig) {
		dismiss();
		show();
		setPackageName(packageName);
	}

	public String getPackageName() {
		return packageName;
	}

	public void show() {
		view = LayoutInflater.from(getContext()).inflate(R.layout.view_guard, null, false);
		view.setOnClickListener(this);
		view.setOnKeyListener(this);

		Drawable bk = WallpaperManager.getInstance(getContext()).getFastDrawable();
		if(bk == null) view.setBackgroundColor(Color.argb(200, 0, 0, 0));
		else view.setBackgroundDrawable(bk);

		final TextView pass = (TextView)view.findViewById(R.id.pass);
		FrameLayout pad = (FrameLayout)view.findViewById(R.id.pad);
		KeyPadView v = new KeyPadView(getContext());
		v.setOnKeyListener(new KeyPadView.OnKeyListener() {
			@Override
			public void onKey(CharSequence text) {
				pass.append(text);
				if(pass.length() == 4) {
					if(onPasswordListener == null || !onPasswordListener.onPassword(pass.getText())) pass.setText(null);
				}
			}
		});
		pad.addView(v, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
		addView(view, LAYOUT_PARAMS);
	}

	public void dismiss() {
		if(view == null) return;
		removeView(view);
		view = null;
	}

	public void setPackageName(String packageName) {
		if(initialized) return;
		PackageManager pm = getContext().getPackageManager();
		try {
			ApplicationInfo info = pm.getApplicationInfo(packageName, 0);

			TextView name = (TextView)view.findViewById(R.id.name);
			name.setText(info.loadLabel(pm));
			Drawable icon = info.loadIcon(pm);
			int size = getContext().getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
			icon.setBounds(0, 0, size, size);
			name.setCompoundDrawables(icon, null, null, null);
			this.packageName = packageName;
			initialized = true;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void setOnPasswordListener(OnPasswordListener listener) {
		onPasswordListener = listener;
	}

	public void setOnCancelListener(OnCancelListener listener) {
		onCancelListener = listener;
	}

	@Override
	public void onClick(View v) {
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if(onCancelListener != null) onCancelListener.onCancel();
			return true;
		}
		return false;
	}

	public interface OnPasswordListener {
		boolean onPassword(CharSequence pass);
	}

	public interface OnCancelListener {
		void onCancel();
	}
}
