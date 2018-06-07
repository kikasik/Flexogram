package com.LovellStudio.Flexogram;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.baoyz.widget.PullRefreshLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class UserPage extends AppCompatActivity {

    private String TAG = "UserPage";

    private ImageView userImage;
    private TextView userNick, userDescription, friendText;
    private String user;
    private String userVk;
    private Boolean userAccepted = false;
    private FrameLayout vkBtn;
    private ListView flexesList;
    private PullRefreshLayout swipeRefreshLayoutFlex;
    private Button friendBtn;
    private File finalLocalFile;
    private String imageRef;
    private Flex[] flexes;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_page);
        user = getIntent().getStringExtra(Lenta.EXTRA_AUTHOR);
        initUI();
        downloadInfo();
    }

    private void initUI() {
        userImage = (ImageView) findViewById(R.id.userImage);
        ImageView backBtn = (ImageView) findViewById(R.id.backBtn);
        userNick = (TextView) findViewById(R.id.userNick);
        friendText = (TextView) findViewById(R.id.friendText);
        friendBtn = (Button) findViewById(R.id.friendBtn);
        userDescription = (TextView) findViewById(R.id.userDescription);
        vkBtn = (FrameLayout) findViewById(R.id.vkBtn);
        flexesList = (ListView) findViewById(R.id.flexesList);
        swipeRefreshLayoutFlex = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayoutFlex);

        friendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (friendText.getText().equals("Вы подписаны")) {
                    FirebaseFirestore.getInstance().collection("users").document(Lenta.userUid)
                            .collection("my_subscriptions").document(user)
                            .delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        friendBtn.setText("Подписаться");
                                        friendText.setText("Вы не подписаны");
                                    }
                                }
                            });
                    FirebaseFirestore.getInstance().collection("users").document(user)
                            .collection("my_subscribers").document(Lenta.userUid)
                            .delete();
                }
                if (friendText.getText().equals("Вы не подписаны")) {
                    Map<String, Object> newSubscription = new HashMap<>();
                    newSubscription.put("accepted", "true");
                    newSubscription.put("user", user);
                    newSubscription.put("userNick", userNick.getText().toString());

                    FirebaseFirestore.getInstance().collection("users").document(Lenta.userUid)
                            .collection("my_subscriptions").document(user)
                            .set(newSubscription)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        friendBtn.setText("Отписаться");
                                        friendText.setText("Вы подписаны");
                                    }
                                }
                            });

                    Map<String, Object> newSubscriber = new HashMap<>();
                    newSubscriber.put("accepted", "true");
                    newSubscriber.put("user", Lenta.userUid);
                    newSubscriber.put("userNick", Lenta.userNickname);

                    FirebaseFirestore.getInstance().collection("users").document(user)
                            .collection("my_subscribers").document(Lenta.userUid)
                            .set(newSubscriber);
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        vkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://vk.com/" + userVk));
                startActivity(browserIntent);
            }
        });

        swipeRefreshLayoutFlex.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);
        swipeRefreshLayoutFlex.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadFlexes();
            }
        });
    }

    private void downloadInfo() {
        FirebaseFirestore.getInstance().collection("users").document(Lenta.userUid)
                .collection("my_subscriptions")
                .whereEqualTo("user", user)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() > 0) {
                            friendBtn.setEnabled(true);
                            friendBtn.setText("Отписаться");
                            friendText.setText("Вы подписаны");
                        } else {
                            friendBtn.setEnabled(true);
                            friendBtn.setText("Подписаться");
                            friendText.setText("Вы не подписаны");
                        }
                        if (user.equals(Lenta.userUid)) {
                            ((FrameLayout) friendText.getParent()).removeView(friendText);
                            ((FrameLayout) friendBtn.getParent()).removeView(friendBtn);
                        }
                    }
                });
        FirebaseFirestore.getInstance().collection("users").document(user)
                .collection("my_subscriptions")
                .whereEqualTo("user", Lenta.userUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() > 0) userAccepted = true;
                        downloadFlexes();
                    }
                });
        FirebaseFirestore.getInstance().collection("users").document(user)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                userNick.setText(task.getResult().getString("login"));
                                userDescription.setText(task.getResult().getString("description"));
                                userVk = task.getResult().getString("vk");
                                imageRef = task.getResult().getString("profileImage");
                                downloadFlexes();
                                try {
                                    File localFile = null;
                                    try {
                                        localFile = File.createTempFile("images", "jpg");
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    finalLocalFile = localFile;
                                    try {
                                        FirebaseStorage.getInstance().getReference()
                                                .child(imageRef)
                                                .getFile(localFile)
                                                .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                        ObjectAnimator.ofFloat(userImage, View.ALPHA, 0f, 1f).start();
                                                        userImage.setImageURI(Uri.fromFile(finalLocalFile));
                                                    }
                                                });
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    }
                                } catch (NullPointerException e) {
                                    e.printStackTrace();
                                }
                            } catch (IllegalStateException e) {
                                e.printStackTrace();
                                userNick.setText("Ошибка");
                                userDescription.setText("...");
                                vkBtn.setEnabled(false);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents to info: ", task.getException());
                        }
                    }
                });
    }

    void downloadFlexes() {
        FirebaseFirestore.getInstance().collection("Ижевск")
                .whereEqualTo("author", user)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() == 0) {
                                swipeRefreshLayoutFlex.setRefreshing(false);
                            }
                            flexes = new Flex[task.getResult().size()];
                            for (int i = 0; i < flexes.length; i++) {
                                flexes[i] = new Flex();
                            }
                            int flexesCount = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                flexes[flexesCount].setName(document.getString("name"));
                                flexes[flexesCount].setDescription(document.getString("description"));
                                flexes[flexesCount].setPrivat(document.getString("private"));
                                flexes[flexesCount].setPassword(document.getString("password"));
                                flexes[flexesCount].setDate(document.getString("date"));
                                flexes[flexesCount].setTime(document.getString("time"));
                                flexes[flexesCount].setFree(document.getString("free"));
                                flexes[flexesCount].setCost(document.getString("cost"));
                                flexes[flexesCount].setCount(document.getString("count"));
                                flexes[flexesCount].setCity(document.getString("city"));
                                flexes[flexesCount].setStreet(document.getString("street"));
                                flexes[flexesCount].setHouse(document.getString("house"));
                                flexes[flexesCount].setAuthor(document.getString("author"));
                                flexes[flexesCount].setAuthorNick(document.getString("authorNick"));
                                flexes[flexesCount].setAuthorDesc(document.getString("authorDesc"));
                                flexes[flexesCount].setAuthorVk(document.getString("authorVk"));
                                flexes[flexesCount].setImageRef(document.getString("imageRef"));
                                flexesCount++;
                                swipeRefreshLayoutFlex.setRefreshing(false);
                            }
                            flexesList.setAdapter(new MyFlexAdapter(UserPage.this, flexes));
                            ObjectAnimator.ofFloat(flexesList, View.ALPHA, 0f, 1f).start();
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
