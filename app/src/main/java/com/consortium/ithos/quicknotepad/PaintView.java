package com.consortium.ithos.quicknotepad;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by ithos on 7/16/18.
 */
public class PaintView extends View {

    public static  int DEFAULT_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.WHITE;
    public static final int DEFAULT_BG_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;
    private float _x, _y;
    private Path _path;
    private Paint _paint;
    private ArrayList<PenPath> _paths = new ArrayList<PenPath>();
    private int _currentColor = DEFAULT_COLOR;
    private int _backgroundColor = DEFAULT_BG_COLOR;
    private int _strokeWidth = DEFAULT_SIZE;
    private MaskFilter _emboss;
    private MaskFilter _blur;
    private  boolean _useEmboss = false;
    private boolean _useBlur = false;
    private Bitmap _bitmap;
    private Canvas _cavas;
    private Paint _bitmapPaint = new Paint(Paint.DITHER_FLAG);


    public  PaintView (Context context) { super(context, null); }

    public  PaintView (Context context, AttributeSet attr)
    {
        super(context, attr);
        init();
    }

    private void init()
    {
        _paint = new Paint();
        _paint.setAntiAlias(true);
        _paint.setDither(true);
        _paint.setColor(DEFAULT_COLOR);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setStrokeJoin(Paint.Join.ROUND);
        _paint.setStrokeCap(Paint.Cap.ROUND);
        _paint.setXfermode(null);
        _paint.setAlpha(0xff);

        _emboss = new EmbossMaskFilter(new float[]{1,1,1}, 0.4f, 6, 3.5f);
        _blur = new BlurMaskFilter(5, BlurMaskFilter.Blur.NORMAL);
    }

    public void externalInit(DisplayMetrics metrics)
    {
        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        _bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        _cavas = new Canvas(_bitmap);

        _currentColor = DEFAULT_COLOR;
        _strokeWidth = DEFAULT_SIZE;
    }

    public void setFilters(boolean blur, boolean emboss)
    {
        _useBlur = blur;
        _useEmboss = emboss;
    }

    public void normal()
    {
        setFilters(false, false);
    }

    public void emboss()
    {
        setFilters(false, true);
    }

    public void blur()
    {
        setFilters(true, false);
    }

    public void clear()
    {
        _backgroundColor = DEFAULT_BG_COLOR;
        _paths.clear();
        setFilters(false, false);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        canvas.save();
        _cavas.drawColor(_backgroundColor);

        for(PenPath fp: _paths)
        {
            _paint.setColor(fp.color);
            _paint.setStrokeWidth(fp.penWidth);
            _paint.setMaskFilter(null);

            if(fp.emboss)
                _paint.setMaskFilter(_emboss);
            else if(fp.blur)
                _paint.setMaskFilter(_blur);

            _cavas.drawPath(fp.penPath, _paint);
        }

        canvas.drawBitmap(_bitmap, 0, 0, _bitmapPaint);
        canvas.restore();
    }

    private void touchStart(float x, float y)
    {
        _path = new Path();
        PenPath fingerPath = new PenPath(_currentColor, _useEmboss, _useBlur, _strokeWidth, _path);
        _paths.add(fingerPath);

        _path.reset();
        _path.moveTo(x, y);

        _x = x;
        _y = y;
    }

    private void touchMove(float x, float y)
    {
        float dx = Math.abs(_x - x);
        float dy = Math.abs(_y - y);

        if(dx > TOUCH_TOLERANCE || dy > TOUCH_TOLERANCE)
        {
            _path.quadTo(_x, _y, (x + _x)/2, (y + _y)/2);
            _x = x;
            _y = y;
        }
    }

    private void touchUp()
    {
        _path.lineTo(_x, _y);
    }

    @Override
    public  boolean onTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }


}
