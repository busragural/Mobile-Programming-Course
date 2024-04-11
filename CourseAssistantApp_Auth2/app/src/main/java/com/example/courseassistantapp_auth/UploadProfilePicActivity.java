package com.example.courseassistantapp_auth;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class UploadProfilePicActivity extends AppCompatActivity {

    private ImageView imageView;
    private FirebaseAuth auth;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;

    private Uri uriImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_profile_pic);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Profile Picture");
            //actionBar.hide();
        }

        auth = FirebaseAuth.getInstance();
        Button uploadButton = findViewById(R.id.uploadPic_uploadButton);
        Button chooseButton = findViewById(R.id.uploadPic_chooseButton);
        Button cameraButton = findViewById(R.id.uploadPic_cameraButton);

        imageView = findViewById(R.id.uploadPic_imageView);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference("DisplayPics");

        Uri uri = firebaseUser.getPhotoUrl();

        Picasso.get().load(uri).into(imageView);

        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPic();
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCamera();
            }
        });


    }

    private void uploadPic() {
        if (uriImage != null) {
            StorageReference reference = storageReference.child(auth.getCurrentUser().getUid() + "." + getFileExtension(uriImage));
            reference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri = uri;
                            firebaseUser = auth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates);
                        }
                    });
                    Toast.makeText(UploadProfilePicActivity.this, "Upload successfully.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UploadProfilePicActivity.this, UserProfileActivity.class);
                    startActivity(intent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UploadProfilePicActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                }
            });
        } else {
            Toast.makeText(UploadProfilePicActivity.this, "No file selected.", Toast.LENGTH_SHORT).show();

        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver resolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(resolver.getType(uri));
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "There's no camera app installed.", Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
//            uriImage = data.getData();
//            imageView.setImageURI(uriImage);
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriImage = data.getData();
            imageView.setImageURI(uriImage);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if (imageBitmap != null) {
                    imageView.setImageBitmap(imageBitmap);
                    Log.d("UploadProfilePic", "Image captured successfully");
                    // Eğer istenirse, Bitmap'i bir Uri'ye dönüştürmek için:
                    uriImage = getImageUri(this, imageBitmap);
                    if (uriImage != null) {
                        imageView.setImageURI(uriImage);
                    } else {
                        Log.e("UploadProfilePic", "Failed to convert Bitmap to Uri");
                        // Null döndüğünde hata mesajı göster
                        Toast.makeText(this, "Failed to convert Bitmap to Uri", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("UploadProfilePic", "Image bitmap is null");
                    // Null döndüğünde hata mesajı göster
                    Toast.makeText(this, "Image bitmap is null", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("UploadProfilePic", "Extras bundle is null");
                // Null döndüğünde hata mesajı göster
                Toast.makeText(this, "Extras bundle is null", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("UploadProfilePic", "onActivityResult: Unexpected requestCode or resultCode");
            // Null döndüğünde hata mesajı göster
            Toast.makeText(this, "Unexpected requestCode or resultCode", Toast.LENGTH_SHORT).show();
        }
    }


    private Uri getImageUri(Context context, Bitmap bitmap) {
        try {

            File tempFile = File.createTempFile("tempImage", ".jpg", context.getExternalCacheDir());


            FileOutputStream fos = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();


            return Uri.fromFile(tempFile);
        } catch (IOException e) {
            Log.e("UploadProfilePic", "Failed to create temp file: " + e.getMessage());
            Toast.makeText(context, "Failed to create temp file", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}