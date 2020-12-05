package com.luisgenesius.chawy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SignInActivity extends AppCompatActivity {
    private EditText phoneNumberET, codeET;
    private Button sendButton;
    private TextView enterCodeTV;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callBacks;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseApp.initializeApp(this);
        userSuccessSigned();
        initializeLayoutElements();

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verificationId != null) verifyPhoneNumberWithCode();
                else startVerification();
            }
        });

        callBacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signIn(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(SignInActivity.this, "Failed to verify", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verId, forceResendingToken);
                verificationId = verId;
                changeLayoutElements();
                sendButton.setText("Submit Verify Code");
            }
        };
    }

    private void userSuccessSigned() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            Intent intent = new Intent(getApplicationContext(), MainPageActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    }

    private void initializeLayoutElements() {
        phoneNumberET = findViewById(R.id.phoneNumberEditText);
        codeET = (EditText) findViewById(R.id.verificationCodeEditText);
        sendButton = findViewById(R.id.verifyButton);
        enterCodeTV = findViewById(R.id.enterCodeTextView);

        codeET.setVisibility(View.INVISIBLE);
        enterCodeTV.setVisibility(View.INVISIBLE);
    }

    private void changeLayoutElements() {
        phoneNumberET.setEnabled(false);
        phoneNumberET.setClickable(false);
        phoneNumberET.setFocusable(false);
        phoneNumberET.setBackgroundColor(getResources().getColor(R.color.common_google_signin_btn_text_dark_disabled));

        enterCodeTV.setVisibility(View.VISIBLE);
        codeET.setVisibility(View.VISIBLE);
    }

    private void verifyPhoneNumberWithCode() {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, codeET.getText().toString());
        signIn(credential);
    }

    private void signIn(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if(user != null) addUserToDatabase(user);
                }
            }
        });
    }

    private void addUserToDatabase(FirebaseUser user) {
        final DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user").child(user.getUid());
        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("phone", user.getPhoneNumber());
                    userMap.put("name", user.getPhoneNumber());
                    userDB.updateChildren(userMap);
                }
                userSuccessSigned();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void startVerification() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumberET.getText().toString(),
                60,
                TimeUnit.SECONDS,
                this,
                callBacks);
    }
}