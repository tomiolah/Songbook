package projector.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.controller.ProjectionScreenController;
import projector.controller.ProjectionTextChangeListener;
import projector.controller.song.SongController;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Sender {

    private static final Logger LOG = LoggerFactory.getLogger(Sender.class);
    private final Thread writer;
    private final DataOutputStream outToClient;
    private final Socket connectionSocket;
    private final Thread reader;
    private final BufferedReader inFromClient;

    Sender(Socket connectionSocket, ProjectionScreenController projectionScreenController, SongController songController) throws IOException {
        this.connectionSocket = connectionSocket;
        outToClient = new DataOutputStream(connectionSocket.getOutputStream());
        inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        writer = new Thread(() -> {
            ProjectionTextChangeListener projectionTextChangeListener = new ProjectionTextChangeListener() {
                @Override
                public void onSetText(String text, ProjectionType projectionType) {
                    try {
                        String s = "start 'text'\n"
                                + text + "\n"
                                + "end 'text'\n"
                                + "start 'projectionType'\n"
                                + projectionType.name() + "\n"
                                + "end 'projectionType'\n";
                        outToClient.write(s.getBytes(StandardCharsets.UTF_8));
                    } catch (SocketException e) {
                        String message = e.getMessage();
                        if (message.equals("Socket closed")) {
                            projectionScreenController.removeProjectionTextChangeListener(this);
                            close();
                            return;
                        } else if (!message.equals("Connection reset by peer: socket write error") &&
                                !message.equals("Software caused connection abort: socket write error") &&
                                !message.equals("Connection reset by peer")
                        ) {
                            LOG.error(message, e);
                        }
                        projectionScreenController.removeProjectionTextChangeListener(this);
                    } catch (Exception e) {
                        LOG.error(e.getMessage(), e);
                        projectionScreenController.removeProjectionTextChangeListener(this);
                        close();
                    }
                }
            };
            projectionScreenController.addProjectionTextChangeListener(projectionTextChangeListener);
            songController.addProjectionTextChangeListener(projectionTextChangeListener);
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
                if (e.getMessage().equals("Socket closed")) {
                    return;
                }
                LOG.error(e.getMessage(), e);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                close();
            }
        });
        reader.start();
    }

    public void close() {
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
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @SuppressWarnings("deprecation")
    public void stop() {
        reader.interrupt();
        try {
            if (outToClient != null && connectionSocket != null) {
                outToClient.writeBytes("Finished\n");
            }
        } catch (SocketException e) {
            if (e.getMessage().equals("Socket closed")) {
                return;
            }
            LOG.error(e.getMessage(), e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        closeConnections();
        writer.stop();
        reader.stop();
        Thread.currentThread().interrupt();
    }
}
