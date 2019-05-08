package com.example.viewpagerpoc;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.zxon.mybluetoothdemo.service.BluetoothService;

public class PhoneContactsFragment extends BaseFragment {

    public static BluetoothService sService;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {

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
    }
}
