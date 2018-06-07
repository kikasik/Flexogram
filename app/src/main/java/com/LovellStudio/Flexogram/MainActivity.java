package com.LovellStudio.Flexogram;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.hanks.htextview.fall.FallTextView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText et_email, et_password;
    Button btn_signin;
    TextView tv_signup;
    FallTextView tv_sign_in;
    SharedPreferences sPref;
    private final String SAVED_MAIL = "saved_mail";
    private final String SAVED_PASSWORD = "saved_password";
    String[] sentences = {"Вход...","Подождите"};
    int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_email = (EditText) findViewById(R.id.et_email);
        et_password = (EditText) findViewById(R.id.et_password);
        btn_signin = (Button) findViewById(R.id.btn_signin);
        tv_signup = (TextView) findViewById(R.id.tv_signup);
        tv_sign_in = (FallTextView) findViewById(R.id.tv_sign_in);
        loadText();
        mAuth = FirebaseAuth.getInstance();
        btn_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (et_email.getText().length()>3&&et_password.getText().length()>4) signin(et_email.getText().toString(), et_password.getText().toString());
                btn_signin.setEnabled(false);
                if (index + 1 >= sentences.length) {
                    index = 0;
                }
                tv_sign_in.animateText(sentences[index++]);
            }
        });

        tv_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent_signup = new Intent(MainActivity.this, Sign_up.class);
                startActivity(intent_signup);
            }
        });
    }

    private void signin(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            System.out.println("Зашел");
                            Intent intent_lenta = new Intent(MainActivity.this, Lenta.class);
                            intent_lenta.putExtra("email", email);
                            intent_lenta.putExtra("password", password);
                            startActivity(intent_lenta);
                            finish();
                        } else {
                            Toast.makeText(MainActivity.this, "Ошибка входа", Toast.LENGTH_SHORT).show();
                            btn_signin.setEnabled(true);
                            tv_sign_in.animateText("Ошибка");
                        }
                    }
                });
    }

    void saveText() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_MAIL, et_email.getText().toString());
        ed.putString(SAVED_PASSWORD, et_password.getText().toString());
        ed.apply();
    }

    void loadText() {
        sPref = getPreferences(MODE_PRIVATE);
        String mail = sPref.getString(SAVED_MAIL, "");
        String password = sPref.getString(SAVED_PASSWORD, "");
        et_email.setText(mail);
        et_password.setText(password);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveText();
    }
}