package com.ariyo.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class SignupActivity extends AppCompatActivity {
TextView txtLogin;
private FirebaseAuth mAuth;
TextInputEditText inpName, inpPhone, inpAge;
Button btnSignup;
FirebaseDatabase database;
private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    String phone, name, age;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        txtLogin=findViewById(R.id.txt_login);

        inpPhone=findViewById(R.id.inp_phone);
        inpName=findViewById(R.id.inp_name);
        inpAge=findViewById(R.id.inp_age);
        txtLogin.setOnClickListener(v -> {
            Intent r=new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(r);
            SignupActivity.this.finish();
        });
        btnSignup=findViewById(R.id.btn_signup);
        btnSignup.setOnClickListener(v -> {
            phone=inpPhone.getText().toString();
            age=inpAge.getText().toString();
            name=inpName.getText().toString();
            if (phone!=null&&age!=null && name!=null){
                if (Integer.parseInt(age)>=13){

                    signUp(phone);
                } else {
                    Toast.makeText(SignupActivity.this, "You Should be older than 13 to chat with girls!!!", Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(SignupActivity.this, "Field Can't be blank", Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void signUp(String phone) {


            Intent r= new Intent(SignupActivity.this, PhoneVerificationActivity.class);
            r.putExtra(ConstantKeys.KEY_PHONE, phone);
            r.putExtra("AddUser", "yes");
            r.putExtra(ConstantKeys.KEY_NAME, name);
            r.putExtra(ConstantKeys.KEY_AGE, age);
            startActivity(r);
            SignupActivity.this.finish();

    }


}