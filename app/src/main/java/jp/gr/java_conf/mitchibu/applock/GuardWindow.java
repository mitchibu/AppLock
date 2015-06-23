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

import java.util.List;

public class GuardWindow extends FloatingWindow implements View.OnKeyListener {
	private final int type;

	private TextView name;
	private TextView pass;
	private String packageName = null;
	private OnPasscodeListener onPasscodeListener = null;
	private OnCancelListener onCancelListener = null;

	public GuardWindow(Context context, int type) {
		super(context);
		this.type = type;
		setContentView(initView());
	}

	public void onConfigurationChanged(@SuppressWarnings("UnusedParameters") Configuration newConfig) {
		dismiss();
		setContentView(initView());
		show();
		setPackageName(null);
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		if(packageName != null && packageName.equals(this.packageName)) return;
		if(packageName == null) packageName = this.packageName;
		PackageManager pm = getContext().getPackageManager();
		try {
			ApplicationInfo info = pm.getApplicationInfo(packageName, 0);

			name.setText(info.loadLabel(pm));
			Drawable icon = info.loadIcon(pm);
			int size = getContext().getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);
			icon.setBounds(0, 0, size, size);
			name.setCompoundDrawables(icon, null, null, null);
			this.packageName = packageName;
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
		view.setOnKeyListener(this);

		Drawable bk = WallpaperManager.getInstance(getContext()).getFastDrawable();
		if(bk == null) view.setBackgroundColor(Color.argb(200, 0, 0, 0));
		else //noinspection deprecation
			view.setBackgroundDrawable(bk);

		name = (TextView)view.findViewById(R.id.name);
		pass = (TextView)view.findViewById(R.id.pass);
		pass.setVisibility(type == 0 ? View.VISIBLE : View.GONE);
		FrameLayout pad = (FrameLayout)view.findViewById(R.id.pad);
		View v;
		switch(type) {
		case 0:
			v = initPin();
			break;
		case 1:
			v = initPattern();
			break;
		default:
			throw new RuntimeException();
		}
		pad.addView(v, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
		return view;
	}

	private View initPin() {
		KeyPadView v = new KeyPadView(getContext());
		v.setOnKeyListener(new KeyPadView.OnKeyListener() {
			@Override
			public void onKey(CharSequence text) {
				pass.append(text);
			}
		});
		v.setOnEnterListener(new KeyPadView.OnEnterListener() {
			@Override
			public void onEnter() {
				if(onPasscodeListener != null)
					onPasscodeListener.onPasscode(GuardWindow.this, pass.getText().toString());
				pass.setText(null);
			}
		});
		return v;
	}

	private View initPattern() {
		PatternPadView v = new PatternPadView(getContext());
		v.setOnFinishedPatternListener(new PatternPadView.OnFinishedPatternListener() {
			@Override
			public void onFinishedPattern(List<Integer> pattern) {
				if(onPasscodeListener != null) {
					StringBuilder sb = new StringBuilder();
					for(Integer i : pattern) {
						sb.append(i).append(' ');
					}
					onPasscodeListener.onPasscode(GuardWindow.this, sb.toString());
				}
			}
		});
		return v;
	}
	public interface OnPasscodeListener {
		void onPasscode(GuardWindow window, String pass);
	}

	public interface OnCancelListener {
		void onCancel(GuardWindow window);
	}
}
