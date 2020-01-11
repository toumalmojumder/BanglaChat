package com.toumal.banglachat;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends Fragment {
    private View ContactsView;
    private RecyclerView myContactsList;

    private DatabaseReference contactsReference,userReference;
    private FirebaseAuth myAuth;

    private String currentUserID;
    public ContactsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        ContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);
        myContactsList = ContactsView.findViewById(R.id.contactsList);
        myContactsList.setLayoutManager(new LinearLayoutManager(getContext()));
        myAuth =FirebaseAuth.getInstance();
        currentUserID = myAuth.getCurrentUser().getUid();
        contactsReference = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");






        return ContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsReference,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,ContactViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactViewHolder holder, int i, @NonNull Contacts contacts) {
                String usersID= getRef(i).getKey();
                userReference.child(usersID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                       if(dataSnapshot.exists()){
                           if (dataSnapshot.child("userState").hasChild("state"))
                           {
                               String state = dataSnapshot.child("userState").child("state").getValue().toString();
                               String date = dataSnapshot.child("userState").child("date").getValue().toString();
                               String time = dataSnapshot.child("userState").child("time").getValue().toString();

                               if (state.equals("online"))
                               {
                                   holder.onlineIcon.setVisibility(View.VISIBLE);
                               }
                               else if (state.equals("offline"))
                               {
                                  // holder.userStatus.setText("Last Seen: " + date + " " + time);
                                   holder.onlineIcon.setVisibility(View.INVISIBLE);

                               }
                           }
                           else
                           {
                               holder.onlineIcon.setVisibility(View.INVISIBLE);
                               holder.userStatus.setText("offline");
                           }
                           if(dataSnapshot.hasChild("image")){
                               String userImage = dataSnapshot.child("image").getValue().toString();
                               String profileStatus = dataSnapshot.child("status").getValue().toString();
                               String profileName = dataSnapshot.child("name").getValue().toString();


                               holder.userName.setText(profileName);
                               holder.userStatus.setText(profileStatus);
                               Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);

                           }
                           else{
                               String profileStatus = dataSnapshot.child("status").getValue().toString();
                               String profileName = dataSnapshot.child("name").getValue().toString();
                               holder.userName.setText(profileName);
                               holder.userStatus.setText(profileStatus);
                           }
                       }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                ContactViewHolder viewHolder = new ContactViewHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }
    public static class ContactViewHolder extends RecyclerView.ViewHolder{

        TextView userName,userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.userProfileName);
            userStatus = itemView.findViewById(R.id.user_Status);
            profileImage = itemView.findViewById(R.id.usersProfileImage);
            onlineIcon = itemView.findViewById(R.id.userOnlineStatus);
        }
    }
}
