package uk.enfa.honkers;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import uk.enfa.honkers.databinding.ActivityShowPostBinding;

public class ShowPostActivity extends AppCompatActivity {

    ActivityShowPostBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityShowPostBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
        getSupportActionBar().setTitle("Honk of " + getIntent().getStringExtra("nickname"));
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Toast.makeText(this, String.valueOf(getIntent().getIntExtra("id", 0)), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}