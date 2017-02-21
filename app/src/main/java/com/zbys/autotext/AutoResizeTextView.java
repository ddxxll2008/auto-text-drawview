package com.zbys.autotext;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.os.Build;
import android.text.TextPaint;
import android.text.method.TransformationMethod;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import static android.content.ContentValues.TAG;

/**
 * a textView that is able to self-adjust its font size depending on the min and max size of the font, and its own
 * size.<br/>
 * code is heavily based on this StackOverflow thread:
 * http://stackoverflow.com/questions/16017165/auto-fit-textview-for-android/21851239#21851239 <br/>
 * It should work fine with most Android versions, but might have some issues on Android 3.1 - 4.04, as setTextSize
 * will only work for the first time. <br/>
 * More info here: https://code.google.com/p/android/issues/detail?id=22493 and here in case you wish to fix it:
 * http://stackoverflow.com/a/21851239/878126
 */
public class AutoResizeTextView extends AppCompatTextView implements View.OnTouchListener{
	private static final int NO_LINE_LIMIT = -1;
	private final RectF _availableSpaceRect = new RectF();
	private final SizeTester _sizeTester;
	private float _maxTextSize, _spacingMult = 1.0f, _spacingAdd = 0.0f, _minTextSize;
	private int _widthLimit, _maxLines;
	private boolean _initialized = false;
	private TextPaint _paint;

	protected int screenWidth;
	protected int screenHeight;
	protected int lastX;
	protected int lastY;
	private int oriLeft;
	private int oriRight;
	private int oriTop;
	private int oriBottom;
	private int dragDirection;
	private static final int TOP = 0x15;
	private static final int LEFT = 0x16;
	private static final int BOTTOM = 0x17;
	private static final int RIGHT = 0x18;
	private static final int LEFT_TOP = 0x11;
	private static final int RIGHT_TOP = 0x12;
	private static final int LEFT_BOTTOM = 0x13;
	private static final int RIGHT_BOTTOM = 0x14;
	private static final int CENTER = 0x19;
	private int offset = 20;

	private int oldWidth, oldHeight, newWidth, newHeight;

