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
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ithos on 7/16/18.
 */
public class PaintView extends View {

    public static  int DEFAULT_SIZE = 10;
    public static final int DEFAULT_COLOR = Color.WHITE;
    public static final int DEFAULT_BG_COLOR = Color.BLACK;
    private static final float TOUCH_TOLERANCE = 4;
    private static final String NOTE_FILE_NAME = "NotePage_";
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
    private ArrayList<IndexedFile> _idxFiles = null;
    private int _currentFileIndex = -1;


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
        _idxFiles = indexNoteFiles(NOTE_FILE_NAME);
        _currentFileIndex = _idxFiles.size();
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

    public void back()
    {
        if(_currentFileIndex > 0) {
            moveCurrentFile();
            clear();
            --_currentFileIndex;
            openNote(_currentFileIndex);
        }

    }

    public void next()
    {
        moveCurrentFile();
        clear();
        if(_currentFileIndex < _idxFiles.size() - 1)
        {
            ++_currentFileIndex;
            openNote(_currentFileIndex);
        }
        else if(_currentFileIndex < _idxFiles.size())
        {
            ++_currentFileIndex;
        }
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
        fingerPath.pathPoints.add(new Point(x, y));

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
            _paths.get(_paths.size() - 1).pathPoints.add(new Point(x, y));
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

    private ArrayList<IndexedFile> indexNoteFiles(String fileKey)
    {
        List<File> files = getListFiles(getContext().getFilesDir(), fileKey);
        ArrayList<IndexedFile> idxFiles = new ArrayList<IndexedFile>();
        int maxNum = 0;
        for(File file : files)
        {
            String fileName = file.getName();
            String num = fileName.substring(fileKey.length());
            int pageNum = Integer.parseInt(num);
            int index = 0;
            for(int i=0; idxFiles.size() > 0 && i <= idxFiles.size(); ++i)
            {
                if( i >= idxFiles.size() ||
                        pageNum < idxFiles.get(i).index)
                {
                    index = i;
                    break;
                }
            }

            idxFiles.add(index, new IndexedFile(file, pageNum));
        }

        return idxFiles;
    }

    private List<File> getListFiles(File ParentDir,  String Key)
    {
        ArrayList<File> inFiles = new ArrayList<File>();
        File[] files = ParentDir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                inFiles.addAll(getListFiles(file, Key));
            } else {
                if(file.getName().contains(Key)){
                    inFiles.add(file);
                }
            }
        }
        return inFiles;
    }

    private void moveCurrentFile()
    {
        if(!_paths.isEmpty())
        {
            saveCurrentNote();
        }
        else if(_currentFileIndex < _idxFiles.size())
        {
            deleteCurrentNote();
        }
    }

    private void deleteCurrentNote()
    {
        int fileNum =  _idxFiles.get(_currentFileIndex).index ;
        File directory = getContext().getFilesDir();
        File file = new File(directory, NOTE_FILE_NAME + fileNum);
        file.delete();
        _idxFiles.remove(_currentFileIndex);
        _paths.clear();
        _path = null;
    }

    private boolean saveCurrentNote()
    {
        boolean newFile = (_currentFileIndex > _idxFiles.size() - 1);
        int fileNum = newFile ? getMaxFileIndex() :
                _idxFiles.get(_currentFileIndex).index ;
        File directory = getContext().getFilesDir();
        File file = new File(directory, NOTE_FILE_NAME + fileNum);

        if(!newFile)
        {
            file.delete();
        }

        try
        {
            file.createNewFile();
            file.setWritable(true);
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);

            for(PenPath path : _paths ) {
                StringBuilder data = new StringBuilder();
                data.append(path.color).append(";" ).append( (path.emboss ? "1" : "0")).append(";").append((path.blur ? "1" : "0")).
                        append(";").append(path.penWidth).append(";");

                for(Point pnt : path.pathPoints)
                {
                    data.append(pnt.x).append(",").append(pnt.y).append(";");
                }

                data.append(System.getProperty("line.separator"));

                myOutWriter.append(data);
            }

            myOutWriter.close();

            fOut.flush();
            fOut.close();

            if(newFile) {
                _idxFiles.add(new IndexedFile(file, fileNum));
            }
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
            return false;
        }

        return true;
    }

    private boolean openNote(int index)
    {
        int fileNum = _idxFiles.get(index).index;

        File directory = getContext().getFilesDir();
        File file = new File(directory, NOTE_FILE_NAME + fileNum);

        try {

            file.setReadable(true);
            FileInputStream fIn = new FileInputStream(file);
            InputStreamReader iSReader = new InputStreamReader(fIn);
            BufferedReader buff = new BufferedReader(iSReader);
            String line = buff.readLine();
            _paths.clear();
            while(line != null){

                String[] data = line.split(";");
                Path tmpPath = new Path();
                PenPath tmpPenPath = new PenPath(Integer.parseInt(data[0]), data[1] == "1", data[2] == "1", Integer.parseInt(data[3]), tmpPath);
                _paths.add(tmpPenPath);
                _path = tmpPath;

                if(data.length > 4)
                {
                    String[] coords = data[4].split(",");
                    float x = Float.parseFloat(coords[0]), y = Float.parseFloat(coords[1]);

                    tmpPath.reset();
                    tmpPath.moveTo(x, y);
                    tmpPenPath.pathPoints.add(new Point(x, y));

                    _x = x;
                    _y = y;

                    for(int i = 5; i < data.length; ++i)
                    {
                        coords = data[i].split(",");
                        x = Float.parseFloat(coords[0]);
                        y = Float.parseFloat(coords[1]);

                        touchMove(x, y);
                    }

                    touchUp();
                }

                line = buff.readLine();
            }

        }catch (IOException e)
        {
            Log.e("Exception", "File read failed: " + e.toString());
            return false;
        }

        return true;
    }

    private int getMaxFileIndex()
    {
        return (_idxFiles.size() > 0 ? _idxFiles.get(_idxFiles.size() - 1).index + 1 : 1);
    }

}
