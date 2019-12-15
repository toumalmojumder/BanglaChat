package com.toumal.banglachat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class GroupChatActivity extends AppCompatActivity {
private Toolbar myToolbar;
private ImageButton sendMessageImageButton;
private EditText userMessageInput;
private ScrollView myScrollView;
private TextView displayTextMessages;
private  String currentGroupName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();
        InitializeFields();
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
}
