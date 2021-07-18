package com.android.chatty;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.chatty.InitThreads.ClientInit;
import com.android.chatty.InitThreads.ServerInit;
import com.android.chatty.util.ActivityUtilities;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/*
 * This activity is the launcher activity. 
 * Once the connection established, the ChatActivity is launched.
 */
public class MainActivity extends Activity{
	public static final String TAG = "MainActivity";	
	public static final String DEFAULT_CHAT_NAME = "";
//	private WifiP2pManager mManager;
//	private Channel mChannel;
//	private WifiDirectBroadcastReceiver mReceiver;
//	private IntentFilter mIntentFilter;
	private Button setUsernameButton;
	private ImageView goToSettings;
	private TextView goToSettingsText;
	private TextView setChatNameLabel;
	private EditText setChatName;
	private ImageView disconnect;
	public static String chatName;
	public static ServerInit server;

	// NSD
	private static final String SERVICE_TYPE = "_peernet._tcp.";
	private static int SERVER_PORT;
	private static final int AVAILABLE_SERVICES_UPDATED = 2;
	String localServiceName;
	int serverSocketPort;

	ListView peersList;
	Switch toggleVisibility;

	NsdManager nsdManager;
	NsdManager.DiscoveryListener discoveryListener;
	NsdManager.RegistrationListener registrationListener;
	NsdManager.ResolveListener resolveListener;

	Map<String, NsdServiceInfo> nsdServicesMap= new HashMap<String, NsdServiceInfo>();
	String[] nsdServiceNameArray;
	NsdServiceInfo[] nsdServiceArray;

	//Getters and Setters
//    public WifiP2pManager getmManager() { return mManager; }
//	public Channel getmChannel() { return mChannel; }
//	public WifiDirectBroadcastReceiver getmReceiver() { return mReceiver; }
//	public IntentFilter getmIntentFilter() { return mIntentFilter; }
	public Button getSetUsernameButton(){ return setUsernameButton; }
	public TextView getSetChatNameLabel() { return setChatNameLabel; }
	public ImageView getGoToSettings() { return goToSettings; }
	public EditText getSetChatName() { return setChatName; }
	public TextView getGoToSettingsText() { return goToSettingsText; }
	public ImageView getDisconnect() { return disconnect; }
	public ListView getPeersList() { return  peersList; }
	public Switch getToggleVisibility() { return toggleVisibility; }
	
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); 
        
        //Init the Channel, Intent filter and Broadcast receiver
        init();

        //Button Go to Settings
        goToSettings = findViewById(R.id.goToSettings);
//        goToSettings();
        
        //Go to Settings text
        goToSettingsText = findViewById(R.id.textGoToSettings);

        peersList = (ListView) findViewById((R.id.peersList));
        setupPeerListItemsClickListener();

        toggleVisibility = (Switch) findViewById(R.id.toggleVisibility);
		setupToggleVisibilityListener();

        //Button Go to Chat
        setUsernameButton = findViewById(R.id.setUsernameButton);
        setupSetUsernameButtonClickListener();
        
        //Set the chat name
        setChatName = findViewById(R.id.setChatName);
        setChatNameLabel = findViewById(R.id.setChatNameLabel);
        setChatName.setText(loadChatName(this));

		//	Set username hint
		String userNameHint = "Default: " + getLocalBluetoothName();
		setChatName.setHint(userNameHint);
        
        //Button Disconnect
        disconnect = findViewById(R.id.disconnect);
//        disconnect();
    }	

    @Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		
		ActivityUtilities.customiseActionBar(this);
	}
    
	@Override
    public void onResume() {
        super.onResume();
//        registerReceiver(mReceiver, mIntentFilter);
//
//		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
//
//			@Override
//			public void onSuccess() {
//				Log.v(TAG, "Discovery process succeeded");
//			}
//
//			@Override
//			public void onFailure(int reason) {
//				Log.v(TAG, "Discovery process failed");
//			}
//		});

		nsdManager.discoverServices(
				SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener);
    }

    @Override
    public void onPause() {
        super.onPause();
//        unregisterReceiver(mReceiver);

		nsdManager.stopServiceDiscovery(discoveryListener);
    }

    @Override
	public void onDestroy() {
		super.onDestroy();

		nsdManager.unregisterService(registrationListener);
		Log.d(TAG, "Service unregistered");
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        int idItem = item.getItemId();
        return super.onOptionsItemSelected(item);
    }	
    
    public void init(){
//    	mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
//        mChannel = mManager.initialize(this, getMainLooper(), null);
//        mReceiver = WifiDirectBroadcastReceiver.createInstance();
//        mReceiver.setmManager(mManager);
//        mReceiver.setmChannel(mChannel);
//        mReceiver.setmActivity(this);
//
//        mIntentFilter = new IntentFilter();
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
//        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        // NSD
		initializeRegistrationListener();
		initializeDiscoveryListener();
		initializeResolveListener();

		nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);
    }
    
    public void setupSetUsernameButtonClickListener(){
    	setUsernameButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(!setChatName.getText().toString().equals("")){
					//Set the chat name
					saveChatName(MainActivity.this, setChatName.getText().toString());

					//Start the init process
//					if(mReceiver.isGroupeOwner() ==  WifiDirectBroadcastReceiver.IS_OWNER){
//						Toast.makeText(MainActivity.this, "I'm the group owner  " + mReceiver.getOwnerAddr().getHostAddress(), Toast.LENGTH_SHORT).show();
//						server = new ServerInit();
//						server.start();
//					}
//					else if(mReceiver.isGroupeOwner() ==  WifiDirectBroadcastReceiver.IS_CLIENT){
//						Toast.makeText(MainActivity.this, "I'm the client", Toast.LENGTH_SHORT).show();
//						ClientInit client = new ClientInit(mReceiver.getOwnerAddr());
//						client.start();
//					}

				}
				else{
					//Set the chat name as local bluetooth name
					saveChatName(MainActivity.this, getLocalBluetoothName());
				}

				chatName = loadChatName(MainActivity.this);
				Log.d(TAG, "chatname: " + chatName);

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						(MainActivity.this).getSetUsernameButton().setVisibility(View.GONE);
						(MainActivity.this).getSetChatName().setVisibility(View.GONE);
						(MainActivity.this).getSetChatNameLabel().setVisibility(View.GONE);
						(MainActivity.this).getDisconnect().setVisibility(View.GONE);
						(MainActivity.this).getPeersList().setVisibility(View.VISIBLE);
						(MainActivity.this).getToggleVisibility().setVisibility(View.VISIBLE);
					}
				});
			}
		});    	
    }
    
