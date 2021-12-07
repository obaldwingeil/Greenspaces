package com.example.greenspaces;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.flexbox.FlexboxLayout;
// import com.google.android.gms.tasks.OnSuccessListener;
// import com.google.firebase.storage.FirebaseStorage;
// import com.google.firebase.storage.StorageReference;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class PhotosFragment extends Fragment {

    View view;
    private Context context;
    private String api_root;

    private FlexboxLayout flex;
    private TextView textView_noPhotos;

    private String id;
    private String parent;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

    private static AsyncHttpClient client = new AsyncHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_photos, container, false);
        context = view.getContext();

        flex = view.findViewById(R.id.flexBox_photos);
        textView_noPhotos = view.findViewById(R.id.textView_noPhotos);

        api_root = getString(R.string.api_root);

        if (getArguments() != null && getArguments().getString("id") != null) {
            id = getArguments().getString("id");
            parent = getArguments().getString("parent");
        }

        getImages();

        return view;
    }

    public void getImages(){
        String api = api_root + "api/user_images/" + id;
        Log.d("api", api);
        JSONObject body = new JSONObject();
        try {
            body.put("parent", parent);
            StringEntity entity = new StringEntity(body.toString());
            client.get(context, api, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.d("api", new String(responseBody));
                    try {
                        JSONObject response = new JSONObject(new String(responseBody));
                        JSONArray images = response.getJSONArray("images");

                        FlexboxLayout.LayoutParams lp = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(8,8,8,8);
                        lp.setWidth(400);
                        lp.setHeight(400);
                        for(int i = 0; i < images.length(); i++){
                            JSONObject image = images.getJSONObject(i);
                            ImageView photo = new ImageView(context);
                            photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            photo.setLayoutParams(lp);
                            storageReference.child(image.getString("url")).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    Picasso.get().load(uri).into(photo); //.rotate(90)
                                }
                            });
                            flex.addView(photo);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    textView_noPhotos.setVisibility(View.VISIBLE);
                    flex.setVisibility(View.GONE);
                }
            });
        } catch (JSONException|UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
