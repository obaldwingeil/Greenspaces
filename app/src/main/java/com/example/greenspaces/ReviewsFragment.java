package com.example.greenspaces;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;

public class ReviewsFragment extends Fragment {

    View view;
    private Context context;

    LocationActivity activity;

    private ProgressBar progressBar_5;
    private ProgressBar progressBar_4;
    private ProgressBar progressBar_3;
    private ProgressBar progressBar_2;
    private ProgressBar progressBar_1;
    private TextView textView_overAll;
    private RatingBar ratingBar_overAll;
    private TextView textView_numReviews;
    private RecyclerView recyclerView_reviews;
    private Button button_write;
    private TextView textView_noReviews;

    private ArrayList<Review> reviewList;

    private String api_root;
    private String locationID;
    private String locationName;
    private Double rating;

    private static AsyncHttpClient client = new AsyncHttpClient();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_reviews, container, false);
        context = view.getContext();

        progressBar_1 = view.findViewById(R.id.progressBar_1);
        progressBar_2 = view.findViewById(R.id.progressBar_2);
        progressBar_3 = view.findViewById(R.id.progressBar_3);
        progressBar_4 = view.findViewById(R.id.progressBar_4);
        progressBar_5 = view.findViewById(R.id.progressBar_5);
        textView_overAll = view.findViewById(R.id.textView_overAll);
        ratingBar_overAll = view.findViewById(R.id.ratingBar_overAll);
        textView_numReviews = view.findViewById(R.id.textView_numReviews);
        recyclerView_reviews = view.findViewById(R.id.recyclerView_reviews);
        button_write = view.findViewById(R.id.button_write);
        textView_noReviews = view.findViewById(R.id.textView_noLocationReviews);

        activity = (LocationActivity)getActivity();
        assert activity != null;
        rating = activity.rating;

        reviewList = new ArrayList<>();
        api_root = getString(R.string.api_root);
        // Intent intent = getIntent();
        locationID = activity.locationID;
        locationName = activity.locationName;
        getReviews();

        button_write.setOnClickListener(v -> writeReview());

        return view;
    }

    public void getReviews(){
        String api = api_root + "api/reviews/" + locationID;
        client.get(api, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    JSONObject response = new JSONObject(new String(responseBody));
                    JSONArray reviews = response.getJSONArray("reviews");
                    textView_overAll.setText(String.valueOf(rating));
                    ratingBar_overAll.setRating(rating.floatValue());
                    textView_numReviews.setText(reviews.length() + " Reviews");
                    int five = 0;
                    int four = 0;
                    int three = 0;
                    int two = 0;
                    int one = 0;
                    for(int i = 0; i < reviews.length(); i++){
                        JSONObject reviewObject = reviews.getJSONObject(i);
                        Log.d("reviews", String.valueOf(reviewObject));
                        if(reviewObject.getDouble("rating") == 5){
                            five++;
                        }
                        else if(reviewObject.getDouble("rating") >= 4){
                            four++;
                        }
                        else if(reviewObject.getDouble("rating") >= 3){
                            three++;
                        }
                        else if(reviewObject.getDouble("rating") >= 2){
                            two++;
                        }
                        else{
                            one++;
                        }

                        JSONArray images = reviewObject.getJSONArray("images");
                        ArrayList<Image> imageArrayList = new ArrayList<>();
                        for(int j = 0; j < images.length(); j++){
                            JSONObject imageObj = images.getJSONObject(j);
                            Image image = new Image(
                                    imageObj.getString("url"),
                                    imageObj.getString("location_id"),
                                    imageObj.getString("review_id"),
                                    imageObj.getString("user_id")
                            );
                            imageArrayList.add(image);
                        }

                        Review review = new Review(
                                reviewObject.getString("review_id"),
                                reviewObject.getString("user_id"),
                                reviewObject.getString("location_id"),
                                reviewObject.getString("description"),
                                reviewObject.getDouble("rating"),
                                reviewObject.getString("user_name"),
                                reviewObject.getString("location_name"),
                                "location",
                                imageArrayList
                        );
                        reviewList.add(review);
                    }
                    progressBar_1.setProgress(one*5);
                    progressBar_2.setProgress(two*5);
                    progressBar_3.setProgress(three*5);
                    progressBar_4.setProgress(four*5);
                    progressBar_5.setProgress(five*5);

                    ReviewAdapter adapter = new ReviewAdapter(reviewList);
                    recyclerView_reviews.setAdapter(adapter);
                    LinearLayoutManager layoutManager =  new LinearLayoutManager(context);
                    recyclerView_reviews.setLayoutManager(layoutManager);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                textView_noReviews.setVisibility(View.VISIBLE);
                recyclerView_reviews.setVisibility(View.GONE);
            }
        });
    }

    public void writeReview(){
        Intent intent = new Intent(context, ReviewActivity.class);
        intent.putExtra("location_name", locationName);
        intent.putExtra("location_id", locationID);
        startActivity(intent);
    }
}
