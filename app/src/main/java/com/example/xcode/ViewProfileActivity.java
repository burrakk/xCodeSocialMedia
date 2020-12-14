package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private TextView userName,userPName,userStatus, userCountry,userGender,userRelation, userBirth;
    private CircleImageView userPP;
    private FirebaseAuth mAuth;
    private Toolbar pToolbar;
    private DatabaseReference profileRef,FriendReqRef,FriendsRef;
    private String currentUid,selectedUid,current_state,saveCurrentDate,saveCurrentTime;
    private Button sendFriendRequestBtn, declineFriendRequestBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        Toolbar pToolbar = (Toolbar) findViewById(R.id.vProfile_toolbar);
        setSupportActionBar(pToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profil");

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        selectedUid = getIntent().getExtras().get("selected_uid").toString();
        profileRef = FirebaseDatabase.getInstance().getReference().child("Users");
        FriendReqRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef= FirebaseDatabase.getInstance().getReference().child("Friends");
        userName = (TextView) findViewById(R.id.vProfile_name);
        userPName = (TextView) findViewById(R.id.vProfile_username);
        userStatus = (TextView) findViewById(R.id.vProfile_status);
        userCountry = (TextView) findViewById(R.id.vProfile_country);
        userGender = (TextView) findViewById(R.id.vProfile_gender);
        userRelation = (TextView) findViewById(R.id.vProfile_relationshipstatus);
        userBirth = (TextView) findViewById(R.id.vProfile_cakeday);
        userPP = (CircleImageView) findViewById(R.id.vProfile_pp);
        sendFriendRequestBtn = (Button) findViewById(R.id.vProfile_sendfriendrequestBtn);
        declineFriendRequestBtn = (Button) findViewById(R.id.vProfile_declinefriendrequestBtn);

        current_state = "not_friends";

        profileRef.child(selectedUid).addValueEventListener(new ValueEventListener() {
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

                    ButtonManagement();


                }
                else
                {
                    Toast.makeText(ViewProfileActivity.this,"Taranan Kod Bir Profile Ait Değil",Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
        declineFriendRequestBtn.setEnabled(false);

        if (!currentUid.equals(selectedUid))
        {
            sendFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendFriendRequestBtn.setEnabled(false);
                    if(current_state.equals("not_friends")){
                        SendFriendRequest();
                    }
                    else if(current_state.equals("request_sent"))
                    {
                        CancelFriendRequest();
                    }
                    else if(current_state.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    else if (current_state.equals("friends"))
                    {
                        UnFriend();

                    }
                }
            });

        }else
        {
            declineFriendRequestBtn.setVisibility(View.INVISIBLE);
            sendFriendRequestBtn.setVisibility(View.INVISIBLE);
        }

    }

    private void UnFriend()
    {
        FriendsRef.child(currentUid).child(selectedUid).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendsRef.child(selectedUid).child(currentUid).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                sendFriendRequestBtn.setEnabled(true);
                                                current_state = "not_friends";
                                                sendFriendRequestBtn.setText("İstek Gönder");

                                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFriendRequestBtn.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });

    }

    private void AcceptFriendRequest()
    {
        Calendar postDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM yyyy");
        saveCurrentDate = currentDate.format(postDate.getTime());

        FriendsRef.child(currentUid).child(selectedUid).child("date").setValue(saveCurrentDate)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task) {
                                               if(task.isSuccessful())
                                               {
                                                   FriendsRef.child(selectedUid).child(currentUid).child("date").setValue(saveCurrentDate)
                                                           .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                      @Override
                                                                                      public void onComplete(@NonNull Task<Void> task) {
                                                                                          if(task.isSuccessful())
                                                                                          {
                                                                                              FriendReqRef.child(currentUid).child(selectedUid).removeValue()
                                                                                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                          @Override
                                                                                                          public void onComplete(@NonNull Task<Void> task)
                                                                                                          {
                                                                                                              if (task.isSuccessful())
                                                                                                              {
                                                                                                                  FriendReqRef.child(selectedUid).child(currentUid).removeValue()
                                                                                                                          .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                              @Override
                                                                                                                              public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                  if (task.isSuccessful())
                                                                                                                                  {
                                                                                                                                      sendFriendRequestBtn.setEnabled(true);
                                                                                                                                      current_state = "friends";
                                                                                                                                      sendFriendRequestBtn.setText("Arkadaşlıktan Çıkar");

                                                                                                                                      declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                                                                                                      declineFriendRequestBtn.setEnabled(false);
                                                                                                                                  }

                                                                                                                              }
                                                                                                                          });
                                                                                                              }

                                                                                                          }
                                                                                                      });


                                                                                          }

                                                                                      }
                                                                                  }
                                                           );

                                               }

                                           }
                                       }
                );
    }

    private void CancelFriendRequest()
    {
        FriendReqRef.child(currentUid).child(selectedUid).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendReqRef.child(selectedUid).child(currentUid).removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                sendFriendRequestBtn.setEnabled(true);
                                                current_state = "not_friends";
                                                sendFriendRequestBtn.setText("İstek Gönder");

                                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFriendRequestBtn.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }

    private void ButtonManagement()
    {
        FriendReqRef.child(currentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(selectedUid))
                {
                    String request_type = snapshot.child(selectedUid).child("request_type").getValue().toString();
                    if (request_type.equals("sent"))
                    {
                        current_state = "request_sent";
                        sendFriendRequestBtn.setText("İsteği İptal Et");
                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                        declineFriendRequestBtn.setEnabled(false);
                    }
                    else if (request_type.equals("received"))
                    {
                        current_state = "request_received";
                        sendFriendRequestBtn.setText("İsteği Kabul Et");
                        declineFriendRequestBtn.setEnabled(true);
                        declineFriendRequestBtn.setVisibility(View.VISIBLE);

                        declineFriendRequestBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelFriendRequest();
                            }
                        });
                    }
                }
                else
                {
                    FriendsRef.child(currentUid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.hasChild(selectedUid))
                                    {
                                        current_state = "friends";
                                        sendFriendRequestBtn.setText("Arkadaşlıktan Çıkar");
                                        declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                        declineFriendRequestBtn.setEnabled(false);
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void SendFriendRequest()
    {
        FriendReqRef.child(currentUid).child(selectedUid).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendReqRef.child(selectedUid).child(currentUid).child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful())
                                            {
                                                sendFriendRequestBtn.setEnabled(true);
                                                current_state = "request_sent";
                                                sendFriendRequestBtn.setText("İsteği İptal Et");

                                                declineFriendRequestBtn.setVisibility(View.INVISIBLE);
                                                declineFriendRequestBtn.setEnabled(false);
                                            }

                                        }
                                    });
                        }

                    }
                });
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(ViewProfileActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
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
}