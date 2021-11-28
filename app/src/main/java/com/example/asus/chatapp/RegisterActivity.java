package com.example.asus.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference storeUserDefaultDataReference;

    private Toolbar mToolbar;

    private EditText RegisterUserName;
    private EditText RegisterUserEmail;
    private EditText RegisterUserPassword;

    private Button CreateAccountButton;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Sign Up");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RegisterUserName = (EditText) findViewById(R.id.edtNama);
        RegisterUserEmail = (EditText) findViewById(R.id.edtEmail);
        RegisterUserPassword = (EditText) findViewById(R.id.edtPassword);

        CreateAccountButton = (Button) findViewById(R.id.btnRegister);

        loadingBar = new ProgressDialog(this);

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String nama = RegisterUserName.getText().toString();
                String email = RegisterUserEmail.getText().toString();
                String password = RegisterUserPassword.getText().toString();

                RegisterAccount(nama, email, password);
            }

            private void RegisterAccount(final String nama, String email, String password) {
                if(TextUtils.isEmpty(nama)){
                    Toast.makeText(RegisterActivity.this, "Please write your name.", Toast.LENGTH_LONG).show();
                }
                if(TextUtils.isEmpty(email)){
                    Toast.makeText(RegisterActivity.this, "Please write your email.", Toast.LENGTH_LONG).show();
                }
                if(TextUtils.isEmpty(password)){
                    Toast.makeText(RegisterActivity.this, "Please write your password.", Toast.LENGTH_LONG).show();
                }
                else {
                    loadingBar.setTitle("Creating New Account");
                    loadingBar.setMessage("Please wait...");
                    loadingBar.show();

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        String DeviceToken = FirebaseInstanceId.getInstance().getToken();

                                        String current_user_id = mAuth.getCurrentUser().getUid();
                                        storeUserDefaultDataReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);

                                        storeUserDefaultDataReference.child("user_name").setValue(nama);
                                        storeUserDefaultDataReference.child("user_status").setValue("Hey there, i am using Chat app.");
                                        storeUserDefaultDataReference.child("user_image").setValue("user_default");
                                        storeUserDefaultDataReference.child("device_token").setValue(DeviceToken);
                                        storeUserDefaultDataReference.child("user_thumb_image").setValue("default_image")
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
                                                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                            startActivity(mainIntent);
                                                            finish();
                                                        }
                                                    }
                                                });
                                    }
                                    else{
                                        Toast.makeText(RegisterActivity.this, "Error Occured, Try Again...",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    loadingBar.dismiss();
                                }
                            });
                }
            }
        });

    }
}
