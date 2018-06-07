package com.LovellStudio.Flexogram;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class MyFlexAdapter extends BaseAdapter {

    MyFlexAdapter(Context mContext, Flex[] flexes) {
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
        View view;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            view = inflater.inflate(R.layout.my_flex_item, viewGroup, false);
        } else {
            view = convertView;
        }
        final TextView textView = view.findViewById(R.id.flexIcon);
        textView.setTag(position);
        ((TextView) view.findViewById(R.id.flexName)).setText(flexes[position].getName());
        if (flexes[0].getAuthor().equals(Lenta.EXTRA_USER)) {
            view.findViewById(R.id.myFlexInfo).setVisibility(View.VISIBLE);
            view.findViewById(R.id.myFlexDelete).setVisibility(View.VISIBLE);
            view.findViewById(R.id.myFlexInfo).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Intent intent = new Intent(mContext, FlexInfo.class);
                    ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "rotation", 0, 360).setDuration(1000);
                    animator.start();
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            intent.putExtra(Lenta.EXTRA_FLEX, flexes[position]);
                            mContext.startActivity(intent);
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
            view.findViewById(R.id.myFlexDelete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "rotation", 0, 360).setDuration(1000);
                    animator.start();
                    animator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {}

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            Lenta.deleteFlex((int) textView.getTag());
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {}

                        @Override
                        public void onAnimationRepeat(Animator animator) {}
                    });
                }
            });
        } else {
            view.findViewById(R.id.myFlexInfo).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.myFlexDelete).setVisibility(View.INVISIBLE);
            for (int i=0;i<Lenta.my_requests.length;i++){
                if (Lenta.my_requests[i].getName().equals(flexes[position].getName())){
                    if (Lenta.my_requests[i].getAccept()) {
                        view.findViewById(R.id.myFlexInfo).setVisibility(View.VISIBLE);
                        view.findViewById(R.id.myFlexInfo).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Intent intent = new Intent(mContext, FlexInfo.class);
                                ObjectAnimator animator = ObjectAnimator.ofFloat(textView, "rotation", 0, 360).setDuration(1000);
                                animator.start();
                                animator.addListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animator) {}

                                    @Override
                                    public void onAnimationEnd(Animator animator) {
                                        intent.putExtra(Lenta.EXTRA_FLEX, flexes[position]);
                                        mContext.startActivity(intent);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animator) {}

                                    @Override
                                    public void onAnimationRepeat(Animator animator) {}
                                });
                            }
                        });
                    }
                }
            }
        }
        return view;
    }
}
