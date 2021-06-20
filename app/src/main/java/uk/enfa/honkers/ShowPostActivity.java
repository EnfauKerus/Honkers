package uk.enfa.honkers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import uk.enfa.honkers.databinding.ActivityShowPostBinding;

public class ShowPostActivity extends AppCompatActivity {

    ActivityShowPostBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityShowPostBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());


        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.floatingActionButtonReply.setOnClickListener( v -> {
            Intent intent = new Intent(this, NewPostActivity.class);
            intent.putExtra("reply_to", getIntent().getLongExtra("id", 0));
            startActivity(intent);
        });
        Intent intent = getIntent();
        try {
            JSONObject json = new JSONObject(intent.getStringExtra("json"));
            getSupportActionBar().setTitle("Honk of " + json.getString("nickname"));
            binding.postMain.textViewPostNickname.setText(json.getString("nickname"));
            binding.postMain.textViewPostContent.setText(json.getString("content"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = df.parse(json.getString("date"));
                df.setTimeZone(TimeZone.getDefault());
                binding.postMain.textViewPostTimestamp.setText(df.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            binding.postMain.textViewPostUsername.setText(json.getString("username"));
            binding.postMain.buttonFav.setText(String.valueOf(json.getInt("fav_count")));
            binding.postMain.buttonComment.setVisibility(View.INVISIBLE);
            getResponses(json.getLong("id"));
            String avatarUrl = getResources().getString(R.string.api_endpoint)+ String.format("/user/%s/avatar", json.getString("username"));

            AppController.getInstance().getImageLoader().get(avatarUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                    binding.postMain.imageViewPostAvatar.setImageDrawable(new BitmapDrawable(getResources(), response.getBitmap()));
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    binding.postMain.imageViewPostAvatar.setImageResource(R.drawable.ic_honker_user);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


    void getResponses(long id){
        String jwt = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).getString("token", "null");
        String url = getResources().getString(R.string.api_endpoint) + String.format("/timeline/%d/responses", id);
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, url, null, this::onRecieveReplies, this::onError){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + jwt);
                return headers;
            }
        });
    }


    void onRecieveReplies(JSONObject response){
        try {
            JSONArray timeline = response.getJSONArray("responses");
            PostReplyAdapter pra = new PostReplyAdapter(timeline);
            binding.recyclerResponses.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerResponses.setAdapter(pra);
        } catch (JSONException e) {
            e.printStackTrace();
            toast("Failed to parse timeline.");
        }


    }


    void onError(VolleyError error){
        toast("Failed to submit: " + error.networkResponse.statusCode);
    }

    void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}