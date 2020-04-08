package com.fabernovel.statefullayout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import com.fabernovel.statefullayout.transitions.DefaultStateTransitionListener
import com.fabernovel.statefullayout.transitions.StateTransition
import com.fabernovel.statefullayout.transitions.StateTransitions

class State : FrameLayout {
    private var currentTransition: StateTransition? = null
    var contentView: View? = null
        private set

    var enterTransition: StateTransition? = null
    var exitTransition: StateTransition? = null

    constructor(context: Context) : this(context, null)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, R.style.Widget_Stateful_State)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int
    ) : this(context, attrs, defStyle, R.attr.stateStyle)

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyle: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyle, defStyleRes) {
        val array = context.obtainStyledAttributes(
            attrs,
            R.styleable.State,
            defStyle,
            defStyleRes
        )
        try {
            val enterAnimRes = array.getResourceId(R.styleable.State_enterTransition, 0)

            if (enterAnimRes != 0) {
                enterTransition = StateTransitions.fromResource(context, enterAnimRes)
            }
            val exitAnimRes = array.getResourceId(R.styleable.State_exitTransition, 0)
            if (exitAnimRes != 0) {
                exitTransition = StateTransitions.fromResource(context, exitAnimRes)
            }

            @LayoutRes
            val contentLayout = array.getResourceId(R.styleable.State_contentLayout, 0)
            if (contentLayout != 0) {
                setContentView(contentLayout)
            }
        } finally {
            array.recycle()
        }
    }

    fun setContentView(@LayoutRes layoutRes: Int) {
        val layoutInflater = LayoutInflater.from(context)
        setContentView(layoutInflater.inflate(layoutRes, this, false))
    }

    fun setContentView(view: View) {
        removeView(contentView)
        addView(view)
    }

    override fun onViewAdded(child: View?) {
        super.onViewAdded(child)
        if (childCount > 1) {
            throw IllegalArgumentException("State can only contain one child. (found: $childCount)")
        }
        if (child != null) {
            contentView = child
        }
    }

    internal fun show(fallbackTransition: StateTransition? = null) {
        currentTransition?.cancel()
        val transition = enterTransition ?: fallbackTransition
        if (transition == null) {
            visibility = View.VISIBLE
        } else {
            currentTransition = transition
            transition.start(this, object : DefaultStateTransitionListener {
                override fun onTransitionStart(transition: StateTransition) {
                    visibility = View.VISIBLE
                }
            })
        }
    }

    internal fun hide(fallbackTransition: StateTransition? = null) {
        currentTransition?.cancel()
        val transition = exitTransition ?: fallbackTransition
        if (transition == null) {
            visibility = View.GONE
        } else {
            currentTransition = transition
            transition.start(this, object : DefaultStateTransitionListener {
                override fun onTransitionEnd(transition: StateTransition) {
                    visibility = View.GONE
                }
            })
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount > 1) {
            throw IllegalArgumentException("State can only contain one child. (found: $childCount)")
        }
    }
}