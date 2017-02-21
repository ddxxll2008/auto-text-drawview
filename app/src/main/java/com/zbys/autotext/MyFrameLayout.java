package com.zbys.autotext;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import static android.R.attr.maxHeight;

/**
 * Created by dingxiaolei on 17/2/13.
 */

public class MyFrameLayout extends FrameLayout {
	private Context context;

	//记录当前点击坐标
	private float currentX;
	private float currentY;

	//时间类型
	private int eventType = 0;

	private AutoResizeTextView autoResizeTextView;

	public MyFrameLayout(Context context) {
		super(context);
		this.context = context;
		recreateTextView(100, 100);
	}

	public MyFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				int oriLeft = autoResizeTextView.getLeft();
				int oriTop = autoResizeTextView.getTop();
				int oriRight = autoResizeTextView.getRight();
				int oriBottom = autoResizeTextView.getBottom();
				if (event.getX() > oriRight - 50 && event.getX() < oriRight
						&& event.getY() > oriBottom - 50 && event.getY() < oriBottom) {
					eventType = 1;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (eventType == 1) {
					recreateTextView((int)event.getX() - autoResizeTextView.getLeft(),(int) event.getY() - autoResizeTextView.getTop());
				}
				break;
		}
		return true;
	}

	private void recreateTextView(int width, int height) {
//		ViewGroup.LayoutParams layoutParams = new LayoutParams(width, height);
//		if (autoResizeTextView!=null) {
//			layoutParams = autoResizeTextView.getLayoutParams();
//			this.removeView(autoResizeTextView);
//		}
		this.removeAllViews();
		autoResizeTextView = new AutoResizeTextView(context);
		autoResizeTextView.setGravity(Gravity.CENTER);
		final int maxLinesCount = 10;
		autoResizeTextView.setMaxLines(maxLinesCount);
		autoResizeTextView.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, maxHeight, getResources().getDisplayMetrics()));
		autoResizeTextView.setEllipsize(TextUtils.TruncateAt.END);
		// since we use it only once per each click, we don't need to cache the results, ever
		autoResizeTextView.setLayoutParams(new LayoutParams(width, height));
		autoResizeTextView.setBackgroundColor(0xff00ff00);
		final String text = "这是个测试";
		autoResizeTextView.setText(text);
		this.addView(autoResizeTextView);
	}
}
