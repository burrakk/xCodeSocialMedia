package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScannerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.WriterException;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import androidmads.library.qrgenearator.QRGSaver;

public class GenerateXCodeActivity extends AppCompatActivity {

    ImageView qrImage;
    Button qrGenerateBtn;
    private String savePath;
    String currentUid,qrCodeImageName;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference uRef;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_x_code);
        savePath = getApplicationContext().getExternalFilesDir("/QRCode/").toString();
        checkifFolderExist();
        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUid =mFirebaseAuth.getCurrentUser().getUid();
        uRef = FirebaseDatabase.getInstance().getReference().child("Users");
        uRef.child(currentUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists())
                {
                    qrCodeImageName = snapshot.child("username").getValue().toString();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        qrImage = (ImageView)findViewById(R.id.xcode_generatedQR);
        qrGenerateBtn = (Button) findViewById(R.id.xcode_generateBtn);
        qrGenerateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRGEncoder qrgEncoder = new QRGEncoder(currentUid,null,QRGContents.Type.TEXT,100);
                bitmap = qrgEncoder.getBitmap();
                qrImage.setImageBitmap(bitmap);
            }
        });

        findViewById(R.id.save_barcode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withContext(GenerateXCodeActivity.this).withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        boolean save = new QRGSaver().save(savePath, qrCodeImageName.trim(), bitmap, QRGContents.ImageType.IMAGE_JPEG);
                        String result = save ? "Kaydedildi" : "Kaydederken Bir Hata Oluştu İzinleri Kontrol Edin";
                        Toast.makeText(GenerateXCodeActivity.this, result, Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(GenerateXCodeActivity.this,"Dosya Erişimi Engellendi!",Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

            }
        });

    }

    private void checkifFolderExist() {
        File folder = new File(getApplicationContext().getExternalFilesDir(null),"QRCode");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
        } else {
            // Do something else on failure
        }
    }

    @Override
    public void onBackPressed() {
        SendUserToMainActivity();
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(GenerateXCodeActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}