package com.LovellStudio.Flexogram;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;

public class FlexInfo extends AppCompatActivity {

    TextView mName, mDesc, mDate, mTime, mCost, mCount, mContacts, mCity, mStreet, mHouse, flexIcon;
    Flex flex;
    EditText passwordEt;
    Button checkPasswordBtn;
    ImageView mBackBtn, flexInfoImage;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flexinfo);

        initializing();
    }

    private void initializing() {
        Intent intent = getIntent();
        flex = (Flex) intent.getSerializableExtra(Lenta.EXTRA_FLEX);
        passwordEt = (EditText) findViewById(R.id.passwordEt);
        checkPasswordBtn = (Button) findViewById(R.id.checkPasswordBtn);
        mBackBtn = (ImageView) findViewById(R.id.flexInfoBackBtn);
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        flexIcon = (TextView) findViewById(R.id.flexIcon);
        mName = (TextView) findViewById(R.id.flexInfoName);
        mDesc = (TextView) findViewById(R.id.flexInfoDesc);
        mDate = (TextView) findViewById(R.id.flexInfoDate);
        mTime = (TextView) findViewById(R.id.flexInfoTime);
        mCost = (TextView) findViewById(R.id.flexInfoCost);
        mCount = (TextView) findViewById(R.id.flexInfoCount);
        mContacts = (TextView) findViewById(R.id.flexInfoContacts);
        mCity = (TextView) findViewById(R.id.flexInfoCity);
        mStreet = (TextView) findViewById(R.id.flexInfoStreet);
        mHouse = (TextView) findViewById(R.id.flexInfoHouse);
        mName.setText(flex.getName());
        mDesc.setText(flex.getDescription());
        mDate.setText(flex.getDate());
        mTime.setText(flex.getTime());
        if (!Boolean.parseBoolean(flex.getFree())) {
            mCost.setText("0");
        } else mCost.setText(flex.getCost());
        if (flex.getPassword().equals("")) ((View) passwordEt.getParent()).setVisibility(View.INVISIBLE);
        else ((View) passwordEt.getParent()).setVisibility(View.VISIBLE);
        checkPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View currentFocus = FlexInfo.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
                }
                if (passwordEt.getText().toString().equals(flex.getPassword())) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(flexIcon, "rotation", 0, 360f);
                    animator.setDuration(750);
                    animator.setStartDelay(500);
                    animator.start();
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            ObjectAnimator.ofFloat(((View) checkPasswordBtn.getParent()), "y", ((View) checkPasswordBtn.getParent()).getY(), 1000).start();
                            ObjectAnimator layoutAnimator = ObjectAnimator.ofFloat(((View) checkPasswordBtn.getParent()), View.ALPHA, 1f, 0f);
                            layoutAnimator.start();
                            layoutAnimator.addListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    ((View) checkPasswordBtn.getParent()).setVisibility(View.GONE);
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {

                                }
                            });
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animator) {

                        }
                    });
                } else passwordEt.setText("");
            }
        });
        mCount.setText(flex.getCount());
        mCity.setText(flex.getCity());
        mStreet.setText(flex.getStreet());
        mHouse.setText(flex.getHouse());
        mContacts.setText(("vk.com/"+flex.getAuthorVk()));
        try {
            if (!flex.getImageRef().equals("")) {
                TextView loadingImage = (TextView) findViewById(R.id.loading_image);
                ObjectAnimator animator = ObjectAnimator.ofFloat(loadingImage,"rotation",0,360f).setDuration(1000);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.start();
                flexInfoImage = (ImageView) findViewById(R.id.flexInfoImage);
                FirebaseStorage.getInstance().getReference().child(flex.getImageRef());

                File localFile = null;
                try {
                    localFile = File.createTempFile("images", "jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final File finalLocalFile = localFile;
                FirebaseStorage.getInstance().getReference()
                        .child(flex.getImageRef())
                        .getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                flexInfoImage.setImageURI(Uri.fromFile(finalLocalFile));
                            }
                        });
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
