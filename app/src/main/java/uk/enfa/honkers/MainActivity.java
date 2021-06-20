package uk.enfa.honkers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import kotlin.jvm.internal.Intrinsics;

public class MainActivity extends AppCompatActivity {
    RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    protected void onResume() {
        updateTimeline();
        super.onResume();
    }

    void updateTimeline(){
        String jwt = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).getString("token", "null");
        Log.d("main", "token: " + jwt);
        AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.GET, getResources().getString(R.string.api_endpoint)+"/timeline/following", null, this::onTimelineReceived, this::onErrorResponse){
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
        } catch (JSONException e) {
            e.printStackTrace();
            toast("Failed to parse timeline");
        }

    }

    void onErrorResponse(VolleyError error){

        toast("Error: "+error.networkResponse.statusCode);
        if(error instanceof AuthFailureError){
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }
    }

    void toast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}