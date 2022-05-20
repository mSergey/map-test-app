package com.gmail.zajcevserg.maptestapp.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater

import androidx.constraintlayout.widget.ConstraintLayout

import com.gmail.zajcevserg.maptestapp.databinding.CustomSwitchLayoutBinding
import com.gmail.zajcevserg.maptestapp.ui.activity.log


class Switch3Way : ConstraintLayout {

    private lateinit var binding: CustomSwitchLayoutBinding
    var currentState: SwitchPositions = SwitchPositions.MIDDLE
    var currentMode: SwitchMode = SwitchMode.THREE_WAY

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        binding = CustomSwitchLayoutBinding.inflate(LayoutInflater.from(context), this)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        binding = CustomSwitchLayoutBinding.inflate(LayoutInflater.from(context), this)
    }

    fun setup() {
        binding.thumb.setOnClickListener {
            log("")
        }
    }

    enum class SwitchMode {
        TWO_WAY,
        THREE_WAY,
    }

    enum class SwitchPositions {
        START,
        MIDDLE,
        END
    }

}
