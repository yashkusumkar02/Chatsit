package com.chatsit.chat.groupVideoCall.openvcall.ui.layout;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.chatsit.chat.R;
import com.chatsit.chat.groupVideoCall.openvcall.model.Message;

import java.util.ArrayList;

@SuppressWarnings("ALL")
public class InChannelMessageListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final ArrayList<Message> mMsgList;

    private final LayoutInflater mInflater;

    public InChannelMessageListAdapter(Activity activity, ArrayList<Message> list) {
        mInflater = activity.getLayoutInflater();
        mMsgList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.in_channel_message, parent, false);
        return new MessageHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message msg = mMsgList.get(position);

        MessageHolder myHolder = (MessageHolder) holder;
        String sender = msg.getSender().name;
        if (TextUtils.isEmpty(sender)) {
            myHolder.itemView.setBackgroundResource(R.drawable.chatbox);
        } else {
            myHolder.itemView.setBackgroundResource(R.drawable.chatbox);
        }
        myHolder.mMsgContent.setText(msg.getContent());
    }

    @Override
    public int getItemCount() {
        return mMsgList.size();
    }

    @Override
    public long getItemId(int position) {
        return mMsgList.get(position).hashCode();
    }

    public class MessageHolder extends RecyclerView.ViewHolder {
        final TextView mMsgContent;

        MessageHolder(View v) {
            super(v);
            mMsgContent = (TextView) v.findViewById(R.id.msg_content);
        }
    }
}
