package levkaantonov.com.study.voiceview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.LinearInterpolator
import levkaantonov.com.study.voiceview.LinearInterpolation.interpolateArray
import levkaantonov.com.study.voiceview.WaveRepository.Companion.MAX_VOLUME

class VoiceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val wavePath = Path()
    private val linePaint = Paint()
    private var itemWidth: Int = resources.getDimension(R.dimen.voice_view_item_width).toInt()
    private var originalData: Array<Int>? = null
    private var measuredData: Array<Int>? = null

    init {
        val displayMetrics = context.resources.displayMetrics
        var itemWidthFromAttr = (TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, DEFAULT_ITEM_WIDTH_DP.toFloat(), displayMetrics
        ) + 0.5f).toInt()
        var itemColorFromAttr = DEFAULT_ITEM_COLOR

        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.VoiceView)
            itemWidthFromAttr =
                typedArray.getDimensionPixelSize(R.styleable.VoiceView_itemWidth, itemWidthFromAttr)
            itemColorFromAttr =
                typedArray.getColor(R.styleable.VoiceView_itemColor, itemColorFromAttr)
            typedArray.recycle()
        }

        itemWidth = itemWidthFromAttr
        originalData = WaveRepository.getWaveData()
        linePaint.style = Paint.Style.STROKE
        linePaint.color = itemColorFromAttr
        linePaint.strokeWidth = itemWidthFromAttr.toFloat()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        val itemCount = (width - paddingStart - paddingEnd + itemWidth) / (itemWidth shl 1)
        measuredData = interpolateArray(requireNotNull(originalData), itemCount).toTypedArray()
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        val measuredData = measuredData ?: return

        wavePath.reset()
        val measuredHeight = measuredHeight - paddingTop - paddingBottom
        var currentX = paddingStart
        for (i in measuredData) {
            val height = i.toFloat() / MAX_VOLUME * measuredHeight
            val startY = measuredHeight / 2f - height / 2f + paddingTop
            val endY = startY + height
            wavePath.moveTo(currentX.toFloat(), startY)
            wavePath.lineTo(currentX.toFloat(), endY)
            currentX += itemWidth shl 1
        }
        canvas.drawPath(wavePath, linePaint)
    }

    companion object {
        private const val DEFAULT_ITEM_WIDTH_DP = 2
        private const val DEFAULT_ITEM_COLOR = Color.BLACK
    }
}

@SuppressLint("NewApi")
class TwoStepsInterpolator : LinearInterpolator() {
    override fun getInterpolation(input: Float): Float {
        return when {
            input < 0.3f -> 0.5f * (input / 0.3f)
            input > 0.7f -> 0.5f + (0.5f * (input - 0.7f) / 0.3f)
            else -> 0.5f
        }
    }
}

object LinearInterpolation {
    @JvmStatic
    fun interpolateArray(source: Array<Int>, destinationLength: Int): IntArray {
        val destination = IntArray(destinationLength)
        destination[0] = source[0]
        var jPrevious = 0
        for (i in 1 until source.size) {
            val j = i * (destination.size - 1) / (source.size - 1)
            interpolate(destination, jPrevious, j, source[i - 1].toDouble(), source[i].toDouble())
            jPrevious = j
        }
        return destination
    }

    private fun interpolate(
        destination: IntArray,
        destFrom: Int,
        destTo: Int,
        valueFrom: Double,
        valueTo: Double
    ) {
        val destLength = destTo - destFrom
        val valueLength = valueTo - valueFrom
        for (i in 0..destLength) {
            destination[destFrom + i] = (valueFrom + valueLength * i / destLength).toInt()
        }
    }
}