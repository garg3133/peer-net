package com.example.chatfull;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class DialogViewActivity extends AppCompatActivity
        implements DialogsListAdapter.OnDialogClickListener<Dialog> {

    public static final String TAG = "DialogViewActivity";

    private static final int SHOW_INFO = 100;
    private static final int ENTER_INFO = 200;
    private static final int CHAT_ACTIVITY = 300;
    protected ImageLoader imageLoader;
    static DialogsListAdapter<Dialog> dialogsAdapter;
    DialogsList dialogsList;
    FloatingActionButton fab1, fab2, fab3;
    boolean isFABOpen;

    static User me, user;
    private final static String SHARED_PREFERENCES_KEY_USER_SELF = "ME";
    private final static String SHARED_PREFERENCES_KEY_DIALOG = "DIALOG_INFO";
    private static String PREFERENCE_FILE_KEY_SELF = "SELF_INFO";
    private static String PREFERENCE_FILE_KEY_DIALOGS = "DIALOG_LIST";
    SharedPreferences sharedPrefSelf, sharedPrefDialog;
    SharedPreferences.Editor editorUser, editorDialog;
    Gson gson;


    private FloatingActionMenu fam;
    private FloatingActionButton fabShowInfo, fabEnterInfo;

    static List<Dialog> dialogArrayList;
    boolean loaded = false, saved = false;
    TextView overlay;

    String image;

    NsdManager nsdManager;
    NsdManager.RegistrationListener registrationListener;

    String localServiceName;
    private static final int SERVER_PORT = 8080;
    private Server myServer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog_view);

        image = "https://cdn1.imggmi.com/uploads/2019/10/19/5bf1857add4ee9b72b31257e2adb9030-full.png";

        imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Picasso.get().load(url).into(imageView);
