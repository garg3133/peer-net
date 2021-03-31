package com.iiitdmj.peer_net.Activites;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.iiitdmj.peer_net.Models.User;
import com.iiitdmj.peer_net.databinding.ActivityTempAuthBinding;

import java.util.concurrent.TimeUnit;

public class TempAuth extends AppCompatActivity {

    ActivityTempAuthBinding binding;
    FirebaseAuth auth;
    FirebaseDatabase database;
    String verificationId;
    String OTP = new String("123456");
    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTempAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dialog = new ProgressDialog(this);
        dialog.setMessage("Registering ...");
        dialog.setCancelable(false);
        dialog.show();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        getSupportActionBar().hide();

        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        binding.continueBtn.setText("Continue " + phoneNumber);

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(auth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(TempAuth.this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {

                            }

                            @Override
                            public void onCodeSent(@NonNull String verifyId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                super.onCodeSent(verifyId, forceResendingToken);
                                verificationId = verifyId;
                                dialog.dismiss();
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);

        binding.continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, OTP);
                auth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
//                            Toast.makeText(TempAuth.this, "Success", Toast.LENGTH_SHORT).show();
                            String uuid = auth.getUid();
                            String phone = auth.getCurrentUser().getPhoneNumber();
                            String name = binding.name.getText().toString();

                            User user = new User(uuid, name, phone, "");
                            database.getReference()
                                    .child("users")
                                    .child(uuid)
                                    .setValue(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Intent intent = new Intent(TempAuth.this, MainActivity.class);
                                            startActivity(intent);
                                            finishAffinity();
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(TempAuth.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
}