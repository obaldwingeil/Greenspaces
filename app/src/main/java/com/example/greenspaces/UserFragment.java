package com.example.greenspaces;

import android.app.FragmentContainer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class UserFragment extends Fragment {

    private Button button_userReviews;
    private Button button_userPhotos;
    private TextView textView_userName;
    private ImageView imageView_settings;

    private String name;
    private String id;

    View view;
    Context context;

    private SharedPreferences sharedPreferences;

    GoogleSignInClient mGoogleSignInClient;
    private String CLIENT_ID;

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
        imageView_settings = view.findViewById(R.id.imageView_settings);

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

        imageView_settings.setOnClickListener(v -> openSettings());

        CLIENT_ID = "698498025598-0vlhl0krm3vjn55org508bf5d4gfja6f.apps.googleusercontent.com";
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(CLIENT_ID)
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);

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

    public void openSettings(){
        PopupMenu settingsMenu = new PopupMenu(context, imageView_settings);
        settingsMenu.getMenuInflater().inflate(R.menu.menu_settings, settingsMenu.getMenu());

        settingsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                signOut();
                return true;
            }
        });
        settingsMenu.show();
    }

    public void signOut(){
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(Objects.requireNonNull(getActivity()), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Intent intent = new Intent(context, MainActivity.class);
                        startActivity(intent);
                    }
                });
    }
}
