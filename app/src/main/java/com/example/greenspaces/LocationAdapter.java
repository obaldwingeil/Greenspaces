package com.example.greenspaces;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private List<Location> locations;
    private Context context;

    private static AsyncHttpClient client = new AsyncHttpClient();

    private SharedPreferences sharedPreferences;
    private String user_id;

    public LocationAdapter(List<Location>locations){
        this.locations = locations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View plantView = inflater.inflate(R.layout.item_location, parent, false);
        ViewHolder viewHolder = new ViewHolder(plantView);
        return viewHolder;

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Location location = locations.get(position);
        String name = location.getName();
        holder.textView_listName.setText(name);
        holder.textView_listType.setText(location.getType());
        switch (location.getType()) {
            case "garden":
                holder.textView_listType.setBackgroundColor(ContextCompat.getColor(context, R.color.garden));
                break;
            case "pool":
                holder.textView_listType.setBackgroundColor(ContextCompat.getColor(context, R.color.pool));
                break;
            case "open water facility":
                holder.textView_listType.setBackgroundColor(ContextCompat.getColor(context, R.color.open_water));
                break;
            case "golf course":
                holder.textView_listType.setBackgroundColor(ContextCompat.getColor(context, R.color.golf));
                break;
            case "dog park":
                holder.textView_listType.setBackgroundColor(ContextCompat.getColor(context, R.color.dog_park));
                break;
            case "national park":
                holder.textView_listType.setBackgroundColor(ContextCompat.getColor(context, R.color.national_park));
                break;
            case "campsite":
                holder.textView_listType.setBackgroundColor(ContextCompat.getColor(context, R.color.campsite));
                break;
            case "beach":
                holder.textView_listType.setBackgroundColor(ContextCompat.getColor(context, R.color.beach));
                break;
            case "city park":
                holder.textView_listType.setBackgroundColor(ContextCompat.getColor(context, R.color.city_park));
                break;
        }

        holder.ratingBar_list.setRating(location.getRating());
        Picasso.get().load(location.getImages().get(0).getUrl()).into(holder.imageView_list);

        if(location.isSaved()){
            Picasso.get().load("file:///android_asset/saved.png").into(holder.imageView_listSaved);
        }
        else{
            Picasso.get().load("file:///android_asset/not_saved.png").into(holder.imageView_listSaved);
        }
        holder.imageView_listSaved.setOnClickListener(v -> {
            if(location.isSaved()){
                location.setSaved(false);
            }
            else{
                location.setSaved(true);
            }
            this.notifyItemChanged(position);
            addLocation(location.getLocation_id(), location.isSaved());
        });

        holder.linearLayout_locationItem.setOnClickListener(v -> {
            Intent intent = new Intent(context, LocationActivity.class);
            intent.putExtra("location_id", location.getLocation_id());
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView textView_listName;
        TextView textView_listType;
        ImageView imageView_list;
        ImageView imageView_listSaved;
        RatingBar ratingBar_list;
        LinearLayout linearLayout_locationItem;

        public ViewHolder(View itemView){
            super(itemView);
            textView_listName = itemView.findViewById(R.id.textView_listName);
            textView_listType = itemView.findViewById(R.id.textView_listType);
            imageView_list = itemView.findViewById(R.id.imageView_list);
            imageView_listSaved = itemView.findViewById(R.id.imageView_listSaved);
            ratingBar_list = itemView.findViewById(R.id.ratingBar_list);
            linearLayout_locationItem = itemView.findViewById(R.id.linearLayout_locationItem);
        }
    }

    public void addLocation(String locationID, Boolean saved){
        sharedPreferences = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        user_id = sharedPreferences.getString("user_id", null);

        if(user_id != null){
            String api = context.getString(R.string.api_root) + "api/user/add/" + user_id;
            JSONObject body = new JSONObject();
            try {
                body.put("locationId", locationID);
                body.put("saved", saved);
                StringEntity entity = new StringEntity(body.toString());
                client.post(context, api, entity, "application/json", new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d("location_added", new String(responseBody));
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
}
