package com.LovellStudio.Flexogram;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.baoyz.widget.PullRefreshLayout;
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
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hanks.htextview.base.HTextView;
import com.yongchun.library.view.ImageSelectorActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import devlight.io.library.ntb.NavigationTabBar;
import id.zelory.compressor.Compressor;

public class Lenta extends AppCompatActivity {

    private static Random random;
    String TAG = "Lenta.java";

    public static final String EXTRA_URI = "com.kikasik.myinstagram.photoUri";
    public static final String EXTRA_AUTHOR = "com.kikasik.myinstagram.author";
    public static final String EXTRA_AUTHOR_NICK = "com.kikasik.myinstagram.authorNick";
    public static final String EXTRA_AUTHOR_VK = "com.kikasik.myinstagram.authorVk";
    public static final String EXTRA_AUTHOR_DESCRIPTION = "com.kikasik.myinstagram.authorDescriprion";
    public static final String EXTRA_I = "com.kikasik.myinstagram.i";
    public static final String EXTRA_FLEX = "com.kikasik.myinstagram.flex";
    public static String EXTRA_USER = "com.kikasik.myinstagram.user";

    private FirebaseAuth mAuth;
    public static MyRequest[] my_requests;
    public static ToMeRequest[] to_me_requests;
    public static String userNickname, userDescription, userUid, userVk;
    public static Boolean[] alreadyLiked;
    public static Flex[] flexes, friendsFlexes = new Flex[0], myFlexes;
    public static My_subscription[] my_subscriptions;
    public static My_subscriber[] my_subscribers;
    private ArrayList<String> images;
    private String[] sentences = {"What do you want here?", "flex", "&", "gram", "Flexogram", "флекс.", "флекс."};
    private FrameLayout loading, loading_line;
    private ToggleButton toggleFlex, toggleInfo, toggleFriends;
    private TextView flexTitle, infoTitle, friendsTitle;
    private EditText userVkEt, profilePageNick, profilePageDescription;
    private ImageView gone, profileImage;
    private ListView lenta, infoList, myFlexesList, myFriendsList;
    private int count = 0, i = 0, index = 0;
    private boolean loaded = false, loadedInfo = false, loadedFriends = false, signedIn = false;
    private PullRefreshLayout swipeRefreshLayout, swipeRefreshLayout_myFlex, swipeRefreshLayout_info, swipeRefreshLayout_friends;
    private FirebaseUser user;

