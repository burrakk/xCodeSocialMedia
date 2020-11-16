package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ProgressDialog loadingBar;
    private ImageButton sPostImage;
    private EditText sPostText;
    private Button sPostBtn;
    private static final int Gallery_Pick=1;
    private Uri imageUri;
    private String desc,saveCurrentDate,saveCurrentTime,postID,downloadURL,currentUid;

    private StorageReference PostRef;
    private DatabaseReference uRef,dbPostRef;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        sPostImage = (ImageButton)findViewById(R.id.select_post_image);
        sPostBtn = (Button)findViewById(R.id.update_post_button);
        sPostText= (EditText)findViewById(R.id.post_text);
        loadingBar = new ProgressDialog(this);

        mAuth=FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        PostRef= FirebaseStorage.getInstance().getReference();
        uRef= FirebaseDatabase.getInstance().getReference().child("Users");
        dbPostRef= FirebaseDatabase.getInstance().getReference().child("Posts");


        sPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });

        Toolbar mToolbar = (Toolbar)findViewById(R.id.update_postpage_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Yeni Gönderi");

        sPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidatePostInfo();
            }
        });

    }

    private void ValidatePostInfo() {
        desc= sPostText.getText().toString();
        if(imageUri == null)
        {
            Toast.makeText(this,"Gönderi İçin Fotoğraf Şeçilmedi !",Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(desc))
        {
            Toast.makeText(this,"Gönderi Açıklama Alanı Boş Bırakıldı!",Toast.LENGTH_SHORT).show();

        }
        else {
            loadingBar.setTitle("Yeni Gönderi");
            loadingBar.setMessage(" Gönderi Duvarınızda Paylaşılıyor..");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoreImageToFirebase();
        }
    }

    private void StoreImageToFirebase() {
        Calendar postDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(postDate.getTime());

        Calendar postTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(postTime.getTime());

        postID = saveCurrentDate+saveCurrentTime;

        StorageReference filePath = PostRef.child("Post_Images").child(imageUri.getLastPathSegment()+postID+".jpg");
        filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    downloadURL = uri.toString();
                    Toast.makeText(PostActivity.this,"Image uploaded",Toast.LENGTH_SHORT).show();

                    SavePostDescToDB();
                }
            });
        }
    });
}

    private void SavePostDescToDB() {
        uRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage=dataSnapshot.child("profileimage").getValue().toString();
                    HashMap postsMap = new HashMap();
                    postsMap.put("uid",currentUid);
                    postsMap.put("date",saveCurrentDate);
                    postsMap.put("time",saveCurrentTime);
                    postsMap.put("description",desc);
                    postsMap.put("postimage",downloadURL);
                    postsMap.put("profileimage",userProfileImage);
                    postsMap.put("uFullName",userFullName);
                    dbPostRef.child(currentUid+postID).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful())
                            {
                                loadingBar.dismiss();
                                Toast.makeText(PostActivity.this,"Gönderiniz Paylaşıldı...",Toast.LENGTH_SHORT).show();
                                SendUserToMainActivity();
                            }
                            else
                            {
                                loadingBar.dismiss();
                                Toast.makeText(PostActivity.this,"Gönderi Oluşturulamadı...",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_Pick && resultCode == RESULT_OK && data!=null)
        {
            imageUri = data.getData();
            sPostImage.setImageURI(imageUri);


        }
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
        Intent mainIntent = new Intent(PostActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}