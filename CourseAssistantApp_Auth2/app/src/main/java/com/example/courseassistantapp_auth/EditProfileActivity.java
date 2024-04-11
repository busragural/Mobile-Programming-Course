package com.example.courseassistantapp_auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditProfileActivity extends AppCompatActivity {


    private EditText name, surname, personId, email, phone, education, instagram, twitter, level;
    private String tmpName, tmpSurname, tmpPersonId, tmpEmail, tmpPhone, tmpEducation, tmpInstagram, tmpTwitter, tmpLevel;
    private FirebaseAuth auth;

    private ArrayList<Boolean> checkboxStates = new ArrayList<>();


    private CheckBox cbEmail, cbPhone, cbTwitter, cbInstagram;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Edit Profile");
        }

        name = findViewById(R.id.edit_name);
        surname = findViewById(R.id.edit_surname);
        personId = findViewById(R.id.edit_personId);
        email = findViewById(R.id.edit_email);
        phone = findViewById(R.id.edit_phone);
        education = findViewById(R.id.edit_education);
        instagram = findViewById(R.id.edit_instagram);
        twitter = findViewById(R.id.edit_twitter);
        level = findViewById(R.id.edit_level);

        // Set icons for Twitter and Instagram clear buttons
        ImageView deleteTwitter = findViewById(R.id.twitter_delete);
        deleteTwitter.setImageResource(R.drawable.baseline_delete_24);

        deleteTwitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitter.setText("");
            }
        });

        ImageView deleteInstagram = findViewById(R.id.instagram_delete);
        deleteInstagram.setImageResource(R.drawable.baseline_delete_24);

        deleteInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                instagram.setText("");
            }
        });



        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        showProfile(firebaseUser);

        Button editButton = findViewById(R.id.edit_button);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateProfile(firebaseUser);
            }
        });


        cbEmail = findViewById(R.id.edit_cb_email);
        cbInstagram = findViewById(R.id.edit_cb_instagram);
        cbPhone = findViewById(R.id.edit_cb_phone);
        cbTwitter = findViewById(R.id.edit_cb_twitter);

        setupCheckboxListeners();


    }

    // Function to update profile
    private void updateProfile(FirebaseUser firebaseUser) {

        tmpName = name.getText().toString();
        tmpSurname = surname.getText().toString();
        tmpEmail = email.getText().toString();
        tmpPersonId = personId.getText().toString();
        tmpPhone = phone.getText().toString();
        tmpInstagram = instagram.getText().toString();
        tmpTwitter = twitter.getText().toString();
        tmpEducation = education.getText().toString();
        tmpLevel = level.getText().toString();

        if (TextUtils.isEmpty(tmpName)) {
            Toast.makeText(EditProfileActivity.this, "Fill in the name field.", Toast.LENGTH_SHORT).show();
            name.setError("Name cannot be empty.");
        } else if (TextUtils.isEmpty(tmpSurname)) {
            Toast.makeText(EditProfileActivity.this, "Fill in the surname field.", Toast.LENGTH_SHORT).show();
            surname.setError("Surname cannot be empty.");
        } else if (TextUtils.isEmpty(tmpPersonId)) {
            Toast.makeText(EditProfileActivity.this, "Fill in the id field.", Toast.LENGTH_SHORT).show();
            personId.setError("ID cannot be empty.");
        } else if (TextUtils.isEmpty(tmpPhone)) {
            phone.setError("ID cannot be empty.");
        } else if (TextUtils.isEmpty(tmpEducation)) {
            education.setError("Education cannot be empty");
        } else {

            // Create a new UserDetails object and set checkbox states
            UserDetails newUser = new UserDetails(tmpName, tmpSurname, tmpPersonId, tmpEmail, tmpPhone, tmpEducation, tmpLevel, tmpInstagram, tmpTwitter);
            checkboxStates.clear();
            checkboxStates.add(cbEmail.isChecked());
            checkboxStates.add(cbPhone.isChecked());
            checkboxStates.add(cbInstagram.isChecked());
            checkboxStates.add(cbTwitter.isChecked());
            newUser.setSettings(checkboxStates);

            // Get database reference and update user
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("RegisteredUsers");
            String userId = firebaseUser.getUid();
            reference.child(userId).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(tmpName).build();
                        firebaseUser.updateProfile(profileUpdate);

                        Intent intent = new Intent(EditProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e) {
                            Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }
    }

    // Function to display profile
    private void showProfile(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("RegisteredUsers");

        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserDetails userDetail = snapshot.getValue(UserDetails.class);
                if (userDetail != null) {
                    tmpName = firebaseUser.getDisplayName();
                    tmpEmail = firebaseUser.getEmail();
                    tmpSurname = userDetail.getSurname();
                    tmpPersonId = userDetail.getPersonId();
                    tmpLevel = userDetail.getLevel();
                    tmpEducation = userDetail.getEducation();
                    tmpPhone = userDetail.getPhone();
                    tmpInstagram = userDetail.getInstagram();
                    tmpTwitter = userDetail.getTwitter();
                    ArrayList<Boolean> settings = userDetail.getSettings();

                    if (settings != null && settings.size() >= 4) {
                        cbEmail.setChecked(settings.get(0));
                        cbPhone.setChecked(settings.get(1));
                        cbInstagram.setChecked(settings.get(2));
                        cbTwitter.setChecked(settings.get(3));
                    }

                    name.setText(tmpName);
                    surname.setText(tmpSurname);
                    email.setText(tmpEmail);
                    personId.setText(tmpPersonId);
                    education.setText(tmpEducation);
                    phone.setText(tmpPhone);
                    instagram.setText(tmpInstagram);
                    twitter.setText(tmpTwitter);
                    level.setText(tmpLevel);

                } else {
                    Toast.makeText(EditProfileActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(EditProfileActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void setupCheckboxListeners() {
        // Initialize checkbox states
        checkboxStates.add(false);
        checkboxStates.add(false);
        checkboxStates.add(false);
        checkboxStates.add(false);

        // Set up listeners for each checkbox to update their respective state in the checkboxStates list

        cbEmail.setOnCheckedChangeListener((buttonView, isChecked) -> checkboxStates.set(0, isChecked));
        cbPhone.setOnCheckedChangeListener((buttonView, isChecked) -> checkboxStates.set(1, isChecked));
        cbInstagram.setOnCheckedChangeListener((buttonView, isChecked) -> checkboxStates.set(2, isChecked));
        cbTwitter.setOnCheckedChangeListener((buttonView, isChecked) -> checkboxStates.set(3, isChecked));
    }

}