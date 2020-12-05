
package com.luisgenesius.chawy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.luisgenesius.chawy.Chat.Chat;
import com.luisgenesius.chawy.User.User;
import com.luisgenesius.chawy.User.UserListAdapter;
import com.luisgenesius.chawy.Utils.CountryToPhonePrefix;

import java.util.ArrayList;
import java.util.HashMap;

public class FindUserActivity extends AppCompatActivity {
    private RecyclerView userRecyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<User> userList, contactList;
    private ArrayList<Chat> chatList;
    private DatabaseReference userDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_user);
        userList = new ArrayList<>();
        contactList = new ArrayList<>();
        chatList = (ArrayList<Chat>) getIntent().getSerializableExtra("chatList");
        userDB = FirebaseDatabase.getInstance().getReference().child("user");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button createRoomButton = findViewById(R.id.createRoomButton);
        createRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createChat();
            }
        });
        initializeRecyclerView();
        getContactList();
    }

    private void createChat() {
        int countSelected = 0;
        for(User user : userList) {
            if(user.getSelected()) {
                countSelected++;
            }
        }

        if(countSelected == 1) {
            Toast.makeText(this, "Cannot make Chat Room with only two people", Toast.LENGTH_SHORT).show();
            return;
        } else if(countSelected == 0) {
            Toast.makeText(this, "Please select friends first", Toast.LENGTH_SHORT).show();
            return;
        }
        String key = FirebaseDatabase.getInstance().getReference().child("chat").push().getKey();
        DatabaseReference chatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(key).child("user");
        chatDB.child(FirebaseAuth.getInstance().getUid()).setValue(true);
        userDB.child(FirebaseAuth.getInstance().getUid()).child("chat").child(key).setValue(true);
        for(User user : userList) {
            if(user.getSelected()) {
                chatDB.child(user.getuId()).setValue(true);
                userDB.child(user.getuId()).child("chat").child(key).setValue(true);
            }
        }
    }

    private void getContactList() {
        String ISOPrefix = getCountryISO();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phone = phoneValidation(phone, ISOPrefix);
            User contact = new User("", name, phone);
            contactList.add(contact);
            getUserDetails(contact);
        }
    }

    private String phoneValidation(String phone, String ISOPrefix) {
        phone = phone.replace(" ", "");
        phone = phone.replace("-", "");
        phone = phone.replace("(", "");
        phone = phone.replace(")", "");

        if(!String.valueOf(phone.charAt(0)).equals("+")) {
            phone = ISOPrefix + phone;
        }
        return phone;
    }

    private void getUserDetails(User contact) {
        Query query = userDB.orderByChild("phone").equalTo(contact.getPhone());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    String phone = "", name = "", notificationKey = "";
                    for(DataSnapshot childSnapshot : snapshot.getChildren()) {
                        if(childSnapshot.child("phone").getValue() != null) { phone = childSnapshot.child("phone").getValue().toString(); }
                        if(childSnapshot.child("name").getValue() != null) { name = childSnapshot.child("name").getValue().toString(); }
                        if(childSnapshot.child("notificationKey").getValue() != null) { notificationKey = childSnapshot.child("notificationKey").getValue().toString(); }

                        User user = new User(childSnapshot.getKey(), name, phone);
                        user.setNotificationKey(notificationKey);
                        if(name.equals(phone)) {
                            for(User contactIter: contactList) {
                                if(contactIter.getPhone().equals(user.getPhone())) user.setName(contactIter.getName());
                            }
                        }
                        userList.add(user);
                        adapter.notifyDataSetChanged();
                        return;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FindUserActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCountryISO() {
        String iso = null;

        TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(getApplicationContext().TELEPHONY_SERVICE);

        if(telephonyManager.getNetworkCountryIso() != null) {
            if(!telephonyManager.getNetworkCountryIso().toString().equals(""))
                iso = telephonyManager.getNetworkCountryIso().toString();
        }

        return CountryToPhonePrefix.getPhone(iso);
    }

    private void initializeRecyclerView() {
        userRecyclerView = findViewById(R.id.userRecyclerView);
        userRecyclerView.setNestedScrollingEnabled(false);
        userRecyclerView.setHasFixedSize(false);

        layoutManager = new LinearLayoutManager(this);
        userRecyclerView.setLayoutManager(layoutManager);
        adapter = new UserListAdapter(userList, chatList);
        userRecyclerView.setAdapter(adapter);
    }
}