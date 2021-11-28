package com.example.asus.chatapp;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>{

    private List<Messages> userMessagesList;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersDatabaseReference;

    public MessagesAdapter(List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }


    @Override
    public MessagesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_user_layout, parent, false);


        mAuth = FirebaseAuth.getInstance();


        return new MessagesViewHolder(v);

    }


    @Override
    public void onBindViewHolder(MessagesViewHolder holder, int position) {

        String message_sender_id = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        String fromUserId = messages.getFrom();
        String fromMessageType = messages.getType();

        UsersDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        UsersDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("user_name").getValue().toString();
                String userImage = dataSnapshot.child("user_thumb_image").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (fromMessageType.equals("text")){

            holder.messageImage.setVisibility(View.INVISIBLE);

            if (message_sender_id.equalsIgnoreCase(fromUserId)){

                holder.messageText.setBackgroundResource(R.drawable.messages_sender_background);

                holder.rChats.setGravity(Gravity.RIGHT);


            }
            else {
                holder.messageText.setBackgroundResource(R.drawable.messages_background);

                holder.rChats.setGravity(Gravity.LEFT);
            }


            holder.messageText.setText(messages.getMessage());
        }
        else{

            holder.messageText.setVisibility(View.INVISIBLE);

            if (message_sender_id.equalsIgnoreCase(fromUserId)){
                Picasso.with(holder.messageImage.getContext()).load(messages.getMessage())
                        .into(holder.messageImage);

                holder.messageImage.setBackgroundResource(R.drawable.messages_sender_background);
                holder.rChats.setGravity(Gravity.RIGHT);
            }
            else {

                Picasso.with(holder.messageImage.getContext()).load(messages.getMessage())
                        .into(holder.messageImage);

                holder.messageImage.setBackgroundResource(R.drawable.messages_background);
                holder.rChats.setGravity(Gravity.LEFT);

            }

        }

    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    public class MessagesViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView userProfileImage;
        public RelativeLayout rChats;
        public ImageView messageImage;


        @SuppressLint("ResourceType")
        public MessagesViewHolder(View view) {
            super(view);

            messageText = (TextView) view.findViewById(R.id.message_text);
//            userProfileImage = (CircleImageView) view.findViewById(R.id.messages_profile_image);
            rChats = (RelativeLayout) view.findViewById(R.id.LayoutContent);
            messageImage = (ImageView) view.findViewById(R.id.message_image);

        }
    }
}
