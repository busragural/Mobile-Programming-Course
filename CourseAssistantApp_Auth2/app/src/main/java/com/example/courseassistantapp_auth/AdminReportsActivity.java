package com.example.courseassistantapp_auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdminReportsActivity extends AppCompatActivity {
    private RecyclerView reportsRecyclerView;
    private ReportAdapter reportAdapter;
    private List<ForumMessage> reportList;
    private DatabaseReference forumReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_reports);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("All Reports");
        }

        reportsRecyclerView = findViewById(R.id.reports_recycler_view);
        reportsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        reportList = new ArrayList<>();
        reportAdapter = new ReportAdapter(reportList);
        reportsRecyclerView.setAdapter(reportAdapter);

        forumReference = FirebaseDatabase.getInstance().getReference("Forum");

        loadAllReports();
    }

    private void loadAllReports() {
        forumReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                reportList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ForumMessage report = snapshot.getValue(ForumMessage.class);
                    reportList.add(report);
                }


                Collections.sort(reportList, new Comparator<ForumMessage>() {
                    @Override
                    public int compare(ForumMessage r1, ForumMessage r2) {
                        return r2.timestamp.compareTo(r1.timestamp);
                    }
                });

                reportAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AdminReportsActivity.this, "Error loading reports: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show(); // Hata durumunda mesaj
            }

        });
    }


    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.admin_profile) {

            Intent intent = new Intent(AdminReportsActivity.this, UserProfileActivity.class);
            startActivity(intent);

        }
        else if (id == R.id.admin_post) {

            Intent intent = new Intent(AdminReportsActivity.this, PostsActivity.class);
            startActivity(intent);

        }
        else {
            Toast.makeText(AdminReportsActivity.this, "Something went wrong.", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}