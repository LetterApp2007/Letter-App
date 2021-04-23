package com.ariyo.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PhoneVerificationActivity extends AppCompatActivity {
Button verifyBtn;
EditText inpOtp;
String name;
String phone;
ProgressBar pd;
String verificationCodeBySystem;
FirebaseAuth mAuth;
PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]

        verifyBtn=findViewById(R.id.verify_btn);
        inpOtp=findViewById(R.id.inp_otp);
        pd=findViewById(R.id.progressBar);
        phone= getIntent().getStringExtra(ConstantKeys.KEY_PHONE);


        sendVerificationCode(phone);

        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = inpOtp.getText().toString();

                if (code.isEmpty() || code.length() < 6) {
                    inpOtp.setError("Wrong OTP...");
                    inpOtp.requestFocus();
                    return;
                }
                pd.setVisibility(View.VISIBLE);
                verifyCode(code);
            }
        });

    }

    private void sendVerificationCode(String phone) {
        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                Toast.makeText(PhoneVerificationActivity.this, "OTP Sent", Toast.LENGTH_SHORT).show();
                verificationCodeBySystem = s;
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    pd.setVisibility(View.VISIBLE);
                    verifyCode(code);
                }


            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(PhoneVerificationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

            }
        };
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phone)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(PhoneVerificationActivity.this)                 // Activity (for callback binding)
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void verifyCode(String codeByUser) {

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, codeByUser);
        signInTheUserByCredentials(credential);

    }

    private void signInTheUserByCredentials(PhoneAuthCredential credential) {

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(PhoneVerificationActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {


                            String AddUser=getIntent().getStringExtra("AddUser");
                            if (AddUser!=null){
//                                SignUp
                                String name=getIntent().getStringExtra(ConstantKeys.KEY_NAME);
                                String age=getIntent().getStringExtra(ConstantKeys.KEY_AGE);
                                String uid = mAuth.getCurrentUser().getUid();

                                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

                                reference.getRef().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Map<String, String> user = (Map<String, String>) snapshot.getValue();
                                        try {
                                            String dbname=user.get(ConstantKeys.KEY_NAME);
                                            if (dbname!=null){
                                                Toast.makeText(PhoneVerificationActivity.this, "User Already Exits!!", Toast.LENGTH_LONG).show();
                                                Intent r = new Intent(PhoneVerificationActivity.this, LoginActivity.class);
                                                r.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(r);
                                                mAuth.getCurrentUser().delete();
                                            }

                                        }catch (Exception a){
                                            addUser(uid, phone, name, age);
                                            Intent r = new Intent(PhoneVerificationActivity.this, MainActivity.class);
                                            r.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(r);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                        Toast.makeText(PhoneVerificationActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                });


                            } else{
                                //Login
                                String uid=mAuth.getCurrentUser().getUid();
                                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");

                                reference.getRef().child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Map<String, String> user = (Map<String, String>) snapshot.getValue();
                                        try {
                                            name=user.get(ConstantKeys.KEY_NAME);
                                            if (name!=null){
                                                Log.d("Debug", name);
                                                Intent r = new Intent(PhoneVerificationActivity.this, MainActivity.class);
                                                r.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(r);
                                            } else {
                                                mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Intent r = new Intent(PhoneVerificationActivity.this, LoginActivity.class);
                                                            r.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(r);
                                                        }
                                                    }
                                                });
                                                pd.setVisibility(View.INVISIBLE);
                                            }
                                        } catch (Exception e){
                                            Toast.makeText(PhoneVerificationActivity.this, "User Does not exists idiot!!", Toast.LENGTH_LONG).show();
                                            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        Intent r = new Intent(PhoneVerificationActivity.this, LoginActivity.class);
                                                        r.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(r);
                                                    }
                                                }
                                            });
                                            pd.setVisibility(View.INVISIBLE);


                                        }



                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        mAuth.getCurrentUser().delete();
                                        Toast.makeText(PhoneVerificationActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                                        Intent r = new Intent(PhoneVerificationActivity.this, LoginActivity.class);
                                        r.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(r);

                                    }
                                });

                            }

                        } else {
                            Toast.makeText(PhoneVerificationActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(PhoneVerificationActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addUser(String uid, String phone, String name, String age) {
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Users");
        Map<String, String> userJson=new HashMap<>() ;
        userJson.put(ConstantKeys.KEY_NAME, name);
        userJson.put(ConstantKeys.KEY_AGE, age);
        userJson.put(ConstantKeys.KEY_PHONE, phone);
        userJson.put(ConstantKeys.KEY_UID, uid);
        reference.child(uid).setValue(userJson).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Intent r=new Intent(PhoneVerificationActivity.this, MainActivity.class);
                    startActivity(r);
                    PhoneVerificationActivity.this.finish();
                }else {
                    Toast.makeText(PhoneVerificationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
