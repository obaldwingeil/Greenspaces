package com.example.greenspaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ReviewActivity extends AppCompatActivity {

    private String location_name;
    private String location_id;

    private TextView textView_name;
    private EditText editText_description;
    private RatingBar ratingBar;
    private FlexboxLayout flex;
    private Button button_post;

    private FlexboxLayout.LayoutParams lp;
    private String api_root;
    private static AsyncHttpClient client = new AsyncHttpClient();

    private SharedPreferences sharedPreferences;

    private ArrayList<String> images;
    private List<String> photos;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Intent intent = getIntent();
        if(intent != null){
            location_name = intent.getStringExtra("location_name");
            location_id = intent.getStringExtra("location_id");
        } else {
            // add search for locations
        }

        api_root = getString(R.string.api_root);

        textView_name = findViewById(R.id.textView_reviewName);
        editText_description = findViewById(R.id.editText_review);
        ratingBar = findViewById(R.id.ratingBar_postRev);
        flex = findViewById(R.id.flexBox_revPhotos);
        button_post = findViewById(R.id.button_post);

        sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);

        images = new ArrayList<>();

        textView_name.setText(location_name);

        ImageView add = new ImageView(this);
        lp = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(8, 8, 8, 8);
        lp.setWidth(150);
        lp.setHeight(150);
        add.setLayoutParams(lp);
        add.setImageDrawable(getResources().getDrawable(R.drawable.add_box, null));
        TextView addImage = new TextView(this);
        addImage.setLayoutParams(lp);
        addImage.setText(R.string.add_image);
        flex.addView(add);
        flex.addView(addImage);

        add.setOnClickListener(v -> openCamera());
        button_post.setOnClickListener(v -> postReview());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Log.d("photos", sharedPreferences.getString("photos", ""));
        String[] temp = sharedPreferences.getString("photos", "").split(", ");
        photos = Arrays.asList(temp);
        Log.d("photos as list", photos.toString() + " " + photos.size());
        for(int i = 0; i < photos.size(); i++){
            if(photos.get(i) != ""){
                Uri file = Uri.fromFile(new File(photos.get(i)));
                images.add("images/" + location_name + "/"+file.getLastPathSegment());
                ImageView add = new ImageView(this);
                lp = new FlexboxLayout.LayoutParams(FlexboxLayout.LayoutParams.WRAP_CONTENT, FlexboxLayout.LayoutParams.WRAP_CONTENT);
                lp.setMargins(8, 8, 8, 8);
                lp.setWidth(300);
                lp.setHeight(300);
                add.setScaleType(ImageView.ScaleType.CENTER_CROP);
                add.setLayoutParams(lp);
                Picasso.get().load(new File(photos.get(i))).into(add);
                flex.addView(add);
            }
        }
    }

    public void saveImages(){
        for(int i = 0; i < photos.size(); i++){
            Uri file = Uri.fromFile(new File(photos.get(i)));
            StorageReference riversRef = storageReference.child(images.get(i));
            UploadTask uploadTask = riversRef.putFile(file);
            Log.d("bucket", storageReference.getBucket());
            // Register observers to listen for when the download is done or if it fails
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e("Failed to upload", exception.getMessage());
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                    Log.d("uploaded", taskSnapshot.getMetadata().getPath());
                }
            });
        }
    }


    public void postReview() {
        if(ratingBar.getRating() == 0 && editText_description.getText().toString().equals("")
        && images.size() == 0){
            Toast.makeText(this, "Cannot post empty review", Toast.LENGTH_LONG).show();
        }
        else{
            if (!photos.get(0).equals("")){
                saveImages();
            }
            sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE);
            String user_id = sharedPreferences.getString("user_id", "id");
            String user_name = sharedPreferences.getString("name", "name");

            String api = api_root + "api/post_review";
            JSONObject body = new JSONObject();
            try {
                body.put("text", editText_description.getText().toString());
                body.put("rating", String.valueOf(ratingBar.getRating()));
                body.put("locationId", location_id);
                body.put("locationName", location_name);
                body.put("userName", user_name);
                body.put("userId", user_id);
                body.put("images", images);

                StringEntity entity = new StringEntity(body.toString());
                client.post(this, api, entity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d("posted review", new String(responseBody));
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("photos",""); // reset photos shared pref
                        editor.apply();
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void openCamera(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}