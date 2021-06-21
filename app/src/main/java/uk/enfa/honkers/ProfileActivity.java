package uk.enfa.honkers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.enfa.honkers.databinding.ActivityProfileBinding;

public class ProfileActivity extends AppCompatActivity {

    ActivityProfileBinding binding;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = getIntent().getStringExtra("username");

        String jwt = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).getString("token", "null");
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, getResources().getString(R.string.api_endpoint) + "/user/" + username, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    binding.textViewProfileUsername.setText("@"+response.getString("username"));
                    binding.textViewProfileNickname.setText(response.getString("nickname"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, this::onErrorResponse){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + jwt);
                return headers;
            }
        });

        String avatarUrl = getResources().getString(R.string.api_endpoint)+ String.format("/user/%s/avatar", username);

        AppController.getInstance().getImageLoader().get(avatarUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                binding.imageViewProfileAvatar.setImageDrawable(new BitmapDrawable(getResources(), response.getBitmap()));
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                binding.imageViewProfileAvatar.setImageResource(R.drawable.ic_honker_user);
            }
        });

        checkFollow();

        binding.buttonFollow.setOnClickListener( v -> {
            int method;
            if((boolean)binding.buttonFollow.getTag()){
                method = Request.Method.DELETE;
            } else method = Request.Method.POST;
            AppController.getInstance().addToRequestQueue(new StringRequest(method, getResources().getString(R.string.api_endpoint) + String.format("/follows/follow/%s", username), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

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
            checkFollow();
        });
    }

    @Override
    protected void onResume() {
        updateTimeline();
        super.onResume();
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    void checkFollow(){
        String jwt = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).getString("token", "null");
        AppController.getInstance().addToRequestQueue(new StringRequest(Request.Method.GET, getResources().getString(R.string.api_endpoint) + String.format("/follows/follow/%s", username), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                binding.buttonFollow.setTag(true);
                binding.buttonFollow.setText("Unfollow");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                binding.buttonFollow.setTag(false);
                binding.buttonFollow.setText("Follow");
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + jwt);
                return headers;
            }
        });
    }

    void updateTimeline(){
        String jwt = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).getString("token", "null");
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, getResources().getString(R.string.api_endpoint)+"/timeline/of/" + username, null, this::onTimelineReceived, this::onErrorResponse){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();

                headers.put("Authorization", "Bearer " + jwt);
                return headers;
            }
        });
    }
    void onTimelineReceived(JSONObject response){
        try {
            JSONArray timeline = response.getJSONArray("timeline");
            PostAdapter pa = new PostAdapter(timeline);
            binding.recyclerViewProfileTimeline.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerViewProfileTimeline.setAdapter(pa);
        } catch (JSONException e) {
            e.printStackTrace();
            toast("Failed to parse timeline.");
        }
    }

    void onErrorResponse(VolleyError error){

        if(error.networkResponse!=null) toast("Error: "+error.networkResponse.statusCode);
        if(error instanceof AuthFailureError){
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
        finish();
    }

    void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}