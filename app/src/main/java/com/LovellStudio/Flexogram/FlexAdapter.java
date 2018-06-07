package com.LovellStudio.Flexogram;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;

import java.io.File;
import java.io.IOException;

class FlexAdapter extends BaseAdapter {

    FlexAdapter(Context mContext, Flex[] flexes) {
        this.mContext = mContext;
        this.flexes = flexes;
    }

    private Context mContext;
    private Flex[] flexes;

    @Override
    public int getCount() {
        return flexes.length;
    }

    @Override
    public Object getItem(int position) {
        return flexes[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.flex_item, viewGroup, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.flexImage.setImageResource(0);
        ObjectAnimator animator = ObjectAnimator.ofFloat(viewHolder.loadingImage, "rotation", 0, 360f).setDuration(1000);
        try {
            if (!flexes[position].getImageRef().equals("")) {
                animator.setRepeatCount(2);
                animator.start();
                FirebaseStorage.getInstance().getReference().child(flexes[position].getImageRef());

                File localFile = null;
                try {
                    localFile = File.createTempFile("images", "jpg");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                final File finalLocalFile = localFile;
                FirebaseStorage.getInstance().getReference()
                        .child(flexes[position].getImageRef())
                        .getFile(localFile)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                ObjectAnimator.ofFloat(viewHolder.flexImage, View.ALPHA, 0f, 1f).start();
                                viewHolder.flexImage.setImageURI(Uri.fromFile(finalLocalFile));
                            }
                        });
            } else {
                viewHolder.flexImage.setImageResource(0);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            viewHolder.flexImage.setImageResource(0);
        }
        viewHolder.name.setText(flexes[position].getName());
        if (!flexes[position].getCost().equals("")) {
            viewHolder.free.setText(flexes[position].getCost());
        } else viewHolder.free.setText("0");
        viewHolder.date.setText(flexes[position].getDate());
        viewHolder.time.setText(flexes[position].getTime());
        viewHolder.moreBtn.setTag(position);
        viewHolder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Lenta.createRequest((Integer) view.getTag());
                ObjectAnimator.ofFloat(viewHolder.flexIcon, "rotation", 0, 360).setDuration(1000).start();
            }
        });
        return convertView;
    }

    private class ViewHolder {
        final ImageView flexImage;
        final TextView flexIcon, loadingImage, name, free, date, time;
        final Button moreBtn;

        ViewHolder(View view) {
            flexImage = view.findViewById(R.id.flexImage);
            flexIcon = view.findViewById(R.id.flexIcon);
            loadingImage = view.findViewById(R.id.loading_image);
            name = view.findViewById(R.id.name);
            free = view.findViewById(R.id.free);
            date = view.findViewById(R.id.date);
            time = view.findViewById(R.id.time);
            moreBtn = view.findViewById(R.id.moreBtn);
        }
    }

}
