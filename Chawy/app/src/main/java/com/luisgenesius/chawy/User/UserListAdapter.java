package com.luisgenesius.chawy.User;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.luisgenesius.chawy.Chat.Chat;
import com.luisgenesius.chawy.MainPageActivity;
import com.luisgenesius.chawy.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserListViewHolder> {

    private ArrayList<User> userList;
    private ArrayList<Chat> chatList;

    public UserListAdapter(ArrayList<User> userList, ArrayList<Chat> chatList){
        this.userList = userList;
        this.chatList = chatList;
    }

    public class UserListViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTV, userPhoneTV;
        Button messageButton;
        CheckBox addCheckBox;
        UserListViewHolder(View view) {
            super(view);
            userNameTV = view.findViewById(R.id.userDetailNameTextView);
            userPhoneTV = view.findViewById(R.id.userDetailPhoneTextView);
            messageButton = view.findViewById(R.id.messageButton);
            addCheckBox = view.findViewById(R.id.addCheckBox);
        }
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_detail, parent, false);

        UserListViewHolder userListViewHolder = new UserListViewHolder(view);
        return userListViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, int position) {
        holder.userNameTV.setText(userList.get(position).getName());
        holder.userPhoneTV.setText(userList.get(position).getPhone());

        holder.addCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                userList.get(holder.getAdapterPosition()).setSelected(isChecked);
            }
        });

        holder.messageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateCreatedChat(position) == false) {
                    Toast.makeText(v.getContext(), "Already created chat", Toast.LENGTH_SHORT).show();
                    goToChatActivity(v);
                    return;
                }
                addChatToDatabase(position, v);
            }
        });
    }

    private boolean validateCreatedChat(int position) {
        boolean checkOwnAccount = true, checkTargetAccount = true;
        for(Chat chat : chatList) {
            if(chat.getUserArrayList().size() > 2) { return true; }
            for(User user : chat.getUserArrayList()) {
                if(user.getuId().equals(FirebaseAuth.getInstance().getUid())) checkOwnAccount = false;
                else if(userList.get(position).getuId().equals(user.getuId())) checkTargetAccount = false;
            }

            if(checkOwnAccount == false && checkTargetAccount == false) { return false; }
            else {
                checkOwnAccount = true;
                checkTargetAccount = true;
            }
        }

        return true;
    }

    private void addChatToDatabase(int position, View v) {
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();

        FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
        FirebaseDatabase.getInstance().getReference().child("user").child(userList.get(position).getuId()).child("chat").child(key).setValue(true);

        String ownUID = FirebaseAuth.getInstance().getUid();
        String recipientUID = userList.get(position).getuId();

        DatabaseReference chatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("user");
        chatDB.child(ownUID).setValue(true);
        chatDB.child(recipientUID).setValue(true);
        goToChatActivity(v);
    }

    private void goToChatActivity(View v) {
        Intent intent = new Intent(v.getContext(), MainPageActivity.class);
        v.getContext().startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}
