package com.chatsit.chat.groupVideoCall.openvcall.ui.layout;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chatsit.chat.R;
import com.chatsit.chat.groupVideoCall.openvcall.model.ConstantApp;


@SuppressWarnings("ALL")
public class VideoEncResolutionAdapter extends RecyclerView.Adapter {

    private final Context mContext;

    private int mSelectedIdx;

    public VideoEncResolutionAdapter(Context context, int selected) {
        this.mContext = context;

        this.mSelectedIdx = selected;
    }

    @NonNull
    @SuppressWarnings("UnnecessaryLocalVariable")
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_enc_resolution_item, parent, false);

        VideoEncResolutionViewHolder ph = new VideoEncResolutionViewHolder(v);
        return ph;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String resolution = mContext.getResources().getStringArray(R.array.string_array_resolutions)[position];
        ((VideoEncResolutionViewHolder) holder).mTextResolution.setText(resolution);

        holder.itemView.setBackgroundResource(position == mSelectedIdx ? R.drawable.chatbox : R.drawable.chatbox);


        ((VideoEncResolutionViewHolder) holder).mTextResolution.setTextColor(mContext.getResources().getColor(position == mSelectedIdx ? android.R.color.white : R.color.colorAccent));
    }

    @Override
    public int getItemCount() {
        return mContext.getResources().getStringArray(R.array.string_array_resolutions).length;
    }

    public class VideoEncResolutionViewHolder extends RecyclerView.ViewHolder {
        public final TextView mTextResolution;

        public VideoEncResolutionViewHolder(View itemView) {
            super(itemView);

            mTextResolution = (TextView) itemView.findViewById(R.id.video_enc_resolution);

            itemView.setOnClickListener(v -> {
                mSelectedIdx = getLayoutPosition();
                notifyDataSetChanged();


                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);

                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(ConstantApp.PrefManager.PREF_PROPERTY_VIDEO_ENC_RESOLUTION, mSelectedIdx);
                editor.apply();
            });
        }
    }
}