//    public void disconnect(){
//    	disconnect.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				mManager.removeGroup(mChannel, null);
//		    	finish();
//			}
//		});
//    }
    
    public void goToSettings(){    	
    	goToSettings.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {

				//Open Wifi settings
		        startActivityForResult(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS), 0);
			}
		});    	
    }

    public void setupPeerListItemsClickListener() {
		peersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				final NsdServiceInfo nsdService = nsdServiceArray[position];

				nsdManager.resolveService(nsdService, resolveListener);

			}
		});
	}

	public void setupToggleVisibilityListener() {
		toggleVisibility.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
				if (compoundButton.isPressed()) {
					if (isChecked) {
						server = new ServerInit();
						serverSocketPort = server.getServerPort();
						registerService(serverSocketPort);

						server.start();
					} else {
						nsdManager.unregisterService(registrationListener);
						server.interrupt();
					}
				}
			}
		});
	}

	public void registerService(int port) {
		// Create the NsdServiceInfo object, and populate it.
		NsdServiceInfo serviceInfo = new NsdServiceInfo();

		// The name is subject to change based on conflicts
		// with other services advertised on the same network.
		serviceInfo.setServiceName(loadChatName(MainActivity.this) + " peernet");
		serviceInfo.setServiceType("_peernet._tcp");
		serviceInfo.setPort(port);

		nsdManager.registerService(
				serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

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
    
    //Save the chat name to SharedPreferences
  	public void saveChatName(Context context, String chatName) {
  		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
  		Editor edit = prefs.edit();
  		edit.putString("chatName", chatName);
  		edit.commit();
  	}

  	//Retrieve the chat name from SharedPreferences
  	public static String loadChatName(Context context) {
  		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
  		return prefs.getString("chatName", DEFAULT_CHAT_NAME);
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
				Toast.makeText(MainActivity.this, "Service registered.", Toast.LENGTH_SHORT).show();
//				handler.obtainMessage(SERVICE_REGISTERED).sendToTarget();
			}

			@Override
			public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
				// Registration failed! Put debugging code here to determine why.
				Log.e(TAG, "Registration failed: " + errorCode);
				Toast.makeText(MainActivity.this, "Service registration failed.", Toast.LENGTH_SHORT).show();
//				toggleVisibility.setOnCheckedChangeListener(null);
				toggleVisibility.setChecked(false);
//				toggleVisibility.setOnCheckedChangeListener(this);
			}

			@Override
			public void onServiceUnregistered(NsdServiceInfo arg0) {
				// Service has been unregistered. This only happens when you call
				// NsdManager.unregisterService() and pass in this listener.
				Log.e(TAG, "Service unregistered.");
				Toast.makeText(MainActivity.this, "Service unregistered.", Toast.LENGTH_SHORT).show();
//				handler.obtainMessage(SERVICE_UNREGISTERED).sendToTarget();
			}

			@Override
			public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
				// Unregistration failed. Put debugging code here to determine why.
				Log.e(TAG, "Un-registration failed: " + errorCode);
				Toast.makeText(MainActivity.this, "Service un-registration failed.", Toast.LENGTH_SHORT).show();
				toggleVisibility.setChecked(true);
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
				Toast.makeText(MainActivity.this, "Discovery started", Toast.LENGTH_SHORT).show();
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

				ClientInit client = new ClientInit(host, port);
				client.start();

				//Open the ChatActivity
				Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
				startActivity(intent);

//				clientClass = new ClientClass(host, port);
//				clientClass.run();
			}
		};
	}

	public void activateGoToChat(){
		(MainActivity.this).getSetUsernameButton().setText("Start the chat");
		(MainActivity.this).getSetUsernameButton().setVisibility(View.VISIBLE);
		(MainActivity.this).getSetChatName().setVisibility(View.VISIBLE);
		(MainActivity.this).getSetChatNameLabel().setVisibility(View.VISIBLE);
		(MainActivity.this).getDisconnect().setVisibility(View.VISIBLE);
//		(MainActivity.this).getGoToSettings().setVisibility(View.GONE);
//		(MainActivity.this).getGoToSettingsText().setVisibility(View.GONE);
		(MainActivity.this).getPeersList().setVisibility(View.GONE);
		(MainActivity.this).getToggleVisibility().setVisibility(View.GONE);

		//// TODO: 06/06/2018 get owneraddress and pass it to main
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