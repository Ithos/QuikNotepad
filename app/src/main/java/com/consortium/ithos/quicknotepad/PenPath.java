package com.consortium.ithos.quicknotepad;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ithos on 7/16/18.
 */
public class PenPath {

    public int color;
    public boolean emboss;
    public  boolean blur;
    public int penWidth;
    public Path penPath;
    public ArrayList<Point> pathPoints = new ArrayList<Point>();

    public PenPath(int color, boolean emboss, boolean blur, int penWidth, Path penPath)
    {
        init(color, emboss, blur, penWidth, penPath);
    }

    public void init(int color, boolean emboss, boolean blur, int penWidth, Path penPath)
    {
        this.color = color;
        this.emboss = emboss;
        this.blur = blur;
        this.penWidth = penWidth;
        this.penPath = penPath;
    }


}
