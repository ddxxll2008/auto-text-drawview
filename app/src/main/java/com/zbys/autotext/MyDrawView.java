package com.zbys.autotext;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import static android.content.ContentValues.TAG;

/**
 * Created by dingxiaolei on 17/2/13.
 */

public class MyDrawView extends View {
	//事件类型, 0代表移动, 1代表缩放
	private static final int MOVEVIEW = 0;
	private static final int DRAGVIEW = 1;

	//背景画笔
	private Paint backPaint;
	//文字画笔
	private TextPaint textPaint;
	//背景区域
	private Rect backRect;
	//文字测量出的区域
	private Rect textMeasureRect;
	//事件类型
	private int eventType;
	//手指点击的位置
	private int lastX, lastY;
	//背景与文字的长宽
	private int rectWidth, rectHeight, textWidth, textHeight;
	//字体大小
	private int textSize;

	private String text = "这是一个测试程序abcdefg!@#$%^&";


	public MyDrawView(Context context) {
		this(context, null);
	}

	public MyDrawView(Context context, AttributeSet attrs) {
		super(context, attrs);

		backPaint = new Paint(); //设置一个笔刷大小是3的黄色的画笔
		backPaint.setColor(Color.YELLOW);
		backPaint.setStrokeJoin(Paint.Join.ROUND);
		backPaint.setStrokeCap(Paint.Cap.ROUND);
		backPaint.setStrokeWidth(3);

		textPaint = new TextPaint();
		textPaint.setColor(Color.BLUE);
		textPaint.setTextSize(100);

		backRect = new Rect(100, 100, 400, 200);
		textMeasureRect = new Rect();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				Log.d(TAG, "onTouchEvent: " + event.getX() + "   " + backRect.right + "   " + event.getY() + "   " +
						backRect.bottom);
				if ((Math.abs(event.getX() - backRect.right)) < 100
						&& ((Math.abs(event.getY() - backRect.bottom) < 100))) {
					eventType = DRAGVIEW;
				} else {
					eventType = MOVEVIEW;
				}
				Log.d(TAG, "onTouchEvent: " + eventType);
				break;
			case MotionEvent.ACTION_MOVE:
				switch (eventType) {
					case MOVEVIEW:
						int dx = (int) (event.getRawX() - lastX);
						int dy = (int) (event.getRawY() - lastY);
						backRect.offset(dx, dy);
						lastX = (int) event.getRawX();
						lastY = (int) event.getRawY();
						break;
					case DRAGVIEW:
						Log.d(TAG, "onTouchEvent: " + backRect.toShortString());
						if (event.getX() > backRect.left + 100 && event.getY() > backRect.top + 100) {
							backRect.set(backRect.left, backRect.top, (int) event.getX(), (int) event.getY());
						}
						break;
				}
				break;
			case MotionEvent.ACTION_UP:
				break;
		}
		invalidate(); //重新绘制区域
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(backRect, backPaint);

		adjustTextSize();

		//文字自动换行
		StaticLayout layout = new StaticLayout(text, textPaint, backRect.width(), Layout.Alignment.ALIGN_NORMAL, 1.0F,
				0.0F, true);
		canvas.save();
		textPaint.setTextAlign(Paint.Align.LEFT);
		//文字的位置
		canvas.translate(backRect.left, backRect.top);
		layout.draw(canvas);
		canvas.restore();

		super.onDraw(canvas);
	}

	/**
	 * 确定文字大小
	 */
	private void adjustTextSize() {
		rectWidth = backRect.right - backRect.left;
		rectHeight = backRect.bottom - backRect.top;
		textSize = (int) (rectHeight * 0.9);
		textPaint.setTextSize((float) (textSize));
		textPaint.getTextBounds(text, 0, text.length(), textMeasureRect);
		textWidth = textMeasureRect.width();
		textHeight = textMeasureRect.height();
		//0.7为文字占据行高的系数
		while (rectWidth * rectHeight * 0.7 < textWidth * textHeight) {
			textSize = textSize - 3;
			textPaint.setTextSize((float) (textSize));
			textPaint.getTextBounds(text, 0, text.length(), textMeasureRect);
			textWidth = textMeasureRect.width();
			textHeight = textMeasureRect.height();
		}
		textPaint.getTextBounds(text, 0, text.length(), textMeasureRect);
		Log.d(TAG, "adjustTextSize: " + textMeasureRect.width() + "   " + textMeasureRect.height());
	}
}
