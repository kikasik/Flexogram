package com.LovellStudio.Flexogram;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class MySubscribersAdapter extends BaseAdapter {

    private List<My_subscriber> subscribersList;
    private ArrayList<My_subscriber> arraylist;

    private Context mContext;

    MySubscribersAdapter(Context mContext) {
        this.mContext = mContext;
        try {
            if (this.subscribersList == null) {
                this.subscribersList = new ArrayList<>(Arrays.asList(Lenta.my_subscribers));
                arraylist = new ArrayList<>();
                arraylist.addAll(this.subscribersList);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        try {
            return subscribersList.size();
        } catch (Exception e) {
            e.printStackTrace();
            subscribersList = new ArrayList<>(Arrays.asList(Lenta.my_subscribers));
            return subscribersList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return subscribersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.my_subscriber_item, viewGroup, false);
        } else {
            view = convertView;
        }
        ((TextView) view.findViewById(R.id.friendName)).setText(subscribersList.get(position).getUserNick());
        final View btnAccept = view.findViewById(R.id.btnAccept);
        view.findViewById(R.id.friendName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(mContext, UserPage.class);
                intent.putExtra(Lenta.EXTRA_AUTHOR, subscribersList.get(position).getUser());
                mContext.startActivity(intent);
            }
        });
        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                ObjectAnimator.ofFloat(btnAccept, "rotation", 0f, 360f).start();
                Map<String, Object> newSubscription = new HashMap<>();
                newSubscription.put("accepted", "true");
                newSubscription.put("user", subscribersList.get(position).getUser());
                newSubscription.put("userNick", subscribersList.get(position).getUserNick());

                FirebaseFirestore.getInstance().collection("users").document(Lenta.userUid)
                        .collection("my_subscriptions").document(subscribersList.get(position).getUser())
                        .set(newSubscription)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    btnAccept.setVisibility(View.INVISIBLE);
                                }
                            }
                        });

                Map<String, Object> newSubscriber = new HashMap<>();
                newSubscriber.put("accepted", "true");
                newSubscriber.put("user", Lenta.userUid);
                newSubscriber.put("userNick", Lenta.userNickname);

                FirebaseFirestore.getInstance().collection("users").document(subscribersList.get(position).getUser())
                        .collection("my_subscribers").document(Lenta.userUid)
                        .set(newSubscriber);
            }
        });
        for (int i = 0; i < Lenta.my_subscriptions.length; i++) {
            if (Lenta.my_subscriptions[i].getUser().equals(subscribersList.get(position).getUser())) {
                if (Lenta.my_subscriptions[i].getAccepted().equals("true")) {
                    view.findViewById(R.id.btnAccept).setVisibility(View.INVISIBLE);
                }
                break;
            }
        }
        return view;
    }

    void filter(String charText) {

        charText = charText.toLowerCase(Locale.getDefault());
        arraylist = new ArrayList<>();
        subscribersList = new ArrayList<>(Arrays.asList(Lenta.my_subscribers));
        arraylist.addAll(this.subscribersList);
        subscribersList.clear();
        if (charText.length() == 0) {
            subscribersList.addAll(arraylist);
        } else {
            for (My_subscriber subscriber : arraylist) {
                if (charText.length() != 0 && subscriber.getUserNick().toLowerCase(Locale.getDefault()).contains(charText)) {
                    subscribersList.add(subscriber);
                }
            }
        }
        notifyDataSetChanged();
    }
}

