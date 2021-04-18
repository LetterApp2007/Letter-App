package com.ariyo.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

public class ChatDetailActivity extends AppCompatActivity {
FirebaseDatabase database;
FirebaseAuth auth;
RecyclerView chatView;
TextView profileName;
EditText inpMsg;
ImageView profileImage, backArrow;
ImageButton btnSend;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);
        database=FirebaseDatabase.getInstance();
        chatView=findViewById(R.id.chat_view);
        inpMsg=findViewById(R.id.inp_msg);
        btnSend=findViewById(R.id.send);
        auth=FirebaseAuth.getInstance();
        final String senderId=auth.getCurrentUser().getUid();
        String recieveId=getIntent().getStringExtra(ConstantKeys.KEY_UID);
        String userName=getIntent().getStringExtra(ConstantKeys.KEY_NAME);
        String profilePicture=getIntent().getStringExtra(ConstantKeys.KEY_IMAGE);
        profileName=findViewById(R.id.profile_name);
        profileImage=findViewById(R.id.profile_image);
        backArrow=findViewById(R.id.backArrow);
        profileName.setText(userName);
        Picasso.get().load(profilePicture).placeholder(R.drawable.account).into(profileImage);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(ChatDetailActivity.this, MainActivity.class);
                startActivity(intent);
                ChatDetailActivity.this.finish();
            }
        });

        final ArrayList<MessageModel> messageModels=new ArrayList<>();
        final ChatAdapter chatAdapter=new ChatAdapter(messageModels, ChatDetailActivity.this);
        final String senderRoom=senderId+recieveId;
        final String receiverRoom=recieveId+senderId;
        chatView.setAdapter(chatAdapter);
        LinearLayoutManager linearLayout=new LinearLayoutManager(this);
        chatView.setLayoutManager(linearLayout);

        database.getReference().child("Chats").child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                messageModels.clear();
                for (DataSnapshot snapshot1: snapshot.getChildren()){
                    MessageModel model=snapshot1.getValue(MessageModel.class);
                    messageModels.add(model);

                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatDetailActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();

            }
        });

        btnSend.setOnClickListener(v -> {
            String message=inpMsg.getText().toString();
            final MessageModel model=new MessageModel(senderId, message);
            model.setTimestamp(new Date().getTime());
            inpMsg.setText("");
            database.getReference().child("Chats").child(senderRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    database.getReference().child("Chats").child(receiverRoom).push().setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            // TODO
                        }
                    });
                }
            });

        });

    }
}