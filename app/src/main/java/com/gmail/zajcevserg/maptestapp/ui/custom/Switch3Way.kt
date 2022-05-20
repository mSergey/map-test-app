package com.gmail.zajcevserg.maptestapp.ui.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.ShapeDrawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.addListener
import androidx.core.view.doOnLayout
import androidx.core.view.forEach
import com.gmail.zajcevserg.maptestapp.R

import com.gmail.zajcevserg.maptestapp.databinding.CustomSwitchLayoutBinding
import com.gmail.zajcevserg.maptestapp.databinding.StubImageBinding
import com.gmail.zajcevserg.maptestapp.ui.activity.log


class Switch3Way : ConstraintLayout {

    var switchPosition: SwitchPositions = SwitchPositions.MIDDLE
    var isTreeWay = true
    var onPositionChangeListener: OnSwitchPositionChangeListener? = null

    private lateinit var binding: CustomSwitchLayoutBinding
    private var mIsAnimationRunning = false
    private val mStartPos = convertDpToPixel(0)
    private val mMiddlePos = convertDpToPixel(10)
    private val mEndPos = convertDpToPixel(20)
    private val mClickListener: SwitchClickListener = SwitchClickListener()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        binding = CustomSwitchLayoutBinding.inflate(LayoutInflater.from(context), this)
        setup(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        binding = CustomSwitchLayoutBinding.inflate(LayoutInflater.from(context), this)
        setup(context, attrs)
    }


    private fun setup(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.Switch3Way)

        with(attributes) {
            val thumbStartColor = getColor(R.styleable.Switch3Way_thumb_color_start, 0)
            val thumbStartMiddle = getColor(R.styleable.Switch3Way_thumb_color_middle, 0)
            val thumbStartEnd = getColor(R.styleable.Switch3Way_thumb_color_end, 0)
            val trackColorStart = getColor(R.styleable.Switch3Way_track_color_start, 0)
            val trackColorEnd = getColor(R.styleable.Switch3Way_track_color_end, 0)
            val trackStartDrawable = binding.trackStart.drawable as ShapeDrawable
        }
        attributes.recycle()
        binding.motionLayer.doOnLayout {
            it.translationX =
                when (switchPosition) {
                    SwitchPositions.START -> mStartPos
                    SwitchPositions.MIDDLE -> mMiddlePos
                    SwitchPositions.END -> mEndPos
                }
            binding.rootVg.setOnClickListener(mClickListener)
            binding.rootVg.forEach { child ->
                child.setOnClickListener(mClickListener)
            }
        }
    }
    private fun animateThumbTranslationX(viewToAnimate: View,
                                         from: Float,
                                         to: Float,
                                         _duration: Long
    ) {
        ObjectAnimator
            .ofFloat(viewToAnimate, "translationX", from, to).apply {
                interpolator = AccelerateInterpolator()
                duration = _duration
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator?) {
                        mIsAnimationRunning = true
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        mIsAnimationRunning = false
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                        mIsAnimationRunning = false
                    }
                })
                start()
            }
    }
    private fun convertDpToPixel(dp: Int): Float {
        return dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
    private fun convertPixelToDp(px: Int): Float {
        return px / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }
    private inner class SwitchClickListener : OnClickListener {
        override fun onClick(view: View) {
            if (mIsAnimationRunning) return
            if (isTreeWay) {
                when (switchPosition) {
                    SwitchPositions.START -> {
                        switchPosition = SwitchPositions.MIDDLE
                        animateThumbTranslationX(binding.motionLayer, mStartPos, mMiddlePos, 125L)
                    }
                    SwitchPositions.MIDDLE -> {
                        switchPosition = SwitchPositions.END
                        animateThumbTranslationX(binding.motionLayer, mMiddlePos, mEndPos, 125L)
                    }
                    SwitchPositions.END -> {
                        switchPosition = SwitchPositions.START
                        animateThumbTranslationX(binding.motionLayer, mEndPos, mStartPos, 250L)
                    }
                }
            } else {
                when (switchPosition) {
                    SwitchPositions.START -> {
                        switchPosition = SwitchPositions.END
                        animateThumbTranslationX(binding.motionLayer, mStartPos, mEndPos, 250L)
                    }
                    SwitchPositions.MIDDLE -> {
                        // nothing to do
                    }
                    SwitchPositions.END -> {
                        switchPosition = SwitchPositions.START
                        animateThumbTranslationX(binding.motionLayer, mEndPos, mStartPos, 250L)
                    }
                }
            }
            onPositionChangeListener?.onSwitchPositionChange(switchPosition)
        }
    }
    enum class SwitchPositions {
        START, MIDDLE, END
    }
    interface OnSwitchPositionChangeListener {
        fun onSwitchPositionChange(position: SwitchPositions)
    }

}
