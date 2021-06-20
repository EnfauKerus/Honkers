package uk.enfa.honkers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.enfa.honkers.databinding.ActivityNewPostBinding;

public class NewPostActivity extends AppCompatActivity {

    ActivityNewPostBinding binding;
    MenuItem submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityNewPostBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("New honk");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_post, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.submit:
                submit = item;
                item.setEnabled(false);
                submitPost();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void submitPost(){

        String jwt = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).getString("token", "null");
        String url = getResources().getString(R.string.api_endpoint)+"/timeline/post";
        JSONObject json = new JSONObject();
        try {
            json.put("content", binding.editTextTextMultiLine.getText().toString());
            AppController.getInstance().addToRequestQueue(new JsonObjectRequest(Request.Method.POST, url, json, this::onSubmitSuccess, this::onError){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Authorization", "Bearer " + jwt);
                    return headers;
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    void onSubmitSuccess(JSONObject response){
        Toast.makeText(getApplicationContext(), "Submit successful", Toast.LENGTH_SHORT);
        finish();
    }

    void onError(VolleyError error){
        Toast.makeText(getApplicationContext(), "Failed to submit: " + error.networkResponse.statusCode, Toast.LENGTH_SHORT);
        if(submit!=null)
            submit.setEnabled(true);
    }
}