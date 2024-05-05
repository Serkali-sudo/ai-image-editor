package com.serhat.aieditor.drawing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


import androidx.core.content.ContextCompat;

import com.serhat.aieditor.R;
import com.serhat.aieditor.Utils;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;


public class DrawView extends View {

    private static final float TOUCH_TOLERANCE = 4;
    private float mX;
    private float mY;
    private Path mPath;
    private Paint mPaint;

    private final ArrayList<Stroke> paths = new ArrayList<>();
    private final ArrayList<Stroke> undonePaths = new ArrayList<>();
    public int currentColour;
    public float strokeWidth;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    Paint background;
    public int backgroundColour;

    private int containerWidth;
    private int containerHeight;
    float[] mv = new float[9];
    Matrix matrix = new Matrix();
    float currentScale;
    float curX;
    float curY;
    float minScale;
    Rect clipBounds;


    public DrawView(Context context) {
        super(context);
        setFocusable(true);
        setFocusableInTouchMode(true);
        initPaints();
    }


    public String captureCanvasAsBase64() {
        if (mBitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            save().compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return "data:image/png;base64," + Base64.encodeToString(byteArray,
                    Base64.DEFAULT);
        }
        return null;
    }

    public DrawView(Context context, AttributeSet attributes) {
        super(context, attributes);
        initPaints();
    }

    public void setUpDrawing() {
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        backgroundColour = Color.TRANSPARENT;
    }


    private void initPaints() {
        background = new Paint();
        setUpDrawing();
    }


    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        Utils.d("DrawingView", "Changed");
        strokeWidth = 50f;
        fitScaleInitial();
        invalidate();

    }

    public void setUpCanvas(int width, int height) {
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        if (currentColour == 0) {
            currentColour = ContextCompat.getColor(getContext(), R.color.paint_color);
        }
        strokeWidth = 50f;
        Utils.d("DrawingView", "Called Setup:" + width + "x" + height);
        fitScaleInitial();
    }

    private void fitScaleInitial() {
        containerWidth = getWidth();
        containerHeight = getHeight();
        if (mBitmap != null && containerWidth > 0 && containerHeight > 0) {
            mCanvas = new Canvas(mBitmap);
            int imgHeight = mBitmap.getHeight();
            int imgWidth = mBitmap.getWidth();
            float scale;
            int initX = 0;
            int initY = 0;

            float scaleX = (float) containerWidth / imgWidth;
            float scaleY = (float) containerHeight / imgHeight;
            scale = Math.min(scaleX, scaleY);

            float minScaleFactor = 0.5f;
            if (scale < minScaleFactor) {
                scale = minScaleFactor;
            }

            float newWidth = imgWidth * scale;
            float newHeight = imgHeight * scale;

            initX = (int) ((containerWidth - newWidth) / 2);
            initY = (int) ((containerHeight - newHeight) / 2);

            strokeWidth /= scale;
            matrix.setScale(scale, scale);
            matrix.postTranslate(initX, initY);

            curX = initX;
            curY = initY;
            currentScale = scale;
            minScale = scale;


        }

    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        if (mBitmap != null) {
            if (mCanvas == null) {
                mCanvas = new Canvas(mBitmap);
            }
        }
        clipBounds = canvas.getClipBounds();
        canvas.drawBitmap(mBitmap, matrix, background);
        canvas.concat(matrix);

        mCanvas.drawColor(backgroundColour);
        for (Stroke currentPath : paths) {
            mPaint.setColor(currentPath.colour);
            mPaint.setStrokeWidth(currentPath.strokeWidth);
            mCanvas.drawPath(currentPath.path, mPaint);
            invalidate();
        }

        canvas.restore();
        invalidate();
    }


    private void touchStart(float x, float y) {
        undonePaths.clear();
        mPath = new Path();

        Stroke currentPath = new Stroke(currentColour, strokeWidth, mPath);
        paths.add(currentPath);

        mPath.reset();
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        matrix.getValues(mv);
        float touchX = (event.getX() * (1 / mv[4]) - (mv[2] / mv[4]));
        float touchY = (event.getY() * (1 / mv[4]) - (mv[5] / mv[4]));
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }


    public void clearCanvas() {
        if (!paths.isEmpty()) {
            paths.clear();
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        }
        invalidate();
    }

    public boolean isCanvasClear() {
        return paths.isEmpty();
    }


    public void undo() {
        if (!paths.isEmpty()) {
            undonePaths.add(paths.remove(paths.size() - 1));
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        } else {
            Toast.makeText(getContext(), "Cant Undo", Toast.LENGTH_SHORT).show();
        }
        invalidate();
    }

    public void redo() {
        if (!undonePaths.isEmpty()) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        } else {
            Toast.makeText(getContext(), "Cant Redo", Toast.LENGTH_SHORT).show();
        }
        invalidate();
    }

    public Bitmap save() {
        Bitmap blackBitmap = Bitmap.createBitmap(mBitmap.getWidth(), mBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(blackBitmap);
        canvas.drawBitmap(mBitmap, 0f, 0f, null);
        canvas.drawColor(Color.BLACK);
        for (Stroke currentPath : paths) {
            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(currentPath.strokeWidth);
            canvas.drawPath(currentPath.path, mPaint);
        }
        return blackBitmap;
    }


}