package com.toumal.banglachat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {
private Toolbar myToolbar;
private ImageButton sendMessageImageButton;
private EditText userMessageInput;
private ScrollView myScrollView;
private TextView displayTextMessages;

private FirebaseAuth myAuth;
private DatabaseReference userReference,groupReference,groupMessageKeyReference;

private  String currentGroupName,currentUserId,currentUserName,currentDate,currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();
        myAuth = FirebaseAuth.getInstance();
        currentUserId = myAuth.getCurrentUser().getUid();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupReference = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        InitializeFields();

        GetUserInfo();

        sendMessageImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageToDatabase();
                userMessageInput.setText("");
            }
        });
    }



    private void InitializeFields() {
        myToolbar = findViewById(R.id.group_bar_layout);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageImageButton = findViewById(R.id.send_message);
        userMessageInput = findViewById(R.id.input_group_message);
        displayTextMessages = findViewById(R.id.group_chat_text_display);
        myScrollView = findViewById(R.id.myScrollView);


    }
    private void GetUserInfo() {
        userReference.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void SendMessageToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupReference.push().getKey();
        if (message.isEmpty()){
            Toast.makeText(this, "Please Write the Message First...", Toast.LENGTH_SHORT).show();
        }
        else{
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd, YYYY");
            currentDate = simpleDateFormat.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat simpleTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = simpleTimeFormat.format(callForTime.getTime());
            HashMap<String,Object> groupMessageKey = new HashMap<>();
            groupReference.updateChildren(groupMessageKey);
            groupMessageKeyReference = groupReference.child(messageKey);
            HashMap<String,Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name",currentUserName);
                messageInfoMap.put("message",message);
                messageInfoMap.put("date",currentDate);
                messageInfoMap.put("time",currentTime);

            groupMessageKeyReference.updateChildren(messageInfoMap);
        }
    }
}
