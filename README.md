# auto-text-drawview
draw text which size is auto on a view


![效果图](https://github.com/ddxxll2008/auto-text-drawview/blob/master/gif/1.gif)

在自定义界面上绘制文字，大小自适应，可以拖动改变位置，拖拽改变大小。

========================================================

##自定义界面上绘制Text，可通过拖动控制文字大小及其位置

最近项目上有个需求，需要在一块区域中显示文字，这块区域可以拖动，也可以通过拖拽右下角来改变大小，里面的文字大小要根据区域的大小进行自适应。刚开始觉得这个需求不难，只需要一个TextView就能实现。
后来发现虽然使用TextView可以很容易实现拖动与缩放的功能，但是文字大小不会改变。在求助github的时候发现了AutoFitTextView控件，参考https://github.com/AndroidDeveloperLB/AutoFitTextView，但是使用的时候，每一次需要根据区域的大小重新创建一个TextView。想想这样对性能消耗太大，不如自己编写一个。

####程序常量与变量
具体含义见注释

```
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
```

####构造函数

因为是将文字绘制在View上，所以在构造函数里生成了一个背景画笔和文字画笔，同时生成背景的矩阵。

```
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
```
####Touch事件
在TouchEvent中，手指按下时，根据手指的点击位置，判断是拖动事件还是缩放事件，并设置eventType。
根据eventType，在手指拖动时，执行移动或者缩放的操作。执行完之后，别忘了invalidate，重新绘制区域。

```
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

```
####onDraw在界面上绘制
在onDraw时间里，需要调用adjustTextSize函数计算文字大小，得到文字大小后，需要进行文字绘制。在绘制的时候，通过StaticLayout对文字进行自动换行操作，将文字绘制在背景的Rect上。

```
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
```
####计算文字大小
首先获取背景的长度和宽度，最开始将文字大小设为行高的0.9倍，然后通过TextPaint.getTextBounds，获取文字的长宽，如果文字的面积大于背景矩阵面积\*0.7（因为行与行之间有间隙，假设文字占据了行高的0.7倍），则减小文字的大小，一直到文字大小稍小于背景面积\*0.7为止。如此便可获得合适的文字大小。

```
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
			textSize = textSize -3;
			textPaint.setTextSize((float) (textSize));
			textPaint.getTextBounds(text, 0, text.length(), textMeasureRect);
			textWidth = textMeasureRect.width();
			textHeight = textMeasureRect.height();
		}
		textPaint.getTextBounds(text, 0, text.length(), textMeasureRect);
		Log.d(TAG, "adjustTextSize: " + textMeasureRect.width() + "   " + textMeasureRect.height());
	}
```
后记：经过这个项目，知道了在解决问题前，应该先好好的分析解决思路。第三方控件并不是万能的，只有深刻的理解了Android上控件的机理，才能准确的找到解决问题的思路。
