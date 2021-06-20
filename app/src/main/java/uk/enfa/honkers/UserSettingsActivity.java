package uk.enfa.honkers;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import uk.enfa.honkers.databinding.ActivityUserSettingsBinding;

public class UserSettingsActivity extends AppCompatActivity {

    ActivityUserSettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityUserSettingsBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());

        binding.buttonSetAvatar.setOnClickListener(this::setAvatar);
        binding.buttonSetNickname.setOnClickListener(this::setNickname);
        binding.buttonSetPassword.setOnClickListener(this::setPassword);

    }

    private void setAvatar(View view){
        Intent intent = new Intent()
                .setType("image/*")
                .setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == RESULT_OK) {
            Uri selectedfile = data.getData(); //The uri with the location of the file
            String baseURL = getResources().getString(R.string.api_endpoint);


        }
    }

    private void setNickname(View view){
        String jwt = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).getString("token", "null");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your nickname");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (DialogInterface dialog, int which) -> {
            String baseURL = getResources().getString(R.string.api_endpoint);
            JSONObject json = new JSONObject();
            try {
                json.put("nickname", input.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PUT,
                    baseURL + "/user/nickname",
                    json,
                    null
                    , null){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();

                    headers.put("Authorization", "Bearer " + jwt);
                    return headers;
                }
            };
            AppController.getInstance().addToRequestQueue(request);
        });

        builder.setNegativeButton("Cancel", (DialogInterface dialog, int which) -> {
            dialog.cancel();
        });
        builder.show();
    }
    private void setPassword(View view){
        String jwt = getSharedPreferences("uk.enfa.honkers", MODE_PRIVATE).getString("token", "null");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your new password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Update password", (DialogInterface dialog, int which) -> {
            String baseURL = getResources().getString(R.string.api_endpoint);
            JSONObject json = new JSONObject();
            try {
                json.put("password", input.getText().toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.PATCH,
                    baseURL + "/auth/password",
                    json,
                    null
                    , null){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();

                    headers.put("Authorization", "Bearer " + jwt);
                    return headers;
                }
            };
            AppController.getInstance().addToRequestQueue(request);
        });

        builder.setNegativeButton("Cancel", (DialogInterface dialog, int which) -> {
            dialog.cancel();
        });
        builder.show();
    }
}