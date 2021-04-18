package com.ariyo.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {
    CircleImageView civ;
    TextInputEditText inpName, inpAge;
    Button btnUpdate;
    FirebaseAuth auth;
    FloatingActionButton editProfile;
    FirebaseDatabase database;
    Uri fileUri;
    String imgUrl;
    List contactList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle("Profile");
        auth=FirebaseAuth.getInstance();
        database=FirebaseDatabase.getInstance();
        editProfile=findViewById(R.id.edit_profile_image);

        civ=findViewById(R.id.profile_image);
        inpAge=findViewById(R.id.inp_age);
        inpName=findViewById(R.id.inp_name);
        btnUpdate=findViewById(R.id.btn_update);

        getAllInfo();
        editProfile.setOnClickListener(v -> {
            BottomSheetDialog sheet=new BottomSheetDialog(ProfileActivity.this);
            View sheetView= LayoutInflater.from(getApplicationContext()).inflate(R.layout.bottom_sheet, null);
            sheet.setContentView(sheetView);
            sheet.show();
            sheet.setCancelable(true);

            ImageButton cameraButton, galleryButton;
            cameraButton=sheetView.findViewById(R.id.btn_cam);
            galleryButton=sheetView.findViewById(R.id.btn_gallery);
            cameraButton.setOnClickListener(v1 -> {
                // code for camera and take a picture
                Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 200);
                sheet.dismiss();
            });
            galleryButton.setOnClickListener(v1 -> {
                Intent lib=new Intent(Intent.ACTION_GET_CONTENT);
                lib.setType("images/*");
                Intent pickFile = new Intent(Intent.createChooser(lib, "Select"));
                startActivityForResult(pickFile, 201);
                sheet.dismiss();

            });
        });
        btnUpdate.setOnClickListener(v -> {
            if (fileUri!=null){
                getImageUploaded();

            }else {
                updateProfile();
            }
        });

    }




    private void getImageUploaded() {
        final ProgressDialog pd = new ProgressDialog(ProfileActivity.this);
        pd.setTitle("Uploading...");
        pd.setCancelable(false);
        pd.show();
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        storageReference.child(auth.getCurrentUser().getUid()+".jpg")
                .putFile(fileUri)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        UploadTask.TaskSnapshot uploadTask = task.getResult();
                        if (uploadTask!=null){
                            uploadTask.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                                if(uri!=null){
                                    imgUrl=uri.toString();
                                    updateProfile();
                                    pd.dismiss();
                                }
                            });

                        }
//

                    }
                }).addOnFailureListener(e -> Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnProgressListener(snapshot -> {
                    double progress
                            = (100.0
                            * snapshot.getBytesTransferred()
                            / snapshot.getTotalByteCount());
                    pd.setMessage(
                            "Uploaded "
                                    + (int) progress + "%");


                });

    }
    
    private void updateProfile() {

        Map<String , Object> userJson=new HashMap<>();
        String name=inpName.getText().toString();
        String age=inpAge.getText().toString();
        userJson.put(ConstantKeys.KEY_NAME, name);
        userJson.put(ConstantKeys.KEY_AGE, age);
        userJson.put(ConstantKeys.KEY_CONTACT_LIST, contactList);
        if (imgUrl!=null) {
            userJson.put(ConstantKeys.KEY_IMAGE, imgUrl);
        }

        database.getReference("Users").getRef().child(auth.getCurrentUser().getUid()).updateChildren(userJson, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error==null){
                    Toast.makeText(ProfileActivity.this, "Updated", Toast.LENGTH_SHORT).show();

                }else {
                    Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }



    private void getAllInfo() {
        database.getReference("Users").getRef().child(auth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Object> user=(Map<String, Object>)snapshot.getValue();
                try {
                    UserAccount account = new UserAccount(user.get(ConstantKeys.KEY_NAME).toString(),
                            user.get(ConstantKeys.KEY_AGE).toString(),
                            user.get(ConstantKeys.KEY_PHONE).toString(),
                            user.get(ConstantKeys.KEY_IMAGE).toString(),
                            (List) user.get(ConstantKeys.KEY_CONTACT_LIST),
                            user.get(ConstantKeys.KEY_UID).toString());
                    inpName.setText(account.getName());
                    inpAge.setText(account.getAge());
                    contactList=account.getContactList();
                    getSupportActionBar().setTitle(account.getName());
                    if (account.getImage()!=null) {
                        Picasso.get().load(account.getImage()).placeholder(R.drawable.account).into(civ);
                    }
                }catch (Exception e){
                    UserAccount account = new UserAccount(user.get(ConstantKeys.KEY_NAME).toString(),
                            user.get(ConstantKeys.KEY_AGE).toString(),
                            user.get(ConstantKeys.KEY_PHONE).toString(),
                            null,
                            (List) user.get(ConstantKeys.KEY_CONTACT_LIST),
                            user.get(ConstantKeys.KEY_UID).toString());
                    inpName.setText(account.getName());
                    inpAge.setText(account.getAge());
                    contactList=account.getContactList();

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // after get the result
        if (requestCode == 200 && resultCode == Activity.RESULT_OK ) {
            // need to display the picture
//            fileUri = data.getData();
            // need to display the picture
            Bitmap bitmap= (Bitmap) data.getExtras().get("data");
            civ.setImageBitmap(bitmap);
            //gallery
            fileUri=Uri.parse(MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "IMG_"+new Date()+".jpg", "-N-A-"));
            store(bitmap);

        } else if (requestCode == 201 && data.getData() != null) {
            fileUri = data.getData();
            Picasso.get().load(fileUri).into(civ);
        }


    }

    private void store(Bitmap bitmap) {

        String parentPath = getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath();
        File folder = new File(parentPath+"/"+getString(R.string.app_name));
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File imgFile = new File(folder,"IMG_"+new Date()+".jpg");
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG,80,fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}