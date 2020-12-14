package com.example.xcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText UserEmail, UserPassword, UserConfirmedPassword;
    private Button CreateAccountBttn;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        UserEmail = (EditText)findViewById(R.id.register_email);
        UserPassword = (EditText)findViewById(R.id.register_password);
        UserConfirmedPassword = (EditText)findViewById(R.id.register_confirm_password);
        CreateAccountBttn = (Button) findViewById(R.id.register_create_account);
        loadingBar = new ProgressDialog(this);

        CreateAccountBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                CreateNewAccount();

            }


        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null)
        {
            SendUserToMainActivity();
        }
    }

    private void CreateNewAccount()
    {

        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();
        String confirmedpass = UserConfirmedPassword.getText().toString();
        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Lütfen Geçerli Bir Mail Adresi Giriniz...",Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Lütfen Şifre Belirleyiniz...",Toast.LENGTH_SHORT).show();
        }
        else  if (TextUtils.isEmpty(confirmedpass))
        {
            Toast.makeText(this,"Lütfen Şifrenizi Doğrulayın...",Toast.LENGTH_SHORT).show();
        }
        else if (!password.equals(confirmedpass))
        {
            Toast.makeText(this,"Şifreleriniz Uyuşmuyor...",Toast.LENGTH_SHORT).show();

        }
        else
            {
                loadingBar.setTitle("Yeni Hesap Oluştur");
                loadingBar.setMessage("Hesabınız Oluşturuluyor, Lütfen Bekleyiniz");
                loadingBar.show();
                loadingBar.setCanceledOnTouchOutside(true);
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this,"Hesabınız Oluşturuldu",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                            SendUserToSetupActivity();
                        }
                        else{
                            String message = task.getException().getMessage();
                            loadingBar.dismiss();
                            Toast.makeText(RegisterActivity.this,"Hesap Oluşturulamadı : "+ message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        }
    }

    private void SendUserToSetupActivity() {
        Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
        startActivity(setupIntent);
        finish();
    }
    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this , MainActivity.class);
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