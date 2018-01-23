package com.example.jonatan.wifitruck;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements JoyStickView.JoyStickListener
{
    public static final String MY_PREFS_NAME = "my_shared_preferences";
    private RelativeLayout m_joyStickLayout;
    private EditText m_serverAddressEditText;
    private EditText m_serverPortEditText;
    private Button m_connectButton;
    private TextView m_msgLogTextView;

    private ServerWriter m_serverWriter;
    private ServerListener m_serverListener;
    private SocketCommunicationSingleton m_commSingleton;
    private JoyStickView m_joyStickView;
    private float m_latestXCmd = 0.5f;
    private float m_latestYCmd = 0.5f;
    private long m_prevElapsedMSJoyStickCmd = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_joyStickLayout = (RelativeLayout) findViewById( R.id.joyStickLayout );
        m_serverAddressEditText = (EditText)findViewById(R.id.serverAddressEditText);
        m_serverPortEditText = (EditText)findViewById(R.id.serverPortEditText);
        m_connectButton = (Button)findViewById(R.id.connectButton);
        m_msgLogTextView = (TextView)findViewById(R.id.msgLogTextView);
        m_joyStickView = new JoyStickView( this );
        m_joyStickLayout.addView( m_joyStickView );

    }

    public void onConnectClick(View view)
    {
        // Initialize the communication singleton
        if ( !m_commSingleton.isConnectedToServer() )
        {
            m_commSingleton = SocketCommunicationSingleton.getInstance();
            m_commSingleton.setClientActivityInstance(this);
            m_commSingleton.initialize(m_serverAddressEditText.getText().toString(), m_serverPortEditText.getText().toString());
        }
        else
        {
            sendMsgToServer("CMD_DSCNNCT##00");
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            m_commSingleton.stopCommSingletonConnectThread();
            m_commSingleton.disconnect();
            m_connectButton.setText("Connect");
            m_connectButton.invalidate();
        }
    }

    private void sendMsgToServer(String str)
    {
        if (m_serverWriter != null)
        {
            m_serverWriter.sendMessage( str );
            printInGuiThread( str + " sent" );
        }
    }

    public void handleIncomingMsgFromServer(String lineFromServer)
    {
        printInGuiThread("From server: " + lineFromServer);
    }

    public void printInGuiThread(final String str)
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable()
        {
            public void run()
            {
                print(str);
            }
        });
    }

    public void print(String str)
    {
        String textViewText = "";
        String lines[] = m_msgLogTextView.getText().toString().split("\n");
        if ( lines.length >= 3 )
        {
            lines[0] = "";
        }
        for (int i = 0; i < lines.length; i++)
        {
            if ( lines[i].compareTo( "" ) != 0 )
            {
                if ( textViewText.compareTo("") == 0 )
                {
                    textViewText = lines[i];
                }
                else
                {
                    textViewText = textViewText + "\n" + lines[i];
                }
            }
        }
        m_msgLogTextView.setText(textViewText + "\n" + str);
        m_msgLogTextView.invalidate();
    }

    public void enableCommunication()
    {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable()
        {
            public void run()
            {
                m_serverWriter = m_commSingleton.getWriter();
                m_serverListener = m_commSingleton.getListener();
                m_connectButton.setText("Disconnect");
                m_connectButton.invalidate();
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("ipAddress", m_serverAddressEditText.getText().toString());
                editor.putString("port", m_serverPortEditText.getText().toString());
                editor.apply();
            }
        });
    }

    @Override
    public void onJoyStickMoved(float xPercent, float yPercent)
    {
        Log.d( "Main activity onJoyStickMoved", "X percent: " + xPercent + " - Y percent: " + yPercent );
        //printInGuiThread( "Main activity onJoyStickMoved X percent: " + xPercent + " - Y percent: " + yPercent );

        if (xPercent == 0.5f && yPercent == 0.5f)
        {
            sendMsgToServer( "CMD_REL##000000" );
        }
        else
        {
            m_latestXCmd = xPercent;
            m_latestYCmd = yPercent;
            long currentElapsedMSJoyStickCmd = System.currentTimeMillis();
            if ( currentElapsedMSJoyStickCmd - m_prevElapsedMSJoyStickCmd > 200 )
            {
                sendMsgToServer( "CMD_J#" + String.format( Locale.ROOT, "%.2f#%.2f", m_latestXCmd, m_latestYCmd ) );
                m_prevElapsedMSJoyStickCmd = currentElapsedMSJoyStickCmd;
            }
        }
    }
}
