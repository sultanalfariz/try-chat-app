package com.example.asus.chatapp;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private TabsPagerAdapter mTabsPagerAdapter;

    FirebaseUser currentUser;
    private DatabaseReference UsersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();


        if (currentUser != null){

            String online_user_id = mAuth.getCurrentUser().getUid();

            UsersReference = FirebaseDatabase.getInstance().getReference().child("Users").child(online_user_id);

        }


        //Tabs MainActivity
        mViewPager = (ViewPager) findViewById(R.id.main_tabs_pager);
        mTabsPagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mTabsPagerAdapter);
        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

        mToolbar = (Toolbar) findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chat App");
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        if(currentUser == null){

            LogOutUser();

        }
        else if (currentUser != null){
            UsersReference.child("online").setValue("true");
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (currentUser != null){
            UsersReference.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    private void LogOutUser() {
        Intent startPageIntent = new Intent(MainActivity.this, StartPageActivity.class);
        startPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startPageIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_button){

            if (currentUser != null){
                UsersReference.child("online").setValue(ServerValue.TIMESTAMP);
            }

            mAuth.signOut();

            LogOutUser();

        }

        if(item.getItemId() == R.id.main_all_user_button){

            Intent allUserIntent = new Intent(MainActivity.this, AllUserActivity.class);
            startActivity(allUserIntent);

        }

        if(item.getItemId() == R.id.main_account_setting_button){

            Intent settingIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingIntent);

        }

        return true;
    }
}
