package com.example.courseassistantapp_auth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    private EditText loginEmail, loginPassword;
    private Button loginButton;
    private TextView registerRedirectText, forgotPasswordText;
    private FirebaseAuth auth;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Login");
            actionBar.hide();
        }

        // Initialize UI elements
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        registerRedirectText = findViewById(R.id.registerRedirectText);
        forgotPasswordText = findViewById(R.id.login_forgot_psw);
        loginButton = findViewById(R.id.login_button);
        ImageView showHideImageView = findViewById(R.id.login_show_hide);


        auth = FirebaseAuth.getInstance();

        // Set click listener for "Forgot Password" text
        forgotPasswordText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "Reset your password.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));

            }
        });


        // Set initial icon and click listener for show/hide password functionality
        showHideImageView.setImageResource(R.drawable.baseline_hide_source_24);
        showHideImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (loginPassword.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())) {
                    loginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    showHideImageView.setImageResource(R.drawable.baseline_hide_source_24);
                } else {
                    loginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    showHideImageView.setImageResource(R.drawable.baseline_panorama_fish_eye_24);
                }
            }
        });

        // Set click listener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();

                if (!validateEmail(email) || !validatePassword(password)) {
                    Toast.makeText(LoginActivity.this, "Error.", Toast.LENGTH_SHORT).show();
                } else {
                    checkUser(email, password); // Proceed to login if input is valid
                }

            }
        });

        registerRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // Method to validate email format
    public Boolean validateEmail(String email) {

        if (email.isEmpty()) {
            loginEmail.setError("Email cannot be empty.");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginEmail.setError("Enter a valid email.");
            return false;
        } else {
            loginEmail.setError(null);
            return true;
        }
    }

    // Method to validate password field
    public Boolean validatePassword(String password) {

        if (password.isEmpty()) {
            loginPassword.setError("Password cannot be empty.");
            return false;
        } else {
            loginPassword.setError(null);
            return true;
        }
    }

    // Method to authenticate user
    public void checkUser(String email, String password) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {         // If login is successful, check if email is verified or user is admin
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser.isEmailVerified() || firebaseUser.getUid().equals("es3cgqpFFKVZJlYcL9C8hYppGCF3") || firebaseUser.getUid().equals("0LM7fM1RWEfRILfr51RSutsdBCu2") || firebaseUser.getUid().equals("7rHkU05l7cQJhNAAwB2gGLMYfL62")) {
                        Toast.makeText(LoginActivity.this, "Logged in successfully.", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, UserProfileActivity.class);
                        startActivity(intent);
                    } else {                           // If email is not verified, prompt user to verify and log out
                        firebaseUser.sendEmailVerification();
                        auth.signOut();
                        showAlertDialog();

                    }

                } else {
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        loginEmail.setError("User does not exists.");
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        loginEmail.setError("Invalid credentials.");
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                }
            }
        });


    }

    // Method to show alert dialog for email verification
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email not verified.");
        builder.setMessage("Please verify your email. You cannot login without verification.");

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}