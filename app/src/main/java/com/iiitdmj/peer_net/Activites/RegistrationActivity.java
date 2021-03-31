package com.iiitdmj.peer_net.Activites;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.iiitdmj.peer_net.databinding.ActivityRegistrationBinding;

public class RegistrationActivity extends AppCompatActivity {

    ActivityRegistrationBinding binding;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
//        if (auth.getCurrentUser() != null) {
//            Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }

        getSupportActionBar().hide();
        binding.usernameTextBox.requestFocus();
        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, TempAuth.class);
                intent.putExtra("phoneNumber", binding.usernameTextBox.getText().toString());
                startActivity(intent);
            }
        });
    }
}