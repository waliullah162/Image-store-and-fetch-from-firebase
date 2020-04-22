package com.example.imagestoreandfetchfromfirebase;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button chooseBtn, saveBtn, displayBtn;
    private ImageView imageView;
    private EditText titleEditText;
    private ProgressBar progressBar;
    private Uri imageUri;



    //create database && Storage  references

    DatabaseReference databaseReference;

    StorageReference storageReference;


    StorageTask uploadTask;



    private static final int IMAGE_REQUEST_CODE=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        chooseBtn=findViewById(R.id.chooseButtonId);
        imageView=findViewById(R.id.imageviewId);
        titleEditText=findViewById(R.id.imageTitleEditTextId);
        displayBtn=findViewById(R.id.displayButtonId);
        saveBtn=findViewById(R.id.saveButtonId);

        progressBar=findViewById(R.id.progressBarId);


        //initialize the database && Storage  references


        databaseReference=FirebaseDatabase.getInstance().getReference("Upload");

        storageReference=FirebaseStorage.getInstance().getReference("Upload");



        chooseBtn.setOnClickListener(this);
        saveBtn.setOnClickListener(this);
        displayBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
       switch (view.getId()){
           case R.id.chooseButtonId:
               openFileChooser();
               break;

           case R.id.saveButtonId:

               if(uploadTask!=null && uploadTask.isInProgress()){
                   Toast.makeText(getApplicationContext(),"Uploading is Progressing",Toast.LENGTH_SHORT).show();

               }
               else{
                   saveData();
               }

               break;

           case R.id.displayButtonId:

               break;
       }

    }




    private void openFileChooser() {

        Intent intent=new Intent();

        //mention the type of the intent

        intent.setType("image/*");

        //set the action
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent,IMAGE_REQUEST_CODE);





    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==IMAGE_REQUEST_CODE && resultCode==RESULT_OK && data!=null && data.getData()!=null){

            //Take an Uri for the selected image

            imageUri=data.getData();

            //load image in image view

            Picasso.with(this).load(imageUri).into(imageView);
        }
    }

    private void saveData() {




        //Fetch the image name from image title edit text

        final String imageName=titleEditText.getText().toString().trim();

        //if Image title is empty

        if(imageName.isEmpty()){
            titleEditText.setError("Enter the image Name");
            titleEditText.requestFocus();
            return;
        }


        //generate an unique name to this image from system

        StorageReference ref=storageReference.child(System.currentTimeMillis()+"."+getFileExtension(imageUri));


        // add the listener

        ref.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getApplicationContext(),"Image is stored successfully",Toast.LENGTH_SHORT).show();

                      //generate a reference for the image
                        Upload upload=new Upload(imageName,taskSnapshot.getStorage().getDownloadUrl().toString());

                        //take a unique key for database reference

                        String uploadId=databaseReference.push().getKey();

                        databaseReference.child(uploadId).setValue(upload);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        // ...

                        Toast.makeText(getApplicationContext(),"Image is not stored successfully",Toast.LENGTH_SHORT).show();

                    }
                });





    }


    //getting an extension of that image

    public String getFileExtension(Uri imageUri){
        ContentResolver contentResolver=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));

    }

}
