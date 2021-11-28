package com.example.asus.chatapp;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity {

    private ImageView ProfileImage;
    private TextView ProfileName;
    private TextView ProfileStatus;
    private Button BtnSendFriendRequest;
    private Button DeclineRequest;

    private DatabaseReference UserReference;

    private String CURRENT_STATE;
    private DatabaseReference FriendRequestReference;

    private FirebaseAuth mAuth;
    private String sender_user_id;
    private String receiver_user_id;

    private DatabaseReference FriendReference;

    private DatabaseReference NotificationReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        NotificationReference = FirebaseDatabase.getInstance().getReference().child("Notifications");
        NotificationReference.keepSynced(true);


        UserReference = FirebaseDatabase.getInstance().getReference().child("Users");

        FriendRequestReference = FirebaseDatabase.getInstance().getReference().child("Friend_Requests");
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();

        FriendReference = FirebaseDatabase.getInstance().getReference().child("Friends");


        receiver_user_id = getIntent().getExtras().get("visit_user_id").toString();

        ProfileImage = (ImageView) findViewById(R.id.display_user_image_profile);
        ProfileName = (TextView) findViewById(R.id.display_user_name_profile);
        ProfileStatus = (TextView) findViewById(R.id.display_user_status_profile);
        BtnSendFriendRequest = (Button) findViewById(R.id.btn_send_friend_req);
        DeclineRequest = (Button) findViewById(R.id.btn_decline_friend_req);


        CURRENT_STATE = "not_friends";


        UserReference.child(receiver_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("user_name").getValue().toString();
                String status = dataSnapshot.child("user_status").getValue().toString();
                String image = dataSnapshot.child("user_image").getValue().toString();

                ProfileName.setText(name);
                ProfileStatus.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.user_default).into(ProfileImage);

                FriendRequestReference.child(sender_user_id)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(receiver_user_id) ) {
                                        String req_type = dataSnapshot.child(receiver_user_id).child("request_type").getValue().toString();

                                        if (req_type.equals("sent")) {
                                            CURRENT_STATE = "request_sent";
                                            BtnSendFriendRequest.setText("Cancel Request");
                                            BtnSendFriendRequest.setBackgroundResource(R.color.RedColor);

                                            DeclineRequest.setVisibility(View.INVISIBLE);
                                            DeclineRequest.setEnabled(false);
                                        }
                                        else if (req_type.equals("received")) {
                                            CURRENT_STATE = "request_received";
                                            BtnSendFriendRequest.setText("Accept Request");

                                            DeclineRequest.setVisibility(View.VISIBLE);
                                            DeclineRequest.setEnabled(true);

                                            DeclineRequest.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    DeclineFriendRequest();
                                                }
                                            });
                                        }
                                    }

                                    else{
                                        FriendReference.child(sender_user_id)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.hasChild(receiver_user_id)){
                                                            CURRENT_STATE = "friends";
                                                            BtnSendFriendRequest.setText("Unfriend");
                                                            BtnSendFriendRequest.setBackgroundResource(R.color.RedColor);

                                                            DeclineRequest.setVisibility(View.INVISIBLE);
                                                            DeclineRequest.setEnabled(false);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                    }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if(!sender_user_id.equals(receiver_user_id)){
            BtnSendFriendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BtnSendFriendRequest.setEnabled(false);

                    if (CURRENT_STATE.equals("not_friends")) {
                        SendFriendRequestToAPerson();
                    }

                    if (CURRENT_STATE.equals("request_sent")) {
                        CancelFriendRequest();
                    }

                    if (CURRENT_STATE.equals("request_received")) {
                        AcceptFriendRequest();
                    }

                    if (CURRENT_STATE.equals("friends")) {
                        UnfriendsFriend();
                    }
                }
            });
        }
        else{
            BtnSendFriendRequest.setVisibility(View.INVISIBLE);
            DeclineRequest.setVisibility(View.INVISIBLE);
        }

        DeclineRequest.setVisibility(View.INVISIBLE);
        DeclineRequest.setEnabled(false);
    }

    private void DeclineFriendRequest() {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                BtnSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                BtnSendFriendRequest.setText("Add Friend");
                                                BtnSendFriendRequest.setBackgroundResource(R.color.GreenColor);

                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                DeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void UnfriendsFriend() {
        FriendReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                BtnSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                BtnSendFriendRequest.setText("Add Friend");
                                                BtnSendFriendRequest.setBackgroundResource(R.color.GreenColor);

                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                DeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptFriendRequest() {
        Calendar CalFordATE = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("d MMMM yyyy");
        final String saveCurrentDate = currentDate.format(CalFordATE.getTime());

        FriendReference.child(sender_user_id).child(receiver_user_id).child("date").setValue(saveCurrentDate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FriendReference.child(receiver_user_id).child(sender_user_id).child("date").setValue(saveCurrentDate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if(task.isSuccessful()){
                                                                                BtnSendFriendRequest.setEnabled(true);
                                                                                CURRENT_STATE = "friends";
                                                                                BtnSendFriendRequest.setText("Unriend");
                                                                                BtnSendFriendRequest.setBackgroundResource(R.color.RedColor);
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void CancelFriendRequest() {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                BtnSendFriendRequest.setEnabled(true);
                                                CURRENT_STATE = "not_friends";
                                                BtnSendFriendRequest.setText("Add Friend");
                                                BtnSendFriendRequest.setBackgroundResource(R.color.GreenColor);

                                                DeclineRequest.setVisibility(View.INVISIBLE);
                                                DeclineRequest.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendFriendRequestToAPerson() {
        FriendRequestReference.child(sender_user_id).child(receiver_user_id).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FriendRequestReference.child(receiver_user_id).child(sender_user_id)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()) {
                                                HashMap<String, String> notificationsData = new HashMap<String, String>();
                                                notificationsData.put("from", sender_user_id);
                                                notificationsData.put("type", "request");

                                                NotificationReference.child(receiver_user_id).push().setValue(notificationsData)
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if(task.isSuccessful()){
                                                                    BtnSendFriendRequest.setEnabled(true);
                                                                    CURRENT_STATE = "request_sent";
                                                                    BtnSendFriendRequest.setText("Cancel Request");
                                                                    BtnSendFriendRequest.setBackgroundResource(R.color.RedColor);

                                                                    DeclineRequest.setVisibility(View.INVISIBLE);
                                                                    DeclineRequest.setEnabled(false);
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
