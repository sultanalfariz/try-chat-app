package com.example.asus.chatapp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private View myMainView;

    private DatabaseReference FriendsReference;
    private FirebaseAuth mAuth;
    private DatabaseReference UsersReference;

    private RecyclerView myChatsList;

    String online_user_id;


    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myMainView =  inflater.inflate(R.layout.fragment_chats, container, false);

        myChatsList = (RecyclerView) myMainView.findViewById(R.id.chats_list);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        FriendsReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);

        UsersReference = FirebaseDatabase.getInstance().getReference().child("Users");



        myChatsList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        myChatsList.setLayoutManager(linearLayoutManager);



        return myMainView;
    }




    @Override
    public void onStart() {

        super.onStart();


        FirebaseRecyclerAdapter<Chats, ChatsFragment.ChatsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Chats, ChatsViewHolder>
                (
                        Chats.class,
                        R.layout.all_user_display_layout,
                        ChatsFragment.ChatsViewHolder.class,
                        FriendsReference
                ) {
            @Override
            protected void populateViewHolder(final ChatsFragment.ChatsViewHolder viewHolder, Chats model, int position) {

                final String list_user_id = getRef(position).getKey();


                UsersReference.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("user_name").getValue().toString();
                        String thumbImage = dataSnapshot.child("user_thumb_image").getValue().toString();

                        String userStatus = dataSnapshot.child("user_status").getValue().toString();


                        if (dataSnapshot.hasChild("online")){
                            String online_status = (String) dataSnapshot.child("online").getValue().toString();

                            viewHolder.setOnlineStatus(online_status);
                        }


                        viewHolder.setUserName(userName);
                        viewHolder.setThumbImage(thumbImage, getContext());

                        viewHolder.setUserStatus(userStatus);


                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (dataSnapshot.child("online").exists()){
                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", list_user_id);
                                    chatIntent.putExtra("user_name", userName);
                                    startActivity(chatIntent);
                                }
                                else {
                                    UsersReference.child(list_user_id).child("online").setValue(ServerValue.TIMESTAMP)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                                    chatIntent.putExtra("visit_user_id", list_user_id);
                                                    chatIntent.putExtra("user_name", userName);
                                                    startActivity(chatIntent);
                                                }
                                            });
                                }
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        myChatsList.setAdapter(firebaseRecyclerAdapter);
    }




    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        View mView;

        public ChatsViewHolder(View itemView){
            super(itemView);

            mView = itemView;
        }

        public void setUserName(String userName) {
            TextView userNameDisplay = (TextView) mView.findViewById(R.id.all_user_username);
            userNameDisplay.setText(userName);
        }

        public void setThumbImage(String thumbImage, Context ctx) {
            CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_user_profile_image);
            Picasso.with(ctx).load(thumbImage).placeholder(R.drawable.user_default).into(thumb_image);
        }

        public void setOnlineStatus(String online_status) {
            ImageView onlineStatusImage = (ImageView) mView.findViewById(R.id.img_user_online);
            TextView onlineStatusUser = (TextView) mView.findViewById(R.id.user_online);

            onlineStatusImage.setVisibility(View.VISIBLE);
            onlineStatusUser.setVisibility(View.VISIBLE);

            if (online_status.equals("true")){
                onlineStatusImage.setImageResource(R.drawable.online);
                onlineStatusUser.setText("Online");
            }
            else{
                onlineStatusImage.setImageResource(R.drawable.offline);
                onlineStatusUser.setText("Offline");
            }
        }

        public void setUserStatus(String userStatus) {
            TextView user_status = (TextView) mView.findViewById(R.id.all_user_status);
            user_status.setText(userStatus);
        }
    }

}
