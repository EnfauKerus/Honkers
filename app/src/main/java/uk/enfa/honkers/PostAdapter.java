package uk.enfa.honkers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.text.TimeZoneNames;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private JSONArray localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private JSONObject localJson;
        private long id;
        private final View view;
        private final ImageView avatar;
        private final TextView nickname;
        private final TextView username;
        private final TextView timestamp;
        private final TextView content;
        private final Button buttonFav;
        private final Button buttonComment;
        public ViewHolder(View view){
            super(view);
            this.view = view;
            avatar = view.findViewById(R.id.imageViewPostAvatar);
            nickname = view.findViewById(R.id.textViewPostNickname);
            username = view.findViewById(R.id.textViewPostUsername);
            timestamp = view.findViewById(R.id.textViewPostTimestamp);
            content = view.findViewById(R.id.textViewPostContent);
            buttonFav = view.findViewById(R.id.buttonFav);
            buttonComment = view.findViewById(R.id.buttonComment);

            buttonComment.setOnClickListener( v -> {
                Context ctx = view.getContext();
                Intent intent = new Intent(ctx, ShowPostActivity.class);
                intent.putExtra("json", localJson.toString());
                ctx.startActivity(intent);
            });
        }

        public void setContent(JSONObject json) throws JSONException {
            localJson = json;
            id = json.getLong("id");
            username.setText("@" + json.getString("username"));
            nickname.setText(json.getString("nickname"));
            content.setText(json.getString("content"));
            buttonFav.setText(json.getString("fav_count"));
            buttonComment.setText(json.getString("replies_count"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = df.parse(json.getString("date"));
                df.setTimeZone(TimeZone.getDefault());
                timestamp.setText(df.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }


            Context ctx = view.getContext();
            String jwt = ctx.getSharedPreferences("uk.enfa.honkers", Context.MODE_PRIVATE).getString("token", "null");

            AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, ctx.getResources().getString(R.string.api_endpoint) + String.format("/fav/post/%d", id), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    buttonFav.setTag(true);
                    buttonFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_star_24, 0, 0, 0);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    buttonFav.setTag(false);
                    buttonFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_star_border_24, 0, 0, 0);
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();

                    headers.put("Authorization", "Bearer " + jwt);
                    return headers;
                }
            });


            buttonFav.setOnClickListener( v -> {

                int method;
                if((boolean)buttonFav.getTag()){
                    method = Request.Method.DELETE;
                }else{
                    method = Request.Method.POST;
                }
                AppController.getInstance().addToRequestQueue(new StringRequest(method, ctx.getResources().getString(R.string.api_endpoint) + String.format("/fav/post/%d", id),  new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int favCount = Integer.parseInt(buttonFav.getText().toString());
                        if((boolean)buttonFav.getTag()){
                            buttonFav.setTag(false);
                            favCount--;
                            buttonFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_star_border_24, 0, 0, 0);
                        }else{
                            buttonFav.setTag(true);
                            favCount++;
                            buttonFav.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_star_24, 0, 0, 0);
                        }
                        buttonFav.setText(String.valueOf(favCount));
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        HashMap<String, String> headers = new HashMap<String, String>();

                        headers.put("Authorization", "Bearer " + jwt);
                        return headers;
                    }
                });
            });

            avatar.setOnClickListener( v -> {
                Intent profile = new Intent(ctx, ProfileActivity.class);
                try {
                    profile.putExtra("username", json.getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ctx.startActivity(profile);
            });

            String avatarUrl = view.getResources().getString(R.string.api_endpoint)+ String.format("/user/%s/avatar", json.getString("username"));

            AppController.getInstance().getImageLoader().get(avatarUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    avatar.setImageDrawable(new BitmapDrawable(view.getResources(), response.getBitmap()));
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    avatar.setImageResource(R.drawable.ic_honker_user);
                }
            });


        }


    }

    public PostAdapter(JSONArray dataSet) {
        localDataSet = dataSet;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_main, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        try {
            JSONObject current = localDataSet.getJSONObject(position);
            holder.setContent(current);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return localDataSet.length();
    }
}
