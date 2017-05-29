package com.example.jonatan.wifitruck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Jonte on 2016-06-06.
 */
public class SocketCommunicationSingleton
{
    private static SocketCommunicationSingleton m_theOnlyInstance = null;
    public static final String DEFAULT_HOST = "83.255.202.208";//"192.168.0.3"; //"46.236.67.12";//"Jontes-ASUS";// //"10.0.2.2"; //LOCAL HOST ON PC FROM EMULATOR
    private static int PORT = 2002;
    private static int m_msgNumber = 0;
    private static boolean m_bConnectedToServer = false;
    private static boolean m_bLoggedIn = false;
    private static Socket m_socket;
    private static ServerWriter m_serverWriter;
    private static ServerListener m_serverListener;
    private static MainActivity m_myMainActivity;
    private static Thread m_connectToServerThread;

    // C-tor
    private SocketCommunicationSingleton()
    {
    }

    public static SocketCommunicationSingleton getInstance()
    {
        if (m_theOnlyInstance == null)
        {
            m_theOnlyInstance = new SocketCommunicationSingleton();
        }
        return m_theOnlyInstance;
    }

    public static void setClientActivityInstance(MainActivity myMainActivity)
    {
        if (m_myMainActivity == null)
        {
            m_myMainActivity = myMainActivity;
        }
    }

    public static void setConnected(boolean isConnected)
    {
        m_bConnectedToServer = isConnected;
    }

    public static boolean isConnectedToServer()
    {
        return m_bConnectedToServer;
    }

    public static void setLoggedIn(boolean isLoggedIn)
    {
        m_bLoggedIn = isLoggedIn;
    }

    public static boolean isLoggedIn()
    {
        return m_bLoggedIn;
    }

    public static void initialize(final String serverAddressFromUser, final String serverPortFromUser)
    {
        //Initialize socket, writer and listener
        m_connectToServerThread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    String serverAddress = (serverAddressFromUser.compareTo("") == 0) ? DEFAULT_HOST : serverAddressFromUser;
                    int port = serverPortFromUser.compareTo("") != 0 ? Integer.parseInt(serverPortFromUser) : PORT;
                    m_socket = new Socket(InetAddress.getByName(serverAddress), port);
                    m_serverWriter = new ServerWriter(new PrintWriter(new BufferedWriter(new OutputStreamWriter(m_socket.getOutputStream())), true), m_myMainActivity);
                    m_serverListener = new ServerListener(new BufferedReader(new InputStreamReader(m_socket.getInputStream())), m_myMainActivity);
                    m_myMainActivity.enableCommunication();
                    m_bConnectedToServer = true;

                } catch (UnknownHostException e)
                {
                    e.printStackTrace();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        });
        m_connectToServerThread.start();
        if (m_bConnectedToServer)
        {
            m_connectToServerThread.interrupt();
        }
    }

    public void stopCommSingletonConnectThread()
    {
        m_connectToServerThread.interrupt();
    }

    public ServerWriter getWriter()
    {
        return m_serverWriter;
    }

    public ServerListener getListener()
    {
        return m_serverListener;
    }

    public static int getMsgNumber()
    {
        int msgNumber = m_msgNumber;
        m_msgNumber++;
        return msgNumber;
    }

    public static void disconnect()
    {
        if ( m_bConnectedToServer )
        {
            try
            {
                setConnected(false);
                setLoggedIn(false);
                m_socket.close();
            } catch (IOException e)
            {
                e.printStackTrace();
                m_myMainActivity.printInGuiThread("Failed to close socket!");
            }
        }
    }
}
