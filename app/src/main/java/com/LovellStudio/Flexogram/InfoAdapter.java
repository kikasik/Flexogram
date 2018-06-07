package com.LovellStudio.Flexogram;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class InfoAdapter extends BaseAdapter {

    InfoAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private Context mContext;

    @Override
    public int getCount() {
        return Lenta.my_requests.length;
    }

    @Override
    public Object getItem(int position) {
        return Lenta.my_requests[position];
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
            view = inflater.inflate(R.layout.info_item, viewGroup, false);
        } else {
            view = convertView;
        }
        final TextView textView = view.findViewById(R.id.flexIcon);
        ((TextView) view.findViewById(R.id.flexName)).setText(Lenta.my_requests[position].getName());

        view.findViewById(R.id.infoBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("InfoAdapter", "click");
                final Intent intent = new Intent(mContext, FlexInfo.class);
                ObjectAnimator animator = ObjectAnimator.ofFloat(textView,"rotation", 0, 360f).setDuration(1000);
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {}

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        for (int i=0;i<Lenta.flexes.length;i++){
                            if (Lenta.flexes[i].getName().equals(Lenta.my_requests[position].getName())
                                    &&Lenta.flexes[i].getAuthor().equals(Lenta.my_requests[position].getAuthor())){
                                intent.putExtra(Lenta.EXTRA_FLEX, Lenta.flexes[i]);
                                mContext.startActivity(intent);

                            }
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {}

                    @Override
                    public void onAnimationRepeat(Animator animator) {}
                });
            }
        });
        if (Lenta.my_requests[position].getAccept()) {
            ((TextView) view.findViewById(R.id.flexAccepted)).setText("Одобрено");
            view.findViewById(R.id.infoBtn).setEnabled(true);
        } else {
            ((TextView) view.findViewById(R.id.flexAccepted)).setText("Пока не одобрено");
            view.findViewById(R.id.infoBtn).setEnabled(false);
            ((TextView) view.findViewById(R.id.flexAccepted)).setTextColor(mContext.getResources().getColor(R.color.sand));
        }
        return view;
    }
}