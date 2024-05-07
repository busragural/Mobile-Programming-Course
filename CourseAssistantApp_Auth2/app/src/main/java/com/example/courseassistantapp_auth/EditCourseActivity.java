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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class EditCourseActivity extends AppCompatActivity {


    private LinearLayout mainLayout;
    private EditText groupCountEditText;
    private ArrayList<EditText> assigneeFields;
    private ArrayList<Button> uploadCsvButtons;
    private Button uploadCsvButton, updateButton;
    private LinearLayout assigneeContainer;
    private CardView newCardView;
    private ScrollView scrollView;
    FirebaseDatabase database;
    DatabaseReference reference;
    private int currentGroupIndex = -1;

    private EditText courseNameEditText;
    private EditText courseCodeEditText;
    private EditText startDateEditText;
    private DatabaseReference courseReference;

    private ActivityResultLauncher<Intent> csvPickerLauncher;
    private HashMap<String, HashMap<String, String>> groupStudentsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_course);
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

        courseNameEditText = findViewById(R.id.input_course_name);
        courseCodeEditText = findViewById(R.id.input_course_code);
        startDateEditText = findViewById(R.id.input_start_date);
        groupCountEditText = findViewById(R.id.input_group_count);

        String courseCode = getIntent().getStringExtra("courseCode");
        if (courseCode == null) {
            Toast.makeText(EditCourseActivity.this, "Error: No course code provided", Toast.LENGTH_LONG).show();
            return;
        }

        courseReference = FirebaseDatabase.getInstance().getReference("Courses").child(courseCode);
        System.out.println("11" + courseCode);
        fetchCourseData(courseCode);


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
        newCardView.setPadding(16, 16, 16, 16);


        LinearLayout newCardViewLayout = new LinearLayout(this);
        newCardViewLayout.setOrientation(LinearLayout.VERTICAL);


        newCardViewLayout.setPadding(16, 16, 16, 16);


        newCardViewLayout.addView(scrollView);
        newCardView.addView(newCardViewLayout);

        mainLayout.addView(newCardView);

        assigneeFields = new ArrayList<>();
        uploadCsvButtons = new ArrayList<>();


        Button uploadCsvButton = new Button(EditCourseActivity.this);
        uploadCsvButton.setText("Upload CSV");

        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonLayoutParams.setMargins(20, 20, 20, 20); // Sol, Üst, Sağ, Alt için boşluklar

        uploadCsvButton.setLayoutParams(buttonLayoutParams);
        uploadCsvButton.setBackgroundResource(R.color.snow);
        assigneeContainer.addView(uploadCsvButton);


        uploadCsvButton.setOnClickListener(v -> {
            //currentGroupIndex = finalI; // Store the group index in a variable
            setGroupIndexForCurrentUser();
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/csv");
            csvPickerLauncher.launch(intent);
        });


        uploadCsvButtons.add(uploadCsvButton);


