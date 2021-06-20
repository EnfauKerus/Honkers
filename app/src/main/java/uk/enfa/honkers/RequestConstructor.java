package uk.enfa.honkers;

import androidx.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

public class RequestConstructor {
    public volatile static String baseURL;

    public static JsonObjectRequest construct(int method, String endpoint, @Nullable JSONObject jsonRequest, Response.Listener responseListener, Response.ErrorListener errorListener){
        JsonObjectRequest request = new JsonObjectRequest(method, baseURL + endpoint, jsonRequest, responseListener, errorListener);
        return request;
    }

}
