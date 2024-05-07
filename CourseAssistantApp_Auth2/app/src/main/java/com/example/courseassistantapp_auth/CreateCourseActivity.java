package com.example.courseassistantapp_auth;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateCourseActivity extends AppCompatActivity {

    private LinearLayout mainLayout;
    private EditText groupCountEditText;
    private ArrayList<EditText> assigneeFields;
    private ArrayList<Button> uploadCsvButtons;
    private Button uploadCsvButton, createButton;
    private LinearLayout assigneeContainer;
    private CardView newCardView;
    private ScrollView scrollView;
    FirebaseDatabase database;
    DatabaseReference reference;
    private int currentGroupIndex = -1;

    private ActivityResultLauncher<Intent> csvPickerLauncher;
    private HashMap<String, HashMap<String, String>> groupStudentsMap = new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_course);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Create Course");
            actionBar.hide();
        }



        mainLayout = findViewById(R.id.main);
        groupCountEditText = findViewById(R.id.input_group_count);


        assigneeContainer = new LinearLayout(this);
        assigneeContainer.setOrientation(LinearLayout.VERTICAL);

        scrollView = new ScrollView(this);
        scrollView.addView(assigneeContainer);

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                scrollView.getWindowVisibleDisplayFrame(r);
                int screenHeight = scrollView.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;

                if (keypadHeight > screenHeight * 0.15) {

                    scrollView.scrollTo(0, r.top);
                }
            }
        });


        newCardView = new CardView(this);
        newCardView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        newCardView.setRadius(10f);
        newCardView.setElevation(10f);
        newCardView.setCardBackgroundColor(getResources().getColor(R.color.white));
        newCardView.setPadding(16,16,16,16);



        LinearLayout newCardViewLayout = new LinearLayout(this);
        newCardViewLayout.setOrientation(LinearLayout.VERTICAL);


        newCardViewLayout.setPadding(16, 16, 16, 16);


        newCardViewLayout.addView(scrollView);
        newCardView.addView(newCardViewLayout);

        mainLayout.addView(newCardView);

        assigneeFields = new ArrayList<>();
        uploadCsvButtons = new ArrayList<>();

        groupCountEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearAssigneeFields();

                if (s.length() > 0) {
                    int groupCount = Integer.parseInt(s.toString());

                    for (int i = 0; i < groupCount; i++) {
                        EditText newField = new EditText(CreateCourseActivity.this);

                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );

                        layoutParams.setMargins(20, 20, 20, 0);
                        newField.setLayoutParams(layoutParams);
                        newField.setHint("Group " + (i + 1) + ": Assigned Instructor");
                        newField.setPadding(16, 16, 16, 16); // Padding
                        newField.setTextColor(getResources().getColor(R.color.black)); // Metin rengi
                        newField.setBackgroundResource(R.drawable.border); // Arka plan

                        assigneeContainer.addView(newField);
                        assigneeFields.add(newField);




                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        createButton = findViewById(R.id.button_create_course);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();  // Initialize Firebase database and reference
                //reference = database.getReference("users");
                createNewCourse();


            }
        });



    }

    private void clearAssigneeFields() {
        assigneeContainer.removeAllViews();
        assigneeFields.clear();
        uploadCsvButtons.clear();
    }

    private void createNewCourse() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference reference = database.getReference("Courses");

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            String courseName = ((EditText) findViewById(R.id.input_course_name)).getText().toString();
            String courseCode = ((EditText) findViewById(R.id.input_course_code)).getText().toString().trim();
            String startDate = ((EditText) findViewById(R.id.input_start_date)).getText().toString();
            int groupCount = Integer.parseInt(((EditText) findViewById(R.id.input_group_count)).getText().toString());

            ArrayList<String> assigneeNames = new ArrayList<>();
            for (EditText assigneeField : assigneeFields) {
                assigneeNames.add(assigneeField.getText().toString());
            }

            HashMap<String, Object> courseData = new HashMap<>();
            courseData.put("courseName", courseName);
            courseData.put("courseCode", courseCode);
            courseData.put("startDate", startDate);
            courseData.put("groupCount", groupCount);
            courseData.put("assignees", assigneeNames);
            courseData.put("createdBy", userEmail);

            // Yeni kurs için benzersiz anahtar
            String newCourseKey = reference.push().getKey();
            DatabaseReference courseReference = reference.child(newCourseKey);

            // Kurs bilgilerini Firebase'e ekleme
            courseReference.setValue(courseData).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Grup ekleme ve anahtar-değer çiftlerini ekleme
                    DatabaseReference groupsReference = courseReference.child("groups");
                    for (int i = 1; i <= groupCount; i++) {
                        String groupKey = "Group_" + i;
                        HashMap<String, Object> groupData = new HashMap<>();
                        groupData.put("0", "test"); // Örnek anahtar-değer çifti

                        // Grupları oluşturun ve anahtar-değer çiftlerini ekleyin
                        groupsReference.child(groupKey).setValue(groupData)
                                .addOnCompleteListener(groupTask -> {
                                    if (!groupTask.isSuccessful()) {
                                        Toast.makeText(CreateCourseActivity.this, "Failed to create group " + groupKey, Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                    // Başarıyla kurs oluşturuldu mesajı
                    Toast.makeText(CreateCourseActivity.this, "Course created successfully.", Toast.LENGTH_LONG).show();
                    // InstructorCourseActivity'e yönlendirme
                    startActivity(new Intent(CreateCourseActivity.this, InstructorCourseActivity.class));
                } else {
                    Toast.makeText(CreateCourseActivity.this, "Failed to create course.", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(CreateCourseActivity.this, "Error: No user is logged in.", Toast.LENGTH_LONG).show();
        }
    }
}