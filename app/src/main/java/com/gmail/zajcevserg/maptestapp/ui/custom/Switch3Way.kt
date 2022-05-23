package com.gmail.zajcevserg.maptestapp.ui.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.forEach
import com.gmail.zajcevserg.maptestapp.R

import com.gmail.zajcevserg.maptestapp.databinding.CustomSwitchLayoutBinding


class Switch3Way : ConstraintLayout {

    var isThreeWay = true
        set(value) {
            if (field == value) return
            field = value
            if (!value && switchPosition == SwitchPositions.MIDDLE) {
                checked = false
            } else if (!value && switchPosition == SwitchPositions.END) {
                checked = true
            }
        }


    var switchPosition: SwitchPositions = SwitchPositions.MIDDLE
        set(value) {
            if (field == value) return
            when {
                field == SwitchPositions.START && value == SwitchPositions.MIDDLE -> {
                    setThumbColor(thumbMiddlePositionColor)
                    moveThumb(mStartPos, mMiddlePos, 120L)
                }
                field == SwitchPositions.START && value == SwitchPositions.END -> {
                    setThumbColor(thumbEndPositionColor)
                    moveThumb(mStartPos, mEndPos, 240L)
                }
                field == SwitchPositions.MIDDLE && value == SwitchPositions.END -> {
                    setThumbColor(thumbEndPositionColor)
                    moveThumb(mMiddlePos, mEndPos, 120L)
                }
                field == SwitchPositions.MIDDLE && value == SwitchPositions.START -> {
                    setThumbColor(thumbStartPositionColor)
                    moveThumb(mMiddlePos, mStartPos, 120L)
                }
                field == SwitchPositions.END && value == SwitchPositions.MIDDLE -> {
                    setThumbColor(thumbMiddlePositionColor)
                    moveThumb(mEndPos, mMiddlePos, 120L)
                }
                field == SwitchPositions.END && value == SwitchPositions.START -> {
                    setThumbColor(thumbStartPositionColor)
                    moveThumb(mEndPos, mStartPos, 240L)
                }
            }
            isThreeWay = true
            field = value
        }

    var checked = false
        set(value) {
            switchPosition = if (value) SwitchPositions.END else SwitchPositions.START
            isThreeWay = false
            field = value
        }

    private lateinit var binding: CustomSwitchLayoutBinding
    private var onPositionChangeListener: ((SwitchPositions)-> Unit)? = null
    private var onSwitchCheckListener: ((Boolean)-> Unit)? = null
    private var mIsAnimationRunning = false
    private val mStartPos = convertDpToPixel(0)
    private val mMiddlePos = convertDpToPixel(9)
    private val mEndPos = convertDpToPixel(18)
    private val mClickListener: SwitchClickListener = SwitchClickListener()
    private var thumbStartPositionColor: Int = 0
    private var thumbMiddlePositionColor: Int = 0
    private var thumbEndPositionColor: Int = 0


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        binding = CustomSwitchLayoutBinding.inflate(LayoutInflater.from(context), this)
        parseAttrs(context, attrs)
        setListeners()
        setup()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        binding = CustomSwitchLayoutBinding.inflate(LayoutInflater.from(context), this)
        parseAttrs(context, attrs)
        setListeners()
        setup()
    }

    fun addPositionChangeListener(listener: (position: SwitchPositions)-> Unit) {
        onPositionChangeListener = listener
    }

    fun addCheckListener(listener: (checked: Boolean)-> Unit) {
        onSwitchCheckListener = listener
    }


    private fun parseAttrs(context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.Switch3Way)
        isThreeWay = attributes.getBoolean(R.styleable.Switch3Way_is_three_way, true)
        val trackColorStart = attributes.getColor(R.styleable.Switch3Way_track_color_start, 0)
        val trackColorEnd = attributes.getColor(R.styleable.Switch3Way_track_color_end, 0)
        thumbStartPositionColor = attributes.getColor(R.styleable.Switch3Way_thumb_color_start, 0)
        thumbMiddlePositionColor = attributes.getColor(R.styleable.Switch3Way_thumb_color_middle, 0)
        thumbEndPositionColor = attributes.getColor(R.styleable.Switch3Way_thumb_color_end, 0)
        attributes.recycle()

        binding.trackStart.imageTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(trackColorStart)
        )

        binding.trackEnd.imageTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(trackColorEnd)
        )
    }




    private fun setup() {
        if (isThreeWay) {
            when (switchPosition) {
                SwitchPositions.START -> setupStartPosition()
                SwitchPositions.MIDDLE -> setupMiddlePosition()
                SwitchPositions.END -> setupEndPosition()
            }
        } else {
            if (checked) setupStartPosition()
            else setupEndPosition()
        }
    }

    private fun setupStartPosition() {
        binding.trackStart.drawable.level = 10000 / 34 * 8
        binding.motionLayer.translationX = mStartPos
        setThumbColor(thumbStartPositionColor)
    }

    private fun setupMiddlePosition() {
        binding.trackStart.drawable.level = 10000 / 2
        binding.motionLayer.translationX = mMiddlePos
        setThumbColor(thumbMiddlePositionColor)
    }

    private fun setupEndPosition() {
        binding.trackStart.drawable.level = 10000 / 34 * 26
        binding.motionLayer.translationX = mEndPos
        setThumbColor(thumbEndPositionColor)
    }

    private fun setListeners() {
        binding.rootVg.setOnClickListener(mClickListener)
        binding.rootVg.forEach { child ->
            child.setOnClickListener(mClickListener)
        }
    }

    private fun setThumbColor(color: Int) {
       binding.thumb.imageTintList = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_enabled)),
            intArrayOf(color)
       )
    }

    private fun moveThumb(from: Float,
                          to: Float,
                          _duration: Long
    ) {
        ObjectAnimator
            .ofFloat(binding.motionLayer, "translationX", from, to)
            .apply {
                interpolator = AccelerateInterpolator()
                duration = _duration
                setEvaluator { completePart, startPos, endPos ->
                    startPos as Float; endPos as Float
                    val translationX = startPos + (endPos - startPos) * completePart
                    binding.trackStart.drawable.level = (10000 / mEndPos * translationX).toInt()
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

            if (isThreeWay) {
                when (switchPosition) {
                    SwitchPositions.START -> {
                        switchPosition = SwitchPositions.MIDDLE
                        /*setThumbColor(thumbMiddlePositionColor)
                        moveThumb(mStartPos, mMiddlePos, 120L)*/
                    }
                    SwitchPositions.MIDDLE -> {

                        switchPosition = SwitchPositions.END
                        /*setThumbColor(thumbEndPositionColor)
                        moveThumb(mMiddlePos, mEndPos, 120L)*/
                    }
                    SwitchPositions.END -> {

                        switchPosition = SwitchPositions.START
                        /*setThumbColor(thumbStartPositionColor)
                        moveThumb(mEndPos, mStartPos, 240L)*/
                    }
                }
                onPositionChangeListener?.invoke(switchPosition)
            } else {
                if (checked) {
                    setThumbColor(thumbStartPositionColor)
                    checked = false
                    moveThumb(mEndPos, mStartPos, 240L)
                } else {
                    setThumbColor(thumbEndPositionColor)
                    checked = true
                    moveThumb(mStartPos, mEndPos, 240L)
                }
                onSwitchCheckListener?.invoke(checked)
            }

        }
    }
    enum class SwitchPositions {
        START, MIDDLE, END
    }

}
