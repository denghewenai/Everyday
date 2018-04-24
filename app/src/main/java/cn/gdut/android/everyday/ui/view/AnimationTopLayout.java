package cn.gdut.android.everyday.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import cn.gdut.android.everyday.R;


/**
 * Created by denghewen on 17/2/8.
 */
public class AnimationTopLayout extends FrameLayout {
    private static final int ANIMATION_TIME = 300;
    private View mChildView;

    public AnimationTopLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() != 1) {
            throw new IllegalArgumentException("只能有一个ChildView");
        }
        mChildView = getChildAt(0);
    }

    public void doAnimation() {
        ObjectAnimator downAnimation = ObjectAnimator.ofFloat(mChildView, "Y", 0f, getResources().getDimension(R.dimen.dimen_48dip));
        downAnimation.setDuration(ANIMATION_TIME / 2);
        downAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mOnAnimationHalfFinishListener != null) {
                    mOnAnimationHalfFinishListener.onHalf(mChildView);
                }
            }
        });
        ObjectAnimator upAnimation = ObjectAnimator.ofFloat(mChildView, "Y", -getResources().getDimension(R.dimen.dimen_48dip), 0f);
        upAnimation.setDuration(ANIMATION_TIME / 2);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(downAnimation, upAnimation);
        animatorSet.start();
    }

    private OnAnimationHalfFinishListener mOnAnimationHalfFinishListener;

    public void setOnAnimationHalfFinishListener(OnAnimationHalfFinishListener onAnimationHalfFinishListener) {
        mOnAnimationHalfFinishListener = onAnimationHalfFinishListener;
    }

    public interface OnAnimationHalfFinishListener {
        void onHalf(View view);
    }
}
