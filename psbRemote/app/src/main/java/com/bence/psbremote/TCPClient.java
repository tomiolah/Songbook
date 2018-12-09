package com.bence.psbremote;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TCPClient {

    static final int PORT = 21042;
    private static final String TAG = TCPClient.class.getSimpleName();
    private static Thread thread;
    private static Thread reader;
    private static Thread writer;
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static BufferedReader inFromServer;

    synchronized static void connectToShared(final MainActivity mainActivity, final String openIp, final ProjectionTextChangeListener projectionTextChangeListener, final SongRemoteListener songRemoteListener) {
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
                                            mainActivity.finish();
                                            return;
                                        }
                                        if (fromServer.equals("start 'text'")) {
                                            newText(projectionTextChangeListener);
                                        } else if (fromServer.equals("start onSongListViewChanged")) {
                                            onSongListViewChanged(songRemoteListener);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, e.getMessage(), e);
                                        break;
                                    }
                                }
                            }
                        });
                        reader.start();
                        writer = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                SongSenderRemoteListener songSenderRemoteListener = new SongSenderRemoteListener() {
                                    @Override
                                    public void onSongListViewItemClick(int position) {
                                        try {
                                            String s = "onSongListViewItemClick\n"
                                                    + position + "\n"
                                                    + "end\n";
                                            outToServer.write(s.getBytes(StandardCharsets.UTF_8));
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };
                                mainActivity.setSongSenderRemoteListener(songSenderRemoteListener);
                            }
                        });
                        writer.start();
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        });
        thread.start();
    }

    private static void newText(ProjectionTextChangeListener projectionTextChangeListener) throws IOException {
        String fromServer;
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

    private static void onSongListViewChanged(SongRemoteListener songRemoteListener) throws IOException {
        ArrayList<String> list = new ArrayList<>();
        int size = Integer.parseInt(inFromServer.readLine());
        for (int i = 0; i < size; ++i) {
            list.add(inFromServer.readLine().replaceAll("╤~'newLinew'~╤", "\n"));
        }
        String fromServer = inFromServer.readLine();
        if (fromServer.equals("end")) {
            songRemoteListener.onSongListViewChanged(list);
        }
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
