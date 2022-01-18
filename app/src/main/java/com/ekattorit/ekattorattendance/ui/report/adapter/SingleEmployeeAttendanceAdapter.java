package com.ekattorit.ekattorattendance.ui.report.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ItemViewSingleAttendanceBinding;
import com.ekattorit.ekattorattendance.ui.report.model.RpAttendance;
import com.ekattorit.ekattorattendance.utils.DateTimeFormat;

import java.text.ParseException;
import java.util.List;

public class SingleEmployeeAttendanceAdapter extends RecyclerView.Adapter<SingleEmployeeAttendanceAdapter.MyViewHolder> {
    private static final String TAG = "DailySalesAdapter";
    private Context context;
    private List<RpAttendance> attendanceList;


    public SingleEmployeeAttendanceAdapter(Context context, List<RpAttendance> attendanceList) {
        this.context = context;
        this.attendanceList = attendanceList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_single_attendance, parent, false);
        ItemViewSingleAttendanceBinding itemViewBinding = DataBindingUtil.bind(view);
        assert itemViewBinding != null;
        return new SingleEmployeeAttendanceAdapter.MyViewHolder(itemViewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        RpAttendance attendance = attendanceList.get(position);
        holder.binding.tvDate.setText(attendance.getXdate());
        holder.binding.tvStatus.setText(attendance.getStatus());
        if (attendance.getStatus().equals("P")){
            holder.binding.tvStatus.setTextColor(context.getResources().getColor(R.color.green_deep));
        }else {
            holder.binding.tvStatus.setTextColor(context.getResources().getColor(R.color.red));
        }

        String inTime = attendance.getFirstScan();
        String outTime = attendance.getLastScan();

        if(inTime !=null && outTime != null){
            try {
                holder.binding.tvFirstScan.setText(DateTimeFormat.timeFormat(inTime));
                holder.binding.tvLastScan.setText(DateTimeFormat.timeFormat(outTime));
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }else {
            holder.binding.tvFirstScan.setText("-");
            holder.binding.tvLastScan.setText("-");
        }




    }


    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemViewSingleAttendanceBinding binding;

        MyViewHolder(@NonNull ItemViewSingleAttendanceBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

        }

    }


}
