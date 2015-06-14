package jp.gr.java_conf.mitchibu.applock;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KeyPadView extends LinearLayout {
	private OnKeyListener onKeyListener = null;
	private OnEnterListener onEnterListener = null;

	public KeyPadView(Context context) {
		this(context, null);
	}

	public KeyPadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOrientation(VERTICAL);
		View v = LayoutInflater.from(context).inflate(R.layout.view_keypad, this, true);

		View.OnClickListener listener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(onKeyListener != null) onKeyListener.onKey(((TextView)v).getText());
			}
		};

		int[] keyId = {
				R.id.key0,
				R.id.key1,
				R.id.key2,
				R.id.key3,
				R.id.key4,
				R.id.key5,
				R.id.key6,
				R.id.key7,
				R.id.key8,
				R.id.key9
		};
		List<String> list = new ArrayList<>();
		for(int i = 0; i < keyId.length; ++ i) list.add(Integer.toString(i));
		Random rand = new Random(System.currentTimeMillis());
		for(int id : keyId) {
			TextView key = (TextView)v.findViewById(id);
			key.setText(list.remove(rand.nextInt(list.size())));
			key.setOnClickListener(listener);
		}
		v.findViewById(R.id.keyE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(onEnterListener != null) onEnterListener.onEnter();
			}
		});
	}

	public void setOnKeyListener(OnKeyListener listener) {
		onKeyListener = listener;
	}

	public void setOnEnterListener(OnEnterListener listener) {
		onEnterListener = listener;
	}

	public interface OnKeyListener {
		void onKey(CharSequence text);
	}
	public interface OnEnterListener {
		void onEnter();
	}
}
