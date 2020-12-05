package com.luisgenesius.chawy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.luisgenesius.chawy.Chat.Chat;
import com.luisgenesius.chawy.Chat.ChatListAdapter;
import com.luisgenesius.chawy.User.User;
import com.luisgenesius.chawy.Utils.SendNotification;
import com.onesignal.OneSignal;

import java.util.ArrayList;

public class MainPageActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private ArrayList<Chat> chatList;
    private Chat chat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        oneSignalInitialization();
        getPermissions();
        initializeRecyclerView();
        getUserChatList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutButton:
                logoutSystem();
                return true;

            case R.id.findUserbutton:
                goToFindUserActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void logoutSystem() {
        OneSignal.setSubscription(false);
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void goToFindUserActivity() {
        Intent intent = new Intent(getApplicationContext(), FindUserActivity.class);
        intent.putExtra("chatList",chatList);
        startActivity(intent);
    }

    private void oneSignalInitialization() {
        OneSignal.startInit(this).init();
        OneSignal.setSubscription(true);
        OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
            @Override
            public void idsAvailable(String userId, String registrationId) {
                FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("notificationKey").setValue(userId);
            }
        });
        OneSignal.setInFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification);
    }

    private void getUserChatList() {
        DatabaseReference userChatDB = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getUid()).child("chat");

        userChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for(DataSnapshot childSnapshot:snapshot.getChildren()) {
                        chat = new Chat(childSnapshot.getKey());
                        boolean exists = false;
                        for(Chat chatIter:chatList) {
                            if(chatIter.getChatId().equals(chat.getChatId())) {
                                exists = true;
                            }
                        }
                        if(exists) {
                            continue;
                        }
                        addUserToChatList(chat);
                        chatList.add(chat);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void addUserToChatList(Chat chat) {
        DatabaseReference chatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chat.getChatId()).child("user");
        chatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userId = "";
                if(snapshot.exists()) {
                    for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                        userId = childSnapshot.getKey();

                        DatabaseReference userDB = FirebaseDatabase.getInstance().getReference().child("user").child(userId);
                        userDB.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    String name = "", phone = "", notificationKey = "";
                                    name = snapshot.child("name").getValue().toString();
                                    phone = snapshot.child("phone").getValue().toString();
                                    notificationKey = snapshot.child("notificationKey").getValue().toString();

                                    chat.addUserToUserList(new User(snapshot.getKey(), name, phone, notificationKey));
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void initializeRecyclerView() {
        chatList = new ArrayList<>();
        chatRecyclerView = findViewById(R.id.chatListRecyclerView);
        chatRecyclerView.setNestedScrollingEnabled(false);
        chatRecyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(layoutManager);
        adapter = new ChatListAdapter(chatList);
        chatRecyclerView.setAdapter(adapter);
    }

    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
        }
    }
}