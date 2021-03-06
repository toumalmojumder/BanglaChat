package com.toumal.banglachat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {


    private Button UpdateAccountSetting;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUser;
    private FirebaseAuth myAuth;
    private DatabaseReference rootReference;
    private static final int GalleryPick =1;
    private StorageReference userProfileImageReference;
    private ProgressDialog loadingBar;

    private  Toolbar SettingsToolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        myAuth = FirebaseAuth.getInstance();
        currentUser = myAuth.getCurrentUser().getUid();
        rootReference = FirebaseDatabase.getInstance().getReference();
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);

        UpdateAccountSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gelleryIntent = new Intent();
                gelleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                gelleryIntent.setType("image/*");
                startActivityForResult(gelleryIntent,GalleryPick);

            }
        });

    }



    private void InitializeFields() {
        UpdateAccountSetting = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar =new ProgressDialog(SettingsActivity.this);


        SettingsToolBar = findViewById(R.id.setting_tool_bar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GalleryPick && resultCode ==RESULT_OK && data!=null){
            Uri ImageUri = data.getData();
            CropImage.activity(ImageUri).setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(this);
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait, your profile image is updating...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();
                Uri resultUri = result.getUri();
                Log.d("Profile Image Reference",resultUri.toString());
                StorageReference filePath = userProfileImageReference.child(currentUser+".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this,"Profile Image uploaded Successfully...",Toast.LENGTH_LONG).show();

                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            Log.d("download url",downloadUrl);
                            rootReference.child("Users").child(currentUser).child("image").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(SettingsActivity.this,"Image save in database successfully",Toast.LENGTH_LONG).show();
                                        loadingBar.dismiss();
                                    }
                                    else{
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingsActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else{
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this,"Error: "+message,Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

    }

    private void UpdateSettings() {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();
        // String setimage = ImageVIew.getDrawable();


        if(setUserName.isEmpty()){
            Toast.makeText(SettingsActivity.this, "Please Write your user name . . . ",Toast.LENGTH_LONG).show();

        }
        if(setStatus.isEmpty()){
            Toast.makeText(SettingsActivity.this, "Please Write your Status  . . . ",Toast.LENGTH_LONG).show();

        }
        else{
            HashMap<String,Object> profilMap = new HashMap<>();
            profilMap.put("uid",currentUser);
            profilMap.put("name",setUserName);
            profilMap.put("status",setStatus);
            rootReference.child("Users").child(currentUser).updateChildren(profilMap).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                    String retrieveImage = dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrieveUserName);
                    userStatus.setText(retrieveStatus);
                    Log.d("retrieveImage",retrieveImage);
                    Picasso.get().load(retrieveImage).into(userProfileImage);

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
