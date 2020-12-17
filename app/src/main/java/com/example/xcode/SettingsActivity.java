package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar settingsToolbar;
    private EditText userStatus, userCountry,userGender,userRelation, userBirth;
    private TextView  userName;
    private Button updateBttn;
    private CircleImageView userPP;
    private DatabaseReference settingsRef;
    private FirebaseAuth mAuth;
    private StorageReference userProfileRef;
    private String currentUid;
    final static int gallery_item = 1;
    private int mYear, mMonth, mDay;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();
        settingsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(currentUid);
        userProfileRef = FirebaseStorage.getInstance().getReference().child("Profile_Images");

        Toolbar settingsToolbar = (Toolbar)findViewById(R.id.acc_settings_toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profil Ayarlarım");

        userName = (TextView) findViewById(R.id.acc_settings_name);
        userStatus = (EditText) findViewById(R.id.acc_settings_status);
        userCountry = (EditText) findViewById(R.id.acc_settings_counrty);
        userGender = (EditText) findViewById(R.id.acc_settings_gender);
        userRelation = (EditText) findViewById(R.id.acc_settings_relationstatus);
        userBirth = (EditText) findViewById(R.id.acc_settings_birth);
        updateBttn = (Button) findViewById(R.id.acc_settings_updatebttn);
        userPP = (CircleImageView) findViewById(R.id.acc_settings_pp);

        settingsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    String mPP = snapshot.child("profileimage").getValue().toString();
                    String mUserName = snapshot.child("fullname").getValue().toString();
                    String mStatus = snapshot.child("status").getValue().toString();
                    String mCountry = snapshot.child("country").getValue().toString();
                    String mGender = snapshot.child("gender").getValue().toString();
                    String mRelation = snapshot.child("relationship_status").getValue().toString();
                    String mBirth = snapshot.child("cakeday").getValue().toString();

                    Picasso.get().load(mPP).placeholder(R.drawable.profile).into(userPP);
                    userName.setText(mUserName);
                    userStatus.setText(mStatus);
                    userCountry.setText(mCountry);
                    userGender.setText(mGender);
                    userRelation.setText(mRelation);
                    userBirth.setText(mBirth);


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        userBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                mYear = c.get(Calendar.YEAR);
                mMonth = c.get(Calendar.MONTH);
                mDay = c.get(Calendar.DAY_OF_MONTH);


                DatePickerDialog datePickerDialog = new DatePickerDialog(SettingsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {

                                userBirth.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);

                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        updateBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateAcc();
            }
        });

        userPP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setAspectRatio(1,1).start(SettingsActivity.this);
            }
        });

    }

    private void ValidateAcc() {
        String userstatus = userStatus.getText().toString();
        String usercountry = userCountry.getText().toString();
        String usergender = userGender.getText().toString();
        String userrelation = userRelation.getText().toString();
        String userbirth = userBirth.getText().toString();
        if (TextUtils.isEmpty(userstatus))
        {
            Toast.makeText(this,"Lütfen Durum Alanını Boş Bırakmayın",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(usercountry))
        {
            Toast.makeText(this,"Ülke Seçiniz",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(usergender))
        {
            Toast.makeText(this,"Bir Cinsiyet Belirleyin",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(userrelation))
        {
            Toast.makeText(this,"İlişki Durumunuzu Şeçin",Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(userbirth))
        {
            Toast.makeText(this,"Lütfen Geçerli Bir Doğum Tarihi Girin",Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Profil Fotoğrafı");
            loadingBar.setMessage(" Seçilen Fotoğraf Düzenleniyor...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            UpdateAccount(userstatus,usercountry,usergender,userrelation,userbirth);
        }
    }

    private void UpdateAccount(String userstatus, String usercountry, String usergender, String userrelation, String userbirth) {
        HashMap userMap = new HashMap();
        userMap.put("status",userstatus);
        userMap.put("country",usercountry);
        userMap.put("gender",usergender);
        userMap.put("relationship_status",userrelation);
        userMap.put("cakeday",userbirth);
        settingsRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful())
                {
                    loadingBar.dismiss();
                    Toast.makeText(SettingsActivity.this,"Profil Ayarlarınız Güncellendi !", Toast.LENGTH_SHORT).show();
                    SendUserToMainActivity();
                }
                else
                {
                    loadingBar.dismiss();
                    Toast.makeText(SettingsActivity.this,"Profil Ayarları Güncellenirken Hata Oluştu Tekrar Deneyin",Toast.LENGTH_SHORT).show();
                }

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
                StorageReference filePath = userProfileRef.child(currentUid + ".jpg");
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
                            Toast.makeText(SettingsActivity.this, "Profil Fotoğrafı Seçildi.", Toast.LENGTH_SHORT).show();

                            final String downloadUrl = downUri.toString();
                            settingsRef.child("profileimage").setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                                selfIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                                startActivity(selfIntent);

                                                Toast.makeText(SettingsActivity.this, "Profil Fotoğrafı Güncellendi!", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().getMessage();
                                                Toast.makeText(SettingsActivity.this, "Hata Oluştu : " + message, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(SettingsActivity.this,"Resim Seçilemedi !",Toast.LENGTH_SHORT).show();
            }
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
        Intent mainIntent = new Intent(SettingsActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }
}