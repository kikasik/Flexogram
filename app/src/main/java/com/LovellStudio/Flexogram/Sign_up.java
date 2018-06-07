package com.LovellStudio.Flexogram;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;


public class Sign_up extends AppCompatActivity {

    String TAG = "Sign_up";

    private FirebaseAuth mAuth;
    FirebaseUser fUser = null;
    MaterialEditText et_email, et_password, et_login, et_description;
    Button btn_signup;
    TextView tv_signin, errorTV;
    String errors;
    private int readyCount;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_up);

        et_email = (MaterialEditText) findViewById(R.id.et_email);
        et_password = (MaterialEditText) findViewById(R.id.et_password);
        et_login = (MaterialEditText) findViewById(R.id.et_login);
        et_description = (MaterialEditText) findViewById(R.id.et_description);
        btn_signup = (Button) findViewById(R.id.btn_signup);
        tv_signin = (TextView) findViewById(R.id.tv_signin);
        errorTV = (TextView) findViewById(R.id.errorTv);

        mAuth = FirebaseAuth.getInstance();
        mAuth.signOut();
        FirebaseAuth.AuthStateListener mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                fUser = firebaseAuth.getCurrentUser();
                Log.e(TAG, "user aren't null");
            }
        };

        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errors = "";
                if (et_email.isCharactersCountValid())
                    if (et_password.isCharactersCountValid())
                        if (et_login.isCharactersCountValid())
                            if (et_description.isCharactersCountValid())
                                writeToFS();
                            else errors += "описание";
                        else errors += "логин";
                    else errors += "пароль";
                else errors += "почту";
                if (errors.length() > 0) errorTV.setText(("Проверьте " + errors));
            }
        });

        tv_signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void signup(final String email, final String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mAuth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(Sign_up.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "signInWithEmail:success");
                                                fUser = mAuth.getCurrentUser();
                                                Map<String, Object> user = new HashMap<>();
                                                user.put("login", et_login.getText().toString());
                                                user.put("description", et_description.getText().toString());

                                                FirebaseFirestore.getInstance().collection("users").document(fUser.getUid())
                                                        .set(user)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                                ready();
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w(TAG, "Error writing document", e);
                                                            }
                                                        });
                                            }
                                        }
                                    });
                            ready();
                        } else {
                            Toast.makeText(Sign_up.this, "Вы ввели некорректные данные или указанная почта зарегестрирована ранее", Toast.LENGTH_SHORT).show();
                            btn_signup.setEnabled(true);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Error registration", e);
            }
        });
    }

    void writeToFS() {
        btn_signup.setEnabled(false);
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("login", et_login.getText().toString())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                signup(et_email.getText().toString(), et_password.getText().toString());

                            } else {
                                Toast.makeText(Sign_up.this, "Ник занят", Toast.LENGTH_SHORT).show();
                                btn_signup.setEnabled(true);
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });


    }

    private void ready() {
        readyCount++;
        if (readyCount == 2) {
            Toast.makeText(Sign_up.this, "Готово!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
