package com.ekattorit.ekattorattendance.ui.report.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;


import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ItemViewDailyAttendanceReportBinding;
import com.ekattorit.ekattorattendance.ui.report.model.RpAttendance;

import java.util.List;

public class DailyAttendanceAdapter extends RecyclerView.Adapter<DailyAttendanceAdapter.MyViewHolder> {
    private static final String TAG = "DailySalesAdapter";
    private Context context;
    private List<RpAttendance> attendanceList;


    public DailyAttendanceAdapter(Context context, List<RpAttendance> attendanceList) {
        this.context = context;
        this.attendanceList = attendanceList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_daily_attendance_report, parent, false);
        ItemViewDailyAttendanceReportBinding itemViewBinding = DataBindingUtil.bind(view);
        assert itemViewBinding != null;
        return new DailyAttendanceAdapter.MyViewHolder(itemViewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RpAttendance attendance = attendanceList.get(position);
        holder.binding.tvCardNo.setText(attendance.getEmpId());
        holder.binding.tvEmployeeName.setText(attendance.getEmpName());
        holder.binding.tvStatus.setText(attendance.getStatus());
        if (attendance.getStatus().equals("P")){
            holder.binding.tvStatus.setTextColor(context.getResources().getColor(R.color.green_deep));
        }else {
            holder.binding.tvStatus.setTextColor(context.getResources().getColor(R.color.red));
        }

        holder.binding.tvShift.setText(attendance.getShift());
        holder.binding.tvWord.setText(String.valueOf(attendance.getWordNo()));


    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemViewDailyAttendanceReportBinding binding;

        MyViewHolder(@NonNull ItemViewDailyAttendanceReportBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

        }

    }


}
