package com.consortium.ithos.quicknotepad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainScene extends AppCompatActivity {

    private PaintView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_scene);
        view = (PaintView) findViewById(R.id.paintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        view.externalInit(metrics);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.normal:
                view.normal();
                return true;
            case R.id.emboss:
                view.emboss();
                return true;
            case R.id.blur:
                view.blur();
                return true;
            case R.id.clear:
                view.clear();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
