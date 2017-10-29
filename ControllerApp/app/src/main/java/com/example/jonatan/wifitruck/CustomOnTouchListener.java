package com.example.jonatan.wifitruck;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Jonatan on 2017-06-04.
 */

public class CustomOnTouchListener implements View.OnTouchListener
{
    private static MainActivity m_mainActivity;

    CustomOnTouchListener(MainActivity mainActivity)
    {
        m_mainActivity = mainActivity;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId())
        {
            case MotionEvent.ACTION_DOWN :
                // Pressed down
                m_mainActivity.printInGuiThread("Pressed down!");
            case MotionEvent.ACTION_UP :
                // Released
                m_mainActivity.printInGuiThread("Released!");
                return false;
            case MotionEvent.ACTION_CANCEL :
                // Dragged finger outside
                m_mainActivity.printInGuiThread("Dragged finger outside!");
                return false;
        }
        return false;
    }
}
