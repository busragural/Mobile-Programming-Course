package com.example.courseassistantapp_auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UserProfileActivity extends AppCompatActivity {

    TextView profileTitle, profileLevel, profileName, profileSurname, profilePersonId, profileEmail, profilePhone, profileEducation;
    String name, surname, email, personId, phone, education, level, title, instagram, twitter;
    Button profileEditButton;
    ImageView imageView, ivWp, ivInsta, ivTwit, ivMail;
    FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Profile");
        }

        profileTitle = findViewById(R.id.profile_title);
        profileLevel = findViewById(R.id.profile_level);
        profileName = findViewById(R.id.profile_name);
        profileSurname = findViewById(R.id.profile_surname);
        profilePersonId = findViewById(R.id.profile_personId);
        profileEmail = findViewById(R.id.profile_email);
        profilePhone = findViewById(R.id.profile_phone);
        profileEducation = findViewById(R.id.profile_education);
        imageView = findViewById(R.id.profile_image);

        auth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = auth.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(UserProfileActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
        } else {
            showProfile(firebaseUser);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, UploadProfilePicActivity.class);
                startActivity(intent);
            }
        });

        ivWp = findViewById(R.id.profile_whatsapp);
        ivInsta = findViewById(R.id.profile_instagram);
        ivMail = findViewById(R.id.profile_gmail);
        ivTwit = findViewById(R.id.profile_twitter);


    }

    private void showProfile(FirebaseUser firebaseUser) {
        String userId = firebaseUser.getUid();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("RegisteredUsers");
        reference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                UserDetails userDetail = snapshot.getValue(UserDetails.class);
                if (userDetail != null) {
                    name = firebaseUser.getDisplayName();
                    email = firebaseUser.getEmail();
                    surname = userDetail.getSurname();
                    personId = userDetail.getPersonId();
                    level = userDetail.getLevel();
                    education = userDetail.getEducation();
                    phone = userDetail.getPhone();
                    instagram = userDetail.getInstagram();
                    twitter = userDetail.getTwitter();
                    ArrayList<Boolean> settings = userDetail.getSettings();


                    profileName.setText(name);


                    if (settings != null && !settings.isEmpty() && settings.get(0)) {
                        profileEmail.setText("***");
                    } else {
                        profileEmail.setText(email);
                    }

                    if (settings != null && !settings.isEmpty() && settings.get(1) && !phone.isEmpty()) {
                        profilePhone.setText("***");
                    } else if (phone.isEmpty()) {
                        profilePhone.setText("No info.");
                    } else {
                        profilePhone.setText(phone);
                    }


                    profileSurname.setText(surname);
                    profilePersonId.setText(personId);
                    profileTitle.setText(name + " " + surname);

                    if (!level.isEmpty()) {
                        profileLevel.setText(level);
                    } else {
                        profileLevel.setText("No info.");
                    }

                    if (!education.isEmpty()) {
                        profileEducation.setText(education);
                    } else {
                        profileEducation.setText("No info.");
                    }


                    ivWp.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (settings != null && !settings.isEmpty() && settings.get(1)) {
                                ivWp.setClickable(false);
                                Toast.makeText(UserProfileActivity.this, "Whatsapp is hidden.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!phone.isEmpty()) {
                                    String formattedPhoneNumber = phone.startsWith("0") ? phone.substring(1) : phone;

                                    Intent whatsappIntent = new Intent(Intent.ACTION_VIEW);
                                    whatsappIntent.setData(Uri.parse("https://wa.me/" + formattedPhoneNumber));
                                    startActivity(whatsappIntent);
                                } else {
                                    Toast.makeText(UserProfileActivity.this, "Phone number  is not available.", Toast.LENGTH_SHORT).show();

                                }

                            }
                        }
                    });


                    ivMail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (settings != null && !settings.isEmpty() && settings.get(0)) {
                                ivMail.setClickable(false);
                                Toast.makeText(UserProfileActivity.this, "Email is hidden.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (!email.isEmpty()) {

                                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                                    emailIntent.setData(Uri.parse("mailto:" + email));
                                    startActivity(emailIntent);
                                } else {
                                    Toast.makeText(UserProfileActivity.this, "Email address is not available.", Toast.LENGTH_SHORT).show();
                                }
                            }


                        }
                    });

                    ivInsta.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (settings != null && !settings.isEmpty() && settings.size() > 2 && settings.get(2)) {
                                ivInsta.setClickable(false);
                                Toast.makeText(UserProfileActivity.this, "Instagram username is hidden.", Toast.LENGTH_SHORT).show();
                            } else {

                                if (instagram != null && !instagram.isEmpty()) {
                                    Intent instagramIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/" + instagram));
                                    instagramIntent.setPackage("com.instagram.android");
                                    if (instagramIntent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(instagramIntent);
                                    } else {
                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/" + instagram)));
                                    }
                                } else {
                                    Toast.makeText(UserProfileActivity.this, "Instagram username is not available.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                    ivTwit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (settings != null && !settings.isEmpty() && settings.size() > 3 && settings.get(3)) {
                                ivTwit.setClickable(false); // Tıklanabilirliği kapat
                                Toast.makeText(UserProfileActivity.this, "Twitter username is hidden.", Toast.LENGTH_SHORT).show();
                            } else {
                                if (twitter != null && !twitter.isEmpty()) {

                                    Intent twitterIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitter));
                                    twitterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    twitterIntent.setPackage("com.twitter.android");

                                    if (twitterIntent.resolveActivity(getPackageManager()) != null) {
                                        startActivity(twitterIntent);
                                    } else {

                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + twitter)));
                                    }
                                } else {
                                    Toast.makeText(UserProfileActivity.this, "Twitter username is not available.", Toast.LENGTH_SHORT).show();
                                }
                            }


                        }
                    });

                    Uri uri = firebaseUser.getPhotoUrl();
                    Picasso.get().load(uri).into(imageView);


                } else {
                    Toast.makeText(UserProfileActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            startActivity(getIntent());
            finish();
            overridePendingTransition(0, 0);
        } else if (id == R.id.menu_update) {
            Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);

        } else if (id == R.id.menu_logout) {
            auth.signOut();
            Toast.makeText(UserProfileActivity.this, "Logged out.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UserProfileActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}