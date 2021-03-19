package com.iiitdmj.peer_net;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.InetAddresses;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // Fix this '.' (https://stackoverflow.com/questions/53510192/android-nsd-why-service-type-dont-match)
    private static final String SERVICE_TYPE = "_peernet._tcp.";
    private static final int AVAILABLE_SERVICES_UPDATED = 2;
    private static final int SERVICE_REGISTERED = 3;
    private static final int SERVICE_UNREGISTERED = 4;
    private static final int DISCOVERY_STARTED = 5;
    private static final int DISCOVERY_STOPPED = 6;
    private static final int CONNECTED_AS_SERVER = 7;
    private static final int CONNECTED_AS_CLIENT = 8;
    private int REQUEST_FINE_LOCATION = 1;
    Button btnOnOff, btnDiscover, btnSend;
    ListView listView;
    TextView read_msg_box, connectionStatus;
    EditText writeMsg;

    static final int MESSAGE_READ = 1;

    ServerClass serverClass;
    ClientClass clientClass;
    SendReceive sendReceive;

    // New variables
    private boolean IS_DEVICE_NSD_REGISTERED;
    private boolean IS_DEVICE_NSD_DISCOVERY_ON;

    ServerSocket serverSocket;
    int serverSocketPort;
    String localServiceName;
    NsdManager nsdManager;
    NsdManager.DiscoveryListener discoveryListener;
    NsdManager.RegistrationListener registrationListener;
    NsdManager.ResolveListener resolveListener;

    Map<String, NsdServiceInfo> nsdServicesMap= new HashMap<String, NsdServiceInfo>();
    String[] nsdServiceNameArray;
    NsdServiceInfo[] nsdServiceArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialWork();
        exqListener();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    read_msg_box.setText(tempMsg);
                    break;
                case AVAILABLE_SERVICES_UPDATED:
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, nsdServiceNameArray);
                    listView.setAdapter(adapter);
                    break;
                case SERVICE_REGISTERED:
                    btnOnOff.setText("Turn Off Visibility");
                    IS_DEVICE_NSD_REGISTERED =true;
                    break;
                case SERVICE_UNREGISTERED:
                    btnOnOff.setText("Turn On Visibility");
                    IS_DEVICE_NSD_REGISTERED = false;
                    break;
                case DISCOVERY_STARTED:
                    btnDiscover.setText("Stop Discovery");
                    connectionStatus.setText("Discovery started.");
                    IS_DEVICE_NSD_DISCOVERY_ON = true;
                    break;
                case DISCOVERY_STOPPED:
                    btnDiscover.setText("Start Discovery");
                    connectionStatus.setText("Discovery stopped.");
                    IS_DEVICE_NSD_DISCOVERY_ON = false;
                    break;
                case CONNECTED_AS_SERVER:
                    connectionStatus.setText("Connected as server.");
                    break;
                case CONNECTED_AS_CLIENT:
                    connectionStatus.setText("Connected as client.");
                    break;
            }
            return true;
        }
    });

    private void exqListener() {
        btnOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IS_DEVICE_NSD_REGISTERED) {
                    nsdManager.unregisterService(registrationListener);
                } else {
                    registerService(serverSocketPort);
                }
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (IS_DEVICE_NSD_DISCOVERY_ON) {
                    nsdManager.stopServiceDiscovery(discoveryListener);
                } else {
                    nsdManager.discoverServices(
                            SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final NsdServiceInfo nsdService = nsdServiceArray[position];

                nsdManager.resolveService(nsdService, resolveListener);

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = writeMsg.getText().toString();
                sendReceive.write(msg.getBytes());
                writeMsg.setText("");
            }
        });
    }

    public String getLocalBluetoothName(){
        // Replace with username later
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        String name = mBluetoothAdapter.getName();
        if(name == null){
            name = Build.MANUFACTURER + " " + Build.MODEL;
        }

        return name;
    }

    public void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(getLocalBluetoothName() + " peernet");
        serviceInfo.setServiceType("_peernet._tcp");
        serviceInfo.setPort(port);

        nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public void initializeServerSocket() {
        // Initialize a server socket on the next available port.
        try {
            serverSocket = new ServerSocket(0);
            serverClass = new ServerClass(serverSocket);
            serverClass.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Store the chosen port.
        serverSocketPort = serverSocket.getLocalPort();
    }

    public void initializeRegistrationListener() {
        registrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name. Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                localServiceName = NsdServiceInfo.getServiceName();
                Log.d(TAG, "Registered Service Name: " + localServiceName);
                handler.obtainMessage(SERVICE_REGISTERED).sendToTarget();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
                Log.e(TAG, "Registration failed: " + errorCode);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.e(TAG, "Service unregistered.");
                handler.obtainMessage(SERVICE_UNREGISTERED).sendToTarget();
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
                Log.e(TAG, "Unregistration failed: " + errorCode);
            }
        };
    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
                handler.obtainMessage(DISCOVERY_STARTED).sendToTarget();
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType() + " Allowed: " + SERVICE_TYPE);
                } else if (service.getServiceName().equals(localServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + localServiceName);
                } else if (service.getServiceName().contains("peernet")){
                    // Add the service to a HashMap
//                    nsdServices.add(service);
                    nsdServicesMap.put(service.getServiceName(), service);

                    nsdServiceNameArray = new String[nsdServicesMap.size()];
                    nsdServiceArray = new NsdServiceInfo[nsdServicesMap.size()];
                    int index = 0;

                    for(Map.Entry<String, NsdServiceInfo> nsdServicesMapEntry : nsdServicesMap.entrySet()) {
                        Log.d(TAG, "Same machine: " + nsdServicesMapEntry.getKey() + " " + nsdServicesMapEntry.getValue());
                        String nsdServiceName = nsdServicesMapEntry.getKey();
                        nsdServiceNameArray[index] = nsdServiceName.substring(0, nsdServiceName.length()-8);
                        nsdServiceArray[index] = nsdServicesMapEntry.getValue();
                        index++;
                    }

                    handler.obtainMessage(AVAILABLE_SERVICES_UPDATED, -1, -1, nsdServiceNameArray).sendToTarget();
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
                if (service.getServiceType().equals(SERVICE_TYPE) && service.getServiceName().contains("peernet") && !service.getServiceName().equals(localServiceName)){
                    // Add the service to a HashMap
                    nsdServicesMap.remove(service.getServiceName());

                    nsdServiceNameArray = new String[nsdServicesMap.size()];
                    nsdServiceArray = new NsdServiceInfo[nsdServicesMap.size()];
                    int index = 0;

                    for(Map.Entry<String, NsdServiceInfo> nsdServicesMapEntry : nsdServicesMap.entrySet()) {
                        Log.d(TAG, "Same machine: " + nsdServicesMapEntry.getKey() + " " + nsdServicesMapEntry.getValue());
                        String nsdServiceName = nsdServicesMapEntry.getKey();
                        nsdServiceNameArray[index] = nsdServiceName.substring(0, nsdServiceName.length()-8);
                        nsdServiceArray[index] = nsdServicesMapEntry.getValue();
                        index++;
                    }

                    handler.obtainMessage(AVAILABLE_SERVICES_UPDATED, -1, -1, nsdServiceNameArray).sendToTarget();
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
                handler.obtainMessage(DISCOVERY_STOPPED).sendToTarget();

                nsdServicesMap.clear();
                nsdServiceNameArray = new String[nsdServicesMap.size()];
                nsdServiceArray = new NsdServiceInfo[nsdServicesMap.size()];

                handler.obtainMessage(AVAILABLE_SERVICES_UPDATED, -1, -1, nsdServiceNameArray).sendToTarget();
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        resolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                Log.e(TAG, "Resolve failed: " + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(localServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                NsdServiceInfo mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();

                Log.e(TAG, "Address: " + host.getHostAddress());
                Log.e(TAG, "Port: " + port);

                clientClass = new ClientClass(host, port);
                clientClass.run();
            }
        };
    }


    public void initialWork() {
        btnOnOff = (Button) findViewById(R.id.onOff);
        btnDiscover = (Button) findViewById(R.id.discover);
        btnSend = (Button) findViewById(R.id.sendButton);

        listView = (ListView) findViewById(R.id.peerListView);
        read_msg_box = (TextView) findViewById(R.id.readMsg);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        writeMsg = (EditText) findViewById(R.id.writeMsg);

        initializeServerSocket();
        initializeRegistrationListener();
        initializeDiscoveryListener();
        initializeResolveListener();

        IS_DEVICE_NSD_REGISTERED = false;
        IS_DEVICE_NSD_DISCOVERY_ON = false;

        // See below line. Original this --> Context
        nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);

    }

    public class ServerClass extends Thread {
        ServerSocket serverSocket;
        Socket socket;

        public ServerClass(ServerSocket skt) {
            serverSocket = skt;
        }

        @Override
        public void run() {
            try {
//                serverSocket = new ServerSocket(8081);
                socket = serverSocket.accept();
                handler.obtainMessage(CONNECTED_AS_SERVER).sendToTarget();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt) {
            socket = skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket!=null) {
                try {
                    bytes = inputStream.read(buffer);
                    if (bytes>0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            new Thread(() -> {
                try {
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    public class ClientClass extends Thread {
        Socket socket;
        String remoteAddress;
        int remotePort;

        public ClientClass (InetAddress remoteAddress, int remotePort) {
            this.remoteAddress = remoteAddress.getHostAddress();
            this.remotePort = remotePort;
            socket = new Socket();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(remoteAddress, remotePort), 500);
                handler.obtainMessage(CONNECTED_AS_CLIENT).sendToTarget();
                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}