    private static Context mContext;
    public static int userCountOfFlex = 0;
    private String imageRef;
    private File finalLocalFile;
    private MySubscribersAdapter mySubscribersAdapter;
    private MySubscriptionsAdapter mySubscriptionsAdapter;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_global);
        initUI();
    }

    private void initUI() {
        final ViewPager viewPager = (ViewPager) findViewById(R.id.vp_horizontal_ntb);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public boolean isViewFromObject(final View view, final Object object) {
                return view.equals(object);
            }

            @Override
            public void destroyItem(final View container, final int position, final Object object) {
                ((ViewPager) container).removeView((View) object);
            }

            @Override
            public Object instantiateItem(final ViewGroup container, final int position) {
                if (position == 0) {
                    final View view = LayoutInflater.from(
                            getBaseContext()).inflate(R.layout.lenta, null, false);
                    initialization(view);
                    if (loaded) {
                        if (toggleFlex.isChecked()) {
                            lenta.setAdapter(new FlexAdapter(Lenta.this, friendsFlexes));
                        } else {
                            lenta.setAdapter(new FlexAdapter(Lenta.this, flexes));
                        }
                    }

                    container.addView(view);
                    return view;
                } else if (position == 1) {
                    final View view = LayoutInflater.from(
                            getBaseContext()).inflate(R.layout.info_page, null, false);
                    initializeInfo(view);
                    if (loadedInfo) infoList.setAdapter(new InfoAdapter(Lenta.this));
                    container.addView(view);
                    return view;
                } else if (position == 2) {
                    final View view = LayoutInflater.from(
                            getBaseContext()).inflate(R.layout.friends_page, null, false);
                    initializeFriends(view);
                    container.addView(view);
                    return view;
                } else if (position == 3) {
                    final View view = LayoutInflater.from(
                            getBaseContext()).inflate(R.layout.profilepage, null, false);
                    initializeProfile(view);
                    if (!signedIn) {
                        signedIn = true;
                        Intent intent = getIntent();
                        signin(intent.getStringExtra("email"), intent.getStringExtra("password"));
                    } else {
                        profileImage.setImageURI(Uri.fromFile(finalLocalFile));
                        profilePageNick.setText(userNickname);
                        profilePageDescription.setText(userDescription);
                        userVkEt.setText(userVk);
                        myFlexesList.setAdapter(new MyFlexAdapter(Lenta.this, myFlexes));
                    }
                    container.addView(view);
                    return view;
                } else return null;
            }
        });

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.folder),
                        getResources().getColor(R.color.evening))
                        .selectedIcon(getResources().getDrawable(R.drawable.flexdoc))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.qaa),
                        getResources().getColor(R.color.fire))
                        .selectedIcon(getResources().getDrawable(R.drawable.qa_second))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.friends),
                        getResources().getColor(R.color.sand))
                        .build()
        );
        models.add(
                new NavigationTabBar.Model.Builder(
                        getResources().getDrawable(R.drawable.profile),
                        getResources().getColor(R.color.sea))
                        .build()
        );
        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(viewPager, 3);
        navigationTabBar.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(final int position, final float positionOffset, final int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(final int position) {
                navigationTabBar.getModels().get(position).hideBadge();
                if (position == 0) {
                    if (signedIn) {
                        ValueAnimator colorAnim = ObjectAnimator.ofInt(loading_line, "backgroundColor"
                                , ((ColorDrawable) loading_line.getBackground()).getColor()
                                , getResources().getColor(R.color.evening));
                        colorAnim.setDuration(300);
                        colorAnim.setEvaluator(new ArgbEvaluator());
                        colorAnim.start();
                    }
                }
                if (position == 1) {
                    if (signedIn) {
                        ValueAnimator colorAnim = ObjectAnimator.ofInt(loading_line, "backgroundColor"
                                , ((ColorDrawable) loading_line.getBackground()).getColor()
                                , getResources().getColor(R.color.fire));
                        colorAnim.setDuration(300);
                        colorAnim.setEvaluator(new ArgbEvaluator());
                        colorAnim.start();
                    }
                }
                if (position == 2) {
                    if (signedIn) {
                        ValueAnimator colorAnim = ObjectAnimator.ofInt(loading_line, "backgroundColor"
                                , ((ColorDrawable) loading_line.getBackground()).getColor()
                                , getResources().getColor(R.color.sand));
                        colorAnim.setDuration(300);
                        colorAnim.setEvaluator(new ArgbEvaluator());
                        colorAnim.start();
                    }
                }
                if (position == 3) {
                    if (signedIn) {
                        ValueAnimator colorAnim = ObjectAnimator.ofInt(loading_line, "backgroundColor"
                                , ((ColorDrawable) loading_line.getBackground()).getColor()
                                , getResources().getColor(R.color.sea));
                        colorAnim.setDuration(300);
                        colorAnim.setEvaluator(new ArgbEvaluator());
                        colorAnim.start();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
            }
        });

        navigationTabBar.setTitleMode(NavigationTabBar.TitleMode.ALL);
        navigationTabBar.setIsTinted(true);
        navigationTabBar.setIsSwiped(true);
        navigationTabBar.setBgColor(Color.BLACK);
    }

    private void initialization(View view) {
        random = new Random();
        lenta = view.findViewById(R.id.lenta);
        ImageView btn = view.findViewById(R.id.btn);
        toggleFlex = view.findViewById(R.id.toggleFlex);
        flexTitle = view.findViewById(R.id.flexTitle);

        toggleFlex.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    downloadFlexes();
                    downloadFriends();
                    flexTitle.setText("События друзей");
                    ObjectAnimator.ofFloat(lenta, "x", 150f, 0f).start();
                    ObjectAnimator.ofFloat(lenta, View.ALPHA, 0f, 1f).start();
                    if (loaded) {
                        lenta.setAdapter(new FlexAdapter(Lenta.this, friendsFlexes));
                    }
                } else {
                    flexTitle.setText("Ижевск");
                    ObjectAnimator.ofFloat(lenta, "x", -150f, 0f).start();
                    ObjectAnimator.ofFloat(lenta, View.ALPHA, 0f, 1f).start();
                    if (loaded) {
                        lenta.setAdapter(new FlexAdapter(Lenta.this, flexes));
                    }
                }
            }
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);
        swipeRefreshLayout.setRefreshing(false);
        swipeRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadFlexes();
            }
        });

        mContext = this.getApplicationContext();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Lenta.this, AddFlex.class);
                intent.putExtra(EXTRA_AUTHOR, userUid);
                intent.putExtra(EXTRA_AUTHOR_NICK, userNickname);
                intent.putExtra(EXTRA_AUTHOR_VK, userVk);
                intent.putExtra(EXTRA_AUTHOR_DESCRIPTION, userDescription);
                intent.putExtra(EXTRA_I, Integer.toString(i));
                startActivity(intent);
            }
        });
    }


    private void initializeFriends(View view) {
        friendsTitle = view.findViewById(R.id.friendsTitle);
        swipeRefreshLayout_friends = view.findViewById(R.id.swipeRefreshLayout_friends);
        toggleFriends = view.findViewById(R.id.toggleFriends);
        myFriendsList = view.findViewById(R.id.friendsList);
        FrameLayout inviteFriends = view.findViewById(R.id.inviteFriends);
        FrameLayout addBtn = view.findViewById(R.id.addBtn);
        SearchView searchView = view.findViewById(R.id.searchView);

        mySubscribersAdapter = new MySubscribersAdapter(Lenta.this);
        mySubscriptionsAdapter = new MySubscriptionsAdapter(Lenta.this);

        inviteFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://vk.com/friends"));
                startActivity(browserIntent);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Lenta.this, AddFriendPage.class);
                startActivity(intent);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                ObjectAnimator.ofFloat(friendsTitle, View.ALPHA, 0F, 1F).start();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchQuery) {
                if (searchQuery.length() > 0 && friendsTitle.getAlpha() == 1) {
                    ObjectAnimator.ofFloat(friendsTitle, View.ALPHA, 1F, 0F).start();
                } else if (searchQuery.length() == 0 && friendsTitle.getAlpha() >= 0)
                    ObjectAnimator.ofFloat(friendsTitle, View.ALPHA, 0F, 1F).start();
                if (toggleFriends.isChecked())
                    ((MySubscriptionsAdapter) myFriendsList.getAdapter()).filter(searchQuery.trim());
                else ((MySubscribersAdapter) myFriendsList.getAdapter()).filter(searchQuery.trim());
                myFriendsList.invalidate();
                return true;
            }
        });

        swipeRefreshLayout_friends.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);
        swipeRefreshLayout_friends.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadFriends();
            }
        });

        toggleFriends.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    friendsTitle.setText("Мои подписки");
                    ObjectAnimator.ofFloat(myFriendsList, "x", 150f, 0f).start();
                    ObjectAnimator.ofFloat(myFriendsList, View.ALPHA, 0f, 1f).start();
                    if (loadedFriends) {
                        myFriendsList.setAdapter(mySubscriptionsAdapter);
                    }
                }
                if (!isChecked) {
                    friendsTitle.setText("Мои подписчики");
                    ObjectAnimator.ofFloat(myFriendsList, "x", -150f, 0f).start();
                    ObjectAnimator.ofFloat(myFriendsList, View.ALPHA, 0f, 1f).start();
                    if (loadedFriends) {
                        myFriendsList.setAdapter(mySubscribersAdapter);
                    }
                }
                downloadFriends();
            }
        });
        if (!toggleFriends.isChecked()) {
            friendsTitle.setText("Мои подписчики");
            ObjectAnimator.ofFloat(myFriendsList, "x", -150f, 0f).start();
            ObjectAnimator.ofFloat(myFriendsList, View.ALPHA, 0f, 1f).start();
            if (loadedFriends) myFriendsList.setAdapter(mySubscribersAdapter);
        }
        myFriendsList.setTextFilterEnabled(true);
    }

    private void initializeProfile(View view) {
        mAuth = FirebaseAuth.getInstance();
        loading = (FrameLayout) findViewById(R.id.loading);
        loading_line = (FrameLayout) findViewById(R.id.loading_line);
        myFlexesList = view.findViewById(R.id.myFlexesList);
        userVkEt = view.findViewById(R.id.userVkEt);
        userVkEt.setInputType(InputType.TYPE_NULL);
        profilePageNick = view.findViewById(R.id.profilePageNick);
        profilePageNick.setInputType(InputType.TYPE_NULL);
        profilePageDescription = view.findViewById(R.id.profilePageDescription);
        profilePageDescription.setEnabled(true);
        swipeRefreshLayout_myFlex = view.findViewById(R.id.swipeRefreshLayoutMyFlex);
        swipeRefreshLayout_myFlex.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);
        swipeRefreshLayout_myFlex.setRefreshing(false);
        swipeRefreshLayout_myFlex.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadFlexes();
            }
        });
        ImageView signOutBtn = view.findViewById(R.id.signOutBtn);
        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Lenta.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        ImageView changeProfileImage = view.findViewById(R.id.changeProfileImage);
        changeProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageSelectorActivity.start(Lenta.this, 1, ImageSelectorActivity.MODE_SINGLE, true, true, true);
            }
        });
        loading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (index + 1 >= sentences.length) {
                    index = 0;
                }
                ((HTextView) loading.getChildAt(0)).animateText(sentences[index++]);
            }
        });
        profileImage = view.findViewById(R.id.profile_image);
        gone = view.findViewById(R.id.gone);
        gone.setTag("edit");
        gone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gone.getTag().equals("edit")) {
                    gone.setImageResource(R.drawable.gone);
                    gone.setTag("gone");
                    userVkEt.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
                    profilePageNick.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT);
                    profilePageDescription.setEnabled(true);
                    profilePageNick.requestFocus();
                    profilePageNick.setFocusableInTouchMode(true);
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(profilePageNick, InputMethodManager.SHOW_FORCED);
                    AnimatorSet as = new AnimatorSet();
                    as.playSequentially(ObjectAnimator.ofFloat(profilePageNick, View.SCALE_Y, 1f, 1.5f),
                            ObjectAnimator.ofFloat(profilePageNick, View.SCALE_Y, 1.5f, 1f),
                            ObjectAnimator.ofFloat(profilePageDescription, View.SCALE_Y, 1f, 1.5f),
                            ObjectAnimator.ofFloat(profilePageDescription, View.SCALE_Y, 1.5f, 1f),
                            ObjectAnimator.ofFloat(userVkEt, View.SCALE_Y, 1f, 1.5f),
                            ObjectAnimator.ofFloat(userVkEt, View.SCALE_Y, 1.5f, 1f));
                    as.setDuration(130);
                    as.start();
                } else {
                    final View et = Lenta.this.getCurrentFocus();
                    if (!userNickname.equals(profilePageNick.getText().toString())) {
                        if (profilePageNick.getText().toString().length() >= 5) {
                            FirebaseFirestore.getInstance().collection("users")
                                    .whereEqualTo("login", profilePageNick.getText().toString())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                if (task.getResult().size() == 0) {
                                                    FirebaseFirestore.getInstance().collection("users").document(userUid)
                                                            .update("login", profilePageNick.getText().toString())
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    ObjectAnimator.ofFloat(userVkEt, View.SCALE_X, 0.7f, 1f).start();
                                                                    ObjectAnimator.ofFloat(userVkEt, View.SCALE_Y, 0.7f, 1f).start();
                                                                    ObjectAnimator.ofFloat(profilePageNick, View.SCALE_X, 0.7f, 1f).start();
                                                                    ObjectAnimator.ofFloat(profilePageNick, View.SCALE_Y, 0.7f, 1f).start();
                                                                    ObjectAnimator.ofFloat(profilePageDescription, View.SCALE_X, 0.7f, 1f).start();
                                                                    ObjectAnimator.ofFloat(profilePageDescription, View.SCALE_Y, 0.7f, 1f).start();
                                                                    userNickname = profilePageNick.getText().toString();
                                                                    gone.setImageResource(R.drawable.edit);
                                                                    gone.setTag("edit");
                                                                    userVkEt.setInputType(InputType.TYPE_NULL);
                                                                    profilePageNick.setInputType(InputType.TYPE_NULL);
                                                                    profilePageDescription.setInputType(InputType.TYPE_NULL);
                                                                    profilePageDescription.setEnabled(false);
                                                                    if (et != null) {
                                                                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                                                        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                                                                    }
                                                                    Log.d(TAG, "Login successfully updated!");
                                                                    Toast.makeText(Lenta.mContext, "Данные изменены", Toast.LENGTH_SHORT).show();
                                                                }
                                                            })
                                                            .addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.w(TAG, "Error updating document", e);
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(Lenta.this, "Ошибка", Toast.LENGTH_SHORT).show();
                                                }

                                            } else {
                                                Log.d("ошибка", "Error getting documents: ", task.getException());
                                            }
                                        }
                                    });
                        }
                    } else {
                        gone.setImageResource(R.drawable.edit);
                        gone.setTag("edit");
                        userVkEt.setInputType(InputType.TYPE_NULL);
                        profilePageNick.setInputType(InputType.TYPE_NULL);
                        profilePageDescription.setEnabled(false);
                        ObjectAnimator.ofFloat(userVkEt, View.SCALE_X, 0.7f, 1f).start();
                        ObjectAnimator.ofFloat(userVkEt, View.SCALE_Y, 0.7f, 1f).start();
                        ObjectAnimator.ofFloat(profilePageNick, View.SCALE_X, 0.7f, 1f).start();
                        ObjectAnimator.ofFloat(profilePageNick, View.SCALE_Y, 0.7f, 1f).start();
                        ObjectAnimator.ofFloat(profilePageDescription, View.SCALE_X, 0.7f, 1f).start();
                        ObjectAnimator.ofFloat(profilePageDescription, View.SCALE_Y, 0.7f, 1f).start();
                        if (et != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
                        }
                    }
                    userVk = userVkEt.getText().toString();
                    FirebaseFirestore.getInstance().collection("users").document(userUid)
                            .update("vk", userVkEt.getText().toString(),
                                    "description", profilePageDescription.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "Desc & vk successfully updated!");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error updating document", e);
                                }
                            });
                }
            }
        });

    }

    private void initializeInfo(View view) {
        infoList = view.findViewById(R.id.infoList);
        infoTitle = view.findViewById(R.id.infoTitle);
        toggleInfo = view.findViewById(R.id.toggleInfo);
        swipeRefreshLayout_info = view.findViewById(R.id.swipeRefreshLayout_info);
        swipeRefreshLayout_info.setRefreshStyle(PullRefreshLayout.STYLE_WATER_DROP);
        swipeRefreshLayout_info.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadInfo();
            }
        });
        toggleInfo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    infoTitle.setText("Запросы мне");
                    ObjectAnimator.ofFloat(infoList, "x", 150f, 0f).start();
                    ObjectAnimator.ofFloat(infoList, View.ALPHA, 0f, 1f).start();
                    if (loadedInfo) infoList.setAdapter(new InfoToMeRequestAdapter(Lenta.this));
                } else {
                    infoTitle.setText("Мои запросы");
                    ObjectAnimator.ofFloat(infoList, "x", -150f, 0f).start();
                    ObjectAnimator.ofFloat(infoList, View.ALPHA, 0f, 1f).start();
                    if (loadedInfo) infoList.setAdapter(new InfoAdapter(Lenta.this));
                }
            }
        });
    }


    private void downloadInfo() {
        FirebaseFirestore.getInstance().collection("users").document(userUid).collection("my_requests")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            count = 0;
                            my_requests = new MyRequest[task.getResult().size()];
                            for (int k = 0; k < my_requests.length; k++) {
                                my_requests[k] = new MyRequest();
                            }
                            for (DocumentSnapshot document : task.getResult()) {
                                my_requests[count].setAuthor(document.getString("author"));
                                my_requests[count].setName(document.getString("name"));
                                my_requests[count].setAccept(document.getBoolean("accept"));
                                my_requests[count].setVk(document.getString("vk"));
                                count++;
                            }
                            FirebaseFirestore.getInstance().collection("users").document(userUid).collection("requests_to_me")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                count = 0;
                                                to_me_requests = new ToMeRequest[task.getResult().size()];
                                                for (int k = 0; k < to_me_requests.length; k++) {
                                                    to_me_requests[k] = new ToMeRequest();
                                                }
                                                for (DocumentSnapshot document : task.getResult()) {
                                                    to_me_requests[count].setUser(document.getString("author"));
                                                    to_me_requests[count].setUserNick(document.getString("authorNick"));
                                                    to_me_requests[count].setUserDesc(document.getString("authorDesc"));
                                                    to_me_requests[count].setUserVk(document.getString("authorVk"));
                                                    to_me_requests[count].setName(document.getString("name"));
                                                    to_me_requests[count].setAccept(document.getBoolean("accept"));
                                                    count++;
                                                }
                                                loadedInfo = true;
                                                try {
                                                    if (toggleInfo.isChecked())
                                                        infoList.setAdapter(new InfoToMeRequestAdapter(Lenta.this));
                                                    else
                                                        infoList.setAdapter(new InfoAdapter(Lenta.this));
                                                    swipeRefreshLayout_info.setRefreshing(false);
                                                } catch (NullPointerException e) {
                                                    e.printStackTrace();
                                                }
                                            } else {
                                                Log.d(TAG, "Error getting documents to info: ", task.getException());
                                            }
                                        }
                                    });
                        } else {
                            Log.d(TAG, "Error getting documents to info: ", task.getException());
                        }
                    }
                });
    }

    private void downloadFlexes() {
        FirebaseFirestore.getInstance().collection("Ижевск")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            flexes = new Flex[task.getResult().size()];
                            int myFlexesCount = 0;
                            for (int k = 0; k < flexes.length; k++) {
                                flexes[k] = new Flex();
                            }
                            count = 0;
                            userCountOfFlex = 0;
                            for (DocumentSnapshot document : task.getResult()) {
                                flexes[count].setName(document.getString("name"));
                                flexes[count].setDescription(document.getString("description"));
                                flexes[count].setPrivat(document.getString("private"));
                                flexes[count].setPassword(document.getString("password"));
                                flexes[count].setDate(document.getString("date"));
                                flexes[count].setTime(document.getString("time"));
                                flexes[count].setFree(document.getString("free"));
                                flexes[count].setCost(document.getString("cost"));
                                flexes[count].setCount(document.getString("count"));
                                flexes[count].setCity(document.getString("city"));
                                flexes[count].setStreet(document.getString("street"));
                                flexes[count].setHouse(document.getString("house"));
                                flexes[count].setAuthor(document.getString("author"));
                                flexes[count].setAuthorNick(document.getString("authorNick"));
                                flexes[count].setAuthorDesc(document.getString("authorDesc"));
                                flexes[count].setAuthorVk(document.getString("authorVk"));
                                flexes[count].setImageRef(document.getString("imageRef"));
                                if (flexes[count].getAuthor().equals(userUid)) {
                                    myFlexesCount++;
                                    userCountOfFlex++;
                                }
                                count++;
                            }
                            Arrays.sort(flexes, new Comparator<Flex>() {
                                @Override
                                public int compare(Flex flex, Flex t1) {
                                    return flex.getDate().compareTo(t1.getDate());
                                }
                            });
                            myFlexes = new Flex[myFlexesCount];
                            for (int k = 0; k < myFlexesCount; k++) {
                                myFlexes[k] = new Flex();
                            }
                            myFlexesCount = 0;
                            final ArrayList<Flex> flexArrayList = new ArrayList<>();
                            for (int k = 0; k < flexes.length; k++) {
                                if (flexes[k].getAuthor().equals(userUid)) {
                                    myFlexes[myFlexesCount] = flexes[k];
                                    myFlexesCount++;
                                }
                                if (loadedFriends)
                                    for (int j = 0; j < my_subscriptions.length; j++) {
                                        if (flexes[k].getAuthor().equals(my_subscriptions[j].getUser())) {
                                            Flex flex = flexes[k];
                                            flexArrayList.add(flex);
                                            friendsFlexes = flexArrayList.toArray(new Flex[flexArrayList.size()]);
                                        }
                                    }
                                loaded = true;
                            }

                            try {
                                swipeRefreshLayout.setRefreshing(false);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            try {
                                swipeRefreshLayout_myFlex.setRefreshing(false);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (toggleFlex.isChecked()) {
                                    lenta.setAdapter(new FlexAdapter(Lenta.this, friendsFlexes));
                                } else {
                                    lenta.setAdapter(new FlexAdapter(Lenta.this, flexes));
                                }
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            myFlexesList.setAdapter(new MyFlexAdapter(Lenta.this, myFlexes));
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void downloadFriends() {
        FirebaseFirestore.getInstance().collection("users").document(userUid)
                .collection("my_subscriptions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            my_subscriptions = null;
                            my_subscriptions = new My_subscription[task.getResult().size()];
                            int mySubscriptionsCount = 0;
                            for (int k = 0; k < my_subscriptions.length; k++) {
                                my_subscriptions[k] = new My_subscription();
                            }
                            for (DocumentSnapshot document : task.getResult()) {
                                my_subscriptions[mySubscriptionsCount].setUser(document.getString("user"));
                                my_subscriptions[mySubscriptionsCount].setUserNick(document.getString("userNick"));
                                my_subscriptions[mySubscriptionsCount].setAccepted(document.getString("accepted"));
                                my_subscriptions[mySubscriptionsCount].setUserVk(document.getString("userVk"));
                                mySubscriptionsCount++;
                            }
                            FirebaseFirestore.getInstance().collection("users").document(userUid)
                                    .collection("my_subscribers")
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if (task.isSuccessful()) {
                                                my_subscribers = null;
                                                my_subscribers = new My_subscriber[task.getResult().size()];
                                                int mySubscribersCount = 0;
                                                for (int k = 0; k < my_subscribers.length; k++) {
                                                    my_subscribers[k] = new My_subscriber();
                                                }
                                                for (DocumentSnapshot document : task.getResult()) {
                                                    my_subscribers[mySubscribersCount].setUser(document.getString("user"));
                                                    my_subscribers[mySubscribersCount].setUserNick(document.getString("userNick"));
                                                    my_subscribers[mySubscribersCount].setAccepted(document.getString("accepted"));
                                                    my_subscribers[mySubscribersCount].setUserVk(document.getString("userVk"));
                                                    mySubscribersCount++;
                                                }
                                                loadedFriends = true;
                                            }
                                            if (toggleFriends.isChecked()) {
                                                swipeRefreshLayout_friends.setRefreshing(false);
                                                myFriendsList.setAdapter(new MySubscriptionsAdapter(Lenta.this));
                                            } else {
                                                swipeRefreshLayout_friends.setRefreshing(false);
                                                myFriendsList.setAdapter(new MySubscribersAdapter(Lenta.this));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void downloadImage() {
        try {
            File localFile = null;
            try {
                localFile = File.createTempFile("images", "jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }

            finalLocalFile = localFile;

            FirebaseStorage.getInstance().getReference()
                    .child(imageRef)
                    .getFile(localFile)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            ObjectAnimator.ofFloat(profileImage, View.ALPHA, 0f, 1f).start();
                            profileImage.setImageURI(Uri.fromFile(finalLocalFile));
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                }
            });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void imageUpload() throws IOException {
        try {
            final File compressedImageFile = new Compressor(this).compressToFile(new File(images.get(0)));
            final Uri file = Uri.fromFile(compressedImageFile);
            imageRef = "images/" + userUid + "profileImage" + ".png";
            StorageReference riversRef = FirebaseStorage.getInstance().getReference()
                    .child(imageRef);
            riversRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Map<String, Object> photoInfo = new HashMap<>();
                            photoInfo.put("profileImage", imageRef);

                            FirebaseFirestore.getInstance()
                                    .collection("users").document(userUid)
                                    .update(photoInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            profileImage.setImageDrawable(Drawable.createFromPath(file.getPath()));
                                            finalLocalFile = compressedImageFile;
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(Lenta.this, "Ошибка при загрузке фото", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    private void signin(final String email, final String password) {
        mAuth.signOut();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            System.out.println("Зашел");
                            user = mAuth.getCurrentUser();
                            userUid = user.getUid();
                            EXTRA_USER = userUid;
                            FirebaseFirestore.getInstance().collection("users").document(userUid)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document != null) {
                                                    userNickname = task.getResult().getData().get("login").toString();
                                                    userDescription = task.getResult().getData().get("description").toString();
                                                    try {
                                                        imageRef = task.getResult().getData().get("profileImage").toString();
                                                        downloadImage();
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        userVk = task.getResult().getData().get("vk").toString();
                                                        userVkEt.setText(userVk);
                                                    } catch (Exception e) {
                                                        userVkEt.setText("");
                                                        userVk = "";
                                                    }
                                                    profilePageNick.setText(userNickname);
                                                    profilePageDescription.setText(userDescription);
                                                    profilePageNick.setInputType(InputType.TYPE_NULL);
                                                    loading.setVisibility(View.INVISIBLE);
                                                    profilePageDescription.setEnabled(false);
                                                    downloadFlexes();
                                                    downloadFriends();
                                                    downloadInfo();
                                                } else {
                                                    Log.d(TAG, "No such document");
                                                }
                                            } else {
                                                Log.d(TAG, "get failed with ", task.getException());
                                                Intent intent = getIntent();
                                                signin(intent.getStringExtra("email"), intent.getStringExtra("password"));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    public static void createRequest(final int position) {
        if (!flexes[position].getAuthor().equals(userUid)) {
            if ((userVk != null || !userVk.equals("")) && userVk.length() >= 3) {
                FirebaseFirestore.getInstance().collection("users").document(userUid).collection("my_requests")
                        .whereEqualTo("name", flexes[position].getName())
                        .whereEqualTo("author", flexes[position].getAuthor())
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    if (task.getResult().size() == 0) {
                                        Map<String, Object> request = new HashMap<>();
                                        request.put("name", flexes[position].getName());
                                        request.put("author", userUid);
                                        request.put("authorNick", userNickname);
                                        request.put("authorDesc", userDescription);
                                        request.put("authorVk", userVk);
                                        request.put("accept", false);

                                        int doc_id = random.nextInt(999999 - 1000 + 1) + 1000;

                                        FirebaseFirestore.getInstance()
                                                .collection("users").document(flexes[position].getAuthor())
                                                .collection("requests_to_me").document(Integer.toString(doc_id))
                                                .set(request);

                                        request = new HashMap<>();
                                        request.put("name", flexes[position].getName());
                                        request.put("author", flexes[position].getAuthor());
                                        request.put("authorNick", flexes[position].getAuthorNick());
                                        request.put("authorDesc", flexes[position].getAuthorDesc());
                                        request.put("authorVk", flexes[position].getAuthorVk());
                                        request.put("accept", false);

                                        FirebaseFirestore.getInstance()
                                                .collection("users").document(userUid)
                                                .collection("my_requests").document(Integer.toString(doc_id))
                                                .set(request);
                                    } else
                                        Toast.makeText(mContext, "Запрос уже отправлен", Toast.LENGTH_SHORT).show();
                                } else {
                                    Log.d("ошибка", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            } else {
                Toast.makeText(Lenta.mContext, "Ссылка на ваш профиль ВКонтакте обязательна", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(Lenta.mContext, "Это ваше событие", Toast.LENGTH_SHORT).show();
        }
    }

    public static void deleteFlex(final int position) {
        FirebaseFirestore.getInstance().collection("Ижевск")
                .whereEqualTo("name", myFlexes[position].getName())
                .whereEqualTo("author", userUid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        userCountOfFlex--;
                                        try {
                                            Toast.makeText(Lenta.mContext, "Событие удалено", Toast.LENGTH_SHORT).show();
                                        } catch (NullPointerException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.d("ошибка", "Error getting documents: ", task.getException());
                        }
                    }
                });
        FirebaseFirestore.getInstance().collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                document.getReference().collection("requests_to_me")
                                        .whereEqualTo("name", myFlexes[position].getName())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                for (DocumentSnapshot document : task.getResult()) {
                                                    document.getReference()
                                                            .delete();
                                                }
                                            }
                                        });
                                document.getReference().collection("my_requests")
                                        .whereEqualTo("name", myFlexes[position].getName())
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                for (DocumentSnapshot document : task.getResult()) {
                                                    document.getReference()
                                                            .delete();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Log.d("ошибка", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }


    public static void acceptFlexStatic(final int position) {
        FirebaseFirestore.getInstance().collection("users").document(userUid)
                .collection("requests_to_me")
                .whereEqualTo("authorNick", to_me_requests[position].getUserNick())
                .whereEqualTo("author", to_me_requests[position].getUser())
                .whereEqualTo("name", to_me_requests[position].getName())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                document.getReference()
                                        .update("accept", true)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FirebaseFirestore.getInstance().collection("users").document(to_me_requests[position].getUser())
                                                        .collection("my_requests")
                                                        .whereEqualTo("author", userUid)
                                                        .whereEqualTo("name", to_me_requests[position].getName())
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    for (DocumentSnapshot document : task.getResult()) {
                                                                        document.getReference()
                                                                                .update("accept", true);
                                                                    }
                                                                } else {
                                                                    Log.e("Lenta", "Error getting documents: ", task.getException());
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            }
                        } else {
                            Log.e("Lenta", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public static void denyFlexStatic(final int position) {
        FirebaseFirestore.getInstance().collection("users").document(userUid)
                .collection("requests_to_me")
                .whereEqualTo("authorNick", to_me_requests[position].getUserNick())
                .whereEqualTo("author", to_me_requests[position].getUser())
                .whereEqualTo("name", to_me_requests[position].getName())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                document.getReference()
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                FirebaseFirestore.getInstance().collection("users").document(to_me_requests[position].getUser())
                                                        .collection("my_requests")
                                                        .whereEqualTo("author", userUid)
                                                        .whereEqualTo("name", to_me_requests[position].getName())
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                                if (task.isSuccessful()) {
                                                                    for (DocumentSnapshot document : task.getResult()) {
                                                                        document.getReference()
                                                                                .delete();
                                                                    }
                                                                } else {
                                                                    Log.e("Lenta", "Error getting documents: ", task.getException());
                                                                }
                                                            }
                                                        });
                                            }
                                        });
                            }
                        } else {
                            Log.e("Lenta", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        my_requests = null;
        to_me_requests = null;
        userNickname = null;
        userDescription = null;
        userUid = null;
        userVk = null;
        alreadyLiked = null;
        flexes = null;
        friendsFlexes = null;
        myFlexes = null;
        my_subscriptions = null;
        my_subscribers = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ImageSelectorActivity.REQUEST_IMAGE) {
            images = (ArrayList<String>) data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
            try {
                imageUpload();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
