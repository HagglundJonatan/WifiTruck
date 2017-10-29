package com.example.jonatan.wifitruck;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener
{
    public static final String MY_PREFS_NAME = "my_shared_preferences";

    private EditText m_serverAddressEditText;
    private EditText m_serverPortEditText;
    private Button m_connectButton;
    private ImageButton m_upArrowButton;
    private ImageButton m_downArrowButton;
    private ImageButton m_leftArrowButton;
    private ImageButton m_rightArrowButton;
    private TextView m_msgLogTextView;

    private ServerWriter m_serverWriter;
    private ServerListener m_serverListener;
    private SocketCommunicationSingleton m_commSingleton;
    private int m_upButtonId, m_downButtonId, m_leftButtonId, m_rightButtonId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_serverAddressEditText = (EditText)findViewById(R.id.serverAddressEditText);
        m_serverPortEditText = (EditText)findViewById(R.id.serverPortEditText);
        m_connectButton = (Button)findViewById(R.id.connectButton);
        m_msgLogTextView = (TextView)findViewById(R.id.msgLogTextView);
        m_upArrowButton = (ImageButton)findViewById(R.id.upArrowButton);
        m_downArrowButton = (ImageButton)findViewById(R.id.downArrowButton);
        m_leftArrowButton = (ImageButton)findViewById(R.id.leftArrowButton);
        m_rightArrowButton = (ImageButton)findViewById(R.id.rightArrowButton);
        m_upArrowButton.setOnTouchListener(this);
        m_downArrowButton.setOnTouchListener(this);
        m_leftArrowButton.setOnTouchListener(this);
        m_rightArrowButton.setOnTouchListener(this);
        m_upButtonId = m_upArrowButton.getId();
        m_downButtonId = m_downArrowButton.getId();
        m_leftButtonId = m_leftArrowButton.getId();
        m_rightButtonId = m_rightArrowButton.getId();

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
            sendMsgToServer("DSCNNCT");
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
            m_serverWriter.sendMessage(str);
            //printInGuiThread(str + " sent");
        }
    }
    public void onUpClick(View view)
    {
        sendMsgToServer("CMD_UPP");
    }

    public void onDownClick(View view)
    {
        sendMsgToServer("CMD_DWN");
        view.setBackgroundResource(R.drawable.downarrowgrey100);
    }

    public void onLeftClick(View view)
    {
        sendMsgToServer("CMD_LFT");
        view.setBackgroundResource(R.drawable.leftarrowgrey100);
    }

    public void onRightClick(View view)
    {
        sendMsgToServer("CMD_RGT");
        view.setBackgroundResource(R.drawable.rightarrowgrey100);
    }

    public void handleIncomingMsgFromServer(String lineFromServer)
    {
        printInGuiThread("From server: " + lineFromServer);
        String msgArray[] = lineFromServer.split(" ");
        switch (msgArray[1])
        {
            case "Connected":
                break;
            case "Command received - Forward":
                break;
            case "Command received - Backward":
                break;
            case "Command received - Turn left":
                break;
            case "Command received - Turn right":
                break;
        }
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
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN :
                // Pressed down
                handleButtonAction(true, v.getId());
                return true;
            case MotionEvent.ACTION_UP :
            case MotionEvent.ACTION_CANCEL :
                // Released
                handleButtonAction(false, v.getId());
                return true;
        }
        return false;
    }

    private void handleButtonAction(boolean bButtonPressed, int id)
    {
        if ( id == m_upButtonId )
        {
            if ( bButtonPressed )
            {
                m_upArrowButton.setImageResource( R.drawable.uparrowgrey100 );
                sendMsgToServer("CMD_UPP");
            }
            else
            {
                m_upArrowButton.setImageResource( R.drawable.uparrow100 );
                // send release cmd
                sendMsgToServer("CMD_RL2");
            }
        }
        else if ( id == m_downButtonId )
        {
            if ( bButtonPressed )
            {
                m_downArrowButton.setImageResource( R.drawable.downarrowgrey100 );
                sendMsgToServer("CMD_DWN");
            }
            else
            {
                m_downArrowButton.setImageResource( R.drawable.downarrow100 );
                // send release cmd
                sendMsgToServer("CMD_RL2");
            }}
        else if ( id == m_leftButtonId )
        {
            if ( bButtonPressed )
            {
                m_leftArrowButton.setImageResource( R.drawable.leftarrowgrey100 );
                sendMsgToServer("CMD_LFT");
            }
            else
            {
                m_leftArrowButton.setImageResource( R.drawable.leftarrow100 );
                // send release cmd
                sendMsgToServer("CMD_RL1");
            }}
        else if ( id == m_rightButtonId )
        {
            if ( bButtonPressed )
            {
                m_rightArrowButton.setImageResource( R.drawable.rightarrowgrey100 );
                sendMsgToServer("CMD_RGT");
            }
            else
            {
                m_rightArrowButton.setImageResource( R.drawable.rightarrow100 );
                // send release cmd
                sendMsgToServer("CMD_RL1");
            }
        }
    }
}
