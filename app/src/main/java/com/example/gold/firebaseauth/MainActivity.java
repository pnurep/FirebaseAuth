package com.example.gold.firebaseauth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private static final String TAG="MainActivity";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    EditText editEmail,editPassword;
    TextView checkEmail,checkPassword,textVerifyEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editEmail = (EditText) findViewById(R.id.editEmail);
        editPassword = (EditText) findViewById(R.id.editPassword);

        checkEmail = (TextView) findViewById(R.id.checkEmail);
        checkPassword = (TextView) findViewById(R.id.checkPassword);
        textVerifyEmail = (TextView) findViewById(R.id.textVerifyEmail);

        mAuth = FirebaseAuth.getInstance();
        // 로그인 아웃을 체크하는 리스너
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // 현재앱의 사용자 정보를 가져온다
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    // 이메일 검증이 안되어 있으면 검증 메일 발송
                    // 동작이 정상적으로 안됨
//                    if(!user.isEmailVerified()) {
//                        mailverification(user);
//                    }else{
//                        textVerifyEmail.setVisibility(View.GONE);
//                    }
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    // 최초 로그인시 이메일 인증
    // FirebaseUser user ;
    // user.isVerified() -> 인증 여부 확인
    // 사용할 타이밍을 못잡음.. 시그널이 없음
    public void mailverification(FirebaseUser user){
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(MainActivity.this, "검증메일이 발송되었습니다. 이메일을 확인해주세요.",
                                    Toast.LENGTH_SHORT).show();
                            textVerifyEmail.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

    public void signup(View view){

        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        checkEmail.setVisibility(View.GONE);
        checkPassword.setVisibility(View.GONE);

        int checkCount = 0;
        if(!SignUtil.validateEmail(email)){
            checkEmail.setVisibility(View.VISIBLE);
            checkCount++;
        }
        if(!SignUtil.validatePassword(password)){
            checkPassword.setVisibility(View.VISIBLE);
            checkCount++;
        }

        if(checkCount > 0){
            return;
        }
        Log.w("SignUP",email+"===="+password);
        // 사용자 등록
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        if (!task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "사용자등록 실패!!",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "등록되었습니다!!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void signin(View view){
        String email = editEmail.getText().toString();
        String password = editPassword.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(MainActivity.this, "로그인에 실패하였습니다", Toast.LENGTH_SHORT).show();
                        }else{
                            Intent intent = new Intent(MainActivity.this, SuccessActivity.class);
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
