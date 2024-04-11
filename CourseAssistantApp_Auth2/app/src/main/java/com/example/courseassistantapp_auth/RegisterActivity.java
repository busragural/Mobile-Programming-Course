package com.example.courseassistantapp_auth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    EditText registerName, registerSurname, registerPersonId, registerEmail, registerPassword;
    TextView loginRedirectText;
    Button registerButton;
    FirebaseDatabase database;
    DatabaseReference reference;
    private static final String TAG = "RegisterActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Hide the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Register");
            actionBar.hide();
        }

        // Initialize UI elements
        registerName = findViewById(R.id.register_name);
        registerSurname = findViewById(R.id.register_surname);
        registerPersonId = findViewById(R.id.register_personId);
        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);
        registerButton = findViewById(R.id.register_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();  // Initialize Firebase database and reference
                reference = database.getReference("users");

                String name = registerName.getText().toString();
                String surname = registerSurname.getText().toString();
                String personId = registerPersonId.getText().toString();
                String email = registerEmail.getText().toString();
                String password = registerPassword.getText().toString();
                String desiredEmailExtension1 = "std.yildiz.edu.tr";
                String desiredEmailExtension2 = "yildiz.edu.tr";

                // Validate user input
                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(RegisterActivity.this, "Fill in the name field.", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(surname)) {
                    Toast.makeText(RegisterActivity.this, "Fill in the surname field.", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(personId)) {
                    Toast.makeText(RegisterActivity.this, "Fill in the id field.", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterActivity.this, "Fill in the email field.", Toast.LENGTH_SHORT).show();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(RegisterActivity.this, "Enter the valid email.", Toast.LENGTH_SHORT).show();
                    //} else if (!email.endsWith("@" + desiredEmailExtension1) && !email.endsWith("@" + desiredEmailExtension2)) {
                    //    Toast.makeText(RegisterActivity.this, "Email must have one of the desired extensions: " + desiredEmailExtension1 + " or " + desiredEmailExtension2, Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Fill in the password field.", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(RegisterActivity.this, "Password should be at least 6 characters.", Toast.LENGTH_SHORT).show();
                } else {

                    registerUser(name, surname, personId, email, password);
                }

            }
        });
        // Redirect to login activity
        loginRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });
    }

    // Method to register a new user
    private void registerUser(String name, String surname, String personId, String email, String password) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {               // If registration is successful, update user profile with provided name
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                    if (firebaseUser != null) {
                        firebaseUser.updateProfile(profileChangeRequest);
                    }

                    String level;
                    if (email.endsWith("@yildiz.edu.tr")) {
                        level = "Instructor";
                    } else if (email.endsWith("@std.yildiz.edu.tr")) {
                        level = "Student";
                    } else {
                        level = "Admin";
                    }

                    ArrayList<Boolean> settings = new ArrayList<>();
                    settings.add(false);
                    settings.add(false);
                    settings.add(false);
                    settings.add(false);


                    // Save user details to Firebase database
                    UserDetails writeUserDetails = new UserDetails(name, surname, personId, email, "", "", level, "", "", settings);

                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("RegisteredUsers");
                    reference.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                firebaseUser.sendEmailVerification();   // If user details are saved successfully, send email verification
                                Toast.makeText(RegisterActivity.this, "User registered successfully. Please verify your email.", Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(RegisterActivity.this, "User registered failed. Please try again.", Toast.LENGTH_LONG).show();

                            }

                        }
                    });

                } else {
                    try {
                        throw Objects.requireNonNull(task.getException());
                    } catch (FirebaseAuthWeakPasswordException e) {
                        registerPassword.setError("Your password is too weak. Kindly use a mix of alphabets, numbers and special characters.");
                        registerPassword.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        registerEmail.setError("Your email is invalid or already in use. Kindly re-enter.");
                        registerEmail.requestFocus();
                    } catch (FirebaseAuthUserCollisionException e) {
                        registerEmail.setError("User is already registered with his email. Use another email.");
                        registerEmail.requestFocus();
                    } catch (Exception e) {
                        Log.e(TAG, Objects.requireNonNull(e.getMessage()));
                        Toast.makeText(RegisterActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

}


