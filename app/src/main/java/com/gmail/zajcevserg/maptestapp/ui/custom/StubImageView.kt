package com.gmail.zajcevserg.maptestapp.ui.custom

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import com.gmail.zajcevserg.maptestapp.R

import com.gmail.zajcevserg.maptestapp.databinding.StubImageBinding

private const val HIDE_ANIM_DURATION = 1500L
private const val SHOW_ANIM_DURATION = 50L

class StubImageView : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val binding = StubImageBinding.inflate(LayoutInflater.from(context), this, true)
        setup(binding, context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val binding = StubImageBinding.inflate(LayoutInflater.from(context), this, true)
        setup(binding, context, attrs)
    }

    fun hide() {
        this.animate().also {
            it.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) { visibility = GONE }
            })
            it.duration = HIDE_ANIM_DURATION
            it.interpolator = DecelerateInterpolator()
            it.alpha(0f)
            it.start()
        }
    }

    fun show(listener: OnShowEndListener?){
        this.animate().also {
            it.setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) { visibility = VISIBLE }
                override fun onAnimationEnd(animation: Animator?) { listener?.onShowEnd() }
            })
            it.duration = SHOW_ANIM_DURATION
            it.interpolator = AccelerateInterpolator()
            it.alpha(1f)
            it.start()
        }
    }

    private fun setup(binding: StubImageBinding, context: Context, attrs: AttributeSet?) {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.StubImageView)
        attributes.run {
            val backgroundColorResId = getColor(R.styleable.StubImageView_si_background_color, 0)
            val drawable = getDrawable(R.styleable.StubImageView_si_image_src)
            val text = getString(R.styleable.StubImageView_si_text)
            recycle()
            this@StubImageView.setBackgroundColor(backgroundColorResId)
            binding.stubImage.setImageDrawable(drawable)
            text?.let {
                binding.stubText.text = it
                binding.stubText.visibility = VISIBLE
            }
        }
    }

    interface OnShowEndListener {
        fun onShowEnd()
    }

}