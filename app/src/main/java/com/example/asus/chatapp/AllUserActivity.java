package com.example.asus.chatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllUserActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView allUserList;
    private DatabaseReference allDatabaseUserRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_user);

        mToolbar = (Toolbar) findViewById(R.id.all_user_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        allUserList = (RecyclerView) findViewById(R.id.all_user_list);
        allUserList.setHasFixedSize(true);
        allUserList.setLayoutManager(new LinearLayoutManager(this));

        allDatabaseUserRef = FirebaseDatabase.getInstance().getReference().child("Users");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<AllUser, AllUsersViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<AllUser, AllUsersViewHolder>(
                        AllUser.class,
                        R.layout.all_user_display_layout,
                        AllUsersViewHolder.class,
                        allDatabaseUserRef)
        {
                @Override
                protected void populateViewHolder(AllUsersViewHolder viewHolder, AllUser model, final int position) {
                    viewHolder.setUser_name(model.getUser_name());
                    viewHolder.setUser_status(model.getUser_status());
                    viewHolder.setUser_thumb_image(getApplicationContext(), model.getUser_thumb_image());

                    viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String visit_user_id = getRef(position).getKey();

                            Intent profileIntent = new Intent(AllUserActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("visit_user_id", visit_user_id);
                            startActivity(profileIntent);
                        }
                    });
                }
        };

        allUserList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class AllUsersViewHolder extends RecyclerView.ViewHolder{
        View mView;

        public AllUsersViewHolder(View itemView){
            super(itemView);

            mView = itemView;
        }

        public void setUser_name(String user_name){
            TextView name = (TextView) mView.findViewById(R.id.all_user_username);
            name.setText(user_name);
        }

        public void setUser_status(String user_status){
            TextView status = (TextView) mView.findViewById(R.id.all_user_status);
            status.setText(user_status);
        }

        public void setUser_thumb_image(Context ctx, String user_thumb_image){
            CircleImageView thumb_image = (CircleImageView) mView.findViewById(R.id.all_user_profile_image);
            Picasso.with(ctx).load(user_thumb_image).placeholder(R.drawable.user_default).into(thumb_image);
        }
    }
}
