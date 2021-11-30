package com.example.greenspaces;

import android.app.FragmentContainer;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.Serializable;
import java.util.ArrayList;

public class UserFragment extends Fragment {

    private Button button_userReviews;
    private Button button_userPhotos;
    private TextView textView_userName;
    private FragmentContainer fragContainer_user;

    private String name;
    private String id;

    View view;
    Context context;

    private SharedPreferences sharedPreferences;

    public UserFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, container, false);
        context = view.getContext();

        sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        name = sharedPreferences.getString("name", null);
        id = sharedPreferences.getString("user_id", null);

        textView_userName = view.findViewById(R.id.textView_userName);
        button_userReviews = view.findViewById(R.id.button_userReviews);
        button_userPhotos = view.findViewById(R.id.button_userPhotos);

        textView_userName.setText(name);

        button_userReviews.setOnClickListener(v -> {
            loadFragment(new MyReviewsFragment(), R.id.fragContainer_user);
        });

        button_userPhotos.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString("parent", "user");
            bundle.putString("id", id);
            PhotosFragment photosFragment = new PhotosFragment();
            photosFragment.setArguments(bundle);
            loadFragment(photosFragment, R.id.fragContainer_user);
        });

        return view;
    }

    public void loadFragment(Fragment fragment, int id){
        FragmentManager fragmentManager = getParentFragmentManager();
        // create a fragment transaction to begin the transaction and replace the fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //replacing the placeholder - fragmentContainterView with the fragment that is passed as parameter
        fragmentTransaction.replace(id, fragment);
        fragmentTransaction.commit();
    }
}
