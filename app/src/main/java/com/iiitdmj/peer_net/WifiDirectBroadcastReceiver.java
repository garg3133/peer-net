//package com.iiitdmj.peer_net;
//
//import android.Manifest;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.NetworkInfo;
//import android.net.wifi.p2p.WifiP2pManager;
//import android.widget.Toast;
//
//import androidx.core.app.ActivityCompat;
//
//public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
//    private WifiP2pManager mManager;
//    private WifiP2pManager.Channel mChannel;
//    private MainActivity mActivity;
//
//    public WifiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MainActivity mActivity) {
//        this.mManager = mManager;
//        this.mChannel = mChannel;
//        this.mActivity = mActivity;
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String action = intent.getAction();
//
//        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
//            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
//
//            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
//                Toast.makeText(context, "WiFi is ON", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "WiFi is OFF", Toast.LENGTH_SHORT).show();
//            }
//        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
//            if (mManager != null) {
//                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(context, "LOCATION PERMISSION NOT GRANTED. CLICK DISCOVER TO GRANT PERMISSION.", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                mManager.requestPeers(mChannel, mActivity.peerListListener);
//            }
//        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
//            if (mManager == null) return;
//
//            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
//
//            if (networkInfo.isConnected()) {
//                mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
//            } else {
//                mActivity.connectionStatus.setText("Device Disconnected.");
//            }
//
//        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
//
//        }
//    }
//}
