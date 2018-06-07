package com.LovellStudio.Flexogram;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class MySubscriptionsAdapter extends BaseAdapter {

    private List<My_subscription> subscriptionsList;
    private ArrayList<My_subscription> arraylist;

    private Context mContext;

    MySubscriptionsAdapter(Context mContext) {
        this.mContext = mContext;
        try {
            if (this.subscriptionsList==null) {
                this.subscriptionsList = new ArrayList<>(Arrays.asList(Lenta.my_subscriptions));
                arraylist = new ArrayList<>();
                arraylist.addAll(this.subscriptionsList);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        try {
            return subscriptionsList.size();
        } catch (Exception e) {
            e.printStackTrace();
            subscriptionsList = new ArrayList<>(Arrays.asList(Lenta.my_subscriptions));
            return subscriptionsList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return subscriptionsList.get(position);
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
            view = inflater.inflate(R.layout.my_subscribtion_item, viewGroup, false);
        } else {
            view = convertView;
        }
        ((TextView) view.findViewById(R.id.friendName)).setText(subscriptionsList.get(position).getUserNick());

        view.findViewById(R.id.friendBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(mContext, UserPage.class);
                intent.putExtra(Lenta.EXTRA_AUTHOR, subscriptionsList.get(position).getUser());
                mContext.startActivity(intent);
            }
        });
        return view;
    }

    void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        arraylist = new ArrayList<>();
        subscriptionsList = new ArrayList<>(Arrays.asList(Lenta.my_subscriptions));
        arraylist.addAll(this.subscriptionsList);
        subscriptionsList.clear();
        if (charText.length() == 0) {
            subscriptionsList.addAll(arraylist);
        } else {
            for (My_subscription subscriber : arraylist) {
                if (charText.length() != 0 && subscriber.getUserNick().toLowerCase(Locale.getDefault()).contains(charText)) {
                    subscriptionsList.add(subscriber);
                }
            }
        }
        notifyDataSetChanged();
    }
}

