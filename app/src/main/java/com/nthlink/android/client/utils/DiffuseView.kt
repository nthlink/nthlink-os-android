package com.nthlink.android.client.utils

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.nthlink.android.client.R

/**
 * https://cloud.tencent.com/developer/article/1742156
 * https://github.com/Airsaid/DiffuseView
 */

class DiffuseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /** 扩散圆圈颜色  */
    private var mColor = ContextCompat.getColor(context, android.R.color.black)

    /** 圆圈中心颜色  */
    private var mCoreColor = ContextCompat.getColor(context, android.R.color.white)

    /** 圆圈中心图片  */
    private var mBitmap: Bitmap? = null

    /** 中心圆半径  */
    private var mCoreRadius = 150f

    /** 扩散圆宽度  */
    private var mDiffuseWidth = 3

    /** 最大宽度  */
    private var mMaxWidth = 255

    /** 扩散速度  */
    private var mDiffuseSpeed = 5

    /** 是否正在扩散中  */
    private var mIsDiffuse = false

    // 透明度集合
    private val mAlphas: MutableList<Int> = ArrayList()

    // 扩散圆半径集合
    private val mWidths: MutableList<Int> = ArrayList()
    private val mPaint = Paint()

    init {
        initPain()
        val a: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.DiffuseView, defStyleAttr, 0)
        mColor = a.getColor(R.styleable.DiffuseView_diffuse_color, mColor)
        mCoreColor = a.getColor(R.styleable.DiffuseView_diffuse_coreColor, mCoreColor)
        mCoreRadius = a.getFloat(R.styleable.DiffuseView_diffuse_coreRadius, mCoreRadius)
        mDiffuseWidth = a.getInt(R.styleable.DiffuseView_diffuse_width, mDiffuseWidth)
        mMaxWidth = a.getInt(R.styleable.DiffuseView_diffuse_maxWidth, mMaxWidth)
        mDiffuseSpeed = a.getInt(R.styleable.DiffuseView_diffuse_speed, mDiffuseSpeed)
        val imageId = a.getResourceId(R.styleable.DiffuseView_diffuse_coreImage, -1)
        if (imageId != -1) mBitmap = BitmapFactory.decodeResource(resources, imageId)
        a.recycle()
    }

    private fun initPain() {
        mPaint.isAntiAlias = true
        mAlphas.add(255)
        mWidths.add(0)
    }

    override fun invalidate() {
        if (hasWindowFocus()) {
            super.invalidate()
        }
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            invalidate()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // 绘制扩散圆
        mPaint.color = mColor
        for (i in mAlphas.indices) {
            // 设置透明度
            val alpha = mAlphas[i]
            mPaint.setAlpha(alpha)
            // 绘制扩散圆
            val width = mWidths[i]
            canvas.drawCircle(
                (getWidth() / 2).toFloat(),
                (height / 2).toFloat(),
                mCoreRadius + width,
                mPaint
            )
            if (alpha > 0 && width < mMaxWidth) {
                mAlphas[i] = if (alpha - mDiffuseSpeed > 0) alpha - mDiffuseSpeed else 1
                mWidths[i] = width + mDiffuseSpeed
            }
        }
        // 判断当扩散圆扩散到指定宽度时添加新扩散圆
        if (mWidths[mWidths.size - 1] >= mMaxWidth / mDiffuseWidth) {
            mAlphas.add(255)
            mWidths.add(0)
        }
        // 超过10个扩散圆，删除最外层
        if (mWidths.size >= 10) {
            mWidths.removeAt(0)
            mAlphas.removeAt(0)
        }

        // 绘制中心圆及图片
        mPaint.setAlpha(255)
        mPaint.setColor(mCoreColor)
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), mCoreRadius, mPaint)

        mBitmap?.let {
            canvas.drawBitmap(
                it,
                (width / 2 - it.width / 2).toFloat(),
                (height / 2 - it.height / 2).toFloat(),
                mPaint
            )
        }

        if (mIsDiffuse) {
            invalidate()
        }
    }

    /**
     * 开始扩散
     */
    fun start() {
        mIsDiffuse = true
        invalidate()
    }

    /**
     * 停止扩散
     */
    fun stop() {
        mIsDiffuse = false
        mWidths.clear()
        mAlphas.clear()
        mAlphas.add(255)
        mWidths.add(0)
        invalidate()
    }

    /**
     * 是否扩散中
     */
    fun isDiffuse(): Boolean {
        return mIsDiffuse
    }

    /**
     * 设置扩散圆颜色
     */
    fun setColor(colorId: Int) {
        mColor = colorId
    }

    /**
     * 设置中心圆颜色
     */
    fun setCoreColor(colorId: Int) {
        mCoreColor = colorId
    }

    /**
     * 设置中心圆图片
     */
    fun setCoreImage(imageId: Int) {
        mBitmap = BitmapFactory.decodeResource(resources, imageId)
    }

    /**
     * 设置中心圆半径
     */
    fun setCoreRadius(radius: Int) {
        mCoreRadius = radius.toFloat()
    }

    /**
     * 设置扩散圆宽度(值越小宽度越大)
     */
    fun setDiffuseWidth(width: Int) {
        mDiffuseWidth = width
    }

    /**
     * 设置最大宽度
     */
    fun setMaxWidth(maxWidth: Int) {
        mMaxWidth = maxWidth
    }

    /**
     * 设置扩散速度，值越大速度越快
     */
    fun setDiffuseSpeed(speed: Int) {
        mDiffuseSpeed = speed
    }
}