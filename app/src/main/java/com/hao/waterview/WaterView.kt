package com.hao.waterview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import java.util.*

class WaterView(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) :
    View(context, attrs, defStyleAttr) {
    // 初始波纹半径,默认为0
    private var mInitialRadius = 0f

    // 最大波纹半径
    private var mMaxRadius = 0f

    // 一个波纹从创建到消失的持续时间
    private var mDuration: Long = 5000

    // 波纹的创建速度，每2s创建一个
    private var mSpeed = 2000

    //填充颜色
    private val color = Color.parseColor("#98FF0000")

    //波纹最大半径占的比例，默认为100%
    private var mMaxRadiusRate = 0.9f
    private var mMaxRadiusSet = false
    private var mIsRunning = false
    private var mLastCreateTime: Long = 0
    private var mCircleList: MutableList<Circle>? = null
    private var mInterpolator: Interpolator? = null
    private var mPaint: Paint? = null
    private var mCreateCircle: Runnable? = object : Runnable {
        override fun run() {
            if (mIsRunning) {
                newCircle()
                postDelayed(this, mSpeed.toLong())
            }
        }
    }

    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?) : this(context, null, 0)

    init {
        mCircleList = ArrayList()
//        mInterpolator = LinearInterpolator()
        mInterpolator = LinearOutSlowInInterpolator()
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.color = color
        mPaint!!.style = Paint.Style.FILL
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (!mMaxRadiusSet) {
            mMaxRadius = Math.max(w, h) * mMaxRadiusRate
        }
    }

    /**
     * 开始
     */
    fun start() {
        if (!mIsRunning) {
            mIsRunning = true
            mCreateCircle!!.run()
        }
    }

    /**
     * 缓慢停止
     */
    fun stop() {
        mIsRunning = false
    }

    /**
     * 立即停止
     */
    fun stopImmediately() {
        mIsRunning = false
        mCircleList!!.clear()
        invalidate()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopImmediately()
        mCreateCircle = null
    }

    override fun onDraw(canvas: Canvas) {
        val iterator = mCircleList!!.iterator()
        while (iterator.hasNext()) {
            val circle = iterator.next()
            val radius = circle.currentRadius
            if (System.currentTimeMillis() - circle.mCreateTime < mDuration) {
                mPaint!!.alpha = circle.alpha
                canvas.drawCircle(
                    width / 2.toFloat(),
                    height / 2.toFloat(),
                    radius,
                    mPaint!!
                )
            } else {
                iterator.remove()
            }
        }
        if (mCircleList!!.size > 0) {
            postInvalidateDelayed(10)
        }
    }

    private fun newCircle() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - mLastCreateTime < mSpeed) {
            return
        }
        val circle = Circle()
        mCircleList!!.add(circle)
        invalidate()
        mLastCreateTime = currentTime
    }

    private inner class Circle internal constructor() {
        val mCreateTime: Long
        val alpha: Int
            get() {
                val percent =
                    (currentRadius - mInitialRadius) / (mMaxRadius - mInitialRadius)
                return (255 - mInterpolator!!.getInterpolation(percent) * 255).toInt()
            }

        val currentRadius: Float
            get() {
                val percent =
                    (System.currentTimeMillis() - mCreateTime) * 1.0f / mDuration
                return mInitialRadius + mInterpolator!!.getInterpolation(percent) * (mMaxRadius - mInitialRadius)
            }

        init {
            mCreateTime = System.currentTimeMillis()
        }
    }

    fun setInterpolator(interpolator: Interpolator?) {
        mInterpolator = interpolator
        if (mInterpolator == null) {
            mInterpolator = LinearInterpolator()
        }
    }

    fun setMaxRadiusRate(maxRadiusRate: Float) {
        mMaxRadiusRate = maxRadiusRate
    }

    /**
     * 设置波纹颜色
     */
    fun setColor(color: Int) {
        mPaint!!.color = color
    }

    /**
     * 设置填充样式
     */
    fun setStyle(style: Paint.Style?) {
        mPaint!!.style = style
    }

    fun setInitialRadius(radius: Float) {
        mInitialRadius = radius
    }

    fun setDuration(duration: Long) {
        mDuration = duration
    }

    fun setMaxRadius(maxRadius: Float) {
        mMaxRadius = maxRadius
        mMaxRadiusSet = true
    }

    fun setSpeed(speed: Int) {
        mSpeed = speed
    }
}