package com.example.viewpagerpoc;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.vcard.VCardEntry;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import bluetooth.client.pbap.BluetoothPbapClient;

public class PhoneContactsFragment extends BaseFragment {

    protected static final int PERMISSIONS_REQUEST_ALL_PERMISSIONS = 1;
    private static final int REQUEST_ENABLE_BT = 0;
    public static BluetoothPbapClient sPbapClient = null;
    private final BroadcastReceiver blueToothReceiver = new MyBroadcastReceiver();
    boolean action_app_disconnect = false;
    ArrayList<BluetoothDevice> availableDevices = new ArrayList();
    //BTDeviceAdapter availableDevicesDdapter = null;
    BluetoothAdapter bluetoothAdapter = null;
    ArrayList<VCardEntry> contacts = new ArrayList();
    BluetoothDevice device = null;
    boolean retrieve_phone_contact = false;
    boolean retrieve_sim_contact = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        //initViews(view);
        initLogic();
        return view;
    }

    private void initLogic(){
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        requestAllPermissions();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.device.action.FOUND");
        filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
        filter.addAction("android.bluetooth.adapter.action.SCAN_MODE_CHANGED");
        filter.addAction("android.bluetooth.adapter.action.DISCOVERY_STARTED");
        filter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
        filter.addAction("android.bluetooth.device.action.BOND_STATE_CHANGED");
        getActivity().registerReceiver(blueToothReceiver, filter);

        //setupNearbyDeviceList();
        Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice d: pairedDevices) {
                String deviceName = d.getName();
                if(deviceName.equalsIgnoreCase("siddu jio")){
                    device = d;
                }
            }
        }

        sPbapClient = new BluetoothPbapClient(device, new BluetoothServiceHandler());
        sPbapClient.connect();
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        MyBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
              /*  if (!DeviceListActivity.isDeviceOnthelist(DeviceListActivity.this.availableDevices, device)) {
                    DeviceListActivity.this.availableDevices.add(device);
                    DeviceListActivity.this.updateNearbyDeviceList();
                }*/
            }
            if (action.equals("android.bluetooth.adapter.action.STATE_CHANGED")) {
                switch (intent.getIntExtra("android.bluetooth.adapter.extra.STATE", Integer.MIN_VALUE)) {
                    case 12:
                       // DeviceListActivity.this.showSnackBar(DeviceListActivity.this.getString(C0938R.string.bt_on));
                       // DeviceListActivity.this.findDevices();
                        break;
                }
            }
            if (action.equals("android.bluetooth.adapter.action.SCAN_MODE_CHANGED")) {
                switch (intent.getIntExtra("android.bluetooth.adapter.extra.SCAN_MODE", Integer.MIN_VALUE)) {
                    case 23:
                       // DeviceListActivity.this.showSnackBar(DeviceListActivity.this.getString(C0938R.string.bt_discoverable));
                        break;
                }
            }
            if (action.equals("android.bluetooth.adapter.action.DISCOVERY_STARTED")) {
                //DeviceListActivity.this.discoveryStarted();
            }
            if (action.equals("android.bluetooth.adapter.action.DISCOVERY_FINISHED")) {
                //DeviceListActivity.this.discoveryFinished();
            }
            if (action.equals("android.bluetooth.device.action.BOND_STATE_CHANGED")) {
                BluetoothDevice mDevice = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
              //  DeviceListActivity.this.hideProgressDialog();
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
                   // DeviceListActivity.this.showSnackBar(DeviceListActivity.this.getString(C0938R.string.device_paired));
                    //DeviceListActivity.this.hideProgressDialog();
                    connectToPhonebook(mDevice);
                   // DeviceListActivity.this.showProgressDialog(DeviceListActivity.this.getString(C0938R.string.contacting_device));
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                    //DeviceListActivity.this.showProgressDialog(DeviceListActivity.this.getString(C0938R.string.pairing_device));
                }
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                   // DeviceListActivity.this.hideProgressDialog();
                }
            }
        }
    }

    private class BluetoothServiceHandler extends Handler {

        private BluetoothServiceHandler() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    break;
                case 102:
                    //hideProgressDialog();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //DialogUtility.showDialog(this, getString(C0938R.string.connection_failed));
                        }
                    });
                    break;
                case 201:
                    //hideProgressDialog();
                    if (sPbapClient == null || sPbapClient.getState() != BluetoothPbapClient.ConnectionState.CONNECTED) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //DialogUtility.showDialog(this, getString(C0938R.string.connection_failed));
                            }
                        });
                        return;
                    }
                    sPbapClient.pullPhoneBook(BluetoothPbapClient.PB_PATH);
                    //showProgressDialog(getString(C0938R.string.retriving_contacts));
                    return;
                case 202:
                    if (action_app_disconnect) {
                        action_app_disconnect = false;
                        return;
                    }
                    //hideProgressDialog();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // DialogUtility.showDialog(this, getString(C0938R.string.connection_failed));
                        }
                    });
                    return;
                default:
                    return;
            }
            if (!retrieve_phone_contact) {
                retrieve_phone_contact = true;
                if (((ArrayList) msg.obj) != null) {
                    contacts.addAll((ArrayList) msg.obj);
                }
                sPbapClient.pullPhoneBook(BluetoothPbapClient.SIM_PB_PATH);
            } else if (!retrieve_sim_contact) {
                //hideProgressDialog();
                retrieve_sim_contact = true;
                if (((ArrayList) msg.obj) != null) {
                    contacts.addAll((ArrayList) msg.obj);
                }
                ArrayList<VCardEntry> useful_contacts = new ArrayList();
                Iterator it = contacts.iterator();
                while (it.hasNext()) {
                    VCardEntry vcard = (VCardEntry) it.next();
                    List<VCardEntry.PhoneData> phones = vcard.getPhoneList();
                    if (phones != null && phones.size() == 1) {
                        useful_contacts.add(vcard);
                    }
                }
               // ReceivedVcards.instance().received_contacts = ContactUtility.sortContacts(useful_contacts);
                //startActivity(new Intent(this, SharedContactsActivity.class));
                action_app_disconnect = true;
                sPbapClient.disconnect();
                retrieve_phone_contact = false;
                retrieve_sim_contact = false;
                contacts.clear();
            }
        }
    }

    public void connectToPhonebook(BluetoothDevice device) {
        sPbapClient = null;
        sPbapClient = new BluetoothPbapClient(device, new BluetoothServiceHandler());
        sPbapClient.connect();
    }

    public void disconnectFromPhonebook() {
        if (sPbapClient != null) {
            sPbapClient.disconnect();
        }
    }

    @SuppressLint({"NewApi"})
    protected void requestAllPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            int read_contact = ContextCompat.checkSelfPermission(getActivity(), "android.permission.READ_CONTACTS");
            int write_contact = ContextCompat.checkSelfPermission(getActivity(), "android.permission.WRITE_CONTACTS");
            int access_location = ContextCompat.checkSelfPermission(getActivity(), "android.permission.ACCESS_FINE_LOCATION");
            if (read_contact != 0 || write_contact != 0 || access_location != 0) {
                requestPermissions(new String[]{"android.permission.READ_CONTACTS", "android.permission.WRITE_CONTACTS", "android.permission.ACCESS_FINE_LOCATION"}, 1);
            }
        }
    }



    /*private void initViews(View view) {

        Intent serviceIntent = new Intent(getContext(), BluetoothService.class);
        ServiceConnection connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d("", "onServiceConnected, init the sService");
                sService = ((BluetoothService.BluetoothServiceBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                sService = null;
            }
        };
        getActivity().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        Button getContacts = view.findViewById(R.id.getContacts);
        getContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPullPhoneBook();
            }
        });
    }

    public void onPullPhoneBook() {
        if (sService != null) {
            sService.getPhoneBook();
        }
    }*/
}
