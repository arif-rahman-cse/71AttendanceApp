package com.ekattorit.ekattorattendance.ui.home.adapter;

import android.content.Context;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ItemViewRecentScanBinding;
import com.ekattorit.ekattorattendance.ui.home.model.ScanItem;
import com.ekattorit.ekattorattendance.utils.DateTimeFormat;

import java.text.ParseException;
import java.util.List;

public class RecentScanAdapter extends RecyclerView.Adapter<RecentScanAdapter.MyViewHolder> {
    private static final String TAG = "DailySalesAdapter";
    private Context context;
    private List<ScanItem> scanItemList;


    public RecentScanAdapter(Context context, List<ScanItem> scanItemList) {
        this.context = context;
        this.scanItemList = scanItemList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_view_recent_scan, parent, false);
        ItemViewRecentScanBinding itemViewBinding = DataBindingUtil.bind(view);
        assert itemViewBinding != null;
        return new RecentScanAdapter.MyViewHolder(itemViewBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ScanItem scanItem = scanItemList.get(position);
        holder.binding.tvCardNo.setText(scanItem.getEmployee().getEmpId());
        holder.binding.tvEmployeeName.setText(scanItem.getEmployee().getEmpName());

        String inTime = scanItem.getFirstScan();
        String outTime = scanItem.getLastScan();

        if (scanItem.isScanStatus()) {
            holder.binding.ivStatus.setImageResource(R.drawable.ic_verified);
        } else {
            holder.binding.ivStatus.setImageResource(R.drawable.ic_round_warning);
        }

        if (inTime != null && outTime != null) {

            try {
                holder.binding.tvFirstScan.setText(DateTimeFormat.timeFormat(inTime));

                if (inTime.substring(0, 5).equals(outTime.substring(0, 5))) {
                    holder.binding.mainView.setBackgroundColor(context.getResources().getColor(R.color.yellow_light));
                    holder.binding.tvLastScan.setText("-");

                } else {
                    holder.binding.mainView.setBackgroundColor(context.getResources().getColor(R.color.white));
                    holder.binding.tvLastScan.setText(DateTimeFormat.timeFormat(outTime));
                }

            } catch (ParseException e) {
                e.printStackTrace();
            }


        } else {
            holder.binding.tvFirstScan.setText("-");
            holder.binding.tvLastScan.setText("-");
        }
    }


    @Override
    public int getItemCount() {
        return scanItemList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ItemViewRecentScanBinding binding;

        MyViewHolder(@NonNull ItemViewRecentScanBinding itemView) {
            super(itemView.getRoot());
            binding = itemView;

        }

    }


}
