package projector.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.application.ProjectionType;
import projector.application.Settings;
import projector.controller.ProjectionScreenController;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class TCPClient {

    static final int PORT = 21041;
    private static final Logger LOG = LoggerFactory.getLogger(TCPClient.class);
    private static Thread thread;
    private static Thread reader;
    private static Socket clientSocket;
    private static DataOutputStream outToServer;
    private static BufferedReader inFromServer;
    private static String openIp;

    public synchronized static void connectToShared(ProjectionScreenController projectionScreenController) {
        if (thread != null) {
            close();
        }
        thread = new Thread(() -> {
            try {
                Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
                List<String> ips = new ArrayList<>();
                while (enumeration.hasMoreElements()) {
                    NetworkInterface n = enumeration.nextElement();
                    Enumeration<InetAddress> ee = n.getInetAddresses();
                    while (ee.hasMoreElements()) {
                        InetAddress i = ee.nextElement();
                        String hostAddress = i.getHostAddress();
                        if (hostAddress.matches("192.168.[12]?[0-9]{1,2}.[12]?[0-9]{1,2}")) {
                            ips.add(hostAddress);
                            System.out.println(hostAddress);
                        }
                    }
                }
                openIp = null;
                List<Thread> threads = new ArrayList<>(ips.size() * 255);
                for (String ip : ips) {
                    String[] split = ip.split("\\.");
                    String firstThree = split[0] + "." + split[1] + "." + split[2] + ".";
                    for (int i = 1; i <= 255; ++i) {
                        String ip1 = firstThree + i;
                        Thread thread = new Thread(() -> {
                            if (isOpenAddress(ip1)) {
                                System.out.println("ip = " + ip1);
                                openIp = ip1;
                            }
                        });
                        thread.start();
                        threads.add(thread);
                    }
                }
                for (Thread thread : threads) {
                    thread.join(5000);
                }
//                openIp = "192.168.43.175";
                System.out.println("openIp = " + openIp);
                if (openIp != null) {
                    clientSocket = new Socket(openIp, PORT);
                    inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
                    outToServer = new DataOutputStream(clientSocket.getOutputStream());

                    Settings settings = Settings.getInstance();
                    settings.setConnectedToShared(true);
                    reader = new Thread(() -> {
                        String fromServer;
                        while (settings.isConnectedToShared()) {
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
                                        String projectionTypeName = inFromServer.readLine();
                                        fromServer = inFromServer.readLine();
                                        if (fromServer.equals("end 'projectionType'")) {
                                            projectionScreenController.setText(text.toString(), ProjectionType.valueOf(projectionTypeName));
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                LOG.error(e.getMessage(), e);
                                break;
                            }
                        }
                    });
                    reader.start();
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
        thread.start();
    }

    private static boolean isOpenAddress(String ip) {
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, PORT), 2000);
            socket.close();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public synchronized static void close() {
        Settings.getInstance().setConnectedToShared(false);
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
            LOG.error(e.getMessage(), e);
        }
        thread.interrupt();
    }
}
