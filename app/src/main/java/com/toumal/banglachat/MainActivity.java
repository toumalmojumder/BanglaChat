package com.toumal.banglachat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {


    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;


    private FirebaseAuth myauth;
    private DatabaseReference rootReference;
    private String currentUserId;

    private View dialogueView;

    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myauth = FirebaseAuth.getInstance();


        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Bangla Chat");//Add Title in this code...

        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
        rootReference = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = myauth.getCurrentUser();
        if(currentUser == null){
            SendUserToLoginActivity();
        }
        else{
            updateUserStatus("online");
            VerifyUserExistance();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = myauth.getCurrentUser();
        if(currentUser != null){
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = myauth.getCurrentUser();
        if(currentUser != null){
            updateUserStatus("offline");
        }
    }

    private void VerifyUserExistance() {
        String currentUserID = myauth.getCurrentUser().getUid();
        rootReference.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if((dataSnapshot.child("name").exists())){
                   // Toast.makeText(MainActivity.this,"Welcome",Toast.LENGTH_LONG).show();
                }
                else{
                    SendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.main_logout_option){
             updateUserStatus("offline");
             myauth.signOut();
             SendUserToLoginActivity();
         }
        if(item.getItemId()==R.id.main_find_friends_option){
            SendUserToFindFriendActivity();

        }
        if(item.getItemId()==R.id.main_settings_option){
            SendUserToSettingsActivity();
        }
        if(item.getItemId()==R.id.main_create_group_option){
            RequestNewGroup();
        }


         return true;
    }

    private void RequestNewGroup() {
        AlertDialog.Builder builder  = new AlertDialog.Builder(MainActivity.this);

        TextView cancel,create;
        final TextInputEditText groupname;

        dialogueView = getLayoutInflater().inflate(R.layout.create_group, null);

        cancel = dialogueView.findViewById(R.id.cancelbutton);
        create = dialogueView.findViewById(R.id.createbutton);
        groupname = dialogueView.findViewById(R.id.groupname);


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String groupnametext = groupname.getText().toString();
                if(groupnametext.isEmpty()){
                    groupname.setError("Field Can Not Be Empty!");
                    groupname.requestFocus();
                }else {
                    CreateNewGroup(groupnametext);

                }
            }
        });

        builder.setView(null);
        builder.setView(dialogueView);
        alertDialog=builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.dismiss();
        alertDialog.show();
    }

    private void CreateNewGroup(final String groupName) {
        rootReference.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this,groupName+" group is created successfully...",Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();

                }
            }
        });
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }
    private void SendUserToFindFriendActivity() {
        Intent findFriendIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendIntent);

    }

    private void updateUserStatus(String state){
        String saveCurrentTime,saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        currentUserId = myauth.getCurrentUser().getUid();
        rootReference.child("Users").child(currentUserId).child("userState").updateChildren(onlineStateMap);
    }
}
