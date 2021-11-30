package com.example.greenspaces;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ListFragment extends Fragment {
    View view;
    Context context;

    private RecyclerView recyclerView_list;
    private TextView textView_noResults;

    private ArrayList<Location> locationArrayList;

    public ListFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, container, false);
        context = view.getContext();

        recyclerView_list = view.findViewById(R.id.recyclerView_list);
        textView_noResults = view.findViewById(R.id.textView_noResults);

        if (getArguments() != null && getArguments().getSerializable("locationArrayList") != null) {
            locationArrayList = (ArrayList<Location>) getArguments().getSerializable("locationArrayList");
            displayLocations();
            Log.d("bundle", "received bundle");
        } else {
            recyclerView_list.setVisibility(View.GONE);
            textView_noResults.setVisibility(View.VISIBLE);
        }

        return view;
    }

    public void displayLocations(){
        LocationAdapter adapter = new LocationAdapter(locationArrayList);
        recyclerView_list.setAdapter(adapter);
        recyclerView_list.setLayoutManager(new LinearLayoutManager(context));
        recyclerView_list.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
    }
}
