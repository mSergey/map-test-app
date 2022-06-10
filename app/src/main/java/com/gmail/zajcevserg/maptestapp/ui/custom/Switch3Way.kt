package com.gmail.zajcevserg.maptestapp.ui.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.constraintlayout.motion.utils.ViewState
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.CustomSwitchLayoutBinding
import com.gmail.zajcevserg.maptestapp.ui.activity.log
import com.google.android.gms.common.util.Hex


class Switch3Way : ConstraintLayout {



    var isThreeWay = true
        set(value) {

            if (field == value) return
            /*if (!value && switchPosition == SwitchPositions.MIDDLE) {
                switchPosition = SwitchPositions.START
            }*/
            field = value
        }

    var switchPosition: SwitchPositions = SwitchPositions.MIDDLE
        set(value) {
            if (field == value) return
            when {
                field == SwitchPositions.START && value == SwitchPositions.MIDDLE -> {
                    moveThumb(mStartPos, mMiddlePos, 120L)
                }
                field == SwitchPositions.START && value == SwitchPositions.END -> {
                    moveThumb(mStartPos, mEndPos, 240L)
                }
                field == SwitchPositions.MIDDLE && value == SwitchPositions.END -> {
                    moveThumb(mMiddlePos, mEndPos, 120L)
                }
                field == SwitchPositions.MIDDLE && value == SwitchPositions.START -> {
                    moveThumb(mMiddlePos, mStartPos, 120L)
                }
                field == SwitchPositions.END && value == SwitchPositions.MIDDLE -> {
                    moveThumb(mEndPos, mMiddlePos, 120L)
                }
                field == SwitchPositions.END && value == SwitchPositions.START -> {
                    moveThumb(mEndPos, mStartPos, 240L)
                }
            }
            field = value
        }


    private lateinit var binding: CustomSwitchLayoutBinding
    private var bundle = Bundle()
    private var mOnPositionChangeListener: ((SwitchPositions) -> Unit)? = null
    private val mClickListener: SwitchClickListener = SwitchClickListener()
    private var mIsAnimationRunning = false
    private val mStartPos = convertDpToPixel(0)
    private val mMiddlePos = convertDpToPixel(9)
    private val mEndPos = convertDpToPixel(18)
    private var thumbStartPositionColor: Int = 0
    private var thumbMiddlePositionColor: Int = 0
    private var thumbEndPositionColor: Int = 0
    private var disabledThumbColor: Int = 0
    private val mInitialStartTrackLevel: Int = 10000 * 8 / 34
    private val mInitialEndTrackLevel: Int = 10000 * 26 / 34
    private val mInitialMiddleTrackLevel: Int = 10000 / 2
    private val mTrackLevelDelta: ((Float) -> Int) = {
        (10000 * it / convertDpToPixel(34)).toInt()
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        binding = CustomSwitchLayoutBinding.inflate(LayoutInflater.from(context), this)
        parseAttrs(context, attrs)
        setListeners()
        this.isClickable = true
        this.isFocusable = true
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        binding = CustomSwitchLayoutBinding.inflate(LayoutInflater.from(context), this)
        parseAttrs(context, attrs)
        setListeners()
        this.isClickable = true
        this.isFocusable = true
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val side = convertDpToPixel(48).toInt()
        val mSideMeasureSpec = MeasureSpec.makeMeasureSpec(side, MeasureSpec.EXACTLY)
        super.onMeasure(mSideMeasureSpec, mSideMeasureSpec)
    }

    fun setOnPositionChangeByClickListener(listener: ((SwitchPositions) -> Unit)?) {
        mOnPositionChangeListener = listener
    }


    private fun parseAttrs(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.Switch3Way)
        val trackColorStart = attributes.getColor(R.styleable.Switch3Way_track_color_start, 0)
        val trackColorEnd = attributes.getColor(R.styleable.Switch3Way_track_color_end, 0)
        thumbStartPositionColor = attributes.getColor(R.styleable.Switch3Way_thumb_color_start, 0)
        thumbMiddlePositionColor = attributes.getColor(R.styleable.Switch3Way_thumb_color_middle, 0)
        thumbEndPositionColor = attributes.getColor(R.styleable.Switch3Way_thumb_color_end, 0)
        isThreeWay = attributes.getBoolean(R.styleable.Switch3Way_is_three_way, true)
        val position = attributes.getInt(R.styleable.Switch3Way_switch_position, 1)
        attributes.recycle()

