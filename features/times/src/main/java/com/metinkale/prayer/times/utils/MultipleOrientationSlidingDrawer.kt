/*
 * Copyright (c) 2013-2023 Metin Kale
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
package com.metinkale.prayer.times.utils

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Canvas
import android.graphics.Rect
import android.os.*
import android.util.AttributeSet
import android.view.*
import android.view.accessibility.AccessibilityEvent
import com.metinkale.prayer.times.R
import kotlin.math.abs
import kotlin.math.hypot

class MultipleOrientationSlidingDrawer : ViewGroup {
    private val mHandleId: Int
    private val mContentId: Int

    /**
     * Returns the handle of the drawer.
     *
     * @return The View reprenseting the handle of the drawer, identified by
     * the "handle" id in XML.
     */
    var handle: View? = null
        private set

    /**
     * Returns the content of the drawer.
     *
     * @return The View reprenseting the content of the drawer, identified by
     * the "content" id in XML.
     */
    var content: View? = null
        private set
    private val mFrame = Rect()
    private val mInvalidate = Rect()
    private var mTracking = false
    private var mLocked = false
    private var mVelocityTracker: VelocityTracker? = null
    private var mOrientation: Orientation? = null
    private var mHandlePos: Side? = null
    private var mVertical = false
    private var mInvert = false

    /**
     * Indicates whether the drawer is currently fully opened.
     *
     * @return True if the drawer is opened, false otherwise.
     */
    var isOpened = false
        private set
    private var mBottomOffset = 0
    private var mTopOffset = 0
    private var mHandleHeight = 0
    private var mHandleWidth = 0
    private var mHandlePad = 0
    private var mOnDrawerOpenListener: OnDrawerOpenListener? = null
    private var mOnDrawerCloseListener: OnDrawerCloseListener? = null
    private var mOnDrawerScrollListener: OnDrawerScrollListener? = null
    private val mHandler: Handler = SlidingHandler()
    private var mAnimatedAcceleration = 0f
    private var mAnimatedVelocity = 0f
    private var mAnimationPosition = 0f
    private var mAnimationLastTime: Long = 0
    private var mCurrentAnimationTime: Long = 0
    private var mTouchDelta = 0
    private var mAnimating = false
    var isAllowSingleTap = true
    var isAnimateOnClick = true
    private val mTapThreshold: Int
    private val mMaximumTapVelocity: Int
    private val mMaximumMinorVelocity: Int
    private val mMaximumMajorVelocity: Int
    private val mMaximumAcceleration: Int
    private val mVelocityUnits: Int

    /**
     * Construct a `MultipleOrientationSlidingDrawer` object programmatically with the specified
     * `handle`, `content` and `orientation`.
     *
     * @param context     Activity context
     * @param handle      Cannot be `null`
     * @param content     Cannot be `null`
     * @param orientation TOP, LEFT, BOTTOM or RIGHT.
     */
    constructor(
        context: Context?,
        handle: View?,
        content: View?,
        orientation: Orientation?
    ) : super(context) {

        // handle
        if (handle == null) {
            throw NullPointerException("Handle cannot be null.")
        }
        addView(handle.also { this.handle = it })
        handle.setOnClickListener(DrawerToggler())

        // content
        requireNotNull(content) { "Content cannot be null." }
        addView(content.also { this.content = it })
        content.visibility = GONE
        mContentId = 0
        mHandleId = mContentId
        this.orientation = orientation
        val density = resources.displayMetrics.density
        mTapThreshold = (TAP_THRESHOLD * density + 0.5f).toInt()
        mMaximumTapVelocity = (MAXIMUM_TAP_VELOCITY * density + 0.5f).toInt()
        mMaximumMinorVelocity = (MAXIMUM_MINOR_VELOCITY * density + 0.5f).toInt()
        mMaximumMajorVelocity = (MAXIMUM_MAJOR_VELOCITY * density + 0.5f).toInt()
        mMaximumAcceleration = (MAXIMUM_ACCELERATION * density + 0.5f).toInt()
        mVelocityUnits = (VELOCITY_UNITS * density + 0.5f).toInt()
    }

    /**
     * Creates a new SlidingDrawer from a specified set of attributes defined in XML.
     *
     * @param context The application's environment.
     * @param attrs   The attributes defined in XML.
     */
    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0) : super(
        context,
        attrs,
        defStyle
    ) {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.MultipleOrientationSlidingDrawer,
            defStyle,
            0
        )
        val orientation = a.getInteger(
            R.styleable.MultipleOrientationSlidingDrawer__orientation,
            Orientation.TOP.value
        )
        this.orientation = Orientation.getByValue(orientation)
        mHandlePos = Side.getByValue(orientation)
        mBottomOffset =
            a.getDimension(R.styleable.MultipleOrientationSlidingDrawer_bottomOffset, 0.0f).toInt()
        mTopOffset =
            a.getDimension(R.styleable.MultipleOrientationSlidingDrawer_topOffset, 0.0f).toInt()
        isAllowSingleTap =
            a.getBoolean(R.styleable.MultipleOrientationSlidingDrawer_allowSingleTap, true)
        isAnimateOnClick =
            a.getBoolean(R.styleable.MultipleOrientationSlidingDrawer_animateOnClick, true)
        val handleId = a.getResourceId(R.styleable.MultipleOrientationSlidingDrawer_handle, 0)
        require(handleId != 0) {
            ("The handle attribute is required and must refer "
                    + "to a valid child.")
        }
        val contentId = a.getResourceId(R.styleable.MultipleOrientationSlidingDrawer_content, 0)
        a.recycle()
        require(contentId != 0) {
            ("The content attribute is required and must refer "
                    + "to a valid child.")
        }
        require(handleId != contentId) {
            ("The content and handle attributes must refer "
                    + "to different children.")
        }
        mHandleId = handleId
        mContentId = contentId
        val density = resources.displayMetrics.density
        mTapThreshold = (TAP_THRESHOLD * density + 0.5f).toInt()
        mMaximumTapVelocity = (MAXIMUM_TAP_VELOCITY * density + 0.5f).toInt()
        mMaximumMinorVelocity = (MAXIMUM_MINOR_VELOCITY * density + 0.5f).toInt()
        mMaximumMajorVelocity = (MAXIMUM_MAJOR_VELOCITY * density + 0.5f).toInt()
        mMaximumAcceleration = (MAXIMUM_ACCELERATION * density + 0.5f).toInt()
        mVelocityUnits = (VELOCITY_UNITS * density + 0.5f).toInt()

