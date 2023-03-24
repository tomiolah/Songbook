package com.bence.songbook.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

class Sender {

    public static final String START_PROJECTION_DTO = "start 'projectionDTO'";
    public static final String END_PROJECTION_DTO = "end 'projectionDTO'";
    private final String TAG = Sender.class.getName();
    private final Thread writer;
    private final DataOutputStream outToClient;
    private final Socket connectionSocket;
    private final Thread reader;
    private final BufferedReader inFromClient;

    Sender(Socket connectionSocket, final List<ProjectionTextChangeListener> projectionTextChangeListeners) throws IOException {
        this.connectionSocket = connectionSocket;
        outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        writer = new Thread(new Runnable() {
            @Override
            public void run() {

                ProjectionTextChangeListener projectionTextChangeListener = new ProjectionTextChangeListener() {
                    @Override
                    public void onSetText(String text) {
                        try {
                            String s = "start 'text'\n"
                                    + text + "\n"
                                    + "end 'text'\n"
                                    + "start 'projectionType'\n"
                                    + "SONG" + "\n"
                                    + "end 'projectionType'\n";
                            //noinspection CharsetObjectCanBeUsed
                            outToClient.write(s.getBytes("UTF-8"));
                        } catch (SocketException e) {
                            String message = e.getMessage();
                            if (message != null) {
                                if (message.equals("Socket closed")) {
                                    projectionTextChangeListeners.remove(this);
                                    close();
                                    return;
                                } else if (!message.equals("Connection reset by peer: socket write error") &&
                                        !message.equals("Software caused connection abort: socket write error")) {
                                    Log.e(TAG, message, e);
                                }
                            }
                            projectionTextChangeListeners.remove(this);
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                            projectionTextChangeListeners.remove(this);
                            close();
                        }
                    }
                };
                projectionTextChangeListeners.add(projectionTextChangeListener);
            }
        });
        writer.start();
        reader = new Thread(() -> {
            try {
                inFromClient.readLine();
//                while (!s.equals("Finished")) {
//                    s = inFromClient.readLine();
//                }
                close();
            } catch (SocketException e) {
//                    if (e.getMessage().equals("Socket closed")) {
//                    }
            } catch (Exception e) {
                close();
            }
        });
        reader.start();
    }

    private void close() {
        closeConnections();
        writer.interrupt();
        reader.interrupt();
    }

    private void closeConnections() {
        try {
            if (connectionSocket != null) {
                connectionSocket.close();
            }
            if (outToClient != null) {
                outToClient.close();
            }
            if (inFromClient != null) {
                inFromClient.close();
            }
        } catch (IOException ignored) {
        }
    }

    void stop() {
        reader.interrupt();
        try {
            if (outToClient != null && connectionSocket != null) {
                outToClient.writeBytes("Finished\n");
            }
        } catch (SocketException e) {
            if ("Socket closed".equals(e.getMessage())) {
                return;
            }
            Log.e(TAG, e.getMessage(), e);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        closeConnections();
        writer.interrupt();
        reader.interrupt();
        Thread.currentThread().interrupt();
    }
}
