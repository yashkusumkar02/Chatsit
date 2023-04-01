package io.stipop.extend

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import io.stipop.Config

class StipopTextView : AppCompatTextView {
    constructor(context: Context) : super(context) {
        setFont()
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setFont()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setFont()
    }

    private fun setFont() {
        if (Config.fontFace != null) {
            this.typeface = Config.fontFace
        }

        // letterSpacing
        this.letterSpacing = Config.fontCharacter
    }
}
