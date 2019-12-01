package com.consortium.ithos.quicknotepad;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

public class MainScene extends AppCompatActivity {

    private PaintView view;
    private boolean timeoutDisabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_scene);
        view = (PaintView) findViewById(R.id.PaintView);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        view.externalInit(metrics);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.main, menu);
	SetScreenTimeoutBlocked(timeoutDisabled, menu.findItem(R.id.AlwaysActive));
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
            case R.id.back:
                view.back();
                return true;
            case R.id.next:
                view.next();
                return true;
            case R.id.clear:
                view.clear();
                return true;
            case R.id.AlwaysActive:
                SetScreenTimeoutBlocked(!item.isChecked(), item);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SetScreenTimeoutBlocked(boolean blocked, MenuItem item)
    {
        MenuItem checkable = item;
        checkable.setChecked(blocked);
        timeoutDisabled = blocked;

        if(timeoutDisabled)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}
