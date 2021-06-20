package uk.enfa.honkers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PostReplyAdapter extends RecyclerView.Adapter<PostReplyAdapter.ViewHolder> {

    private JSONArray localDataSet;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private long id;
        private final ImageView avatar;
        private final TextView nickname;
        private final TextView username;
        private final TextView timestamp;
        private final TextView content;

        public ViewHolder(View view){
            super(view);
            avatar = view.findViewById(R.id.imageViewPostReplyAvatar);
            nickname = view.findViewById(R.id.textViewPostReplyNickname);
            username = view.findViewById(R.id.textViewPostReplyUsername);
            timestamp = view.findViewById(R.id.textViewPostReplyTimestamp);
            content = view.findViewById(R.id.textViewPostReplyContent);
        }

        public void setContent(JSONObject json) throws JSONException {
            id = json.getLong("id");
            username.setText("@" + json.getString("username"));
            nickname.setText(json.getString("nickname"));
            content.setText(json.getString("content"));
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date date = df.parse(json.getString("date"));
                df.setTimeZone(TimeZone.getDefault());
                timestamp.setText(df.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }


        }


    }

    public PostReplyAdapter(JSONArray dataSet) {
        localDataSet = dataSet;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_reply, parent, false);
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
