package com.chatsit.chat.groupVideoCall.openvcall.ui.layout;


import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

public class SettingsButtonDecoration extends RecyclerView.ItemDecoration {

    private static final int divider = 12;
    private static final int header = 4;
    private static final int footer = 4;

    @Override
    public void getItemOffsets(Rect outRect, @NonNull View view, RecyclerView parent, @NonNull RecyclerView.State state) {
        int itemCount = Objects.requireNonNull(parent.getAdapter()).getItemCount();
        int viewPosition = parent.getChildAdapterPosition(view);

        outRect.left = divider;
        outRect.right = divider;

        if (viewPosition == 0) {
            outRect.top = header;
            outRect.bottom = divider / 2;
        } else if (viewPosition == itemCount - 1) {
            outRect.top = divider / 2;
            outRect.bottom = footer;
        } else {
            outRect.top = divider / 2;
            outRect.bottom = divider / 2;
        }
    }
}

