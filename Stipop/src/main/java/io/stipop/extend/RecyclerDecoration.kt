@file:Suppress("UsePropertyAccessSyntax", "UsePropertyAccessSyntax")

package io.stipop.extend

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class RecyclerDecoration(private val divHeight: Int): RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.getChildAdapterPosition(view) != parent.getAdapter()!!.getItemCount() - 1)
            outRect.right = divHeight
    }

}