package com.LovellStudio.Flexogram;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AddFriendPage extends AppCompatActivity {

    private String TAG = "AddFriendPage";

    TextView addFriendTitle;
    ListView addFriendList;
    android.support.v7.widget.SearchView searchView;
    private ImageView backBtn;
    public static My_subscriber[] users;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_friend_page);
        initUI();
        downloadInfo();
    }

    private void initUI() {
        backBtn = (ImageView) findViewById(R.id.backBtn);
        addFriendTitle = (TextView) findViewById(R.id.addFriendTitle);
        addFriendList = (ListView) findViewById(R.id.addFriendList);
        searchView = (android.support.v7.widget.SearchView) findViewById(R.id.searchView);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ObjectAnimator.ofFloat(addFriendTitle, View.ALPHA, 0F, 1F).start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                if (searchQuery.length() > 0 && addFriendTitle.getAlpha() == 1) {
                    ObjectAnimator.ofFloat(addFriendTitle, View.ALPHA, 1F, 0F).start();
                } else if (searchQuery.length() == 0 && addFriendTitle.getAlpha() >= 0)
                    ObjectAnimator.ofFloat(addFriendTitle, View.ALPHA, 0F, 1F).start();
                addFriendList.invalidate();
                ((AddFriendAdapter) addFriendList.getAdapter()).filter(searchQuery.trim());
                return true;
            }
        });
    }

    private void downloadInfo() {
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            users = new My_subscriber[task.getResult().size()];
                            for (int i = 0; i < users.length; i++) {
                                users[i] = new My_subscriber();
                            }
                            int usersCount = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                users[usersCount].setUser(document.getId());
                                users[usersCount].setUserNick(document.getString("login"));
                                usersCount++;
                            }
                            ObjectAnimator.ofFloat(addFriendList, View.ALPHA, 0f,1f).start();
                            addFriendList.setAdapter(new AddFriendAdapter(AddFriendPage.this));
                        }
                    }
                });
    }
}
