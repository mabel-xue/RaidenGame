package com.mabel.leidiangame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/*游戏结束时的界面*/
public class EndView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	private Bitmap background;		//背景图片
	private Bitmap button;			//按钮图片
	private Bitmap button2;
	private Canvas canvas;			// 画布资源
	private Paint paint; 			// 画笔
	private SurfaceHolder sfh;
	private Thread thread;			// 绘图线程
	private int scoreSum;			//记录总分
	private float button_x;
	private float button_y;
	private float button_y2;
	private float scalex;			//背景缩放的比例
	private float scaley;
	private float screen_width;
	private float screen_height;
	private boolean threadFlag;
	private boolean isBtChange;
	private boolean isBtChange2;
	private String startGame = "重新挑战";
	private String exitGame = "退出游戏";
	private MainActivity mainActivity;
	private GameSoundPool sounds;

	public EndView(Context context,int scoreSum) {
		super(context);
		this.mainActivity = (MainActivity) context;
		this.scoreSum = scoreSum;
		sounds = new GameSoundPool(mainActivity);
		sounds.setOpen(true);
		sounds.initSysSound();
		sfh = this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();
		thread = new Thread(this);
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		screen_width = this.getWidth();
		screen_height = this.getHeight();
		initBitmap(); // 初始化图片资源
		threadFlag = true;
		thread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		threadFlag = false;
		release();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(event.getAction() == MotionEvent.ACTION_DOWN && event.getPointerCount() == 1){
			float x = event.getX();
			float y = event.getY();
			//判断第一个按钮是否被按下
			if(x > button_x && x < button_x + button.getWidth()
					&& y > button_y && y < button_y + button.getHeight()) {
				sounds.playSound(1, 0);
				isBtChange = true;
				drawSelf();
				mainActivity.toMainView();
			}
			//判断第二个按钮是否被按下
			else if(x > button_x && x < button_x + button.getWidth()
					&& y > button_y2 && y < button_y2 + button.getHeight()) {
				sounds.playSound(1, 0);
				isBtChange2 = true;
				drawSelf();
				threadFlag = false;
				System.exit(0);
			}
			return true;
		}
		else if(event.getAction() == MotionEvent.ACTION_MOVE){
			float x = event.getX();
			float y = event.getY();
			if(x > button_x && x < button_x + button.getWidth()
					&& y > button_y && y < button_y + button.getHeight()) {
				isBtChange = true;
			} else{
				isBtChange = false;
			}
			if(x > button_x && x < button_x + button.getWidth()
					&& y > button_y2 && y < button_y2 + button.getHeight()) {
				isBtChange2 = true;
			} else{
				isBtChange2 = false;
			}
			return true;
		}
		else if(event.getAction() == MotionEvent.ACTION_UP){
			isBtChange = false;
			isBtChange2 = false;
			return true;
		}
		return false;
	}

	// 初始化图片
	public void initBitmap() {
		background = BitmapFactory.decodeResource(getResources(), R.drawable.bg_01);
		button = BitmapFactory.decodeResource(getResources(), R.drawable.button);
		button2 = BitmapFactory.decodeResource(getResources(), R.drawable.button2);
		scalex = screen_width / background.getWidth();
		scaley = screen_height / background.getHeight();
		button_x =  screen_width/2 - button.getWidth()/2;
		button_y = screen_height/2 + button.getHeight();
		button_y2 = button_y + button.getHeight() + 40;
	}

	// 绘图函数
	public void drawSelf() {
		try {
			canvas = sfh.lockCanvas();
			canvas.drawColor(Color.BLACK); // 绘制背景色
			canvas.save();
			// 计算背景图片与屏幕的比例
			canvas.scale(scalex, scaley, 0, 0);
			canvas.drawBitmap(background, 0, 0, paint);   // 绘制背景图
			canvas.restore();
			if(isBtChange){
				canvas.drawBitmap(button2, button_x, button_y, paint);
			} else{
				canvas.drawBitmap(button, button_x, button_y, paint);
			}
			if(isBtChange2){
				canvas.drawBitmap(button2, button_x, button_y2, paint);
			} else{
				canvas.drawBitmap(button, button_x, button_y2, paint);
			}
			paint.setTextSize(40);
			Rect rect = new Rect();
			//返回包围整个字符串的最小的一个Rect区域
			paint.getTextBounds(startGame, 0, startGame.length(), rect);
			float strWid = rect.width();
			float strHei = rect.height();
			canvas.drawText(startGame, screen_width/2 - strWid/2, button_y + button.getHeight()/2 + strHei/2, paint);
			canvas.drawText(exitGame, screen_width/2 - strWid/2, button_y2 + button.getHeight()/2 + strHei/2, paint);
			paint.setTextSize(60);
			paint.setColor(Color.WHITE);
			float textLength = paint.measureText("总分:" + String.valueOf(scoreSum));
			canvas.drawText("总分:" + String.valueOf(scoreSum),
					screen_width/2 - textLength/2, screen_height/2 - 100, paint);
		} catch (Exception err) {
			err.printStackTrace();
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}

	public void release(){
		if(!background.isRecycled()){
			background.recycle();
		}
		if(!button.isRecycled()){
			button.recycle();
		}
		if(!button2.isRecycled()){
			button2.recycle();
		}
	}

	@Override
	public void run() {
		while (threadFlag) {
			long startTime = System.currentTimeMillis();
			drawSelf();
			long endTime = System.currentTimeMillis();
			try {
				if (endTime - startTime < 500)
					Thread.sleep(500 - (endTime - startTime));
			} catch (InterruptedException err) {
				err.printStackTrace();
			}
		}
	}
}
