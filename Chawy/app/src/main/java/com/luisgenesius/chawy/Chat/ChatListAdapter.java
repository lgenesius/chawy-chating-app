package com.luisgenesius.chawy.Chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.luisgenesius.chawy.ChatActivity;
import com.luisgenesius.chawy.MainPageActivity;
import com.luisgenesius.chawy.R;
import com.luisgenesius.chawy.User.User;

import java.io.Serializable;
import java.util.ArrayList;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {

    private ArrayList<Chat> chatList;
    public ChatListAdapter(ArrayList<Chat> chatList){
        this.chatList = chatList;
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {
        public TextView title, lastMessage;
        public CardView chatCardView;
        public ChatListViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.titleTextView);
            lastMessage = view.findViewById(R.id.lastMessageTextView);
            chatCardView = view.findViewById(R.id.chatCardView);
        }
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_detail, parent, false);

        ChatListViewHolder chatListViewHolder = new ChatListViewHolder(view);
        return chatListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {

        DatabaseReference parentChatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatList.get(position).getChatId());
        DatabaseReference chatDB = parentChatDB.child("user");

        chatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String recipientUid = "";
                if(snapshot.exists()) {
                    if(snapshot.getChildrenCount() <= 2) {
                        for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                            if(!childSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                                recipientUid = childSnapshot.getKey();
                            }
                        }

                        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user").child(recipientUid);
                        userDB.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                holder.title.setText(snapshot.child("name").getValue().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    } else {
                        holder.title.setText("Chat Room" + " " + "(" + snapshot.getChildrenCount() + ")");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        parentChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("lastMessage").exists()) {
                    holder.lastMessage.setText(snapshot.child("lastMessage").getValue().toString());
                }
                else {
                    holder.lastMessage.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.chatCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                intent.putExtra("chatObject",chatList.get(holder.getAdapterPosition()));
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }
}