        switchPosition = SwitchPositions.values()[position]
        when (switchPosition) {
            SwitchPositions.START -> setupStartPosition()
            SwitchPositions.MIDDLE -> setupMiddlePosition()
            SwitchPositions.END -> setupEndPosition()
        }
        val disabledTrackColor =
            ResourcesCompat.getColor(resources, R.color.disabled_switch_track_color, null)
        disabledThumbColor =
            ResourcesCompat.getColor(resources, R.color.disabled_switch_thumb_color, null)
        binding.trackStart.imageTintList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(-android.R.attr.state_enabled)
            ),
            intArrayOf(trackColorStart, disabledTrackColor)
        )

        binding.trackEnd.imageTintList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(-android.R.attr.state_enabled)
            ),
            intArrayOf(trackColorEnd, disabledTrackColor)
        )

        setThumbColor(when(switchPosition) {
            SwitchPositions.START -> thumbStartPositionColor
            SwitchPositions.MIDDLE -> thumbMiddlePositionColor
            SwitchPositions.END -> thumbEndPositionColor
        })
    }

    private fun setListeners() {
        this.setOnClickListener(mClickListener)
        this.forEach {
            it.setOnClickListener(mClickListener)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        this.forEach { it.isEnabled = enabled }
        super.setEnabled(enabled)
    }


    private fun setThumbColor(color: Int) {

        binding.thumb.imageTintList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_enabled),
                intArrayOf(-android.R.attr.state_enabled)
            ),
            intArrayOf(color, disabledThumbColor)
        )

    }

    private fun moveThumb(from: Float,
                          to: Float,
                          _duration: Long
    ) {
        if (windowToken != null && ViewCompat.isLaidOut(this)) {
            when (to) {
                mStartPos -> setThumbColor(thumbStartPositionColor)
                mMiddlePos -> setThumbColor(thumbMiddlePositionColor)
                mEndPos -> setThumbColor(thumbEndPositionColor)
            }
            ObjectAnimator
                .ofFloat(binding.thumbMotionLayer, "translationX", from, to)
                .apply {
                    interpolator = AccelerateInterpolator()
                    duration = _duration
                    setEvaluator { completePart, startPos, endPos ->
                        startPos as Float; endPos as Float
                        val translationX = startPos + (endPos - startPos) * completePart
                        binding.trackStart.drawable.level = mInitialStartTrackLevel + mTrackLevelDelta(translationX)
                        binding.trackEnd.drawable.level = mInitialEndTrackLevel - mTrackLevelDelta(translationX)
                        translationX
                    }
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
        } else {
            when (to) {
                mStartPos -> setupStartPosition()
                mMiddlePos -> setupMiddlePosition()
                mEndPos -> setupEndPosition()
            }
        }
    }

    private fun setupStartPosition() {
        binding.trackStart.drawable.level = mInitialStartTrackLevel
        binding.trackEnd.drawable.level = mInitialEndTrackLevel
        binding.thumbMotionLayer.translationX = mStartPos
        setThumbColor(thumbStartPositionColor)
    }

    private fun setupMiddlePosition() {
        binding.trackStart.drawable.level = mInitialMiddleTrackLevel
        binding.trackEnd.drawable.level = mInitialMiddleTrackLevel
        binding.thumbMotionLayer.translationX = mMiddlePos
        setThumbColor(thumbMiddlePositionColor)
    }

    private fun setupEndPosition() {
        binding.trackStart.drawable.level = mInitialEndTrackLevel
        binding.trackEnd.drawable.level = mInitialStartTrackLevel
        binding.thumbMotionLayer.translationX = mEndPos
        setThumbColor(thumbEndPositionColor)
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
            switchPosition = if (isThreeWay) {
                when (switchPosition) {
                    SwitchPositions.START -> SwitchPositions.MIDDLE
                    SwitchPositions.MIDDLE -> SwitchPositions.END
                    SwitchPositions.END -> SwitchPositions.START
                }
            } else {
                when (switchPosition) {
                    SwitchPositions.START -> SwitchPositions.END
                    SwitchPositions.END -> SwitchPositions.START
                    SwitchPositions.MIDDLE -> SwitchPositions.START
                }
            }
            mOnPositionChangeListener?.invoke(switchPosition)
        }
    }

    enum class SwitchPositions(posLevel: Int) {
        START(1), MIDDLE(1), END(3)
    }

}
