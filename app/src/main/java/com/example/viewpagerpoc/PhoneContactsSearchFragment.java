package com.example.viewpagerpoc;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.android.vcard.VCardEntry;

import java.util.ArrayList;

public class PhoneContactsSearchFragment extends BaseFragment implements AdapterOnItemClickListener {

    ArrayList<VCardEntry> contacts = new ArrayList<>();

    //Views
    RecyclerView recyclerView;
    TextView txtBluetoothDevice;
    PhoneFragmentContactsAdapter phoneFragmentContactsAdapter;
    Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_search, container, false);
        mContext = getActivity();
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        searchView(view);

        recyclerView = view.findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        phoneFragmentContactsAdapter = new PhoneFragmentContactsAdapter(null, this);
        recyclerView.setAdapter(phoneFragmentContactsAdapter);
    }

    private void searchView(View view) {
        SearchView searchView = view.findViewById(R.id.searchView);
        EditText searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setHintTextColor(getResources().getColor(R.color.white));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                performSearch(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return true;
            }
        });
    }

    private void performSearch(String s) {

    }

    @Override
    public void onItemClickListener(int position) {
        Bundle bundle = new Bundle();
        bundle.putString("displayName", contacts.get(position).getDisplayName());
        zoneAFragmentReplaceCallbacks.updateFragment(ZoneAFragmentsEnum.PHONE_CONTACTS_DETAIL_FRAGMENT, bundle);
    }
}