//                Glide.with(getApplicationContext()).asBitmap().load(R.drawable.dialog).into(imageView);
//                Glide.with(getApplicationContext())
//                        .load(Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +R.drawable.dialog).toString())
//                        .into(imageView);
            }
        };

        dialogsList = (DialogsList) findViewById(R.id.dialogsList);
        dialogsAdapter = new DialogsListAdapter<>(imageLoader);
        dialogsAdapter.setOnDialogClickListener(this);
        dialogsList.setAdapter(dialogsAdapter);

        gson = new Gson();
        sharedPrefSelf = this.getSharedPreferences(PREFERENCE_FILE_KEY_SELF, Context.MODE_PRIVATE);
        editorUser = sharedPrefSelf.edit();
        String jsonDataStringSelfUser = sharedPrefSelf.getString(SHARED_PREFERENCES_KEY_USER_SELF, "");
        me = gson.fromJson(jsonDataStringSelfUser, User.class);


        overlay = findViewById(R.id.overlay);
        if (dialogsAdapter.isEmpty())
            overlay.setVisibility(View.VISIBLE);

        fabShowInfo = findViewById(R.id.showInfoFab);
        fabEnterInfo = findViewById(R.id.enterInfoFab);
        fam = findViewById(R.id.fab_menu);
        fabShowInfo.setOnClickListener(onButtonClick());
        fabEnterInfo.setOnClickListener(onButtonClick());

        loaded = false;
        saved = false;
        dialogArrayList = new ArrayList<>();

        initializeRegistrationListener();

        nsdManager = (NsdManager) this.getSystemService(Context.NSD_SERVICE);

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (loaded == false) {
            sharedPrefDialog = this.getSharedPreferences(PREFERENCE_FILE_KEY_DIALOGS, MODE_PRIVATE);
            editorDialog = sharedPrefDialog.edit();
            String jsonDataStringDialogArray = sharedPrefDialog.getString(SHARED_PREFERENCES_KEY_DIALOG, "");
            Log.e("DialogArrar", jsonDataStringDialogArray);
            if ((jsonDataStringDialogArray != null || jsonDataStringDialogArray != "null") && jsonDataStringDialogArray.length() > 2) {
                Dialog dialodArray[] = gson.fromJson(jsonDataStringDialogArray, Dialog[].class);
                if (dialodArray != null) {
                    for (Dialog d : dialodArray) {
                        dialogArrayList.add(d);
                    }
                    dialogsAdapter.addItems(dialogArrayList);
                    overlay.setVisibility(View.INVISIBLE);
                }
            }
            loaded = true;
        }
    }

    @Override

    protected void onPause() {
        if (saved == false) {
            Log.e("PAUSE", "SAVE");
            String jsonDataString = gson.toJson(dialogArrayList);
            Log.e("PAUSE", jsonDataString);
            editorDialog.putString(SHARED_PREFERENCES_KEY_DIALOG, jsonDataString);
            editorDialog.commit();
            saved = true;
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (saved == false) {
            Log.e("BACK", "SAVE");
            String jsonDataString = gson.toJson(dialogArrayList);
            Log.e("PAUSE", jsonDataString);
            editorDialog.putString(SHARED_PREFERENCES_KEY_DIALOG, jsonDataString);
            editorDialog.commit();
            saved = true;
        }
        loaded = false;
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (saved == false) {
            Log.e("DESTROY", "SAVE");
            String jsonDataString = gson.toJson(dialogArrayList);
            editorDialog.putString(SHARED_PREFERENCES_KEY_DIALOG, jsonDataString);
            editorDialog.commit();
            saved = true;
        }
    }

    private View.OnClickListener onButtonClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == fabShowInfo) {
//                    Intent intent = new Intent(getApplicationContext(), ShowInfoActivity.class);
//                    startActivityForResult(intent, SHOW_INFO);
                    Log.d(TAG, fabShowInfo.getLabelText());
                    if (fabShowInfo.getLabelText().equals("Turn on visibility")) {
                        myServer = new Server(DialogViewActivity.this, getSelfIpAddress(), SERVER_PORT);
                        registerService(SERVER_PORT);
                    } else {
                        myServer.onDestroy();
                        nsdManager.unregisterService(registrationListener);
                        Log.d(TAG, "Service unregistered");
                    }


                } else if (view == fabEnterInfo) {
                    Intent intent = new Intent(getApplicationContext(), FindPeers.class);
                    intent.putExtra("localServiceName", localServiceName);
                    startActivityForResult(intent, ENTER_INFO);
//                } else if (view == fabFindPeer) {

                }
                fam.close(true);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == SHOW_INFO || requestCode == ENTER_INFO) && data != null) {
            if (resultCode == RESULT_OK) {
                user = (User) data.getSerializableExtra("user");
                Dialog dialog = dialogsAdapter.getItemById(user.getName());
                if (dialog == null) {
                    dialog = new Dialog(user.getName(), user.getName(), image, new ArrayList<User>(Arrays.asList(user)), null, 0);
                    dialogsAdapter.addItem(0, dialog);

//                    dialogArrayList = new ArrayList<>();
                    dialogArrayList.add(dialog);

                    String jsonDataStringDialog = gson.toJson(dialogArrayList);
                    editorDialog.putString(SHARED_PREFERENCES_KEY_DIALOG, jsonDataStringDialog);
                    editorDialog.commit();

                    overlay.setVisibility(View.INVISIBLE);
                }
                onDialogClick(dialog);
            }
        }
    }

    @Override
    public void onDialogClick(Dialog dialog) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("user", dialog.getUsers().get(0));
        intent.putExtra("dialog", dialog);
        startActivity(intent);
    }

    public void setConnected(User user) {
        Intent data = new Intent();
        data.putExtra("user",user);
        setResult(RESULT_OK,data);
        finish();
    }

    public void registerService(int port) {
        // Create the NsdServiceInfo object, and populate it.
        NsdServiceInfo serviceInfo = new NsdServiceInfo();

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.setServiceName(me.getName() + " peernet");
        serviceInfo.setServiceType("_peernet._tcp");
        serviceInfo.setPort(port);

        nsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);

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
                Toast.makeText(DialogViewActivity.this, "Service registered.", Toast.LENGTH_SHORT).show();
//				handler.obtainMessage(SERVICE_REGISTERED).sendToTarget();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (DialogViewActivity.this).fabShowInfo.setLabelText("Turn off visibility");
                    }
                });
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed! Put debugging code here to determine why.
                Log.e(TAG, "Registration failed: " + errorCode);
                Toast.makeText(DialogViewActivity.this, "Service registration failed.", Toast.LENGTH_SHORT).show();
//				toggleVisibility.setOnCheckedChangeListener(null);
//                toggleVisibility.setChecked(false);
//				toggleVisibility.setOnCheckedChangeListener(this);
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered. This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
                Log.e(TAG, "Service unregistered.");
                Toast.makeText(DialogViewActivity.this, "Service unregistered.", Toast.LENGTH_SHORT).show();
//				handler.obtainMessage(SERVICE_UNREGISTERED).sendToTarget();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        (DialogViewActivity.this).fabShowInfo.setLabelText("Turn on visibility");
                    }
                });
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed. Put debugging code here to determine why.
                Log.e(TAG, "Un-registration failed: " + errorCode);
                Toast.makeText(DialogViewActivity.this, "Service un-registration failed.", Toast.LENGTH_SHORT).show();
//                toggleVisibility.setChecked(true);
            }
        };
    }

    // Returns device IP Address
    public static String getSelfIpAddress() {
        String self_ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        self_ip = inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            Log.e("GET_IP", "IP NOT FOUND");
        }
        return self_ip;
    }
}
