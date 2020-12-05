
package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private EditText UserName, FullName, CountryName;
    private Button SaveInfoButton;
    private CircleImageView ProfileImage;
    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private StorageReference userProfileRef;
    private String currentUserID;

    final static int gallery_item = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUserID);
        userProfileRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");
        loadingBar = new ProgressDialog(this);
        UserName = (EditText) findViewById(R.id.setup_username);
        FullName = (EditText) findViewById(R.id.setup_fullname);
        CountryName = (EditText) findViewById(R.id.setup_countryname);
        SaveInfoButton = (Button) findViewById(R.id.setup_info_button);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_profile_image);

        SaveInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveSetupInfo();
            }
        });

        ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(SetupActivity.this);
            }
        });
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    if (dataSnapshot.hasChild("profileimage"))
                    {
                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else
                    {
                        Toast.makeText(SetupActivity.this, "Lütfen önce profil fotoğrafı belirleyiniz...", Toast.LENGTH_SHORT).show();
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==gallery_item  && resultCode == RESULT_OK && data!=null )
        {
            Uri ImageUri = data.getData();
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {


            CropImage.ActivityResult result =CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profil Fotoğrafı");
                loadingBar.setMessage(" Seçilen Fotoğraf Düzenleniyor...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultUri = result.getUri();
                StorageReference filePath = userProfileRef.child(currentUserID + ".jpg");
                filePath.putFile(resultUri);
                filePath.putFile(resultUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downUri = task.getResult();
                            Toast.makeText(SetupActivity.this, "Profil Fotoğrafı Seçildi.", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = downUri.toString();
                            userRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                                selfIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                                startActivity(selfIntent);

                                                Toast.makeText(SetupActivity.this, "Profil Fotoğrafı Güncellendi!", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SetupActivity.this, "Hata Oluştu : " + message, Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });

                        }

                    }
                });

            }
            else {
                loadingBar.dismiss();
                Toast.makeText(SetupActivity.this,"Resim Seçilemedi !",Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void SaveSetupInfo()
    {
        String username = UserName.getText().toString();
        String fullname = FullName.getText().toString();
        String country = CountryName.getText().toString();

        if (TextUtils.isEmpty(username)){
            Toast.makeText(this,"Lütfen Geçerli Kullanıcı Adı Giriniz...",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(fullname)){
            Toast.makeText(this,"Lütfen Adınızı Giriniz...",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(country)){
            Toast.makeText(this,"Lütfen Geçerli Ülke Adı Giriniz...",Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Profil Bilgileri");
            loadingBar.setMessage(" Tercihlerinizi İşliyoruz, Lütfen Bekleyiniz");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            HashMap usermap = new HashMap();
            usermap.put("username",username);
            usermap.put("fullname",fullname);
            usermap.put("country",country);
            usermap.put("status","Selam, sen de xCode ile bağlan");
            usermap.put("gender","none");
            usermap.put("cakeday","11.11.20");
            usermap.put("relationship_status","none");
            userRef.updateChildren(usermap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(SetupActivity.this,"Profilin Başarıyla Oluşturuldu, Hoşgeldin !",Toast.LENGTH_LONG).show();
                        SendUserToMainActivity();
                        loadingBar.dismiss();
                    }
                    else{
                        loadingBar.dismiss();
                        String message = task.getException().getMessage();
                        Toast.makeText(SetupActivity.this,"Hata Oluştu : "+message,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SetupActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}