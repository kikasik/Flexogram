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

class AddFriendAdapter extends BaseAdapter {

    private List<My_subscriber> usersList;
    private ArrayList<My_subscriber> arraylist;

    private Context mContext;

    AddFriendAdapter(Context mContext) {
        this.mContext = mContext;
        try {
            if (this.usersList == null) {
                this.usersList = new ArrayList<>(Arrays.asList(AddFriendPage.users));
                arraylist = new ArrayList<>();
                arraylist.addAll(this.usersList);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getCount() {
        try {
            return usersList.size();
        } catch (Exception e) {
            e.printStackTrace();
            usersList = new ArrayList<>(Arrays.asList(AddFriendPage.users));
            return usersList.size();
        }
    }

    @Override
    public Object getItem(int position) {
        return usersList.get(position);
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
        ((TextView) view.findViewById(R.id.friendName)).setText(usersList.get(position).getUserNick());
        view.findViewById(R.id.friendName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(mContext, UserPage.class);
                intent.putExtra(Lenta.EXTRA_AUTHOR, usersList.get(position).getUser());
                mContext.startActivity(intent);
            }
        });
            view.findViewById(R.id.btnAccept).setVisibility(View.INVISIBLE);
        return view;
    }

    void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        arraylist = new ArrayList<>();
        usersList = new ArrayList<>(Arrays.asList(AddFriendPage.users));
        arraylist.addAll(this.usersList);
        usersList.clear();
        if (charText.length() == 0) {
            usersList.addAll(arraylist);
        } else {
            for (My_subscriber subscriber : arraylist) {
                if (charText.length() != 0 && subscriber.getUserNick().toLowerCase(Locale.getDefault()).contains(charText)) {
                    usersList.add(subscriber);
                }
            }
        }
        notifyDataSetChanged();
    }
}

