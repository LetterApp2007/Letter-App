package com.ariyo.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Field;

public class LoginActivity extends AppCompatActivity {
TextView txtSignup;
FirebaseAuth mAuth;
String phone;
TextInputLayout textInputLayout;
TextInputEditText inpPhone;
Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();
        textInputLayout = findViewById(R.id.lay_phone);
        inpPhone=findViewById(R.id.inp_phone);
        btnLogin=findViewById(R.id.btn_login);
        txtSignup=findViewById(R.id.txt_signup);
        txtSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent r=new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(r);
                LoginActivity.this.finish();
            }
        });
        btnLogin.setOnClickListener(v-> {
            textInputLayout.setError(null);
            phone=inpPhone.getText().toString();
            if (!phone.equals("")) {
                if (!phone.startsWith("+91"))
                    phone = "+91" + phone;
                phone = MyClass.removeSpaces(phone);
                if (phone.length() != 13)
                    textInputLayout.setError("Invalid phone number");
                else
                    login(phone);
            }
            else {
                textInputLayout.setError("Field cannot be empty");
            }
        });
        if (mAuth.getCurrentUser()!=null ) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            LoginActivity.this.finish();
        }

    }

    private void login(String phone) {
        Intent r= new Intent(LoginActivity.this, PhoneVerificationActivity.class);
        r.putExtra(ConstantKeys.KEY_PHONE, phone);
        startActivity(r);
    }
}