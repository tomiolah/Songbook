package com.bence.psbremote;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.bence.psbremote.ui.activity.AbstractFullscreenActivity;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static com.bence.psbremote.TCPClient.PORT;

public class MainActivity extends AbstractFullscreenActivity {

    private SongSenderRemoteListener songSenderRemoteListener;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final List<String> openIps = new ArrayList<>();
        findShared(openIps);
        final ListView listView = findViewById(R.id.listView);
        if (openIps.size() > 0) {
            String connectToShared = openIps.get(0);
            TCPClient.connectToShared(this, connectToShared, new ProjectionTextChangeListener() {
                @Override
                public void onSetText(final String text) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println(text);
                                setText(text);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, new SongRemoteListener() {
                @Override
                public void onSongListViewChanged(final List<String> list) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this,
                                    android.R.layout.simple_list_item_1, android.R.id.text1, list);
                            listView.setAdapter(adapter);
                        }
                    });
                }
            });
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                songSenderRemoteListener.onSongListViewItemClick(position);
            }
        });
    }

    private void findShared(final List<String> openIps) {
        try {
            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
            List<String> ips = new ArrayList<>();
            while (enumeration.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) enumeration.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String hostAddress = i.getHostAddress();
                    if (hostAddress.matches("192.168.[12]?[0-9]{1,2}.[12]?[0-9]{1,2}")) {
                        ips.add(hostAddress);
                    }
                }
            }
            List<Thread> threads = new ArrayList<>(ips.size() * 255);
            for (String ip : ips) {
                String[] split = ip.split("\\.");
                String firstThree = split[0] + "." + split[1] + "." + split[2] + ".";
                for (int i = 1; i <= 255; ++i) {
                    final String ip1 = firstThree + i;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (isOpenAddress(ip1)) {
                                    openIps.add(ip1);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    thread.start();
                    threads.add(thread);
                }
            }
            for (Thread thread : threads) {
                thread.join(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSongSenderRemoteListener(SongSenderRemoteListener songSenderRemoteListener) {
        this.songSenderRemoteListener = songSenderRemoteListener;
    }
}
