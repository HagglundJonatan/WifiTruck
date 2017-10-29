package com.example.jonatan.wifitruck;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Jonte on 2015-11-26.
 */
public class ServerListener extends Thread
{
    private boolean m_quit = false;
    private Socket m_socket;
    private BufferedReader m_fromServer;
    private MainActivity m_myMainActivity;

    public ServerListener(BufferedReader fromServer, MainActivity myMainActivity)
    {
        m_fromServer = fromServer;
        m_myMainActivity = myMainActivity;
        start();
    }

    public void stopServerListener()
    {
        m_quit = true;
    }

    public void run()
    {
        while (!m_quit)
        {
            char msgFromServer[] = new char[25];
            int charsReceived = 0;
            try
            {
                charsReceived = m_fromServer.read(msgFromServer, 0 , 25); //eller 7!???!?!
                if (msgFromServer == null || charsReceived == 0)
                {
                    //m_parentActiv>ity.printInGuiThread("Null message received from server");
                    m_quit = true;
                }
                else
                {
                    String strFromServer = String.copyValueOf(msgFromServer);
                    m_myMainActivity.handleIncomingMsgFromServer(strFromServer);
                }
            } catch (IOException e)
            {
                //m_parentActivity.printInGuiThread("Error while receiving message");
                e.printStackTrace();
                m_myMainActivity.handleIncomingMsgFromServer("0 DISCONNECTED");
                m_quit = true;
            }
        }
    }
}