//        groupCountEditText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                clearAssigneeFields();
//
//                if (s.length() > 0) {
//                    int groupCount = Integer.parseInt(s.toString());
//
//                    for (int i = 0; i < groupCount; i++) {
//                        EditText newField = new EditText(EditCourseActivity.this);
//
//                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                LinearLayout.LayoutParams.WRAP_CONTENT
//                        );
//
//                        layoutParams.setMargins(20, 20, 20, 0);
//                        newField.setLayoutParams(layoutParams);
//                        newField.setHint("Group " + (i + 1) + ": Assigned Instructor");
//                        newField.setPadding(16, 16, 16, 16); // Padding
//                        newField.setTextColor(getResources().getColor(R.color.black)); // Metin rengi
//                        newField.setBackgroundResource(R.drawable.border); // Arka plan
//
//                        assigneeContainer.addView(newField);
//                        assigneeFields.add(newField);
//
//                        Button uploadCsvButton = new Button(EditCourseActivity.this);
//                        uploadCsvButton.setText("Upload CSV for Group " + (i + 1));
//
//                        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                LinearLayout.LayoutParams.WRAP_CONTENT
//                        );
//                        buttonLayoutParams.setMargins(20, 20, 20, 20); // Sol, Üst, Sağ, Alt için boşluklar
//
//                        uploadCsvButton.setLayoutParams(buttonLayoutParams);
//                        uploadCsvButton.setBackgroundResource(R.color.snow);
//                        assigneeContainer.addView(uploadCsvButton);
//
//
//                        int finalI = i;
//                        uploadCsvButton.setOnClickListener(v -> {
//                            currentGroupIndex = finalI; // Store the group index in a variable
//                            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                            intent.setType("text/csv");
//                            csvPickerLauncher.launch(intent);
//                        });
//
//
//                        uploadCsvButtons.add(uploadCsvButton);
//                    }
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });

        csvPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        System.out.println("data" + data);
                        if (data != null && data.getData() != null) {
                            Uri csvUri = data.getData();
                            int groupIndex = currentGroupIndex;

                            System.out.println("groupindexx: " + groupIndex);

                            if (groupIndex >= 0) {
                                try {

                                    String fileName = csvUri.getLastPathSegment();
                                    System.out.println("filenameee:" + fileName);
                                    if (fileName == null) {
                                        Toast.makeText(EditCourseActivity.this, "Error: Could not retrieve file name", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    //uploadCsvButtons.get(groupIndex).setText(fileName);

                                    // CSV dosyasını oku ve Firebase'e kaydet
                                    InputStream inputStream = getContentResolver().openInputStream(csvUri);
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                                    List<String[]> csvData = new ArrayList<>();
                                    String line;
                                    while ((line = reader.readLine()) != null) {
                                        csvData.add(line.split(","));
                                    }

                                    HashMap<String, String> studentData = new HashMap<>();
                                    for (String[] row : csvData) {
                                        if (row.length > 1) {
                                            studentData.put(row[0], row[1]);
                                        }
                                    }

                                    String groupKey = "Group_" + (groupIndex + 1);

                                    System.out.println("groupkey" + groupKey);
                                    groupStudentsMap.put(groupKey, studentData);

                                    Toast.makeText(EditCourseActivity.this, "CSV file data processed for " + groupKey, Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(EditCourseActivity.this, "Error processing CSV file.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }
                }
        );


        updateButton = findViewById(R.id.button_update_course);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                database = FirebaseDatabase.getInstance();  // Initialize Firebase database and reference
                //reference = database.getReference("users");
                updateCourse();

            }
        });


    }


    private void clearAssigneeFields() {
        assigneeContainer.removeAllViews();
        assigneeFields.clear();
        uploadCsvButtons.clear();
    }

    private void updateCourse() {
        String courseCode = getIntent().getStringExtra("courseCode");
        if (courseCode == null) {
            Toast.makeText(EditCourseActivity.this, "Error: No course code provided", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference coursesReference = database.getReference("Courses");

        coursesReference.orderByChild("courseCode").equalTo(courseCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot courseSnapshot = dataSnapshot.getChildren().iterator().next();

                if (courseSnapshot == null) {
                    Toast.makeText(EditCourseActivity.this, "Course not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, Object> updates = new HashMap<>();

                Course course = courseSnapshot.getValue(Course.class);

                if (course == null) {
                    Toast.makeText(EditCourseActivity.this, "Error: Course data missing", Toast.LENGTH_SHORT).show();
                    return;
                }

                String newCourseName = ((EditText) findViewById(R.id.input_course_name)).getText().toString().trim();
                if (!newCourseName.isEmpty() && !newCourseName.equals(course.getCourseName())) {
                    updates.put("courseName", newCourseName); // Sadece değişen kısmı güncelleyin
                }

                String newCourseCode = ((EditText) findViewById(R.id.input_course_code)).getText().toString().trim();
                if (!newCourseCode.isEmpty() && !newCourseCode.equals(course.getCourseCode())) {
                    updates.put("courseCode", newCourseCode);
                }

                String newStartDate = ((EditText) findViewById(R.id.input_start_date)).getText().toString().trim();
                if (!newStartDate.isEmpty() && !newStartDate.equals(course.getStartDate())) {
                    updates.put("startDate", newStartDate);
                }

                ArrayList<String> newAssignees = new ArrayList<>();
                for (EditText assigneeField : assigneeFields) {
                    String assignee = assigneeField.getText().toString().trim();
                    if (!assignee.isEmpty() && !newAssignees.contains(assignee)) {
                        newAssignees.add(assignee);
                    }
                }


                if (!newAssignees.isEmpty() && !newAssignees.equals(course.getAssignees())) {
                    updates.put("assignees", newAssignees);
                }

                String groupCountStr = ((EditText) findViewById(R.id.input_group_count)).getText().toString().trim();
                int groupCount = course.getGroupCount();
                try {
                    groupCount = Integer.parseInt(groupCountStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(EditCourseActivity.this, "Invalid group count.", Toast.LENGTH_LONG).show();
                }

                if (groupCount != course.getGroupCount()) {
                    updates.put("groupCount", groupCount);
                }

                if (currentGroupIndex >= 0 && currentGroupIndex < groupCount) {
                    String groupKey = "Group_" + (currentGroupIndex + 1);
                    HashMap<String, String> groupData = groupStudentsMap.get(groupKey);

                    if (groupData != null) {
                        DatabaseReference groupReference = coursesReference.child(courseSnapshot.getKey()).child("groups").child(groupKey);

                        updates.put("groups/" + groupKey, groupData);
                    }
                }

                // Firebase'e güncelleme yapın
                DatabaseReference courseReference = coursesReference.child(courseSnapshot.getKey());
                courseReference.updateChildren(updates).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(EditCourseActivity.this, InstructorCourseActivity.class);
                        startActivity(intent);
                        Toast.makeText(EditCourseActivity.this, "Course updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(EditCourseActivity.this, "Failed to update course", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditCourseActivity.this, "Error fetching course data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void fetchCourseData(String courseCode) {
        if (courseCode == null) {
            Toast.makeText(this, "Error: Course code is missing", Toast.LENGTH_LONG).show();
            return;
        }

        DatabaseReference coursesReference = FirebaseDatabase.getInstance().getReference("Courses");

        coursesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean courseFound = false;
                int index = 0;
                Iterable<DataSnapshot> snapshots = dataSnapshot.getChildren();
                Iterator<DataSnapshot> iterator = snapshots.iterator();

                while (iterator.hasNext() && !courseFound) {
                    DataSnapshot snapshot = iterator.next();
                    Course course = snapshot.getValue(Course.class);

                    if (course != null && course.getCourseCode().equals(courseCode)) {
                        courseNameEditText.setText(course.getCourseName());
                        courseCodeEditText.setText(course.getCourseCode());
                        startDateEditText.setText(course.getStartDate());
                        groupCountEditText.setText(String.valueOf(course.getGroupCount()));
                        courseFound = true;
                    }

                    index++;
                }

                if (!courseFound) {
                    Toast.makeText(EditCourseActivity.this, "Course with code " + courseCode + " not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EditCourseActivity.this, "Error fetching course data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setGroupIndexForCurrentUser() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        if (auth.getCurrentUser() != null) {
            String currentUserEmail = auth.getCurrentUser().getEmail();
            String courseCode = getIntent().getStringExtra("courseCode");

            if (courseCode != null && currentUserEmail != null) {
                DatabaseReference coursesReference = database.getReference("Courses");


                Query courseQuery = coursesReference.orderByChild("courseCode").equalTo(courseCode);

                courseQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot courseSnapshot = null;
                        if (dataSnapshot.exists()) {
                            courseSnapshot = dataSnapshot.getChildren().iterator().next();
                        }

                        if (courseSnapshot != null) {
                            Course course = courseSnapshot.getValue(Course.class);

                            if (course != null) {
                                List<String> assignees = course.getAssignees();
                                if (assignees != null) {
                                    currentGroupIndex = assignees.indexOf(currentUserEmail);

                                    if (currentGroupIndex < 0) { // Eğer kullanıcı bulunamazsa
                                        Toast.makeText(EditCourseActivity.this, "User is not assigned to any group.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        } else { // Eğer kurs bulunamazsa
                            Toast.makeText(EditCourseActivity.this, "Course with code " + courseCode + " not found.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(EditCourseActivity.this, "Error fetching course data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }



}
