package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NewAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        NewAccountLink = (TextView) findViewById(R.id.register_account_link);
        UserEmail = (EditText) findViewById(R.id.login_email);
        UserPassword = (EditText) findViewById(R.id.login_password);
        LoginButton = (Button) findViewById(R.id.login_account);
        mAuth = FirebaseAuth.getInstance();
        loadingBar = new ProgressDialog(this);

        NewAccountLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SendUserToRegisterActivity();

            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                AllowUserLogin();    
            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null)
        {
            SendUserToMainActivity();
        }
    }

    private void AllowUserLogin()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Email Adresi Giriniz...",Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this,"Şifrenizi Giriniz...",Toast.LENGTH_SHORT).show();
        }
        else
            {
                loadingBar.setTitle("Giriş Yapılıyor");
                loadingBar.setMessage("Profiliniz Yükleniyor");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Hoşgeldiniz", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        }
                        else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,"Giriş Hatalı : "+message,Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();

                        }

                    }
                });

            }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity()
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    }