package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView userName,userPName,userStatus, userCountry,userGender,userRelation, userBirth;
    private CircleImageView userPP;
    private FirebaseAuth mAuth;
    private DatabaseReference profileRef;
    private String currentUid;
    private ImageButton xcodeView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        profileRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);

        Toolbar settingsToolbar = (Toolbar)findViewById(R.id.mProfile_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profilim");

        userName = (TextView) findViewById(R.id.mProfile_name);
        userPName = (TextView) findViewById(R.id.mProfile_username);
        userStatus = (TextView) findViewById(R.id.mProfile_status);
        userCountry = (TextView) findViewById(R.id.mProfile_country);
        userGender = (TextView) findViewById(R.id.mProfile_gender);
        userRelation = (TextView) findViewById(R.id.mProfile_relationshipstatus);
        userBirth = (TextView) findViewById(R.id.mProfile_cakeday);
        userPP = (CircleImageView) findViewById(R.id.mProfile_pp);
        xcodeView = (ImageButton)findViewById(R.id.xcode_viewBtn);

        profileRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    String mPP = snapshot.child("profileimage").getValue().toString();
                    String mUserName = snapshot.child("fullname").getValue().toString();
                    String mProfileName = snapshot.child("username").getValue().toString();
                    String mStatus = snapshot.child("status").getValue().toString();
                    String mCountry = snapshot.child("country").getValue().toString();
                    String mGender = snapshot.child("gender").getValue().toString();
                    String mRelation = snapshot.child("relationship_status").getValue().toString();
                    String mBirth = snapshot.child("cakeday").getValue().toString();
                    Picasso.get().load(mPP).placeholder(R.drawable.profile).into(userPP);
                    userName.setText(mUserName);
                    userPName.setText("@" + mProfileName);
                    userStatus.setText(mStatus);
                    userCountry.setText("Ülke : " + mCountry);
                    userGender.setText("Cinsiyet : " + mGender);
                    userRelation.setText("İlişki Durumu : " + mRelation);
                    userBirth.setText("Doğum Tarihi : " + mBirth);


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        xcodeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToGenerateXCode();
            }
        });

    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id== android.R.id.home)
        {
            SendUserToMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ProfileActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToGenerateXCode() {
        Intent xCodeGenerateIntent = new Intent(ProfileActivity.this , GenerateXCodeActivity.class);
        xCodeGenerateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(xCodeGenerateIntent);
        finish();
    }


}