package com.globant.labs.swipper2.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

import com.globant.labs.swipper2.R;

public class ReviewsExpandablePanel extends LinearLayout {

    private final int mHandleId;
    private final int mContentId;

    private View mHandle;
    private View mContent;

    private boolean mExpanded = false;
    private int mCollapsedHeight = 0;
    private int mContentHeight = 0;
    private int mAnimationDuration = 0;
    private int mHandleHeight = 0;

    private OnExpandListener mListener;

    public ReviewsExpandablePanel(Context context) {
        this(context, null);
    }

    public ReviewsExpandablePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mListener = new DefaultOnExpandListener();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandablePanel, 0, 0);

        // How high the content should be in "collapsed" state
        mCollapsedHeight = (int) a.getDimension(R.styleable.ExpandablePanel_collapsedHeight, 0.0f);

        // How long the animation should take
        mAnimationDuration = a.getInteger(R.styleable.ExpandablePanel_animationDuration, 500);

        int handleId = a.getResourceId(R.styleable.ExpandablePanel_handle, 0);
        if (handleId == 0) {
            throw new IllegalArgumentException(
                "The handle attribute is required and must refer "
                    + "to a valid child.");
        }

        int contentId = a.getResourceId(R.styleable.ExpandablePanel_content, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException("The content attribute is required and must refer to a valid child.");
        }

        mHandleId = handleId;
        mContentId = contentId;

        a.recycle();
    }

    public void setOnExpandListener(OnExpandListener listener) {
        mListener = listener; 
    }

    public void setCollapsedHeight(int collapsedHeight) {
        mCollapsedHeight = collapsedHeight;
    }

    public void setAnimationDuration(int animationDuration) {
        mAnimationDuration = animationDuration;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mHandle = findViewById(mHandleId);
        if (mHandle == null) {
            throw new IllegalArgumentException(
                "The handle attribute is must refer to an"
                    + " existing child.");
        }

        mContent = findViewById(mContentId);
        if (mContent == null) {
            throw new IllegalArgumentException(
                "The content attribute must refer to an"
                    + " existing child.");
        }

        android.view.ViewGroup.LayoutParams lp = mContent.getLayoutParams();
        lp.height = mCollapsedHeight;
        mContent.setLayoutParams(lp);

        mHandle.setOnClickListener(new PanelToggler());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // First, measure how high content wants to be
        mContent.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
        mContentHeight = mContent.getMeasuredHeight();
        mHandleHeight = mHandle.getMeasuredHeight();

        if(!mExpanded) {
	        LinearLayout layout = ((LinearLayout) mContent);
	        if(layout.getChildAt(0) != null) {
	        	mCollapsedHeight = layout.getChildAt(0).getMeasuredHeight() + layout.getPaddingTop() + layout.getPaddingBottom();
	            android.view.ViewGroup.LayoutParams lp = mContent.getLayoutParams();
	            lp.height = mCollapsedHeight;
	            mContent.setLayoutParams(lp);
	        }
        }
        
        mContent.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
        mContentHeight = mContent.getMeasuredHeight();
        mHandleHeight = mHandle.getMeasuredHeight();        
        
        if(!mExpanded) {
	        if (mContentHeight <= mCollapsedHeight) {
	            mHandle.setVisibility(View.GONE);
	        } else {
	            mHandle.setVisibility(View.VISIBLE);
	        }
        }

        // Then let the usual thing happen
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class PanelToggler implements OnClickListener {
        public void onClick(View v) {
        	expand();
        }
    }
    
    public void expand() {
    	if(!mExpanded) {
	    	mListener.onBeforeExpand(mHandle, mContent);
	        Animation a;
            a = new ExpandAnimation(mCollapsedHeight, mContentHeight, mHandleHeight);             
	        mListener.onExpand(mHandle, mContent);
	        a.setDuration(mAnimationDuration);
	        mContent.startAnimation(a);
	        mExpanded = true;
    	}
    }

    private class ExpandAnimation extends Animation {
        private final int mStartHeight;
        private final int mDeltaHeight;
        private final int mHandleHeight;

        public ExpandAnimation(int startHeight, int endHeight, int handleHeight) {
            mStartHeight = startHeight;
            mDeltaHeight = endHeight - startHeight;
            mHandleHeight = handleHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
        	android.view.ViewGroup.LayoutParams hlp = mHandle.getLayoutParams();
            hlp.height = (int) (mHandleHeight * (1 - interpolatedTime));
            mHandle.setLayoutParams(hlp);
        	
            if(interpolatedTime == 1){
            	mHandle.setVisibility(View.GONE);
            }
            
        	android.view.ViewGroup.LayoutParams lp = mContent.getLayoutParams();
            lp.height = (int) (mStartHeight + mDeltaHeight * interpolatedTime);
            mContent.setLayoutParams(lp);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    public interface OnExpandListener {
    	public void onBeforeExpand(View handle, View content);
        public void onExpand(View handle, View content); 
        public void onCollapse(View handle, View content);
    }

    private class DefaultOnExpandListener implements OnExpandListener {
    	public void onBeforeExpand(View handle, View content) {}
        public void onCollapse(View handle, View content) {}
        public void onExpand(View handle, View content) {}
    }
}