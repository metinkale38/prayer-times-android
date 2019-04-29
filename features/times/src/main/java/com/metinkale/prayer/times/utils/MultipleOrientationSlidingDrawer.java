/*
 * Copyright (c) 2013-2019 Metin Kale
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metinkale.prayer.times.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;


import com.metinkale.prayer.times.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class MultipleOrientationSlidingDrawer extends ViewGroup {

    private static final int TAP_THRESHOLD = 6;
    private static final float MAXIMUM_TAP_VELOCITY = 100.0f;
    private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
    private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
    private static final float MAXIMUM_ACCELERATION = 2000.0f;
    private static final int VELOCITY_UNITS = 1000;
    private static final int MSG_ANIMATE = 1000;
    private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

    private static final int EXPANDED_FULL_OPEN = -10001;
    private static final int COLLAPSED_FULL_CLOSED = -10002;

    private final int mHandleId;
    private final int mContentId;

    @Nullable
    private View mHandle;
    @Nullable
    private View mContent;

    private final Rect mFrame = new Rect();
    private final Rect mInvalidate = new Rect();
    private boolean mTracking;
    private boolean mLocked;

    @Nullable
    private VelocityTracker mVelocityTracker;

    private Orientation mOrientation;
    private Side mHandlePos;
    private boolean mVertical;
    private boolean mInvert;

    private boolean mExpanded;
    private int mBottomOffset;
    private int mTopOffset;
    private int mHandleHeight;
    private int mHandleWidth;
    private int mHandlePad;

    private OnDrawerOpenListener mOnDrawerOpenListener;
    private OnDrawerCloseListener mOnDrawerCloseListener;
    private OnDrawerScrollListener mOnDrawerScrollListener;

    private final Handler mHandler = new SlidingHandler();
    private float mAnimatedAcceleration;
    private float mAnimatedVelocity;
    private float mAnimationPosition;
    private long mAnimationLastTime;
    private long mCurrentAnimationTime;
    private int mTouchDelta;
    private boolean mAnimating;
    private boolean mAllowSingleTap = true;
    private boolean mAnimateOnClick = true;

    private final int mTapThreshold;
    private final int mMaximumTapVelocity;
    private final int mMaximumMinorVelocity;
    private final int mMaximumMajorVelocity;
    private final int mMaximumAcceleration;
    private final int mVelocityUnits;

    /**
     * Construct a <code>MultipleOrientationSlidingDrawer</code> object programmatically with the specified
     * <code>handle</code>, <code>content</code> and <code>orientation</code>.
     *
     * @param context     Activity context
     * @param handle      Cannot be <code>null</code>
     * @param content     Cannot be <code>null</code>
     * @param orientation TOP, LEFT, BOTTOM or RIGHT.
     */
    public MultipleOrientationSlidingDrawer(Context context, @Nullable View handle, @Nullable View content, Orientation orientation) {
        super(context);

        // handle
        if (handle == null) {
            throw new NullPointerException("Handle cannot be null.");
        }
        addView(mHandle = handle);
        mHandle.setOnClickListener(new DrawerToggler());

        // content
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }
        addView(mContent = content);
        mContent.setVisibility(View.GONE);

        mHandleId = mContentId = 0;

        setOrientation(orientation);

        float density = getResources().getDisplayMetrics().density;
        mTapThreshold = (int) ((TAP_THRESHOLD * density) + 0.5f);
        mMaximumTapVelocity = (int) ((MAXIMUM_TAP_VELOCITY * density) + 0.5f);
        mMaximumMinorVelocity = (int) ((MAXIMUM_MINOR_VELOCITY * density) + 0.5f);
        mMaximumMajorVelocity = (int) ((MAXIMUM_MAJOR_VELOCITY * density) + 0.5f);
        mMaximumAcceleration = (int) ((MAXIMUM_ACCELERATION * density) + 0.5f);
        mVelocityUnits = (int) ((VELOCITY_UNITS * density) + 0.5f);
    }

    /**
     * Creates a new SlidingDrawer from a specified set of attributes defined in XML.
     *
     * @param context The application's environment.
     * @param attrs   The attributes defined in XML.
     */
    public MultipleOrientationSlidingDrawer(@NonNull Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Creates a new SlidingDrawer from a specified set of attributes defined in XML.
     *
     * @param context  The application's environment.
     * @param attrs    The attributes defined in XML.
     * @param defStyle The style to apply to this widget.
     */
    public MultipleOrientationSlidingDrawer(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MultipleOrientationSlidingDrawer, defStyle, 0);

        int orientation = a.getInteger(R.styleable.MultipleOrientationSlidingDrawer__orientation, Orientation.TOP.value);
        setOrientation(Orientation.getByValue(orientation));
        mHandlePos = Side.getByValue(orientation);
        mBottomOffset = (int) a.getDimension(R.styleable.MultipleOrientationSlidingDrawer_bottomOffset, 0.0f);
        mTopOffset = (int) a.getDimension(R.styleable.MultipleOrientationSlidingDrawer_topOffset, 0.0f);
        mAllowSingleTap = a.getBoolean(R.styleable.MultipleOrientationSlidingDrawer_allowSingleTap, true);
        mAnimateOnClick = a.getBoolean(R.styleable.MultipleOrientationSlidingDrawer_animateOnClick, true);


        int handleId = a.getResourceId(R.styleable.MultipleOrientationSlidingDrawer_handle, 0);
        if (handleId == 0) {
            throw new IllegalArgumentException("The handle attribute is required and must refer "
                    + "to a valid child.");
        }

        int contentId = a.getResourceId(R.styleable.MultipleOrientationSlidingDrawer_content, 0);

        a.recycle();

        if (contentId == 0) {
            throw new IllegalArgumentException("The content attribute is required and must refer "
                    + "to a valid child.");
        }

        if (handleId == contentId) {
            throw new IllegalArgumentException("The content and handle attributes must refer "
                    + "to different children.");
        }

        mHandleId = handleId;
        mContentId = contentId;

        float density = getResources().getDisplayMetrics().density;
        mTapThreshold = (int) ((TAP_THRESHOLD * density) + 0.5f);
        mMaximumTapVelocity = (int) ((MAXIMUM_TAP_VELOCITY * density) + 0.5f);
        mMaximumMinorVelocity = (int) ((MAXIMUM_MINOR_VELOCITY * density) + 0.5f);
        mMaximumMajorVelocity = (int) ((MAXIMUM_MAJOR_VELOCITY * density) + 0.5f);
        mMaximumAcceleration = (int) ((MAXIMUM_ACCELERATION * density) + 0.5f);
        mVelocityUnits = (int) ((VELOCITY_UNITS * density) + 0.5f);

//        a.recycle();

        setAlwaysDrawnWithCacheEnabled(false);
    }

    /**
     * Get the current orientation of this sliding tray.
     */
    public Orientation getOrientation() {
        return mOrientation;
    }

    /**
     * Lets you change the orientation of the sliding tray at runtime.
     * <p/>
     * Orientation must be from one of TOP, LEFT, BOTTOM, RIGHT.
     *
     * @param orientation orientation of the sliding tray.
     */
    public void setOrientation(Orientation orientation) {
        mOrientation = orientation;

        mVertical = (mOrientation == Orientation.BOTTOM) || (mOrientation == Orientation.TOP);
        mInvert = (mOrientation == Orientation.LEFT) || (mOrientation == Orientation.TOP);

        requestLayout();
        invalidate();
    }

    /**
     * Get the current positioning of this sliding tray handle.
     */
    public Side getHandlePosition() {
        return mHandlePos;
    }

    /**
     * Change the handle positioning of the sliding tray at runtime.
     * <p/>
     * HandlePos must be {@link Side#TOP}, {@link Side#CENTER} or {@link Side#BOTTOM} for horizontal orientation
     * or must be {@link Side#LEFT}, {@link Side#CENTER} or {@link Side#RIGHT} for vertical orientation.
     * <p/>
     * Default is {@linkplain Side#CENTER}.
     *
     * @param side Handle Pos of the drawer handle.
     */
    public void setHandlePosition(Side side) {
        mHandlePos = side;
        requestLayout();
        invalidate();
    }

    /**
     * Add padding to drawer handle when handle is not centered.
     * <p/>
     * Note this padding is only effective when handle is not centered.
     *
     * @param padding padding in pixels.
     */
    public void setHandlePadding(int padding) {
        mHandlePad = padding;
        requestLayout();
        invalidate();
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mHandleId > 0) {
            mHandle = findViewById(mHandleId);
            if (mHandle == null) {
                throw new IllegalArgumentException("The handle attribute is must refer to an existing child.");
            }
            mHandle.setOnClickListener(new DrawerToggler());
        }

        if (mContentId > 0) {
            mContent = findViewById(mContentId);
            if (mContent == null) {
                throw new IllegalArgumentException("The content attribute is must refer to an existing child.");
            }
            mContent.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if ((widthSpecMode == MeasureSpec.UNSPECIFIED) || (heightSpecMode == MeasureSpec.UNSPECIFIED)) {
            throw new RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions");
        }

        View handle = mHandle;
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);

        if (mVertical) {
            int height = heightSpecSize - handle.getMeasuredHeight() - mTopOffset;
            mContent.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else {
            int width = widthSpecSize - handle.getMeasuredWidth() - mTopOffset;
            mContent.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY));
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        long drawingTime = getDrawingTime();
        View handle = mHandle;
        Orientation orientation = mOrientation;

        drawChild(canvas, handle, drawingTime);

        if (mTracking || mAnimating) {
            Bitmap cache = mContent.getDrawingCache();
            if (cache != null) {
                // called when opening

                switch (orientation) {
                    case TOP:
                        canvas.drawBitmap(cache, 0, handle.getTop() - cache.getHeight(), null);
                        break;
                    case LEFT:
                        canvas.drawBitmap(cache, handle.getLeft() - cache.getWidth(), 0, null);
                        break;
                    case BOTTOM:
                        canvas.drawBitmap(cache, 0, handle.getBottom(), null);
                        break;
                    case RIGHT:
                        canvas.drawBitmap(cache, handle.getRight(), 0, null);
                        break;
                }
            } else {
                // called when closing
                canvas.save();
                switch (orientation) {
                    case TOP:
                        canvas.translate(0, handle.getTop() - mContent.getHeight());
                        break;
                    case LEFT:
                        canvas.translate(handle.getLeft() - mContent.getWidth(), 0);
                        break;
                    case BOTTOM:
                        canvas.translate(0, handle.getTop() - mTopOffset);
                        break;
                    case RIGHT:
                        canvas.translate(handle.getLeft() - mTopOffset, 0);
                        break;
                }
                drawChild(canvas, mContent, drawingTime);
                canvas.restore();
            }
        } else if (mExpanded) {
            drawChild(canvas, mContent, drawingTime);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mTracking) {
            return;
        }

        int width = r - l;
        int height = b - t;

        View handle = mHandle;

        int childWidth = handle.getMeasuredWidth();
        int childHeight = handle.getMeasuredHeight();

        int childLeft = 0;
        int childTop = 0;

        View content = mContent;


        switch (mOrientation) {

            case TOP:
                switch (mHandlePos) {
                    case LEFT:
                        childLeft = mHandlePad;
                        break;
                    case RIGHT:
                        childLeft = width - childWidth - mHandlePad;
                        break;
                    default:
                        childLeft = (width - childWidth) / 2;
                        break;
                }
                childTop = mExpanded ? (height - childHeight - mTopOffset) : -mBottomOffset;

                content.layout(0, height - childHeight - mTopOffset - content.getMeasuredHeight(),
                        content.getMeasuredWidth(), height - childHeight - mTopOffset);
                break;

            case BOTTOM:
                switch (mHandlePos) {
                    case LEFT:
                        childLeft = mHandlePad;
                        break;
                    case RIGHT:
                        childLeft = width - childWidth - mHandlePad;
                        break;
                    default:
                        childLeft = (width - childWidth) / 2;
                        break;
                }
                childTop = mExpanded ? mTopOffset : ((height - childHeight) + mBottomOffset);

                content.layout(0, mTopOffset + childHeight, content.getMeasuredWidth(),
                        mTopOffset + childHeight + content.getMeasuredHeight());
                break;

            case RIGHT:
                childLeft = mExpanded ? mTopOffset : ((width - childWidth) + mBottomOffset);
                switch (mHandlePos) {
                    case TOP:
                        childTop = mHandlePad;
                        break;
                    case BOTTOM:
                        childTop = height - childHeight - mHandlePad;
                        break;
                    default:
                        childTop = (height - childHeight) / 2;
                        break;
                }

                content.layout(mTopOffset + childWidth, 0,
                        mTopOffset + childWidth + content.getMeasuredWidth(),
                        content.getMeasuredHeight());
                break;

            case LEFT:
                childLeft = mExpanded ? (width - childWidth - mTopOffset) : -mBottomOffset;
                switch (mHandlePos) {
                    case TOP:
                        childTop = mHandlePad;
                        break;
                    case BOTTOM:
                        childTop = height - childHeight - mHandlePad;
                        break;
                    default:
                        childTop = (height - childHeight) / 2;
                        break;
                }

                content.layout(width - childWidth - mTopOffset - content.getMeasuredWidth(), 0,
                        width - childWidth - mTopOffset, content.getMeasuredHeight());
                break;
        }

        handle.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        mHandleHeight = handle.getHeight();
        mHandleWidth = handle.getWidth();
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        if (mLocked) {
            return false;
        }

        int action = event.getAction();

        float x = event.getX();
        float y = event.getY();

        Rect frame = mFrame;
        View handle = mHandle;

        handle.getHitRect(frame);
        if (!mTracking && !frame.contains((int) x, (int) y)) {
            return false;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            mTracking = true;

            handle.setPressed(true);
            // Must be called before prepareTracking()
            prepareContent();

            // Must be called after prepareContent()
            if (mOnDrawerScrollListener != null) {
                mOnDrawerScrollListener.onScrollStarted();
            }

            int pt = getSide();
            mTouchDelta = (int) (y - pt);
            prepareTracking(pt);
            mVelocityTracker.addMovement(event);
        }

        return true;
    }

    private int getSide() {
        return mVertical ? mHandle.getTop() : mHandle.getLeft();
    }

    private int getOppositeSide() {
        int pt = 0;
        switch (mOrientation) {
            case TOP:
                pt = mHandle.getBottom();
                break;
            case LEFT:
                pt = mHandle.getRight();
                break;
            case BOTTOM:
                pt = mHandle.getTop();
                break;
            case RIGHT:
                pt = mHandle.getLeft();
                break;
        }
        return pt;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (mLocked) {
            return false;
        }

        if (mTracking) {
            mVelocityTracker.addMovement(event);
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    moveHandle((int) (mVertical ? event.getY() : event.getX()) - mTouchDelta);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(mVelocityUnits);

                    float yVelocity = velocityTracker.getYVelocity();
                    float xVelocity = velocityTracker.getXVelocity();
                    boolean negative;

                    boolean vertical = mVertical;
                    if (vertical) {
                        negative = yVelocity < 0;
                        if (xVelocity < 0) {
                            xVelocity = -xVelocity;
                        }
                        if (xVelocity > mMaximumMinorVelocity) {
                            xVelocity = mMaximumMinorVelocity;
                        }
                    } else {
                        negative = xVelocity < 0;
                        if (yVelocity < 0) {
                            yVelocity = -yVelocity;
                        }
                        if (yVelocity > mMaximumMinorVelocity) {
                            yVelocity = mMaximumMinorVelocity;
                        }
                    }

                    float velocity = (float) Math.hypot(xVelocity, yVelocity);
                    if (negative) {
                        velocity = -velocity;
                    }

                    int top = mHandle.getTop();
                    int left = mHandle.getLeft();

                    if (Math.abs(velocity) < mMaximumTapVelocity) {

                        if (inThreshold(top, left)) {
                            if (mAllowSingleTap) {
                                playSoundEffect(SoundEffectConstants.CLICK);

                                if (mExpanded) {
                                    //animateClose(vertical ? top : left);
                                    animateClose(getSide());
                                } else {
                                    animateOpen(getSide());
                                    //animateOpen(vertical ? top : left);
                                }
                            } else {
                                performFling(vertical ? top : left, velocity, false);
                            }

                        } else {
                            performFling(vertical ? top : left, velocity, false);
                        }
                    } else {
                        performFling(vertical ? top : left, velocity, false);
                    }
                    break;
            }
        }

        return mTracking || mAnimating || super.onTouchEvent(event);
    }

    private boolean inThreshold(int top, int left) {
        switch (mOrientation) {
            case TOP:
                return mExpanded ? top > getBottom() - getTop() - mHandleHeight - mTopOffset - mTapThreshold : top < (mTapThreshold - mBottomOffset);
            case LEFT:
                return mExpanded ? left > getRight() - getLeft() - mHandleWidth - mTopOffset - mTapThreshold : left < (mTapThreshold - mBottomOffset);
            case BOTTOM:
                return mExpanded ? top < (mTapThreshold + mTopOffset) : top > (mBottomOffset + getBottom()) - getTop() - mHandleHeight - mTapThreshold;
            case RIGHT:
                return mExpanded ? left < (mTapThreshold + mTopOffset) : left > (mBottomOffset + getRight()) - getLeft() - mHandleWidth - mTapThreshold;
        }
        return false;
    }

    private void animateClose(int position) {
        prepareTracking(position);
        performFling(position, mMaximumAcceleration * (mInvert ? -1 : 1), true);
    }

    private void animateOpen(int position) {
        prepareTracking(position);
        performFling(position, mMaximumAcceleration * (mInvert ? 1 : -1), true);
    }

    private void performFling(int position, float velocity, boolean always) {
        mAnimationPosition = position;
        mAnimatedVelocity = velocity;

        if (mExpanded) {
            if (mInvert) {
                if (always || velocity < -mMaximumMajorVelocity ||
                        position < (mVertical ? getHeight() : getWidth()) / 2 &&
                                velocity > -mMaximumMajorVelocity) {
                    // We are expanded and are now going to animate away.
                    mAnimatedAcceleration = -mMaximumAcceleration;
                    if (velocity > 0) {
                        mAnimatedVelocity = 0;
                    }
                } else {
                    // We are expanded, but they didn't move sufficiently to cause
                    // us to retract.  Animate back to the expanded position.
                    mAnimatedAcceleration = mMaximumAcceleration;
                    if (velocity < 0) {
                        mAnimatedVelocity = 0;
                    }
                }
            } else if (always || velocity > mMaximumMajorVelocity ||
                    position > mTopOffset + (mVertical ? mHandleHeight : mHandleWidth) &&
                            velocity > -mMaximumMajorVelocity) {
                // We are expanded, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the collapsed position.
                mAnimatedAcceleration = mMaximumAcceleration;
                if (velocity < 0) {
                    mAnimatedVelocity = 0;
                }
            } else {
                // We are expanded and are now going to animate away.
                mAnimatedAcceleration = -mMaximumAcceleration;
                if (velocity > 0) {
                    mAnimatedVelocity = 0;
                }
            }
        } else {
            //else if (!always && (velocity > mMaximumMajorVelocity ||
            //		(position > (mVertical ? getHeight() : getWidth()) / 2 &&
            //				velocity > -mMaximumMajorVelocity))) {
            if (velocity > mMaximumMajorVelocity ||
                    position > (mVertical ? getHeight() : getWidth()) / 2 &&
                            velocity > -mMaximumMajorVelocity) {
                // We are collapsed, and they moved enough to allow us to expand.
                mAnimatedAcceleration = mMaximumAcceleration;
                if (velocity < 0) {
                    mAnimatedVelocity = 0;
                }
            } else {
                // We are collapsed, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the collapsed position.
                mAnimatedAcceleration = -mMaximumAcceleration;
                if (velocity > 0) {
                    mAnimatedVelocity = 0;
                }
            }
        }

//		if (mInvert)
//			mAnimatedAcceleration *= -1;
        long now = SystemClock.uptimeMillis();
        mAnimationLastTime = now;
        mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
        mAnimating = true;
        mHandler.removeMessages(MSG_ANIMATE);
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
        stopTracking();
    }

    private void prepareTracking(int position) {
        mTracking = true;
        mVelocityTracker = VelocityTracker.obtain();
        boolean opening = !mExpanded;
        if (opening) {
            mAnimatedAcceleration = mMaximumAcceleration;
            mAnimatedVelocity = mMaximumMajorVelocity;
            switch (mOrientation) {
                case TOP:
                case LEFT:
                    mAnimationPosition = mBottomOffset;
                    break;
                case BOTTOM:
                    mAnimationPosition = (mBottomOffset + getHeight()) - mHandleHeight;
                    break;
                case RIGHT:
                    mAnimationPosition = (mBottomOffset + getWidth()) - mHandleWidth;
                    break;
            }

            moveHandle((int) mAnimationPosition);
            mAnimating = true;
            mHandler.removeMessages(MSG_ANIMATE);
            long now = SystemClock.uptimeMillis();
            mAnimationLastTime = now;
            mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
            mAnimating = true;
        } else {
            if (mAnimating) {
                mAnimating = false;
                mHandler.removeMessages(MSG_ANIMATE);
            }
            moveHandle(position);
        }
    }

    private void moveHandle(int position) {
        View handle = mHandle;

        switch (mOrientation) {
            case TOP:
                if (position == EXPANDED_FULL_OPEN) {
                    handle.offsetTopAndBottom(getBottom() - getTop() - mTopOffset - mHandleHeight - handle.getTop());
                    invalidate();
                } else if (position == COLLAPSED_FULL_CLOSED) {
                    handle.offsetTopAndBottom(-mBottomOffset - handle.getTop());
                    invalidate();
                } else {
                    int top = handle.getTop();
                    int deltaY = position - top;
                    if (position < -mBottomOffset) {
                        deltaY = -mBottomOffset - top;
                    } else if (position > (getBottom() - getTop() - mTopOffset - mHandleHeight)) {
                        deltaY = getBottom() - getTop() - mTopOffset - mHandleHeight - top;
                    }
                    handle.offsetTopAndBottom(deltaY);

                    Rect frame = mFrame;
                    Rect region = mInvalidate;

                    handle.getHitRect(frame);
                    region.set(frame);

                    region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY);
                    region.union(0, frame.bottom - deltaY, getWidth(), (frame.bottom - deltaY) + mContent.getHeight());

                    invalidate();
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener.onScrolling(handle.getTop());
                }

                break;

            case BOTTOM:
                if (position == EXPANDED_FULL_OPEN) {
                    handle.offsetTopAndBottom(mTopOffset - handle.getTop());
                    invalidate();
                } else if (position == COLLAPSED_FULL_CLOSED) {
                    handle.offsetTopAndBottom((mBottomOffset + getBottom()) - getTop() - mHandleHeight - handle.getTop());
                    invalidate();
                } else {
                    int top = handle.getTop();
                    int deltaY = position - top;
                    if (position < mTopOffset) {
                        deltaY = mTopOffset - top;
                    } else if (deltaY > ((mBottomOffset + getBottom()) - getTop() - mHandleHeight - top)) {
                        deltaY = (mBottomOffset + getBottom()) - getTop() - mHandleHeight - top;
                    }
                    handle.offsetTopAndBottom(deltaY);

                    Rect frame = mFrame;
                    Rect region = mInvalidate;

                    handle.getHitRect(frame);
                    region.set(frame);

                    region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY);
                    region.union(0, frame.bottom - deltaY, getWidth(), (frame.bottom - deltaY) + mContent.getHeight());

                    invalidate(region);
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener.onScrolling(handle.getTop());
                }

                break;

            case RIGHT:
                if (position == EXPANDED_FULL_OPEN) {
                    handle.offsetLeftAndRight(mTopOffset - handle.getLeft());
                    invalidate();
                } else if (position == COLLAPSED_FULL_CLOSED) {
                    handle.offsetLeftAndRight(-mBottomOffset);
                    invalidate();
                } else {
                    int left = handle.getLeft();
                    int deltaX = position - left;
                    if (position < mTopOffset) {
                        deltaX = mTopOffset - left;
                    } else if (deltaX > ((mBottomOffset + getRight()) - getLeft() - mHandleWidth - left)) {
                        deltaX = (mBottomOffset + getRight()) - getLeft() - mHandleWidth - left;
                    }
                    handle.offsetLeftAndRight(deltaX);
                    Rect frame = mFrame;
                    Rect region = mInvalidate;

                    handle.getHitRect(frame);
                    region.set(frame);

                    region.union(frame.left - deltaX, frame.top, frame.right - deltaX, frame.bottom);
                    region.union(frame.right - deltaX, 0, (frame.right - deltaX) + mContent.getWidth(), getHeight());

                    invalidate(region);
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener.onScrolling(handle.getLeft());
                }

                break;

            case LEFT:
                if (position == EXPANDED_FULL_OPEN) {
                    handle.offsetLeftAndRight(getRight() - getLeft() - mTopOffset - mHandleWidth - handle.getLeft());
                    invalidate();
                } else if (position == COLLAPSED_FULL_CLOSED) {
                    handle.offsetLeftAndRight(-mBottomOffset - handle.getLeft());
                    invalidate();
                } else {
                    int left = handle.getLeft();
                    int deltaX = position - left;
                    if (position < -mBottomOffset) {
                        deltaX = -mBottomOffset - left;
                    } else if (position > (getRight() - getLeft() - mTopOffset - mHandleWidth)) {
                        deltaX = getRight() - getLeft() - mTopOffset - mHandleWidth - left;
                    }
                    handle.offsetLeftAndRight(deltaX);

                    Rect frame = mFrame;
                    Rect region = mInvalidate;

                    handle.getHitRect(frame);
                    region.set(frame);

                    region.union(frame.left - deltaX, frame.top, frame.right - deltaX, frame.bottom);
                    region.union(frame.right - deltaX, 0, (frame.right - deltaX) + mContent.getWidth(), getHeight());

                    invalidate(region);
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener.onScrolling(handle.getLeft());
                }

                break;
        }
    }

    private void prepareContent() {

        if (mAnimating) {
            return;
        }

        // Something changed in the content, we need to honor the layout request
        // before creating the cached bitmap
        View content = mContent;
        if (content.isLayoutRequested()) {
            measureContent();
        }

        // Try only once... we should really loop but it's not a big deal
        // if the draw was cancelled, it will only be temporary anyway
        content.getViewTreeObserver().dispatchOnPreDraw();
        content.buildDrawingCache();

        content.setVisibility(View.GONE);
    }

    public void measureContent() {
        View content = mContent;
        if (mVertical) {
            int childHeight = mHandle.getHeight();
            int height = getBottom() - getTop() - childHeight - mTopOffset;
            content.measure(MeasureSpec.makeMeasureSpec(getRight() - getLeft(), MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            if (mOrientation == Orientation.TOP) {
                content.layout(0, height - content.getMeasuredHeight(), content.getMeasuredWidth(), height);
            } else {
                content.layout(0, mTopOffset + childHeight, content.getMeasuredWidth(),
                        mTopOffset + childHeight + content.getMeasuredHeight());
            }

        } else {
            int childWidth = mHandle.getWidth();
            int width = getRight() - getLeft() - childWidth - mTopOffset;
            content.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(getBottom() - getTop(), MeasureSpec.EXACTLY));
            if (mOrientation == Orientation.RIGHT) {
                content.layout(childWidth + mTopOffset, 0,
                        mTopOffset + childWidth + content.getMeasuredWidth(), content.getMeasuredHeight());
            } else {
                content.layout(width - content.getMeasuredWidth(), 0, width, content.getMeasuredHeight());
            }
        }
    }

    private void stopTracking() {
        mHandle.setPressed(false);
        mTracking = false;

        if (mOnDrawerScrollListener != null) {
            mOnDrawerScrollListener.onScrollEnded();
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void doAnimation() {
        if (mAnimating) {

            incrementAnimation();

            if (mInvert) {
                if (mAnimationPosition >= ((mVertical ? getHeight() : getWidth()) - mTopOffset)) {
                    mAnimating = false;
                    openDrawer();
                } else if (mAnimationPosition < -mBottomOffset) {
                    mAnimating = false;
                    closeDrawer();
                } else {
                    moveHandle((int) mAnimationPosition);
                    mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
                    mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
                }
            } else {
                if (mAnimationPosition >= ((mBottomOffset + (mVertical ? getHeight() : getWidth())) - 1)) {
                    mAnimating = false;
                    closeDrawer();
                } else if (mAnimationPosition < mTopOffset) {
                    mAnimating = false;
                    openDrawer();
                } else {
                    moveHandle((int) mAnimationPosition);
                    mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
                    mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
                }
            }
        }
    }

    private void incrementAnimation() {
        long now = SystemClock.uptimeMillis();
        float t = (now - mAnimationLastTime) / 1000.0f;                   // ms -> s
        float position = mAnimationPosition;
        float v = mAnimatedVelocity;                                // px/s
        float a = mAnimatedAcceleration;                            // px/s/s
        mAnimationPosition = position + v * t + (0.5f * a * t * t);     // px
        mAnimatedVelocity = v + a * t;                                  // px/s
        mAnimationLastTime = now;                                         // ms
    }

    /**
     * Toggles the drawer open and close. Takes effect immediately.
     *
     * @see #open()
     * @see #close()
     * @see #animateClose()
     * @see #animateOpen()
     * @see #animateToggle()
     */
    public void toggle() {
        if (mExpanded) {
            closeDrawer();
        } else {
            openDrawer();
        }
        invalidate();
        requestLayout();
    }

    /**
     * Toggles the drawer open and close with an animation.
     *
     * @see #open()
     * @see #close()
     * @see #animateClose()
     * @see #animateOpen()
     * @see #toggle()
     */
    public void animateToggle() {
        if (mExpanded) {
            animateClose();
        } else {
            animateOpen();
        }
    }

    /**
     * Opens the drawer immediately.
     *
     * @see #toggle()
     * @see #close()
     * @see #animateOpen()
     */
    public void open() {
        openDrawer();
        invalidate();
        requestLayout();

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    /**
     * Closes the drawer immediately.
     *
     * @see #toggle()
     * @see #open()
     * @see #animateClose()
     */
    public void close() {
        closeDrawer();
        invalidate();
        requestLayout();
    }

    /**
     * Closes the drawer with an animation.
     *
     * @see #close()
     * @see #open()
     * @see #animateOpen()
     * @see #animateToggle()
     * @see #toggle()
     */
    public void animateClose() {
        prepareContent();
        OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateClose(getSide());

        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    /**
     * Opens the drawer with an animation.
     *
     * @see #close()
     * @see #open()
     * @see #animateClose()
     * @see #animateToggle()
     * @see #toggle()
     */
    public void animateOpen() {
        prepareContent();
        OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateOpen(getSide());

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    private void closeDrawer() {
        moveHandle(COLLAPSED_FULL_CLOSED);
        mContent.setVisibility(View.GONE);
        mContent.destroyDrawingCache();

        if (!mExpanded) {
            return;
        }

        mExpanded = false;

        if (mOnDrawerCloseListener != null) {
            mOnDrawerCloseListener.onDrawerClosed();
        }
    }

    private void openDrawer() {
        moveHandle(EXPANDED_FULL_OPEN);
        mContent.setVisibility(View.VISIBLE);

        if (mExpanded) {
            return;
        }

        mExpanded = true;

        if (mOnDrawerOpenListener != null) {
            mOnDrawerOpenListener.onDrawerOpened();
        }
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes open.
     *
     * @param onDrawerOpenListener The listener to be notified when the drawer is opened.
     */
    public void setOnDrawerOpenListener(OnDrawerOpenListener onDrawerOpenListener) {
        mOnDrawerOpenListener = onDrawerOpenListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes close.
     *
     * @param onDrawerCloseListener The listener to be notified when the drawer is closed.
     */
    public void setOnDrawerCloseListener(OnDrawerCloseListener onDrawerCloseListener) {
        mOnDrawerCloseListener = onDrawerCloseListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer starts or ends
     * a scroll. A fling is considered as a scroll. A fling will also trigger a
     * activity_base opened or activity_base closed event.
     *
     * @param onDrawerScrollListener The listener to be notified when scrolling
     *                               starts or stops.
     */
    public void setOnDrawerScrollListener(OnDrawerScrollListener onDrawerScrollListener) {
        mOnDrawerScrollListener = onDrawerScrollListener;
    }

    /**
     * Returns the handle of the drawer.
     *
     * @return The View reprenseting the handle of the drawer, identified by
     * the "handle" id in XML.
     */
    @Nullable
    public View getHandle() {
        return mHandle;
    }

    /**
     * Convenience method to get the size of the handle.
     * May not return a valid size until the view is layed out.
     *
     * @return Return the height if the component is vertical
     * otherwise returns the width for Horizontal orientation.
     */
    public int getHandleSize() {
        return mVertical ? mHandle.getHeight() : mHandle.getWidth();
    }

    /**
     * Returns the content of the drawer.
     *
     * @return The View reprenseting the content of the drawer, identified by
     * the "content" id in XML.
     */
    @Nullable
    public View getContent() {
        return mContent;
    }

    /**
     * Unlocks the SlidingDrawer so that touch events are processed.
     *
     * @see #lock()
     */
    public void unlock() {
        mLocked = false;
    }

    /**
     * Locks the SlidingDrawer so that touch events are ignores.
     *
     * @see #unlock()
     */
    public void lock() {
        mLocked = true;
    }

    /**
     * Indicates whether the drawer is currently fully opened.
     *
     * @return True if the drawer is opened, false otherwise.
     */
    public boolean isOpened() {
        return mExpanded;
    }

    /**
     * Indicates whether the drawer is scrolling or flinging.
     *
     * @return True if the drawer is scroller or flinging, false otherwise.
     */
    public boolean isMoving() {
        return mTracking || mAnimating;
    }

    public int getBottomOffset() {
        return mBottomOffset;
    }

    public void setBottomOffset(int offset) {
        mBottomOffset = offset;
        invalidate();
    }

    public int getTopOffset() {
        return mTopOffset;
    }

    public void setTopOffset(int offset) {
        mTopOffset = offset;
        invalidate();
    }

    public boolean isAllowSingleTap() {
        return mAllowSingleTap;
    }

    public void setAllowSingleTap(boolean mAllowSingleTap) {
        this.mAllowSingleTap = mAllowSingleTap;
    }

    public boolean isAnimateOnClick() {
        return mAnimateOnClick;
    }

    public void setAnimateOnClick(boolean mAnimateOnClick) {
        this.mAnimateOnClick = mAnimateOnClick;
    }

    /**
     * Drawer click listener.
     */
    private class DrawerToggler implements OnClickListener {
        public void onClick(View v) {
            if (mLocked) {
                return;
            }
            // mAllowSingleTap isn't relevant here; you're *always*
            // allowed to open/close the drawer by clicking with the
            // trackball.

            if (mAnimateOnClick) {
                animateToggle();
            } else {
                toggle();
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private class SlidingHandler extends Handler {
        public void handleMessage(@NonNull Message m) {
            switch (m.what) {
                case MSG_ANIMATE:
                    doAnimation();
                    break;
            }
        }
    }

    /**
     * Callback invoked when the drawer is opened.
     */
    public interface OnDrawerOpenListener {
        /**
         * Invoked when the drawer becomes fully open.
         */
        void onDrawerOpened();
    }

    /**
     * Callback invoked when the drawer is closed.
     */
    public interface OnDrawerCloseListener {
        /**
         * Invoked when the drawer becomes fully closed.
         */
        void onDrawerClosed();
    }

    /**
     * Callback invoked when the drawer is scrolled.
     */
    public interface OnDrawerScrollListener {
        /**
         * Invoked when the user starts dragging/flinging the drawer's handle.
         */
        void onScrollStarted();

        /**
         * Invoked when the user scrolls the drawer's handle.
         */
        void onScrolling(int pos);

        /**
         * Invoked when the user stops dragging/flinging the drawer's handle.
         */
        void onScrollEnded();
    }


    public enum Side {
        TOP(0), LEFT(1), BOTTOM(2), RIGHT(3), FRONT(4), BACK(5), CENTER(6);

        public final int value;

        Side(int value) {
            this.value = value;
        }

        @NonNull
        public static Side getByValue(int value) {
            for (Side s : Side.values()) {
                if (s.value == value) {
                    return s;
                }
            }
            throw new IllegalArgumentException("There is no 'Side' enum with this value");
        }
    }

    public enum Orientation {
        TOP(0), LEFT(1), BOTTOM(2), RIGHT(3);
        public final int value;

        Orientation(int value) {
            this.value = value;
        }

        @NonNull
        public static Orientation getByValue(int value) {
            for (Orientation s : Orientation.values()) {
                if (s.value == value) {
                    return s;
                }
            }
            throw new IllegalArgumentException("There is no 'Orientation' enum with this value");
        }
    }
}
