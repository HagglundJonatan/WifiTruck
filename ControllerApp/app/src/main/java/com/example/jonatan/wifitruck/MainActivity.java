package com.example.jonatan.wifitruck;

import android.content.SharedPreferences;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity
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
    }

    public void onConnectClick(View view)
    {
        // Initialize the communication singleton
        m_commSingleton = SocketCommunicationSingleton.getInstance();
        m_commSingleton.setClientActivityInstance(this);
        m_commSingleton.initialize(m_serverAddressEditText.getText().toString(), m_serverPortEditText.getText().toString());
    }

    public void onUpClick(View view)
    {
        m_serverWriter.sendMessage("COMMAND UP");
        printInGuiThread("COMMAND UP sent");
    }

    public void onDownClick(View view)
    {
        m_serverWriter.sendMessage("COMMAND DOWN");
        printInGuiThread("COMMAND DOWN sent");
    }

    public void onLeftClick(View view)
    {
        m_serverWriter.sendMessage("COMMAND LEFT");
        printInGuiThread("COMMAND LEFT sent");
    }

    public void onRightClick(View view)
    {
        m_serverWriter.sendMessage("COMMAND RIGHT");
        printInGuiThread("COMMAND RIGHT sent");
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
        if ( lines.length >= 20 )
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
                m_connectButton.setEnabled(false);
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("ipAddress", m_serverAddressEditText.getText().toString());
                editor.putString("port", m_serverPortEditText.getText().toString());
                editor.apply();
            }
        });
    }
}
