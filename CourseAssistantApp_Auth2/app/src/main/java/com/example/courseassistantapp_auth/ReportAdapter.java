package com.example.courseassistantapp_auth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.courseassistantapp_auth.ForumMessage;
import com.example.courseassistantapp_auth.R;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {
    private List<ForumMessage> reportList;

    public ReportAdapter(List<ForumMessage> reportList) {
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.report_item_layout, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        ForumMessage report = reportList.get(position);
        holder.courseCodeTextView.setText("Course Code: " + report.courseCode);
        holder.messageTextView.setText("Message: " + report.messageText);
        holder.recipientTextView.setText("Recipient: " + report.recipent);
        holder.timestampTextView.setText("Timestamp: " + report.timestamp);
        holder.topicTextView.setText("Topic: " + report.topic);
        holder.userEmailTextView.setText("User: " + report.userEmail);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView courseCodeTextView;
        TextView messageTextView;
        TextView recipientTextView;
        TextView timestampTextView;
        TextView topicTextView;
        TextView userEmailTextView;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            courseCodeTextView = itemView.findViewById(R.id.report_course_code);
            messageTextView = itemView.findViewById(R.id.report_message_text);
            recipientTextView = itemView.findViewById(R.id.report_recipient);
            timestampTextView = itemView.findViewById(R.id.report_timestamp);
            topicTextView = itemView.findViewById(R.id.report_topic);
            userEmailTextView = itemView.findViewById(R.id.report_user_email);
        }
    }
}
