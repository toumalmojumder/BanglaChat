package com.toumal.banglachat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseUser currentUser;
    private Button loginButton;
    private EditText userEmail,userPassword;
    private TextView needNewAccountLink, forgetPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginButton = findViewById(R.id.login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        needNewAccountLink = findViewById(R.id.need_new_account);
        forgetPasswordLink = findViewById(R.id.forgot_password_link);

        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });
    }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(currentUser != null){
            SendUserToMainActivity();
        }

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        startActivity(mainIntent);
    }
}
