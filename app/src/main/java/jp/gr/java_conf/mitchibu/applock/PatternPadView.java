package jp.gr.java_conf.mitchibu.applock;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class PatternPadView extends View {
	private static final int COLS = 3;
	private static final int ROWS = 3;
	private static final int RADIUS_DOT = 16;

	private final List<Integer> patternList = new ArrayList<>();
	private final Rect[] rect = new Rect[COLS * ROWS];
	private final Paint paintDot = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint paintArea = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint paintLine = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint paintDecision = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Path path = new Path();

	private int radius;
	private int moveX;
	private int moveY;
	private OnFinishedPatternListener onFinishedPatternListener = null;

	public PatternPadView(Context context) {
		this(context, null);
	}

	public PatternPadView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PatternPadView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		paintDot.setColor(Color.WHITE);
		paintDot.setStyle(Paint.Style.FILL);

		paintArea.setColor(Color.WHITE);
		paintLine.setStrokeWidth(3);
		paintArea.setStyle(Paint.Style.STROKE);

		paintDecision.setColor(paintDot.getColor());
		paintDecision.setStrokeWidth(RADIUS_DOT);
		paintDecision.setStyle(Paint.Style.STROKE);

		paintLine.setColor(Color.YELLOW);
		paintLine.setStrokeWidth(paintDecision.getStrokeWidth());
		paintLine.setStyle(paintDecision.getStyle());
	}

	public void setOnFinishedPatternListener(OnFinishedPatternListener onFinishedPatternListener) {
		this.onFinishedPatternListener = onFinishedPatternListener;
	}

	@Override
	public boolean onTouchEvent(@NonNull MotionEvent event) {
		switch(event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			patternList.clear();
			path.reset();
			for(int i = 0; i < rect.length; ++ i) {
				Rect r = rect[i];
				if((patternList.isEmpty() || patternList.get(patternList.size() - 1) != i) && Math.hypot(r.centerX() - event.getX(), r.centerY() - event.getY()) < radius) {
					moveX = r.centerX();
					moveY = r.centerY();
					patternList.add(i);
					path.moveTo(r.centerX(), r.centerY());
					invalidate();
					return true;
				}
			}
			break;
		case MotionEvent.ACTION_MOVE:
			for(int i = 0; i < rect.length; ++ i) {
				Rect r = rect[i];
				if((patternList.isEmpty() || patternList.get(patternList.size() - 1) != i) && Math.hypot(r.centerX() - event.getX(), r.centerY() - event.getY()) < radius) {
					patternList.add(i);
					path.lineTo(r.centerX(), r.centerY());
					invalidate();
					return true;
				}
			}
			moveX = (int)event.getX();
			moveY = (int)event.getY();
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if(onFinishedPatternListener != null) onFinishedPatternListener.onFinishedPattern(patternList);
			patternList.clear();
			path.reset();
			invalidate();
			return true;
		}
		return false;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		int width = w / COLS;
		int height = h / ROWS;

		radius = Math.min(width, height) / 4;

		int i = 0;
		for(int row = 0, y = 0; row < ROWS; ++ row, y += height) {
			for(int col = 0, x = 0; col < COLS; ++ col, x += width) {
				rect[i ++] = new Rect(x, y, x + width, y + height);
			}
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawPath(path, paintDecision);
		int i = patternList.isEmpty() ? -1 : patternList.get(patternList.size() - 1);
		if(i >= 0) canvas.drawLine(rect[i].centerX(), rect[i].centerY(), moveX, moveY, paintLine);

		for(Rect r : rect) {
			canvas.drawCircle(r.centerX(), r.centerY(), RADIUS_DOT, paintDot);
			canvas.drawCircle(r.centerX(), r.centerY(), radius, paintArea);
		}
	}

	public interface OnFinishedPatternListener {
		void onFinishedPattern(List<Integer> pattern);
	}
}
