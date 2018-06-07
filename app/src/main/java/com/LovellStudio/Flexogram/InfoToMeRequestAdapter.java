package com.LovellStudio.Flexogram;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.BounceInterpolator;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

class InfoToMeRequestAdapter extends BaseAdapter {

    InfoToMeRequestAdapter(Context mContext) {
        this.mContext = mContext;
    }

    private Context mContext;

    @Override
    public int getCount() {
        return Lenta.to_me_requests.length;
    }

    @Override
    public Object getItem(int position) {
        return Lenta.to_me_requests[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        View view;
        if (convertView==null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.info_to_me_request_item, viewGroup, false);
        } else {
            view = convertView;
        }
        final TextView flexIcon = view.findViewById(R.id.flexIcon);
        final TextView acceptedTv = view.findViewById(R.id.flexAccepted);
        final TextView deletedTv = view.findViewById(R.id.deleteInfo);
        ((TextView) view.findViewById(R.id.flexName)).setText(Lenta.to_me_requests[position].getName());
        view.findViewById(R.id.infoBtn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Lenta.acceptFlexStatic(position);
                ObjectAnimator animator = ObjectAnimator.ofFloat(flexIcon,"rotation", 0, 360f).setDuration(1000);
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        view.findViewById(R.id.infoBtn1).setEnabled(false);
                        acceptedTv.setText("Одобрено");
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
        });
        view.findViewById(R.id.infoBtn2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Lenta.denyFlexStatic(position);
                ObjectAnimator animator = ObjectAnimator.ofFloat(flexIcon,"rotation", 0, 360f).setDuration(1000);
                animator.start();
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        ObjectAnimator.ofFloat((LinearLayout) flexIcon.getParent().getParent(),View.ALPHA,1f,0f).start();
                        ObjectAnimator deletedTv_animator = ObjectAnimator.ofFloat(deletedTv, View.SCALE_X, 0.5f, 1f);
                        deletedTv_animator.setDuration(1000);
                        deletedTv_animator.setInterpolator(new BounceInterpolator());
                        deletedTv_animator.start();
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {

                    }
                });
            }
        });
        if (Lenta.to_me_requests[position].getAccept()){
            ((TextView) view.findViewById(R.id.flexAccepted)).setText("Одобрено");
            view.findViewById(R.id.infoBtn1).setEnabled(false);
        } else ((TextView) view.findViewById(R.id.flexAccepted)).setText("");
        ((TextView) view.findViewById(R.id.flexUserNick)).setText(Lenta.to_me_requests[position].getUserNick());
        view.findViewById(R.id.flexUserNick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent intent = new Intent(mContext, UserPage.class);
                intent.putExtra(Lenta.EXTRA_AUTHOR, Lenta.to_me_requests[position].getUser());
                mContext.startActivity(intent);
            }
        });
        return view;
    }
}