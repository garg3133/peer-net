package com.example.chatfull;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.material.snackbar.Snackbar;
import com.notbytes.barcode_reader.BarcodeReaderActivity;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class FindPeers extends AppCompatActivity{

    public static final String TAG = "FindPeers";
    private static final int BARCODE_READER_ACTIVITY_REQUEST = 1208;

    private EditText ipInput, portInput;
    private Button connectBtn, scanBtn;
    private Client myClient;
    private User user;
//    FrameLayout progressOverlay;

    // NSD
    private static final String SERVICE_TYPE = "_peernet._tcp.";
    private static int SERVER_PORT;
    private static final int AVAILABLE_SERVICES_UPDATED = 2;
    String localServiceName;
    int serverSocketPort;

    ListView peersList;

    NsdManager nsdManager;
    NsdManager.DiscoveryListener discoveryListener;
    NsdManager.ResolveListener resolveListener;

    Map<String, NsdServiceInfo> nsdServicesMap= new HashMap<String, NsdServiceInfo>();
    String[] nsdServiceNameArray;
    NsdServiceInfo[] nsdServiceArray;

    public void setUser(User user) {
        this.user = user;
        Intent data = new Intent();
        data.putExtra("user",user);
        setResult(RESULT_OK,data);
//        progressOverlay.setVisibility(View.INVISIBLE);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_peers);

//        ipInput = findViewById(R.id.ipInput);
//        portInput = findViewById(R.id.portInput);
//        connectBtn = findViewById(R.id.connectBtn);
//        scanBtn = findViewById(R.id.scan_button);
//        progressOverlay = findViewById(R.id.progress_overlay);

        localServiceName = getIntent().getStringExtra("localServiceName");

        peersList = (ListView) findViewById((R.id.peersList));
        setupPeerListItemsClickListener();

//        initializeRegistrationListener();
        initializeDiscoveryListener();
        initializeResolveListener();

        nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();
//        progressOverlay.setVisibility(View.INVISIBLE);
        if (myClient != null && !myClient.isCancelled())
            myClient.cancel(true);

        nsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (myClient != null && !myClient.isCancelled())
            myClient.cancel(true);

        nsdManager.stopServiceDiscovery(discoveryListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myClient != null && !myClient.isCancelled())
            myClient.cancel(true);
    }

//    public void connectBtnListener(View view) {
//        if(portInput.getText().length() < 2 || ipInput.getText().length() < 2){
//            Snackbar snackbar = Snackbar
//                    .make(ipInput, "Please Enter Valid IP Address and/or Port number.", Snackbar.LENGTH_LONG);
//            snackbar.show();
//            return;
//        }
//
//        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(portInput.getWindowToken(), 0);
//        progressOverlay.setVisibility(View.VISIBLE);
//        myClient = new Client(ipInput.getText().toString(), Integer.parseInt(portInput.getText().toString()), this);
//        myClient.execute();
//    }

//    public void onScanBtnClick(View view) {
//        FragmentManager supportFragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
//        Fragment fragmentById = supportFragmentManager.findFragmentById(R.id.fm_container);
//        if (fragmentById != null) {
//            fragmentTransaction.remove(fragmentById);
//        }
//        fragmentTransaction.commitAllowingStateLoss();
//        Intent launchIntent = BarcodeReaderActivity.getLaunchIntent(this, true, false);
//        startActivityForResult(launchIntent, BARCODE_READER_ACTIVITY_REQUEST);
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (resultCode != Activity.RESULT_OK) {
//            Toast.makeText(this, "error in  scanning", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (requestCode == BARCODE_READER_ACTIVITY_REQUEST && data != null) {
//            Barcode barcode = data.getParcelableExtra(BarcodeReaderActivity.KEY_CAPTURED_BARCODE);
//            String client_ip = barcode.rawValue.substring(0, barcode.rawValue.indexOf(':'));
//            String client_port = barcode.rawValue.substring(barcode.rawValue.indexOf(':') + 1);
//
//            ipInput.setText(client_ip);
//            portInput.setText(client_port);
//        }
//
//    }

    public void setupPeerListItemsClickListener() {
        peersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final NsdServiceInfo nsdService = nsdServiceArray[position];

                nsdManager.resolveService(nsdService, resolveListener);

//                progressOverlay.setVisibility(View.VISIBLE);

            }
        });
    }

//    public void initializeRegistrationListener() {
//        registrationListener = new NsdManager.RegistrationListener() {
//
//            @Override
//            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
//                // Save the service name. Android may have changed it in order to
//                // resolve a conflict, so update the name you initially requested
//                // with the name Android actually used.
//                localServiceName = NsdServiceInfo.getServiceName();
//                Log.d(TAG, "Registered Service Name: " + localServiceName);
//                Toast.makeText(FindPeers.this, "Service registered.", Toast.LENGTH_SHORT).show();
////				handler.obtainMessage(SERVICE_REGISTERED).sendToTarget();
//            }
//
//            @Override
//            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
//                // Registration failed! Put debugging code here to determine why.
//                Log.e(TAG, "Registration failed: " + errorCode);
//                Toast.makeText(FindPeers.this, "Service registration failed.", Toast.LENGTH_SHORT).show();
////				toggleVisibility.setOnCheckedChangeListener(null);
//                toggleVisibility.setChecked(false);
////				toggleVisibility.setOnCheckedChangeListener(this);
//            }
//
//            @Override
//            public void onServiceUnregistered(NsdServiceInfo arg0) {
//                // Service has been unregistered. This only happens when you call
//                // NsdManager.unregisterService() and pass in this listener.
//                Log.e(TAG, "Service unregistered.");
//                Toast.makeText(FindPeers.this, "Service unregistered.", Toast.LENGTH_SHORT).show();
////				handler.obtainMessage(SERVICE_UNREGISTERED).sendToTarget();
//            }
//
//            @Override
//            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
//                // Unregistration failed. Put debugging code here to determine why.
//                Log.e(TAG, "Un-registration failed: " + errorCode);
//                Toast.makeText(FindPeers.this, "Service un-registration failed.", Toast.LENGTH_SHORT).show();
//                toggleVisibility.setChecked(true);
//            }
//        };
//    }

    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
                Toast.makeText(FindPeers.this, "Discovery started", Toast.LENGTH_SHORT).show();
//				handler.obtainMessage(DISCOVERY_STARTED).sendToTarget();
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
//				handler.obtainMessage(DISCOVERY_STOPPED).sendToTarget();

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

//                ClientInit client = new ClientInit(host, port);
//                client.start();

                myClient = new Client(host.getHostAddress(), port, FindPeers.this);
                myClient.execute();

                //Open the ChatActivity
//                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
//                startActivity(intent);

//				clientClass = new ClientClass(host, port);
//				clientClass.run();
            }
        };
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case AVAILABLE_SERVICES_UPDATED:
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, nsdServiceNameArray);
                    peersList.setAdapter(adapter);
                    break;
            }
            return true;
        }
    });
}
