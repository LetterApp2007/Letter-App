package com.ariyo.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
FirebaseAuth mAuth;
List<String> userContacts;
List<UserAccount> displayList;
List<String> contactList;
String phone, uid;
RecyclerView recyclerView;


    private FirebaseDatabase fireDb; // declare for Firebase database
    private DatabaseReference dataRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth=FirebaseAuth.getInstance();
        userContacts=new ArrayList<>();
        contactList=new ArrayList<>();
        displayList=new ArrayList<>();
        fireDb = FirebaseDatabase.getInstance();
        dataRef = fireDb.getReference("Users");
        recyclerView=findViewById(R.id.recycler_view);
        uid=mAuth.getCurrentUser().getUid();
        phone=mAuth.getCurrentUser().getPhoneNumber();




        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(MainActivity.this, RecyclerView.VERTICAL, false);

        recyclerView.setLayoutManager(linearLayoutManager);
        syncUserContacts();


    }




    private void syncUserContacts() {
        ProgressDialog pd =new ProgressDialog(MainActivity.this);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1);
        } else {
            getContact();
        }

        dataRef.getRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot mSnap:snapshot.getChildren()) {
                    Map<String, Object> user= (Map<String, Object>) mSnap.getValue();
                    for (String contact: userContacts) {
                        if (contact.equals(user.get(ConstantKeys.KEY_PHONE).toString())){
                            if (!contact.equals(phone)) {
                                contactList.add(contact);
                                try {
                                    UserAccount account = new UserAccount(
                                            user.get(ConstantKeys.KEY_NAME).toString(),
                                            user.get(ConstantKeys.KEY_AGE).toString(),
                                            user.get(ConstantKeys.KEY_PHONE).toString(),
                                            user.get(ConstantKeys.KEY_IMAGE).toString(),
                                            (List) user.get(ConstantKeys.KEY_CONTACT_LIST),
                                            user.get(ConstantKeys.KEY_UID).toString());

                                    displayList.add(account);


                                }catch (Exception e){
                                    UserAccount account = new UserAccount(
                                            user.get(ConstantKeys.KEY_NAME).toString(),
                                            user.get(ConstantKeys.KEY_AGE).toString(),
                                            user.get(ConstantKeys.KEY_PHONE).toString(),
                                            null,
                                            (List) user.get(ConstantKeys.KEY_CONTACT_LIST),
                                            user.get(ConstantKeys.KEY_UID).toString());
                                    displayList.add(account);
                                }

                            }
                        }

                    }

                }
                RVAdapter adapter=new RVAdapter(MainActivity.this, displayList, R.layout.recycler_item);
                recyclerView.setAdapter(adapter);
                Map<String, Object> userJson=new HashMap<>();
                userJson.put(ConstantKeys.KEY_CONTACT_LIST, contactList);
                dataRef.child(mAuth.getCurrentUser().getUid()).updateChildren(userJson).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
//                            Toast.makeText(MainActivity.this, "Done Syncing Contacts", Toast.LENGTH_LONG).show();
                            pd.dismiss();
                        } else {

                            Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode==1){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getContact();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getContact() {

        Cursor cursor=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (cursor.moveToNext()){
            String mobile=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            userContacts.add(mobile.trim());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_logout:
                mAuth.signOut();
                MainActivity.this.finish();
                break;
            case R.id.menu_settings:

                Intent intent=new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}