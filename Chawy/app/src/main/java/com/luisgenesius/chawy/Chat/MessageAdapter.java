package com.luisgenesius.chawy.Chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.luisgenesius.chawy.R;
import com.luisgenesius.chawy.User.User;
import com.stfalcon.frescoimageviewer.ImageViewer;


import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private ArrayList<Message> messageList;
    private Chat chat;
    public MessageAdapter(ArrayList<Message> messageList, Chat chat){
        this.messageList = messageList;
        this.chat = chat;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {
        private TextView messages, sender;
        private RelativeLayout insideRelLayout;
        private Button viewMediaButton;
        public MessageViewHolder(View view) {
            super(view);

            messages = view.findViewById(R.id.messageTextView);
            sender = view.findViewById(R.id.chatNameTextView);
            insideRelLayout = view.findViewById(R.id.insideRelativeLayout);
            viewMediaButton = view.findViewById(R.id.viewMediaButton);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_detail, parent, false);

        MessageViewHolder messageViewHolder = new MessageViewHolder(view);
        return messageViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        holder.messages.setText(messageList.get(position).getMessage());
        for(User user : chat.getUserArrayList()) {
            if(user.getuId().equals(messageList.get(position).getSenderId())) {
                holder.sender.setText(user.getName());
            }
        }

        if(messageList.get(holder.getAdapterPosition()).getMediaUrlList().isEmpty()) {
            holder.viewMediaButton.setVisibility(View.GONE);
            
        }

        holder.viewMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ImageViewer.Builder(v.getContext(), messageList.get(holder.getAdapterPosition()).getMediaUrlList())
                        .setStartPosition(0)
                        .show();
            }
        });

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        if(messageList.get(position).getSenderId().equals(FirebaseAuth.getInstance().getUid())) {
            holder.insideRelLayout.setLayoutParams(params);
        } else {
            holder.insideRelLayout.setLayoutParams(params2);
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}