//        a.recycle();
        isAlwaysDrawnWithCacheEnabled = false
    }

    /**
     * Lets you change the orientation of the sliding tray at runtime.
     *
     * Orientation must be from one of TOP, LEFT, BOTTOM, RIGHT.
     */
    var orientation: Orientation?
        get() = mOrientation
        set(orientation) {
            mOrientation = orientation
            mVertical = mOrientation == Orientation.BOTTOM || mOrientation == Orientation.TOP
            mInvert = mOrientation == Orientation.LEFT || mOrientation == Orientation.TOP
            requestLayout()
            invalidate()
        }

    /**
     * Change the handle positioning of the sliding tray at runtime.
     *
     *
     * HandlePos must be [Side.TOP], [Side.CENTER] or [Side.BOTTOM] for horizontal orientation
     * or must be [Side.LEFT], [Side.CENTER] or [Side.RIGHT] for vertical orientation.
     *
     *
     * Default is [Side.CENTER].
     */
    var handlePosition: Side?
        get() = mHandlePos
        set(side) {
            mHandlePos = side
            requestLayout()
            invalidate()
        }

    /**
     * Add padding to drawer handle when handle is not centered.
     *
     *
     * Note this padding is only effective when handle is not centered.
     *
     * @param padding padding in pixels.
     */
    fun setHandlePadding(padding: Int) {
        mHandlePad = padding
        requestLayout()
        invalidate()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (mHandleId > 0) {
            handle = findViewById(mHandleId)
            requireNotNull(handle) { "The handle attribute is must refer to an existing child." }
            handle!!.setOnClickListener(DrawerToggler())
        }
        if (mContentId > 0) {
            content = findViewById(mContentId)
            requireNotNull(content) { "The content attribute is must refer to an existing child." }
            content!!.visibility = GONE
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw RuntimeException("SlidingDrawer cannot have UNSPECIFIED dimensions")
        }
        val handle = handle
        measureChild(handle, widthMeasureSpec, heightMeasureSpec)
        if (mVertical) {
            val height = heightSpecSize - handle!!.measuredHeight - mTopOffset
            content!!.measure(
                MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
        } else {
            val width = widthSpecSize - handle!!.measuredWidth - mTopOffset
            content!!.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY)
            )
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val drawingTime = drawingTime
        val handle = handle
        val orientation = mOrientation
        drawChild(canvas, handle, drawingTime)
        if (mTracking || mAnimating) {
            val cache = content!!.drawingCache
            if (cache != null) {
                // called when opening
                when (orientation) {
                    Orientation.TOP -> canvas.drawBitmap(
                        cache,
                        0f,
                        (handle!!.top - cache.height).toFloat(),
                        null
                    )
                    Orientation.LEFT -> canvas.drawBitmap(
                        cache,
                        (handle!!.left - cache.width).toFloat(),
                        0f,
                        null
                    )
                    Orientation.BOTTOM -> canvas.drawBitmap(
                        cache,
                        0f,
                        handle!!.bottom.toFloat(),
                        null
                    )
                    Orientation.RIGHT -> canvas.drawBitmap(
                        cache,
                        handle!!.right.toFloat(),
                        0f,
                        null
                    )
                    else -> {}
                }
            } else {
                // called when closing
                canvas.save()
                when (orientation) {
                    Orientation.TOP -> canvas.translate(
                        0f,
                        (handle!!.top - content!!.height).toFloat()
                    )
                    Orientation.LEFT -> canvas.translate(
                        (handle!!.left - content!!.width).toFloat(),
                        0f
                    )
                    Orientation.BOTTOM -> canvas.translate(
                        0f,
                        (handle!!.top - mTopOffset).toFloat()
                    )
                    Orientation.RIGHT -> canvas.translate(
                        (handle!!.left - mTopOffset).toFloat(),
                        0f
                    )
                    else -> {}
                }
                drawChild(canvas, content, drawingTime)
                canvas.restore()
            }
        } else if (isOpened) {
            drawChild(canvas, content, drawingTime)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (mTracking) {
            return
        }
        val width = r - l
        val height = b - t
        val handle = handle
        val childWidth = handle!!.measuredWidth
        val childHeight = handle.measuredHeight
        var childLeft = 0
        var childTop = 0
        val content = content
        when (mOrientation) {
            Orientation.TOP -> {
                childLeft = when (mHandlePos) {
                    Side.LEFT -> mHandlePad
                    Side.RIGHT -> width - childWidth - mHandlePad
                    else -> (width - childWidth) / 2
                }
                childTop = if (isOpened) height - childHeight - mTopOffset else -mBottomOffset
                content!!.layout(
                    0, height - childHeight - mTopOffset - content.measuredHeight,
                    content.measuredWidth, height - childHeight - mTopOffset
                )
            }
            Orientation.BOTTOM -> {
                childLeft = when (mHandlePos) {
                    Side.LEFT -> mHandlePad
                    Side.RIGHT -> width - childWidth - mHandlePad
                    else -> (width - childWidth) / 2
                }
                childTop = if (isOpened) mTopOffset else height - childHeight + mBottomOffset
                content!!.layout(
                    0, mTopOffset + childHeight, content.measuredWidth,
                    mTopOffset + childHeight + content.measuredHeight
                )
            }
            Orientation.RIGHT -> {
                childLeft = if (isOpened) mTopOffset else width - childWidth + mBottomOffset
                childTop = when (mHandlePos) {
                    Side.TOP -> mHandlePad
                    Side.BOTTOM -> height - childHeight - mHandlePad
                    else -> (height - childHeight) / 2
                }
                content!!.layout(
                    mTopOffset + childWidth, 0,
                    mTopOffset + childWidth + content.measuredWidth,
                    content.measuredHeight
                )
            }
            Orientation.LEFT -> {
                childLeft = if (isOpened) width - childWidth - mTopOffset else -mBottomOffset
                childTop = when (mHandlePos) {
                    Side.TOP -> mHandlePad
                    Side.BOTTOM -> height - childHeight - mHandlePad
                    else -> (height - childHeight) / 2
                }
                content!!.layout(
                    width - childWidth - mTopOffset - content.measuredWidth, 0,
                    width - childWidth - mTopOffset, content.measuredHeight
                )
            }
            else -> {}
        }
        handle.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight)
        mHandleHeight = handle.height
        mHandleWidth = handle.width
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (mLocked) {
            return false
        }
        val action = event.action
        val x = event.x
        val y = event.y
        val frame = mFrame
        val handle = handle
        handle!!.getHitRect(frame)
        if (!mTracking && !frame.contains(x.toInt(), y.toInt())) {
            return false
        }
        if (action == MotionEvent.ACTION_DOWN) {
            mTracking = true
            handle.isPressed = true
            // Must be called before prepareTracking()
            prepareContent()

            // Must be called after prepareContent()
            if (mOnDrawerScrollListener != null) {
                mOnDrawerScrollListener!!.onScrollStarted()
            }
            val pt = side
            mTouchDelta = (y - pt).toInt()
            prepareTracking(pt)
            mVelocityTracker!!.addMovement(event)
        }
        return true
    }

    private val side: Int
        private get() = if (mVertical) handle!!.top else handle!!.left
    private val oppositeSide: Int
        private get() {
            var pt = 0
            when (mOrientation) {
                Orientation.TOP -> pt = handle!!.bottom
                Orientation.LEFT -> pt = handle!!.right
                Orientation.BOTTOM -> pt = handle!!.top
                Orientation.RIGHT -> pt = handle!!.left
                else -> {}
            }
            return pt
        }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mLocked) {
            return false
        }
        if (mTracking) {
            mVelocityTracker!!.addMovement(event)
            when (event.action) {
                MotionEvent.ACTION_MOVE -> moveHandle((if (mVertical) event.y else event.x).toInt() - mTouchDelta)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val velocityTracker = mVelocityTracker
                    velocityTracker!!.computeCurrentVelocity(mVelocityUnits)
                    var yVelocity = velocityTracker.yVelocity
                    var xVelocity = velocityTracker.xVelocity
                    val negative: Boolean
                    val vertical = mVertical
                    if (vertical) {
                        negative = yVelocity < 0
                        if (xVelocity < 0) {
                            xVelocity = -xVelocity
                        }
                        if (xVelocity > mMaximumMinorVelocity) {
                            xVelocity = mMaximumMinorVelocity.toFloat()
                        }
                    } else {
                        negative = xVelocity < 0
                        if (yVelocity < 0) {
                            yVelocity = -yVelocity
                        }
                        if (yVelocity > mMaximumMinorVelocity) {
                            yVelocity = mMaximumMinorVelocity.toFloat()
                        }
                    }
                    var velocity = hypot(xVelocity.toDouble(), yVelocity.toDouble()).toFloat()
                    if (negative) {
                        velocity = -velocity
                    }
                    val top = handle!!.top
                    val left = handle!!.left
                    if (abs(velocity) < mMaximumTapVelocity) {
                        if (inThreshold(top, left)) {
                            if (isAllowSingleTap) {
                                playSoundEffect(SoundEffectConstants.CLICK)
                                if (isOpened) {
                                    //animateClose(vertical ? top : left);
                                    animateClose(side)
                                } else {
                                    animateOpen(side)
                                    //animateOpen(vertical ? top : left);
                                }
                            } else {
                                performFling(if (vertical) top else left, velocity, false)
                            }
                        } else {
                            performFling(if (vertical) top else left, velocity, false)
                        }
                    } else {
                        performFling(if (vertical) top else left, velocity, false)
                    }
                }
            }
        }
        return mTracking || mAnimating || super.onTouchEvent(event)
    }

    private fun inThreshold(top: Int, left: Int): Boolean {
        when (mOrientation) {
            Orientation.TOP -> return if (isOpened) top > bottom - getTop() - mHandleHeight - mTopOffset - mTapThreshold else top < mTapThreshold - mBottomOffset
            Orientation.LEFT -> return if (isOpened) left > right - getLeft() - mHandleWidth - mTopOffset - mTapThreshold else left < mTapThreshold - mBottomOffset
            Orientation.BOTTOM -> return if (isOpened) top < mTapThreshold + mTopOffset else top > mBottomOffset + bottom - getTop() - mHandleHeight - mTapThreshold
            Orientation.RIGHT -> return if (isOpened) left < mTapThreshold + mTopOffset else left > mBottomOffset + right - getLeft() - mHandleWidth - mTapThreshold
            else -> {}
        }
        return false
    }

    private fun animateClose(position: Int) {
        prepareTracking(position)
        performFling(position, (mMaximumAcceleration * if (mInvert) -1 else 1).toFloat(), true)
    }

    private fun animateOpen(position: Int) {
        prepareTracking(position)
        performFling(position, (mMaximumAcceleration * if (mInvert) 1 else -1).toFloat(), true)
    }

    private fun performFling(position: Int, velocity: Float, always: Boolean) {
        mAnimationPosition = position.toFloat()
        mAnimatedVelocity = velocity
        if (isOpened) {
            if (mInvert) {
                if (always || velocity < -mMaximumMajorVelocity || position < (if (mVertical) height else width) / 2 &&
                    velocity > -mMaximumMajorVelocity
                ) {
                    // We are expanded and are now going to animate away.
                    mAnimatedAcceleration = -mMaximumAcceleration.toFloat()
                    if (velocity > 0) {
                        mAnimatedVelocity = 0f
                    }
                } else {
                    // We are expanded, but they didn't move sufficiently to cause
                    // us to retract.  Animate back to the expanded position.
                    mAnimatedAcceleration = mMaximumAcceleration.toFloat()
                    if (velocity < 0) {
                        mAnimatedVelocity = 0f
                    }
                }
            } else if (always || velocity > mMaximumMajorVelocity || position > mTopOffset + (if (mVertical) mHandleHeight else mHandleWidth) &&
                velocity > -mMaximumMajorVelocity
            ) {
                // We are expanded, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the collapsed position.
                mAnimatedAcceleration = mMaximumAcceleration.toFloat()
                if (velocity < 0) {
                    mAnimatedVelocity = 0f
                }
            } else {
                // We are expanded and are now going to animate away.
                mAnimatedAcceleration = -mMaximumAcceleration.toFloat()
                if (velocity > 0) {
                    mAnimatedVelocity = 0f
                }
            }
        } else {
            //else if (!always && (velocity > mMaximumMajorVelocity ||
            //		(position > (mVertical ? getHeight() : getWidth()) / 2 &&
            //				velocity > -mMaximumMajorVelocity))) {
            if (velocity > mMaximumMajorVelocity ||
                position > (if (mVertical) height else width) / 2 &&
                velocity > -mMaximumMajorVelocity
            ) {
                // We are collapsed, and they moved enough to allow us to expand.
                mAnimatedAcceleration = mMaximumAcceleration.toFloat()
                if (velocity < 0) {
                    mAnimatedVelocity = 0f
                }
            } else {
                // We are collapsed, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the collapsed position.
                mAnimatedAcceleration = -mMaximumAcceleration.toFloat()
                if (velocity > 0) {
                    mAnimatedVelocity = 0f
                }
            }
        }

//		if (mInvert)
//			mAnimatedAcceleration *= -1;
        val now = SystemClock.uptimeMillis()
        mAnimationLastTime = now
        mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION
        mAnimating = true
        mHandler.removeMessages(MSG_ANIMATE)
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime)
        stopTracking()
    }

    private fun prepareTracking(position: Int) {
        mTracking = true
        mVelocityTracker = VelocityTracker.obtain()
        val opening = !isOpened
        if (opening) {
            mAnimatedAcceleration = mMaximumAcceleration.toFloat()
            mAnimatedVelocity = mMaximumMajorVelocity.toFloat()
            when (mOrientation) {
                Orientation.TOP, Orientation.LEFT -> mAnimationPosition = mBottomOffset.toFloat()
                Orientation.BOTTOM -> mAnimationPosition =
                    (mBottomOffset + height - mHandleHeight).toFloat()
                Orientation.RIGHT -> mAnimationPosition =
                    (mBottomOffset + width - mHandleWidth).toFloat()
                else -> {}
            }
            moveHandle(mAnimationPosition.toInt())
            mAnimating = true
            mHandler.removeMessages(MSG_ANIMATE)
            val now = SystemClock.uptimeMillis()
            mAnimationLastTime = now
            mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION
            mAnimating = true
        } else {
            if (mAnimating) {
                mAnimating = false
                mHandler.removeMessages(MSG_ANIMATE)
            }
            moveHandle(position)
        }
    }

    private fun moveHandle(position: Int) {
        val handle = handle
        when (mOrientation) {
            Orientation.TOP -> {
                if (position == EXPANDED_FULL_OPEN) {
                    handle!!.offsetTopAndBottom(bottom - top - mTopOffset - mHandleHeight - handle.top)
                    invalidate()
                } else if (position == COLLAPSED_FULL_CLOSED) {
                    handle!!.offsetTopAndBottom(-mBottomOffset - handle.top)
                    invalidate()
                } else {
                    val top = handle!!.top
                    var deltaY = position - top
                    if (position < -mBottomOffset) {
                        deltaY = -mBottomOffset - top
                    } else if (position > bottom - getTop() - mTopOffset - mHandleHeight) {
                        deltaY = bottom - getTop() - mTopOffset - mHandleHeight - top
                    }
                    handle.offsetTopAndBottom(deltaY)
                    val frame = mFrame
                    val region = mInvalidate
                    handle.getHitRect(frame)
                    region.set(frame)
                    region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY)
                    region.union(
                        0,
                        frame.bottom - deltaY,
                        width,
                        frame.bottom - deltaY + content!!.height
                    )
                    invalidate()
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener!!.onScrolling(handle.top)
                }
            }
            Orientation.BOTTOM -> {
                if (position == EXPANDED_FULL_OPEN) {
                    handle!!.offsetTopAndBottom(mTopOffset - handle.top)
                    invalidate()
                } else if (position == COLLAPSED_FULL_CLOSED) {
                    handle!!.offsetTopAndBottom(mBottomOffset + bottom - top - mHandleHeight - handle.top)
                    invalidate()
                } else {
                    val top = handle!!.top
                    var deltaY = position - top
                    if (position < mTopOffset) {
                        deltaY = mTopOffset - top
                    } else if (deltaY > mBottomOffset + bottom - getTop() - mHandleHeight - top) {
                        deltaY = mBottomOffset + bottom - getTop() - mHandleHeight - top
                    }
                    handle.offsetTopAndBottom(deltaY)
                    val frame = mFrame
                    val region = mInvalidate
                    handle.getHitRect(frame)
                    region.set(frame)
                    region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY)
                    region.union(
                        0,
                        frame.bottom - deltaY,
                        width,
                        frame.bottom - deltaY + content!!.height
                    )
                    invalidate(region)
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener!!.onScrolling(handle.top)
                }
            }
            Orientation.RIGHT -> {
                if (position == EXPANDED_FULL_OPEN) {
                    handle!!.offsetLeftAndRight(mTopOffset - handle.left)
                    invalidate()
                } else if (position == COLLAPSED_FULL_CLOSED) {
                    handle!!.offsetLeftAndRight(-mBottomOffset)
                    invalidate()
                } else {
                    val left = handle!!.left
                    var deltaX = position - left
                    if (position < mTopOffset) {
                        deltaX = mTopOffset - left
                    } else if (deltaX > mBottomOffset + right - getLeft() - mHandleWidth - left) {
                        deltaX = mBottomOffset + right - getLeft() - mHandleWidth - left
                    }
                    handle.offsetLeftAndRight(deltaX)
                    val frame = mFrame
                    val region = mInvalidate
                    handle.getHitRect(frame)
                    region.set(frame)
                    region.union(frame.left - deltaX, frame.top, frame.right - deltaX, frame.bottom)
                    region.union(
                        frame.right - deltaX,
                        0,
                        frame.right - deltaX + content!!.width,
                        height
                    )
                    invalidate(region)
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener!!.onScrolling(handle.left)
                }
            }
            Orientation.LEFT -> {
                if (position == EXPANDED_FULL_OPEN) {
                    handle!!.offsetLeftAndRight(right - left - mTopOffset - mHandleWidth - handle.left)
                    invalidate()
                } else if (position == COLLAPSED_FULL_CLOSED) {
                    handle!!.offsetLeftAndRight(-mBottomOffset - handle.left)
                    invalidate()
                } else {
                    val left = handle!!.left
                    var deltaX = position - left
                    if (position < -mBottomOffset) {
                        deltaX = -mBottomOffset - left
                    } else if (position > right - getLeft() - mTopOffset - mHandleWidth) {
                        deltaX = right - getLeft() - mTopOffset - mHandleWidth - left
                    }
                    handle.offsetLeftAndRight(deltaX)
                    val frame = mFrame
                    val region = mInvalidate
                    handle.getHitRect(frame)
                    region.set(frame)
                    region.union(frame.left - deltaX, frame.top, frame.right - deltaX, frame.bottom)
                    region.union(
                        frame.right - deltaX,
                        0,
                        frame.right - deltaX + content!!.width,
                        height
                    )
                    invalidate(region)
                }
                if (mOnDrawerScrollListener != null) {
                    mOnDrawerScrollListener!!.onScrolling(handle.left)
                }
            }
            else -> {}
        }
    }

    private fun prepareContent() {
        if (mAnimating) {
            return
        }

        // Something changed in the content, we need to honor the layout request
        // before creating the cached bitmap
        val content = content
        if (content!!.isLayoutRequested) {
            measureContent()
        }

        // Try only once... we should really loop but it's not a big deal
        // if the draw was cancelled, it will only be temporary anyway
        content.viewTreeObserver.dispatchOnPreDraw()
        content.buildDrawingCache()
        content.visibility = GONE
    }

    fun measureContent() {
        val content = content
        if (mVertical) {
            val childHeight = handle!!.height
            val height = bottom - top - childHeight - mTopOffset
            content!!.measure(
                MeasureSpec.makeMeasureSpec(right - left, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
            if (mOrientation == Orientation.TOP) {
                content.layout(0, height - content.measuredHeight, content.measuredWidth, height)
            } else {
                content.layout(
                    0, mTopOffset + childHeight, content.measuredWidth,
                    mTopOffset + childHeight + content.measuredHeight
                )
            }
        } else {
            val childWidth = handle!!.width
            val width = right - left - childWidth - mTopOffset
            content!!.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(bottom - top, MeasureSpec.EXACTLY)
            )
            if (mOrientation == Orientation.RIGHT) {
                content.layout(
                    childWidth + mTopOffset, 0,
                    mTopOffset + childWidth + content.measuredWidth, content.measuredHeight
                )
            } else {
                content.layout(width - content.measuredWidth, 0, width, content.measuredHeight)
            }
        }
    }

    private fun stopTracking() {
        handle!!.isPressed = false
        mTracking = false
        if (mOnDrawerScrollListener != null) {
            mOnDrawerScrollListener!!.onScrollEnded()
        }
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    private fun doAnimation() {
        if (mAnimating) {
            incrementAnimation()
            if (mInvert) {
                if (mAnimationPosition >= (if (mVertical) height else width) - mTopOffset) {
                    mAnimating = false
                    openDrawer()
                } else if (mAnimationPosition < -mBottomOffset) {
                    mAnimating = false
                    closeDrawer()
                } else {
                    moveHandle(mAnimationPosition.toInt())
                    mCurrentAnimationTime += ANIMATION_FRAME_DURATION.toLong()
                    mHandler.sendMessageAtTime(
                        mHandler.obtainMessage(MSG_ANIMATE),
                        mCurrentAnimationTime
                    )
                }
            } else {
                if (mAnimationPosition >= mBottomOffset + if (mVertical) height else width - 1) {
                    mAnimating = false
                    closeDrawer()
                } else if (mAnimationPosition < mTopOffset) {
                    mAnimating = false
                    openDrawer()
                } else {
                    moveHandle(mAnimationPosition.toInt())
                    mCurrentAnimationTime += ANIMATION_FRAME_DURATION.toLong()
                    mHandler.sendMessageAtTime(
                        mHandler.obtainMessage(MSG_ANIMATE),
                        mCurrentAnimationTime
                    )
                }
            }
        }
    }

    private fun incrementAnimation() {
        val now = SystemClock.uptimeMillis()
        val t = (now - mAnimationLastTime) / 1000.0f // ms -> s
        val position = mAnimationPosition
        val v = mAnimatedVelocity // px/s
        val a = mAnimatedAcceleration // px/s/s
        mAnimationPosition = position + v * t + 0.5f * a * t * t // px
        mAnimatedVelocity = v + a * t // px/s
        mAnimationLastTime = now // ms
    }

    /**
     * Toggles the drawer open and close. Takes effect immediately.
     *
     * @see .open
     * @see .close
     * @see .animateClose
     * @see .animateOpen
     * @see .animateToggle
     */
    fun toggle() {
        if (isOpened) {
            closeDrawer()
        } else {
            openDrawer()
        }
        invalidate()
        requestLayout()
    }

    /**
     * Toggles the drawer open and close with an animation.
     *
     * @see .open
     * @see .close
     * @see .animateClose
     * @see .animateOpen
     * @see .toggle
     */
    fun animateToggle() {
        if (isOpened) {
            animateClose()
        } else {
            animateOpen()
        }
    }

    /**
     * Opens the drawer immediately.
     *
     * @see .toggle
     * @see .close
     * @see .animateOpen
     */
    fun open() {
        openDrawer()
        invalidate()
        requestLayout()
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
    }

    /**
     * Closes the drawer immediately.
     *
     * @see .toggle
     * @see .open
     * @see .animateClose
     */
    fun close() {
        closeDrawer()
        invalidate()
        requestLayout()
    }

    /**
     * Closes the drawer with an animation.
     *
     * @see .close
     * @see .open
     * @see .animateOpen
     * @see .animateToggle
     * @see .toggle
     */
    fun animateClose() {
        prepareContent()
        val scrollListener = mOnDrawerScrollListener
        scrollListener?.onScrollStarted()
        animateClose(side)
        scrollListener?.onScrollEnded()
    }

    /**
     * Opens the drawer with an animation.
     *
     * @see .close
     * @see .open
     * @see .animateClose
     * @see .animateToggle
     * @see .toggle
     */
    fun animateOpen() {
        prepareContent()
        val scrollListener = mOnDrawerScrollListener
        scrollListener?.onScrollStarted()
        animateOpen(side)
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
        scrollListener?.onScrollEnded()
    }

    private fun closeDrawer() {
        moveHandle(COLLAPSED_FULL_CLOSED)
        content!!.visibility = GONE
        content!!.destroyDrawingCache()
        if (!isOpened) {
            return
        }
        isOpened = false
        if (mOnDrawerCloseListener != null) {
            mOnDrawerCloseListener!!.onDrawerClosed()
        }
    }

    private fun openDrawer() {
        moveHandle(EXPANDED_FULL_OPEN)
        content!!.visibility = VISIBLE
        if (isOpened) {
            return
        }
        isOpened = true
        if (mOnDrawerOpenListener != null) {
            mOnDrawerOpenListener!!.onDrawerOpened()
        }
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes open.
     *
     * @param onDrawerOpenListener The listener to be notified when the drawer is opened.
     */
    fun setOnDrawerOpenListener(onDrawerOpenListener: OnDrawerOpenListener?) {
        mOnDrawerOpenListener = onDrawerOpenListener
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes close.
     *
     * @param onDrawerCloseListener The listener to be notified when the drawer is closed.
     */
    fun setOnDrawerCloseListener(onDrawerCloseListener: OnDrawerCloseListener?) {
        mOnDrawerCloseListener = onDrawerCloseListener
    }

    /**
     * Sets the listener that receives a notification when the drawer starts or ends
     * a scroll. A fling is considered as a scroll. A fling will also trigger a
     * activity_base opened or activity_base closed event.
     *
     * @param onDrawerScrollListener The listener to be notified when scrolling
     * starts or stops.
     */
    fun setOnDrawerScrollListener(onDrawerScrollListener: OnDrawerScrollListener?) {
        mOnDrawerScrollListener = onDrawerScrollListener
    }

    /**
     * Convenience method to get the size of the handle.
     * May not return a valid size until the view is layed out.
     *
     * @return Return the height if the component is vertical
     * otherwise returns the width for Horizontal orientation.
     */
    val handleSize: Int
        get() = if (mVertical) handle!!.height else handle!!.width

    /**
     * Unlocks the SlidingDrawer so that touch events are processed.
     *
     * @see .lock
     */
    fun unlock() {
        mLocked = false
    }

    /**
     * Locks the SlidingDrawer so that touch events are ignores.
     *
     * @see .unlock
     */
    fun lock() {
        mLocked = true
    }

    /**
     * Indicates whether the drawer is scrolling or flinging.
     *
     * @return True if the drawer is scroller or flinging, false otherwise.
     */
    val isMoving: Boolean
        get() = mTracking || mAnimating
    var bottomOffset: Int
        get() = mBottomOffset
        set(offset) {
            mBottomOffset = offset
            invalidate()
        }
    var topOffset: Int
        get() = mTopOffset
        set(offset) {
            mTopOffset = offset
            invalidate()
        }

    /**
     * Drawer click listener.
     */
    private inner class DrawerToggler : OnClickListener {
        override fun onClick(v: View) {
            if (mLocked) {
                return
            }
            // mAllowSingleTap isn't relevant here; you're *always*
            // allowed to open/close the drawer by clicking with the
            // trackball.
            if (isAnimateOnClick) {
                animateToggle()
            } else {
                toggle()
            }
        }
    }

    @SuppressLint("HandlerLeak")
    private inner class SlidingHandler : Handler() {
        override fun handleMessage(m: Message) {
            if (m.what == MSG_ANIMATE) {
                doAnimation()
            }
        }
    }

    /**
     * Callback invoked when the drawer is opened.
     */
    fun interface OnDrawerOpenListener {
        /**
         * Invoked when the drawer becomes fully open.
         */
        fun onDrawerOpened()
    }

    /**
     * Callback invoked when the drawer is closed.
     */
    fun interface OnDrawerCloseListener {
        /**
         * Invoked when the drawer becomes fully closed.
         */
        fun onDrawerClosed()
    }

    /**
     * Callback invoked when the drawer is scrolled.
     */
    interface OnDrawerScrollListener {
        /**
         * Invoked when the user starts dragging/flinging the drawer's handle.
         */
        fun onScrollStarted()

        /**
         * Invoked when the user scrolls the drawer's handle.
         */
        fun onScrolling(pos: Int)

        /**
         * Invoked when the user stops dragging/flinging the drawer's handle.
         */
        fun onScrollEnded()
    }

    enum class Side(val value: Int) {
        TOP(0), LEFT(1), BOTTOM(2), RIGHT(3), FRONT(4), BACK(5), CENTER(6);

        companion object {
            fun getByValue(value: Int): Side {
                for (s in values()) {
                    if (s.value == value) {
                        return s
                    }
                }
                throw IllegalArgumentException("There is no 'Side' enum with this value")
            }
        }
    }

    enum class Orientation(val value: Int) {
        TOP(0), LEFT(1), BOTTOM(2), RIGHT(3);

        companion object {
            fun getByValue(value: Int): Orientation {
                for (s in values()) {
                    if (s.value == value) {
                        return s
                    }
                }
                throw IllegalArgumentException("There is no 'Orientation' enum with this value")
            }
        }
    }

    companion object {
        private const val TAP_THRESHOLD = 6
        private const val MAXIMUM_TAP_VELOCITY = 100.0f
        private const val MAXIMUM_MINOR_VELOCITY = 150.0f
        private const val MAXIMUM_MAJOR_VELOCITY = 200.0f
        private const val MAXIMUM_ACCELERATION = 2000.0f
        private const val VELOCITY_UNITS = 1000
        private const val MSG_ANIMATE = 1000
        private const val ANIMATION_FRAME_DURATION = 1000 / 60
        private const val EXPANDED_FULL_OPEN = -10001
        private const val COLLAPSED_FULL_CLOSED = -10002
    }
}