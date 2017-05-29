package com.example.jonatan.wifitruck;

import java.io.PrintWriter;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by Jonte on 2015-12-03.
 */
public class ServerWriter extends Thread
{
    private PrintWriter m_toServer;
    private MainActivity m_myMainActivity;
    private Boolean m_quit = false;
    private ArrayBlockingQueue<String> msgQueue;

    public ServerWriter(PrintWriter toServer, MainActivity myMainActivity)
    {
        m_toServer = toServer;
        m_myMainActivity = myMainActivity;
        msgQueue = new ArrayBlockingQueue<String>(100);
        start();
    }

    public void run()
    {
        String msg;
        while (!m_quit)
        {
            try
            {
                msg =  msgQueue.poll();
                if ( msg != null )
                {
                    m_toServer.println(msg);
                }
                this.sleep(1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void stopServerWriter()
    {
        m_quit = true;
    }

    public void sendMessage(String message)
    {
        this.interrupt();
        try
        {
            msgQueue.put(message);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}