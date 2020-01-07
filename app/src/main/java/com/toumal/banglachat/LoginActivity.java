package com.toumal.banglachat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth myauth;
    private DatabaseReference userReference;

    private Button loginButton,phoneLogin;
    private EditText userEmail,userPassword;
    private TextView needNewAccountLink, forgetPasswordLink;

    private ProgressDialog loadingBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        myauth =FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");


        loginButton = findViewById(R.id.login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        needNewAccountLink = findViewById(R.id.need_new_account);
        forgetPasswordLink = findViewById(R.id.forgot_password_link);
        phoneLogin = findViewById(R.id.phone_login_button);

        phoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent phoneLoginIntent = new Intent(LoginActivity.this,PhoneLoginActivity.class);
                startActivity(phoneLoginIntent);
            }
        });
        loadingBar = new ProgressDialog(this);

        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });
    }

    private void AllowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if(email.isEmpty()){
            Toast.makeText(this, "Please enter Email",Toast.LENGTH_LONG).show();
        }
        if(password.isEmpty()){
            Toast.makeText(this, "Please enter Password",Toast.LENGTH_LONG).show();
        }

        else{

            loadingBar.setTitle("Sign In");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            myauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        String currentUserId = myauth.getCurrentUser().getUid();
                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                        userReference.child(currentUserId).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    SendUserToMainActivity();
                                    Toast.makeText(LoginActivity.this, "Logged in Successfull...",Toast.LENGTH_LONG).show();
                                    loadingBar.dismiss();
                                }
                            }
                        });


                    }
                    else{
                        String message = task.getException().toString();
                        Toast.makeText(LoginActivity.this, "Error : "+message,Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();

                    }
                }
            });
        }

        }

    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if(currentUser != null){
//            SendUserToMainActivity();
//        }
//
//    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
