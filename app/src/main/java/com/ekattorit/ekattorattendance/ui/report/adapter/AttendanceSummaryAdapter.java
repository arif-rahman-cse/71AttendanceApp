package com.ekattorit.ekattorattendance.ui.report.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ItemViewSummaryAttendanceBinding;
import com.ekattorit.ekattorattendance.ui.report.model.AttendanceListItem;

import java.text.ParseException;
import java.util.List;

public class AttendanceSummaryAdapter extends RecyclerView.Adapter<AttendanceSummaryAdapter.MyViewHolder> {
    private static final String TAG = "DailySalesAdapter";
    private Context context;
    private List<AttendanceListItem> attendanceList;


    public AttendanceSummaryAdapter(Context context, List<AttendanceListItem> attendanceList) {
        this.context = context;
        this.attendanceList = attendanceList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_summary_attendance, parent, false);
        ItemViewSummaryAttendanceBinding itemViewBinding = DataBindingUtil.bind(view);
        assert itemViewBinding != null;
        return new AttendanceSummaryAdapter.MyViewHolder(itemViewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        AttendanceListItem attendance = attendanceList.get(position);

        holder.binding.tvEmployeeId.setText(attendance.getEmpId());
        holder.binding.tvEmployeeName.setText(attendance.getEmpName());

        holder.binding.tvPresent.setText(String.valueOf(attendance.getTotalPresent()));

        holder.binding.tvAbsent.setText(String.valueOf(attendance.getTotalAbsent()));



    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemViewSummaryAttendanceBinding binding;

        MyViewHolder(@NonNull ItemViewSummaryAttendanceBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

        }

    }


}
