package jp.gr.java_conf.mitchibu.applock;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class GuardWindow extends FloatingWindow implements View.OnKeyListener, View.OnClickListener {
	private TextView name;
	private String packageName = null;
	private boolean initialized = false;
	private OnPasscodeListener onPasscodeListener = null;
	private OnCancelListener onCancelListener = null;

	public GuardWindow(Context context) {
		super(context);
		setContentView(initView());
	}

	public void onConfigurationChanged(@SuppressWarnings("UnusedParameters") Configuration newConfig) {
		initialized = false;
		dismiss();
		setContentView(initView());
		show();
		setPackageName(packageName);
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		if(initialized) return;
		PackageManager pm = getContext().getPackageManager();
		try {
			ApplicationInfo info = pm.getApplicationInfo(packageName, 0);

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

	public void setOnPasscodeListener(OnPasscodeListener listener) {
		onPasscodeListener = listener;
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
			if(onCancelListener != null) onCancelListener.onCancel(this);
			return true;
		}
		return false;
	}

	private View initView() {
		@SuppressLint("InflateParams")
		View view = LayoutInflater.from(getContext()).inflate(R.layout.view_guard, null, false);
		view.setOnClickListener(this);
		view.setOnKeyListener(this);

		Drawable bk = WallpaperManager.getInstance(getContext()).getFastDrawable();
		if(bk == null) view.setBackgroundColor(Color.argb(200, 0, 0, 0));
		else //noinspection deprecation
			view.setBackgroundDrawable(bk);

		name = (TextView)view.findViewById(R.id.name);
		final TextView pass = (TextView)view.findViewById(R.id.pass);
		FrameLayout pad = (FrameLayout)view.findViewById(R.id.pad);
		KeyPadView v = new KeyPadView(getContext());
		v.setOnKeyListener(new KeyPadView.OnKeyListener() {
			@Override
			public void onKey(CharSequence text) {
				pass.append(text);
				if(pass.length() == 4) {
					if(onPasscodeListener == null || !onPasscodeListener.onPasscode(GuardWindow.this, pass.getText()))
						pass.setText(null);
				}
			}
		});
		pad.addView(v, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
		return view;
	}

	public interface OnPasscodeListener {
		boolean onPasscode(GuardWindow window, CharSequence pass);
	}

	public interface OnCancelListener {
		void onCancel(GuardWindow window);
	}
}
