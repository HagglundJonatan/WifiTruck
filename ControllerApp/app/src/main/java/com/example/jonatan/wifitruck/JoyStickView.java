package com.example.jonatan.wifitruck;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Jonatan on 2018-01-06.
 */

public class JoyStickView extends SurfaceView implements  SurfaceHolder.Callback, View.OnTouchListener
{
    private int m_topRadius;
    private int m_baseRadius;
    private int m_centerX;
    private int m_centerY;
    private JoyStickListener joyStickCallback;
    private float m_newX, m_newY, m_oldX, m_oldY = 0.0f;

    private void setupDimensions()
    {
        m_topRadius = Math.min(getWidth(), getHeight() / 5);
        m_baseRadius = Math.min(getWidth(), getHeight() / 3);
        m_centerX = getWidth() / 2;
        m_centerY = getHeight() / 2;
    }
    public JoyStickView(Context context)
    {
        super( context );
        getHolder().addCallback( this );
        setOnTouchListener( this );
        if ( context instanceof JoyStickListener)
        {
            joyStickCallback = (JoyStickListener) context;
        }
    }

    public JoyStickView(Context context, AttributeSet attrs)
    {
        super( context, attrs );
        getHolder().addCallback( this );
        setOnTouchListener( this );
    }

    public JoyStickView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super( context, attrs, defStyleAttr );
        getHolder().addCallback( this );
        setOnTouchListener( this );
    }

    public JoyStickView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super( context, attrs, defStyleAttr, defStyleRes );
        getHolder().addCallback( this );
        setOnTouchListener( this );
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        setupDimensions();
        drawJoyStick(m_centerX, m_centerY);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {

    }

    private void drawJoyStick(float newX, float newY)
    {
        if ( getHolder().getSurface().isValid())
        {
            Canvas joyStickCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            joyStickCanvas.drawColor( Color.TRANSPARENT, PorterDuff.Mode.CLEAR );
            colors.setARGB( 255, 50, 50, 50 ); // background rec
            joyStickCanvas.drawRect( m_centerX - (getWidth()/2), m_centerY - (getHeight()/2), m_centerX + (getWidth()/2), m_centerY + (getHeight()/2), colors );
            colors.setARGB( 255, 0, 76, 155 ); // joystick base
            joyStickCanvas.drawCircle( m_centerX, m_centerY, m_baseRadius, colors );
            colors.setARGB( 255, 0, 0, 255 ); // joystick color
            joyStickCanvas.drawCircle( newX, newY, m_topRadius, colors );
            colors.setARGB( 255, 255, 255, 255 );
            colors.setTextSize( 40 );

            joyStickCanvas.drawText( "X:" + newX, 10, 40, colors );
            joyStickCanvas.drawText( "Y:" + newY, 10, 80, colors );
            getHolder().unlockCanvasAndPost( joyStickCanvas ); // write new drawing to the SurfaceView
        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if ( v.equals( this ) )
        {
            if ( event.getAction() != event.ACTION_UP )
            {
                m_newX = event.getX();
                m_newY = event.getY();
                m_oldX = m_newX;
                m_oldY = m_newY;
                // Calc the hypotenusa and if its' lower than the base radius all is fine
                float displacement = (float) Math.sqrt( Math.pow( m_newX - m_centerX, 2 ) + Math.pow( m_newY - m_centerY, 2 ) );
                if (displacement < m_baseRadius)
                {
                    drawJoyStick( m_newX, m_newY );
                    joyStickCallback.onJoyStickMoved( (1 - ((m_newX - m_centerX) / m_baseRadius + 1) / 2), 1- (((m_newY - m_centerY) / m_baseRadius + 1) / 2 ));
                }
                else
                {
                    // Calc x and y contraints
                    float ratio = m_baseRadius / displacement;
                    float constrainedX = m_centerX + (m_newX - m_centerX) * ratio;
                    float constrainedY = m_centerY + (m_newY - m_centerY) * ratio;
                    drawJoyStick( constrainedX, constrainedY );
                    joyStickCallback.onJoyStickMoved( 1 - (((constrainedX - m_centerX) / m_baseRadius + 1) / 2), 1 - ( ((constrainedY - m_centerY) / m_baseRadius + 1) / 2 ));
                }
            }
            else
            {
                drawJoyStick( m_centerX, m_centerY );
                joyStickCallback.onJoyStickMoved( 0.5f, 0.5f);
            }
        }
        return true;
    }

    public interface JoyStickListener
    {
        void onJoyStickMoved(float xPercent, float yPercent);
    }
}
