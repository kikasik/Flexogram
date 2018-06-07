package com.LovellStudio.Flexogram;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bigkoo.pickerview.TimePickerView;
import com.dd.CircularProgressButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yongchun.library.view.ImageSelectorActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import id.zelory.compressor.Compressor;

public class AddFlex extends AppCompatActivity {

    private String TAG = "AddFlex";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    ImageView calendarBtn, clockBtn, backBtn, getPhotoBtn;
    MaterialEditText nameEt, descEt, dateEt, timeEt, pinEt, priceEt, freePlace, cityEt, streetEt, houseEt;
    private TimePickerView pvTime;
    private CircularProgressButton progressButton;
    private String user, userNick, userDesc, userVk, errors = "", imageRef;
    private ToggleButton togglePrivate, toggleCost;
    TextView errorTv;
    ArrayList<String> images;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addflex);

        initialization();
    }

    void initialization() {
        Intent intent = getIntent();
        user = intent.getStringExtra(Lenta.EXTRA_AUTHOR);
        userNick = intent.getStringExtra(Lenta.EXTRA_AUTHOR_NICK);
        userDesc = intent.getStringExtra(Lenta.EXTRA_AUTHOR_DESCRIPTION);
        userVk = intent.getStringExtra(Lenta.EXTRA_AUTHOR_VK);
        nameEt = (MaterialEditText) findViewById(R.id.nameEt);
        descEt = (MaterialEditText) findViewById(R.id.descEt);
        dateEt = (MaterialEditText) findViewById(R.id.dateEt);
        timeEt = (MaterialEditText) findViewById(R.id.timeEt);
        priceEt = (MaterialEditText) findViewById(R.id.priceEt);
        freePlace = (MaterialEditText) findViewById(R.id.freePlace);
        cityEt = (MaterialEditText) findViewById(R.id.cityEt);
        streetEt = (MaterialEditText) findViewById(R.id.streetEt);
        houseEt = (MaterialEditText) findViewById(R.id.houseEt);
        pinEt = (MaterialEditText) findViewById(R.id.pin);

        errorTv = (TextView) findViewById(R.id.errorTv);

        backBtn = (ImageView) findViewById(R.id.backToLenta);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        getPhotoBtn = (ImageView) findViewById(R.id.getPhoto);
        getPhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissionREAD_EXTERNAL_STORAGE(AddFlex.this)) {
                    ImageSelectorActivity.start(AddFlex.this, 1, ImageSelectorActivity.MODE_SINGLE, true, true, true);
                }
            }
        });

        togglePrivate = (ToggleButton) findViewById(R.id.togglePrivate);
        togglePrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    pinEt.setEnabled(true);
                    pinEt.setMinCharacters(4);
                } else {
                    pinEt.setText("");
                    pinEt.setEnabled(false);
                    pinEt.setMinCharacters(0);
                }
            }
        });

        toggleCost = (ToggleButton) findViewById(R.id.toggleCost);
        toggleCost.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    priceEt.setEnabled(true);
                } else {
                    priceEt.setText("");
                    priceEt.setEnabled(false);
                }
            }
        });
        final Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        final Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + 7);

        calendarBtn = (ImageView) findViewById(R.id.calendarBtn);
        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pvTime = new TimePickerView.Builder(AddFlex.this, new TimePickerView.OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {//Callback
                        dateEt.setText(getTime(date));
                    }
                })
                        .setType(new boolean[]{true, true, true, false, false, false})// type of date
                        .setCancelText("Отмена")
                        .setSubmitText("Ок")
                        .setContentSize(18)
                        .setTitleSize(20)
                        .setTitleText("ДATA")
                        .setTitleColor(Color.WHITE)
                        .setSubmitColor(Color.WHITE)
                        .setCancelColor(Color.WHITE)
                        .setTitleBgColor(Color.BLACK)//night mode
                        .setBgColor(Color.WHITE)//night mode
                        .setRangDate(startDate, endDate)
                        .setLabel("Год", "Месяц", "День", "hours", "mins", "seconds")
                        .setDividerColor(Color.BLACK)
                        .build();
                pvTime.show();
            }
        });

        clockBtn = (ImageView) findViewById(R.id.clockBtn);
        clockBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pvTime = new TimePickerView.Builder(AddFlex.this, new TimePickerView.OnTimeSelectListener() {
                    @Override
                    public void onTimeSelect(Date date, View v) {//Callback
                        timeEt.setText(DateFormat.format("kk:mm", date).toString());
                    }
                })
                        .setType(new boolean[]{false, false, false, true, true, false})// type of date
                        .setCancelText("Отмена")
                        .setSubmitText("Ок")
                        .setContentSize(18)
                        .setTitleSize(20)
                        .setTitleText("ВРЕМЯ")
                        .setTitleColor(Color.WHITE)
                        .setSubmitColor(Color.WHITE)
                        .setCancelColor(Color.WHITE)
                        .setTitleBgColor(Color.BLACK)//night mode
                        .setBgColor(Color.WHITE)//night mode
                        .setRangDate(startDate, endDate)
                        .setLabel("Год", "Месяц", "День", "Час(ов)", "Минут", "минут")
                        .setDividerColor(Color.BLACK)
                        .build();
                pvTime.show();
            }
        });
        progressButton = (CircularProgressButton) findViewById(R.id.progressButton);
        progressButton.setIndeterminateProgressMode(true);
        progressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (progressButton.getProgress()<=0) {
                    errors = "";
                    if (nameEt.isCharactersCountValid())
                        if (descEt.isCharactersCountValid())
                            if (!togglePrivate.isChecked() || pinEt.isCharactersCountValid())
                                if (dateEt.length() >= 10 && (dateEt.getText().toString().substring(0,4).equals("2017")||dateEt.getText().toString().substring(0,4).equals("2018")))
                                    if (timeEt.length() >= 3)
                                        if (!toggleCost.isChecked() || toggleCost.isChecked() && priceEt.length() >= 1)
                                            if (freePlace.length() >= 1 && (!freePlace.getText().toString().equals("0") || !freePlace.getText().toString().equals("00")))
                                                if (cityEt.isCharactersCountValid())
                                                    if (streetEt.isCharactersCountValid())
                                                        if (houseEt.isCharactersCountValid())
                                                            if (userVk.length() >= 3)
                                                                findFlexWithThisName();
                                                            else
                                                                errors += "свой адрес Вконтакте на начальном экране";
                                                        else errors += "дом ";
                                                    else errors += "улицу ";
                                                else errors += "город ";
                                            else errors += "количество мест ";
                                        else errors += "стоимость ";
                                    else errors += "время ";
                                else errors += "дату ";
                            else errors += "пароль ";
                        else errors += "описание ";
                    else errors += "название ";
                    if (errors.length() == 0) {
                        progressButton.setProgress(50);
                        errorTv.setText("");
                    } else errorTv.setText(("Проверьте " + errors));
                }
            }
        });

    }

    private void findFlexWithThisName() {
        if (Lenta.userCountOfFlex < 2) {
            FirebaseFirestore.getInstance().collection(cityEt.getText().toString())
                    .whereEqualTo("city", cityEt.getText().toString())
                    .whereEqualTo("name", nameEt.getText().toString())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult().size() == 0) {
                                    try {
                                        imageUpload();
                                        Lenta.userCountOfFlex++;
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(AddFlex.this, "Событие с таким названием уже есть.", Toast.LENGTH_SHORT).show();
                                    progressButton.setProgress(-1);
                                    ObjectAnimator animator = ObjectAnimator.ofFloat(progressButton, "rotation", 0, 360f);
                                    animator.setDuration(1000);
                                    animator.setStartDelay(700);
                                    animator.start();
                                    animator.addListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animator) {
                                            progressButton.setProgress(0);
                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationCancel(Animator animator) {

                                        }

                                        @Override
                                        public void onAnimationRepeat(Animator animator) {

                                        }
                                    });
                                }

                            } else {
                                Log.d("ошибка", "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else {
            Toast.makeText(AddFlex.this, "У вас активно максимальное кол-во событий.", Toast.LENGTH_SHORT).show();
            progressButton.setProgress(0);
            ObjectAnimator animator = ObjectAnimator.ofFloat(progressButton, "rotation", 0, 360f);
            animator.setDuration(1000);
            animator.setStartDelay(700);
            animator.start();
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                    progressButton.setProgress(0);
                }

                @Override
                public void onAnimationEnd(Animator animator) {

                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
        }

    }

    private void imageUpload() throws IOException {
        try {
            File compressedImageFile = new Compressor(this).compressToFile(new File(images.get(0)));
            Uri file = Uri.fromFile(compressedImageFile);
            imageRef = "images/" + user + nameEt.getText().toString() + (100000 + (int) (Math.random() * ((999999 - 100000) + 1))) + ".png";
            StorageReference riversRef = FirebaseStorage.getInstance().getReference()
                    .child(imageRef);
            riversRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            createFlex();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Log.e(TAG, "ошибка загрузки фото");
                        }
                    });
        } catch (NullPointerException e) {
            e.printStackTrace();
            createFlex();
        }
    }

    private void createFlex() {
        Log.e(TAG, "flex, flex");
        Map<String, Object> flex = new HashMap<>();
        Log.e(TAG, "прошел");
        flex.put("name", nameEt.getText().toString());
        flex.put("description", descEt.getText().toString());
        flex.put("private", Boolean.toString(togglePrivate.isChecked()));
        flex.put("password", pinEt.getText().toString());
        flex.put("date", dateEt.getText().toString());
        flex.put("time", timeEt.getText().toString());
        flex.put("free", Boolean.toString(toggleCost.isChecked()));
        flex.put("cost", priceEt.getText().toString());
        flex.put("count", freePlace.getText().toString());
        flex.put("city", cityEt.getText().toString());
        flex.put("street", streetEt.getText().toString());
        flex.put("house", houseEt.getText().toString());
        flex.put("author", user);
        flex.put("authorNick", userNick);
        flex.put("authorDesc", userDesc);
        flex.put("authorVk", userVk);
        flex.put("imageRef", imageRef);

        FirebaseFirestore.getInstance().collection(cityEt.getText().toString()).document(nameEt.getText().toString())
                .set(flex)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        progressButton.setProgress(100);
                        progressButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                finish();
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressButton.setProgress(-1);
                    }
                });
        Log.e(TAG, nameEt.getText().toString() + "\n" +
                descEt.getText().toString() + "\n" +
                togglePrivate.isChecked() + "\n" +
                pinEt.getText().toString() + "\n" +
                dateEt.getText().toString() + "\n" +
                timeEt.getText().toString() + "\n" +
                toggleCost.isChecked() + "\n" +
                priceEt.getText().toString() + "\n" +
                freePlace.getText().toString() + "\n" +
                cityEt.getText().toString() + "\n" +
                streetEt.getText().toString() + "\n" +
                houseEt.getText().toString() + "\n");
    }

    private String getTime(Date date) {
        return DateFormat.format("yyyy.MM.dd", date).toString();
    }

    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            android.Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // do your stuff
                } else {
                    //Toast.makeText("zhopa", "GET_ACCOUNTS Denied",
                    //        Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == ImageSelectorActivity.REQUEST_IMAGE) {
            images = (ArrayList<String>) data.getSerializableExtra(ImageSelectorActivity.REQUEST_OUTPUT);
        }
    }
}
