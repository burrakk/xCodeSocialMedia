package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PostDetailsonClick extends AppCompatActivity {

    private ImageView postimage;
    private TextView postdescription;
    private Button deletepostBttn, editpostBttn;
    private String PostKey,currentUid,postUid,edit_desc,edit_postimage;
    private FirebaseAuth mAuth;
    private DatabaseReference editRef;
    private Toolbar pdetailtoolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detailson_click);

        mAuth = FirebaseAuth.getInstance();
        currentUid = mAuth.getCurrentUser().getUid();

        PostKey = getIntent().getExtras().get("PostKey").toString();
        editRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        androidx.appcompat.widget.Toolbar pdetailtoolbar= (androidx.appcompat.widget.Toolbar)findViewById(R.id.edit_page_toolbar);
        setSupportActionBar(pdetailtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Gönderi Detayı");
        postimage = (ImageView) findViewById(R.id.edit_post_image);
        postdescription = (TextView) findViewById(R.id.edit_post_description);
        editpostBttn = (Button) findViewById(R.id.edit_post_bttn);
        deletepostBttn = (Button) findViewById(R.id.delete_post_bttn);

        editpostBttn.setVisibility(View.INVISIBLE);
        deletepostBttn.setVisibility(View.INVISIBLE);

        editRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    edit_desc = snapshot.child("description").getValue().toString();
                    edit_postimage = snapshot.child("postimage").getValue().toString();
                    postUid = snapshot.child("uid").getValue().toString();
                    postdescription.setText(edit_desc);
                    Picasso.get().load(edit_postimage).into(postimage);
                    if (currentUid.equals(postUid))
                    {
                        editpostBttn.setVisibility(View.VISIBLE);
                        deletepostBttn.setVisibility(View.VISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        editpostBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditCurrentPost(edit_desc);
            }
        });
        deletepostBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteCurrentPost();
            }
        });




    }

    private void EditCurrentPost(String edit_description) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PostDetailsonClick.this);
        builder.setTitle("Gönderiyi Düzenle");
        final EditText inputField = new EditText(PostDetailsonClick.this);
        inputField.setText(edit_description);
        builder.setView(inputField);
        builder.setPositiveButton("Onayla", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 editRef.child("description").setValue(inputField.getText().toString());
                 Toast.makeText(PostDetailsonClick.this,"Gönderiniz Düzenlendi!",Toast.LENGTH_SHORT).show();

            }
        });

        builder.setNegativeButton("Vazgeç", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });

        Dialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);


    }

    private void DeleteCurrentPost() {
        editRef.removeValue();
        SendUserToMainActivity();
        Toast.makeText(this,"Gönderiniz Silindi.",Toast.LENGTH_SHORT).show();

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PostDetailsonClick.this , MainActivity.class);
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