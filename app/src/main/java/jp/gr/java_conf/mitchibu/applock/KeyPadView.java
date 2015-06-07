package jp.gr.java_conf.mitchibu.applock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class KeyPadView extends LinearLayout {
	private OnKeyListener onKeyListener = null;

	public KeyPadView(Context context) {
		this(context, null);
	}

	public KeyPadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		View v =LayoutInflater.from(context).inflate(R.layout.view_keypad, this, true);

		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(onKeyListener != null) onKeyListener.onKey(((TextView)v).getText());
			}
		};
		v.findViewById(R.id.key0).setOnClickListener(listener);
		v.findViewById(R.id.key1).setOnClickListener(listener);
		v.findViewById(R.id.key2).setOnClickListener(listener);
		v.findViewById(R.id.key3).setOnClickListener(listener);
		v.findViewById(R.id.key4).setOnClickListener(listener);
		v.findViewById(R.id.key5).setOnClickListener(listener);
		v.findViewById(R.id.key6).setOnClickListener(listener);
		v.findViewById(R.id.key7).setOnClickListener(listener);
		v.findViewById(R.id.key8).setOnClickListener(listener);
		v.findViewById(R.id.key9).setOnClickListener(listener);
	}

	public void setOnKeyListener(OnKeyListener listener) {
		onKeyListener = listener;
	}

	public interface OnKeyListener {
		void onKey(CharSequence text);
	}
}
