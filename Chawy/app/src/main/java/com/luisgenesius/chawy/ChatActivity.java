package com.luisgenesius.chawy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.SimpleDraweeView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.luisgenesius.chawy.Chat.Chat;
import com.luisgenesius.chawy.Chat.ChatListAdapter;
import com.luisgenesius.chawy.Chat.MediaAdapter;
import com.luisgenesius.chawy.Chat.Message;
import com.luisgenesius.chawy.Chat.MessageAdapter;
import com.luisgenesius.chawy.User.User;
import com.luisgenesius.chawy.Utils.SendNotification;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private static final int REQUEST_CALL = 1;
    private RecyclerView chatRecyclerView, mediaRecyclerView;
    private RecyclerView.Adapter adapter, mediaAdapter;
    private RecyclerView.LayoutManager layoutManager, mediaLayoutManager;

    private ArrayList<Message> messageList;

    private Button sendMessageButton, addMediaButton;
    private Chat chat;

    private DatabaseReference chatDB, lastMessageDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Fresco.initialize(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        layoutManager = new LinearLayoutManager(this);
        chat = (Chat) getIntent().getSerializableExtra("chatObject");
        lastMessageDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chat.getChatId());
        chatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chat.getChatId()).child("message");
        sendMessageButton = findViewById(R.id.sendMessageButton);

        addMediaButton = findViewById(R.id.addMediaButton);
        addMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        getChatMessages();
        initializeMessageRecyclerView();
        initializeMediaRecyclerView();
        changeTheTitle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(chat.getUserArrayList().size() <= 2) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.personal_chat_menu, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.voiceCallUserButton:
                voiceCallUser();
        }
        return super.onOptionsItemSelected(item);
    }

    private void voiceCallUser() {
        if(chat.getUserArrayList().size() <= 2) {
            String phoneNumber = "";
            for(User user : chat.getUserArrayList()) {
                if(!user.getuId().equals(FirebaseAuth.getInstance().getUid())) {
                    phoneNumber = user.getPhone();
                    break;
                }
            }

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:"+phoneNumber));
                startActivity(intent);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CALL) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                voiceCallUser();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private int PICK_IMAGE_INTENT = 1;
    private ArrayList<String> mediaUriList = new ArrayList<>();
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture(s)"), PICK_IMAGE_INTENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == PICK_IMAGE_INTENT) {
                if(data.getClipData() == null) {
                    mediaUriList.add(data.getData().toString());
                } else {
                    for(int i = 0; i < data.getClipData().getItemCount(); i++) {
                        mediaUriList.add(data.getClipData().getItemAt(i).getUri().toString());
                    }
                }

                mediaAdapter.notifyDataSetChanged();
            }
        }
    }

    private void changeTheTitle() {
        if(chat.getUserArrayList().size() <= 2) {
            User targetUser = null;
            for(User user : chat.getUserArrayList()) {
                if(!user.getuId().equals(FirebaseAuth.getInstance().getUid())) { targetUser = user; }
            }

            if(targetUser != null) ChatActivity.this.setTitle(targetUser.getName().toString());
            else ChatActivity.this.setTitle("Anonymous");
        } else {
            boolean check = false;
            String userName = "";
            User targetUser = null;
            for(User user : chat.getUserArrayList()) {
                if(!user.getuId().equals(FirebaseAuth.getInstance().getUid())) {
                    targetUser = user;

                    if(check == false) { userName = targetUser.getName(); check = true; }
                    else { userName = userName + ", " + targetUser.getName(); }
                }
            }
            ChatActivity.this.setTitle(userName);
        }
    }

    private void getChatMessages() {
        chatDB.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if(snapshot.exists()) {
                    String text = "", creatorId = "";

                    ArrayList<String> mediaUrlList = new ArrayList<>();
                    if(snapshot.child("text").getValue() != null) {
                        text = snapshot.child("text").getValue().toString();
                    }

                    if(snapshot.child("creator").getValue() != null) {
                        creatorId = snapshot.child("creator").getValue().toString();
                    }

                    if(snapshot.child("media").getChildrenCount() > 0) {
                        for(DataSnapshot mediaSnapshot : snapshot.child("media").getChildren()) {
                            mediaUrlList.add(mediaSnapshot.getValue().toString());
                        }
                    }

                    Message message = new Message(snapshot.getKey(), creatorId, text, mediaUrlList);
                    messageList.add(message);
                    layoutManager.scrollToPosition(messageList.size()-1);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private int totalMediaUploaded = 0;
    private ArrayList<String> mediaIdList = new ArrayList<>();
    private EditText messageEditText;
    private void sendMessage() {
       messageEditText = findViewById(R.id.messageEditText);
       
       if(messageEditText.getText().toString().isEmpty() && mediaUriList.isEmpty()) {
           Toast.makeText(this, "Nothing to be send", Toast.LENGTH_SHORT).show();
           return;
       }

       String messageId = chatDB.push().getKey();
       DatabaseReference newMessageDB = chatDB.child(messageId);

       Map newMessageMap = new HashMap<>();
       newMessageMap.put("creator", FirebaseAuth.getInstance().getUid());

       if(!messageEditText.getText().toString().isEmpty()) {
           newMessageMap.put("text", messageEditText.getText().toString());
       }

       if(!mediaUriList.isEmpty()) {
           for(String mediaUri : mediaUriList) {
               String mediaId = newMessageDB.child("media").push().getKey();
               mediaIdList.add(mediaId);
               final StorageReference filePath = FirebaseStorage.getInstance().getReference().child("chat").child(chat.getChatId()).child("message").child(messageId).child(mediaId);

               UploadTask uploadTask = filePath.putFile(Uri.parse(mediaUri));

               uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                   @Override
                   public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                       filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                           @Override
                           public void onSuccess(Uri uri) {
                                newMessageMap.put("/media/" + mediaIdList.get(totalMediaUploaded) + "/", uri.toString());

                                totalMediaUploaded++;
                                if(totalMediaUploaded == mediaUriList.size()) {
                                    updateDatabaseNewMessage(newMessageDB, newMessageMap);
                                }
                           }
                       });
                   }
               });
           }
       } else {
           if(!messageEditText.getText().toString().isEmpty()) {
               updateDatabaseNewMessage(newMessageDB, newMessageMap);
           }
       }
    }

    private void updateDatabaseNewMessage(DatabaseReference newMessageDB, Map newMessageMap) {
        newMessageDB.updateChildren(newMessageMap);
        messageEditText.setText(null);
        mediaUriList.clear();
        mediaIdList.clear();
        mediaAdapter.notifyDataSetChanged();
        addLastMessageToDB(newMessageMap);
        sendNotification(newMessageMap);
    }

    private void addLastMessageToDB(Map newMessageMap) {
        if(newMessageMap.get("text") != null) lastMessageDB.child("lastMessage").setValue(newMessageMap.get("text").toString());
        else lastMessageDB.child("lastMessage").setValue("Sent Media");
    }

    private void sendNotification(Map newMessageMap) {
        String message;
        if(newMessageMap.get("text") != null) message = newMessageMap.get("text").toString();
        else message = "Sent Media";

        User targetUser = null;
        for(User user : chat.getUserArrayList()) {
            if(!user.getuId().equals(FirebaseAuth.getInstance().getUid())) {
                targetUser = user;
                new SendNotification(message, "New Message", targetUser.getNotificationKey());
            }
        }
    }

    private void initializeMessageRecyclerView() {
        messageList = new ArrayList<>();
        chatRecyclerView = findViewById(R.id.messageRecyclerView);
        chatRecyclerView.setNestedScrollingEnabled(false);
        chatRecyclerView.setHasFixedSize(false);

        chatRecyclerView.setLayoutManager(layoutManager);
        adapter = new MessageAdapter(messageList, chat);
        chatRecyclerView.setAdapter(adapter);
    }

    private void initializeMediaRecyclerView() {
        mediaUriList = new ArrayList<>();
        mediaRecyclerView = findViewById(R.id.mediaRecyclerView);
        mediaRecyclerView.setNestedScrollingEnabled(false);
        mediaRecyclerView.setHasFixedSize(false);
        mediaLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.HORIZONTAL, false);
        mediaRecyclerView.setLayoutManager(mediaLayoutManager);
        mediaAdapter = new MediaAdapter(getApplicationContext(), mediaUriList);
        mediaRecyclerView.setAdapter(mediaAdapter);
    }
}