	/**
	 * 初始化获取屏幕宽高
	 */
	protected void initScreenW_H() {
		screenHeight = getResources().getDisplayMetrics().heightPixels - 40;
		screenWidth = getResources().getDisplayMetrics().widthPixels;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "onTouch: ");
		int action = event.getAction();
		if (action == MotionEvent.ACTION_DOWN) {
			oriLeft = v.getLeft();
			oriRight = v.getRight();
			oriTop = v.getTop();
			oriBottom = v.getBottom();
			lastY = (int) event.getRawY();
			lastX = (int) event.getRawX();
			dragDirection = getDirection(v, (int) event.getX(),
					(int) event.getY());
			oldWidth = v.getWidth();
			oldHeight = v.getHeight();
		}
		// 处理拖动事件
		delDrag(v, event, action);
		invalidate();
		return true;
	}

	/**
	 * 获取触摸点flag
	 *
	 * @param v
	 * @param x
	 * @param y
	 * @return
	 */
	protected int getDirection(View v, int x, int y) {
		int left = v.getLeft();
		int right = v.getRight();
		int bottom = v.getBottom();
		int top = v.getTop();
		if (x < 40 && y < 40) {
			return LEFT_TOP;
		}
		if (y < 40 && right - left - x < 40) {
			return RIGHT_TOP;
		}
		if (x < 40 && bottom - top - y < 40) {
			return LEFT_BOTTOM;
		}
		if (right - left - x < 40 && bottom - top - y < 40) {
			return RIGHT_BOTTOM;
		}
		if (x < 40) {
			return LEFT;
		}
		if (y < 40) {
			return TOP;
		}
		if (right - left - x < 40) {
			return RIGHT;
		}
		if (bottom - top - y < 40) {
			return BOTTOM;
		}
		return CENTER;
	}

	/**
	 * 处理拖动事件
	 *
	 * @param v
	 * @param event
	 * @param action
	 */
	protected void delDrag(View v, MotionEvent event, int action) {
		switch (action) {
			case MotionEvent.ACTION_MOVE:
				Log.d(TAG, "delDrag: ");
//				float oldDist = spacing(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
				int dx = (int) event.getRawX() - lastX;
				int dy = (int) event.getRawY() - lastY;
				switch (dragDirection) {
					case CENTER: // 点击中心-->>移动
						center(v, dx, dy);
					case RIGHT_BOTTOM: // 右下
						right(v, dx);
						bottom(v, dy);
						break;
				}
				if (dragDirection != CENTER) {
					v.layout(oriLeft, oriTop, oriRight, oriBottom);
					Log.d(TAG, "delDrag: " + "adjustTextSize");
					setMaxLines(10);
				}
				lastX = (int) event.getRawX();
				lastY = (int) event.getRawY();
				break;
			case MotionEvent.ACTION_UP:
				dragDirection = 0;
				break;
		}
	}

	/**
	 * 触摸点为中心->>移动
	 *
	 * @param v
	 * @param dx
	 * @param dy
	 */
	private void center(View v, int dx, int dy) {
		int left = v.getLeft() + dx;
		int top = v.getTop() + dy;
		int right = v.getRight() + dx;
		int bottom = v.getBottom() + dy;
		if (left < -offset) {
			left = -offset;
			right = left + v.getWidth();
		}
		if (right > screenWidth + offset) {
			right = screenWidth + offset;
			left = right - v.getWidth();
		}
		if (top < -offset) {
			top = -offset;
			bottom = top + v.getHeight();
		}
		if (bottom > screenHeight + offset) {
			bottom = screenHeight + offset;
			top = bottom - v.getHeight();
		}
		v.layout(left, top, right, bottom);
	}

	/**
	 * 触摸点为下边缘
	 *
	 * @param v
	 * @param dy
	 */
	private void bottom(View v, int dy) {
		oriBottom += dy;
		if (oriBottom > screenHeight + offset) {
			oriBottom = screenHeight + offset;
		}
		if (oriBottom - oriTop - 2 * offset < 200) {
			oriBottom = 200 + oriTop + 2 * offset;
		}
	}

	/**
	 * 触摸点为右边缘
	 *
	 * @param v
	 * @param dx
	 */
	private void right(View v, int dx) {
		oriRight += dx;
		if (oriRight > screenWidth + offset) {
			oriRight = screenWidth + offset;
		}
		if (oriRight - oriLeft - 2 * offset < 200) {
			oriRight = oriLeft + 2 * offset + 200;
		}
	}

	private interface SizeTester {
		/**
		 * @param suggestedSize  Size of text to be tested
		 * @param availableSpace available space in which text must fit
		 * @return an integer < 0 if after applying {@code suggestedSize} to
		 * text, it takes less space than {@code availableSpace}, > 0
		 * otherwise
		 */
		int onTestSize(int suggestedSize, RectF availableSpace);
	}

	public AutoResizeTextView(final Context context) {
		this(context, null, android.R.attr.textViewStyle);
	}

	public AutoResizeTextView(final Context context, final AttributeSet attrs) {
		this(context, attrs, android.R.attr.textViewStyle);
	}

	public AutoResizeTextView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		// using the minimal recommended font size
		_minTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics());
		_maxTextSize = getTextSize();
		_paint = new TextPaint(getPaint());
		if (_maxLines == 0)
			// no value was assigned during construction
			_maxLines = NO_LINE_LIMIT;
		// prepare size tester:
		_sizeTester = new SizeTester() {
			final RectF textRect = new RectF();

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public int onTestSize(final int suggestedSize, final RectF availableSpace) {
				_paint.setTextSize(suggestedSize);
				final TransformationMethod transformationMethod = getTransformationMethod();
				final String text;
				if (transformationMethod != null)
					text = transformationMethod.getTransformation(getText(), AutoResizeTextView.this).toString();
				else
					text = getText().toString();
				final boolean singleLine = getMaxLines() == 1;
				if (singleLine) {
					textRect.bottom = _paint.getFontSpacing();
					textRect.right = _paint.measureText(text);
				} else {
					final StaticLayout layout = new StaticLayout(text, _paint, _widthLimit, Alignment.ALIGN_NORMAL,
							_spacingMult, _spacingAdd, true);
					// return early if we have more lines
					if (getMaxLines() != NO_LINE_LIMIT && layout.getLineCount() > getMaxLines())
						return 1;
					textRect.bottom = layout.getHeight();
					int maxWidth = -1;
					int lineCount = layout.getLineCount();
					for (int i = 0; i < lineCount; i++) {
						int end = layout.getLineEnd(i);
						if (i < lineCount - 1 && end > 0 && !isValidWordWrap(text.charAt(end - 1), text.charAt(end)))
							return 1;
						if (maxWidth < layout.getLineRight(i) - layout.getLineLeft(i))
							maxWidth = (int) layout.getLineRight(i) - (int) layout.getLineLeft(i);
					}
					//for (int i = 0; i < layout.getLineCount(); i++)
					//    if (maxWidth < layout.getLineRight(i) - layout.getLineLeft(i))
					//        maxWidth = (int) layout.getLineRight(i) - (int) layout.getLineLeft(i);
					textRect.right = maxWidth;
				}
				textRect.offsetTo(0, 0);
				if (availableSpace.contains(textRect))
					// may be too small, don't worry we will find the best match
					return -1;
				// else, too big
				return 1;
			}
		};
		_initialized = true;
		initScreenW_H();
		setOnTouchListener(this);
	}

	public boolean isValidWordWrap(char before, char after) {
		return before == ' ' || before == '-';
	}

	@Override
	public void setAllCaps(boolean allCaps) {
		super.setAllCaps(allCaps);
		adjustTextSize();
	}

	@Override
	public void setTypeface(final Typeface tf) {
		super.setTypeface(tf);
		adjustTextSize();
	}

	@Override
	public void setTextSize(final float size) {
		_maxTextSize = size;
		adjustTextSize();
	}

	@Override
	public void setMaxLines(final int maxLines) {
		super.setMaxLines(maxLines);
		_maxLines = maxLines;
		adjustTextSize();
	}

	@Override
	public int getMaxLines() {
		return _maxLines;
	}

	@Override
	public void setSingleLine() {
		super.setSingleLine();
		_maxLines = 1;
		adjustTextSize();
	}

	@Override
	public void setSingleLine(final boolean singleLine) {
		super.setSingleLine(singleLine);
		if (singleLine)
			_maxLines = 1;
		else _maxLines = NO_LINE_LIMIT;
		adjustTextSize();
	}

	@Override
	public void setLines(final int lines) {
		super.setLines(lines);
		_maxLines = lines;
		adjustTextSize();
	}

	@Override
	public void setTextSize(final int unit, final float size) {
		final Context c = getContext();
		Resources r;
		if (c == null)
			r = Resources.getSystem();
		else r = c.getResources();
		_maxTextSize = TypedValue.applyDimension(unit, size, r.getDisplayMetrics());
		adjustTextSize();
	}

	@Override
	public void setLineSpacing(final float add, final float mult) {
		super.setLineSpacing(add, mult);
		_spacingMult = mult;
		_spacingAdd = add;
	}

	/**
	 * Set the lower text size limit and invalidate the view
	 *
	 * @param minTextSize
	 */
	public void setMinTextSize(final float minTextSize) {
		_minTextSize = minTextSize;
		adjustTextSize();
	}

	private void adjustTextSize() {
		// This is a workaround for truncated text issue on ListView, as shown here: https://github
		// .com/AndroidDeveloperLB/AutoFitTextView/pull/14
		// TODO think of a nicer, elegant solution.
//    post(new Runnable()
//    {
//    @Override
//    public void run()
//      {
		if (!_initialized)
			return;
		final int startSize = (int) _minTextSize;
		final int heightLimit = getMeasuredHeight() - getCompoundPaddingBottom() - getCompoundPaddingTop();
		_widthLimit = getMeasuredWidth() - getCompoundPaddingLeft() - getCompoundPaddingRight();
		if (_widthLimit <= 0)
			return;
		_paint = new TextPaint(getPaint());
		_availableSpaceRect.right = _widthLimit;
		_availableSpaceRect.bottom = heightLimit;
		superSetTextSize(startSize);
//      }
//    });
	}

	private void superSetTextSize(int startSize) {
		int textSize = binarySearch(startSize, (int) _maxTextSize, _sizeTester, _availableSpaceRect);
		super.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
	}

	private int binarySearch(final int start, final int end, final SizeTester sizeTester, final RectF availableSpace) {
		int lastBest = start, lo = start, hi = end - 1, mid;
		while (lo <= hi) {
			mid = lo + hi >>> 1;
			final int midValCmp = sizeTester.onTestSize(mid, availableSpace);
			if (midValCmp < 0) {
				lastBest = lo;
				lo = mid + 1;
			} else if (midValCmp > 0) {
				hi = mid - 1;
				lastBest = hi;
			} else return mid;
		}
		// make sure to return last best
		// this is what should always be returned
		return lastBest;
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
		super.onTextChanged(text, start, before, after);
		adjustTextSize();
	}

	@Override
	protected void onSizeChanged(final int width, final int height, final int oldwidth, final int oldheight) {
		Log.d(TAG, "onSizeChanged: ");
		super.onSizeChanged(width, height, oldwidth, oldheight);
		if (width != oldwidth || height != oldheight)
			adjustTextSize();
	}
}