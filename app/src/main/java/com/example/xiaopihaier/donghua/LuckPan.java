package com.example.xiaopihaier.donghua;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

/**
 * Created by 背书包的小屁孩儿 on 17-1-4.
 */

public class LuckPan extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    /**
     * 用于绘制的线程
     */
    private Thread t;

    /**
     * 线程的控制开关
     *
     * @param context
     */
    private boolean isRunning;
    /**
     * 盘快的奖项
     */
    private String[] mStrs = new String[]{"单反相机", "IPAD", "恭喜发财", "IPHONE", "服装一套", "恭喜发财"};
    /**
     * 奖项的图片
     */
    private int[] mImgs = new int[]{R.mipmap.danfan, R.mipmap.ipad, R.mipmap.xiaolian, R.mipmap.iphone, R.mipmap.fuzhuang, R.mipmap.xiaolian};
    /**
     * 与图片对应的bitmap数组
     */
    private Bitmap[] mImgsBitmap;
    private Bitmap mBgBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg2);

    /**
     * 盘快的颜色
     */
    private int[] mColor = new int[]{0xffffc300, 0xfff17e01, 0xffffc300, 0xfff17e01, 0xffffc300, 0xfff17e01, 0xffffc300, 0xfff17e01, 0xffffc300, 0xfff17e01, 0xffffc300, 0xfff17e01};

    private int mItemCount = 6;

    /**
     * 绘制盘快的画笔
     */
    private Paint mArcPaint;
    /**
     * 绘制文本的画笔
     */
    private Paint mTextPaint;


    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics());
    /**
     * 盘快的范围
     */
    private RectF mRange = new RectF();
    /**
     * 整个盘快的直径
     */
    private int mRadius;
    /**
     * 转盘的中心位置
     */
    private int mCenter;
    /**
     * 这里我们的padding直接已paddingLeft为准
     */
    private int mPadding;

    /**
     * 滚动的速度
     */
    private double mSpeed;
    private volatile float mStartAngle = 0;
    /**
     * 判断是否点击了停止按钮
     */
    private boolean isShouldEnd;


    public LuckPan(Context context) {
        this(context, null);
    }

    public LuckPan(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
        //可获得焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        //设置常量
        setKeepScreenOn(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        mPadding = getPaddingLeft();
        //直径
        mRadius = width - mPadding * 2;
        //中心点
        mCenter = width / 2;

        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //初始化绘制盘快的画笔
        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setDither(true);


        //初始化绘制盘快的画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(0xffffffff);
        mTextPaint.setTextSize(mTextSize);

        //初始化盘快绘制的范围
        mRange = new RectF(mPadding, mPadding, mPadding + mRadius, mPadding + mRadius);


        //初始化图片
        mImgsBitmap = new Bitmap[mItemCount];
        for (int i = 0; i < mItemCount; i++) {
            mImgsBitmap[i] = BitmapFactory.decodeResource(getResources(), mImgs[i]);
        }

        isRunning = true;
        t = new Thread(this);
        t.start();


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
    }

    @Override
    public void run() {
        //不断进行绘制
        while (isRunning) {
            long start = System.currentTimeMillis();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < 50) {
                try {
                    Thread.sleep(50 - (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw() {
        try {
            mCanvas = mHolder.lockCanvas();
            if (mCanvas != null) {
                //绘制背景
                drawBg();
                //绘制盘快
                float tmpAngel = mStartAngle;
                float sweepAngle = 360 / mItemCount;
                for (int i = 0; i < mItemCount; i++) {
                    mArcPaint.setColor(mColor[i]);
                    //绘制盘快
                    mCanvas.drawArc(mRange, tmpAngel, sweepAngle, true, mArcPaint);
                    //绘制文本
                    drawText(tmpAngel, sweepAngle, mStrs[i]);
                    //绘制icon
                    drawIcon(tmpAngel, mImgsBitmap[i]);
                    tmpAngel += sweepAngle;
                }
                mStartAngle += mSpeed;
                //如果点击了停止按钮
                if (isShouldEnd) {
                    mSpeed -= 1;
                }
                if (mSpeed <= 0) {
                    mSpeed = 0;
                    isShouldEnd = false;
                }
            }
        } catch (Exception e) {
        } finally {
            if (mCanvas != null) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    /**
     * 点击启动旋转
     */
    public void luckyStart(int index) {
        //计算每一项角度
        float angle = 360 / mItemCount;
        //计算每一项中奖范围(当前index)
        //1->150~210
        //0->210~270
        float from = 233 - (index + 1) * angle;
        float end = from + angle;

        //设置停下来需要旋转的距离
        float targetFrom = 3 * 360 + from;
        float targetEnd = 3 * 360 + end;

        /**
         * v1-0
         * 且每次-1
         * (v1+0)*(v1+1)/2=targetFrom
         */
        float v1 = (float) (-1 + Math.sqrt((1 + 8 * targetFrom)) / 2);
        float v2 = (float) (-1 + Math.sqrt((1 + 8 * targetEnd)) / 2);

        mSpeed = v1 + Math.random() * (v2 - v1);
        if (mSpeed >= 49) {
            luckyStart(index);
        } else {
            return;
        }
        Log.i("mSpeed", String.valueOf(mSpeed));
        isShouldEnd = false;
    }

    public void luckyEnd() {
        mStartAngle = 0;
        isShouldEnd = true;
    }

    /**
     * 转盘是否在旋转
     *
     * @return
     */
    public boolean isStart() {
        return mSpeed != 0;
    }

    public boolean isShouldEnd() {
        return isShouldEnd;
    }

    /**
     * 绘制ico
     *
     * @param tmpAngel
     * @param bitmap
     */
    private void drawIcon(float tmpAngel, Bitmap bitmap) {
        //设置图片的宽度为直径/8
        int imgWidth = mRadius / 8;
        //Math.PI/180
        float angle = (float) ((tmpAngel + 360 / mItemCount / 2) * Math.PI / 180);

        int x = (int) (mCenter + mRadius / 2 / 2 * Math.cos(angle));
        int y = (int) (mCenter + mRadius / 2 / 2 * Math.sin(angle));
        //确定那个图片位置
        Rect rect = new Rect(x - imgWidth / 2, y - imgWidth / 2, x + imgWidth / 2, y + imgWidth / 2);
        mCanvas.drawBitmap(bitmap, null, rect, null);
    }

    /**
     * 绘制每个盘快的文本
     *
     * @param tmpAngel
     * @param sweepAngle
     * @param mStr
     */
    private void drawText(float tmpAngel, float sweepAngle, String mStr) {
        Path path = new Path();
        path.addArc(mRange, tmpAngel, sweepAngle);
        //利用水平偏移量让文字居中
        float textWidth = mTextPaint.measureText(mStr);
        int hOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
        int vOffset = mRadius / 2 / 6;//垂直偏移量
        mCanvas.drawTextOnPath(mStr, path, hOffset, vOffset, mTextPaint);
    }

    /**
     * 绘制背景
     */
    private void drawBg() {
        mCanvas.drawColor(0xffffffff);
        mCanvas.drawBitmap(mBgBitmap, null, new Rect(mPadding / 2, mPadding / 2, getMeasuredWidth() - mPadding / 2, getMeasuredHeight() - mPadding / 2), null);
    }
}
