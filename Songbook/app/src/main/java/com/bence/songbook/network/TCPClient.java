package com.bence.songbook.network;

import android.util.Log;

import com.bence.songbook.ui.activity.ConnectToSharedFullscreenActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class TCPClient {

    public static final String TAG = TCPClient.class.getSimpleName();
    public static final int PORT = 21041;
    private static Thread thread;
    private static Thread reader;
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static BufferedReader inFromServer;

    public synchronized static void connectToShared(final ConnectToSharedFullscreenActivity connectToSharedFullscreenActivity, final String openIp, final ProjectionTextChangeListener projectionTextChangeListener) {
        if (thread != null) {
            close();
        }
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (openIp != null) {
                        clientSocket = new Socket(openIp, PORT);
                        inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
                        outToServer = new DataOutputStream(clientSocket.getOutputStream());
                        reader = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String fromServer;
                                while (true) {
                                    try {
                                        fromServer = inFromServer.readLine();
                                        if (fromServer == null) {
                                            close();
                                            return;
                                        }
                                        if (fromServer.equals("Finished")) {
                                            outToServer.close();
                                            outToServer = null;
                                            close();
                                            connectToSharedFullscreenActivity.finish();
                                            return;
                                        }
                                        if (fromServer.equals("start 'text'")) {
                                            StringBuilder text = new StringBuilder(inFromServer.readLine());
                                            fromServer = inFromServer.readLine();
                                            while (!fromServer.equals("end 'text'")) {
                                                text.append("\n").append(fromServer);
                                                fromServer = inFromServer.readLine();
                                            }
                                            fromServer = inFromServer.readLine();
                                            if (fromServer.equals("start 'projectionType'")) {
                                                inFromServer.readLine();
                                                fromServer = inFromServer.readLine();
                                                if (fromServer.equals("end 'projectionType'")) {
                                                    projectionTextChangeListener.onSetText(text.toString());
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, e.getMessage(), e);
                                        break;
                                    }
                                }
                            }
                        });
                        reader.start();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        });
        thread.start();
    }

    public synchronized static void close() {
        try {
            if (outToServer != null) {
                outToServer.writeBytes("Finished\n");
                outToServer.close();
            }
            if (inFromServer != null) {
                inFromServer.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        if (reader != null) {
            reader.interrupt();
        }
        thread.interrupt();
    }
}
