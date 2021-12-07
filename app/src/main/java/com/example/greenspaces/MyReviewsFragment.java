package com.example.greenspaces;

import android.content.Context;
import android.content.SharedPreferences;
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

public class MyReviewsFragment extends Fragment {

    private RecyclerView recyclerView_myReviews;
    private TextView textView_noReviews;

    View view;
    Context context;

    private ArrayList<Review> reviewList;
    private ArrayList<String> reviewIDs;
    private String api_root;
    private String user_id;

    private static AsyncHttpClient client = new AsyncHttpClient();

    private SharedPreferences sharedPreferences;

    public MyReviewsFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_myreviews, container, false);
        context = view.getContext();

        sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString("user_id", null);

        recyclerView_myReviews = view.findViewById(R.id.recyclerView_myReviews);
        textView_noReviews = view.findViewById(R.id.textView_noReviews);

        reviewList = new ArrayList<>();
        reviewIDs = new ArrayList<>();
        api_root = getString(R.string.api_root);

        getReviews();

        return view;
    }

    public void getReviews(){
        String api = api_root + "api/reviews/user/" + user_id;
        client.get(api, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    JSONArray reviews = response.getJSONArray("reviews");

                    for(int i = 0; i < reviews.length(); i++){
                        JSONObject reviewObject = reviews.getJSONObject(i);
                        Review review = new Review(
                                reviewObject.getString("review_id"),
                                reviewObject.getString("user_id"),
                                reviewObject.getString("location_id"),
                                reviewObject.getString("description"),
                                reviewObject.getDouble("rating"),
                                reviewObject.getString("user_name"),
                                reviewObject.getString("location_name"),
                                "user"
                        );
                        reviewList.add(review);
                        reviewIDs.add(reviewObject.getString("review_id"));
                    }
                    getImages();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                textView_noReviews.setVisibility(View.VISIBLE);
                recyclerView_myReviews.setVisibility(View.GONE);
            }
        });
    }

    public void getImages() {
        String api = api_root + "api/images/review_ids";
        Log.d("api", api);
        JSONObject body = new JSONObject();
        try {
            body.put("review_ids", reviewIDs);
            StringEntity entity = new StringEntity(body.toString());
            client.get(context, api, entity, "application/json", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Log.d("api_response", new String(responseBody));
                    try {
                        JSONObject response = new JSONObject(new String(responseBody));
                        JSONArray images = response.getJSONArray("images");
                        for (int i = 0; i < reviewList.size(); i++) {
                            Review review = reviewList.get(i);
                            for (int j = 0; j < images.length(); j++) {
                                JSONObject image = images.getJSONObject(j);
                                if (image.getString("location_id").equals(review.getLocation_id())) {
                                    Image imageObj = new Image(
                                            image.getString("url"),
                                            image.getString("location_id"),
                                            image.getString("user_id"),
                                            image.getString("review_id")
                                    );
                                    review.addImage(imageObj);
                                }
                            }
                        }

                        ReviewAdapter adapter = new ReviewAdapter(reviewList);
                        recyclerView_myReviews.setAdapter(adapter);
                        recyclerView_myReviews.setLayoutManager(new LinearLayoutManager(context));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Log.d("api_failure", new String(responseBody));
                }
            });
        } catch (JSONException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
