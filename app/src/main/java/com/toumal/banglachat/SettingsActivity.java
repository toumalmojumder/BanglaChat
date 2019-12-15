package com.toumal.banglachat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button UpdateAccountSetting;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUser;
    private FirebaseAuth myauth;
    private DatabaseReference rootReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });
        
        RetrieveUserInfo();

    }



    private void InitializeFields() {
        UpdateAccountSetting = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        myauth = FirebaseAuth.getInstance();
        currentUser = myauth.getCurrentUser().getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();
        if(setUserName.isEmpty()){
            Toast.makeText(SettingsActivity.this, "Please Write your user name . . . ",Toast.LENGTH_LONG).show();

        }
        if(setStatus.isEmpty()){
            Toast.makeText(SettingsActivity.this, "Please Write your Status  . . . ",Toast.LENGTH_LONG).show();

        }
        else{
            HashMap<String,String> profilMap = new HashMap<>();
            profilMap.put("uid",currentUser);
            profilMap.put("name",setUserName);
            profilMap.put("status",setStatus);
         rootReference.child("Users").child(currentUser).setValue(profilMap).addOnCompleteListener(new OnCompleteListener<Void>() {
             @Override
             public void onComplete(@NonNull Task<Void> task) {
                 if(task.isSuccessful()){
                     Toast.makeText(SettingsActivity.this, "Profile Updated Successfully",Toast.LENGTH_LONG).show();
                     SendUserToMainActivity();
                 }
                 else{
                     String message = task.getException().toString();
                     Toast.makeText(SettingsActivity.this, "Error: "+message,Toast.LENGTH_LONG).show();
                 }
             }
         });
        }
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void RetrieveUserInfo() {
rootReference.child("Users").child(currentUser).addValueEventListener(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&(dataSnapshot.hasChild("image"))){
            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
        //    String retrieveImage = dataSnapshot.child("image").getValue().toString();

            userName.setText(retrieveUserName);
            userStatus.setText(retrieveStatus);

        }
        else if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))){
            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
            String retrieveStatus = dataSnapshot.child("status").getValue().toString();


            userName.setText(retrieveUserName);
            userStatus.setText(retrieveStatus);
        }
        else{
            userName.setVisibility(View.VISIBLE);
            Toast.makeText(SettingsActivity.this, "Please set & Update your profile information...",Toast.LENGTH_LONG).show();

        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError databaseError) {

    }
});
    }

}
