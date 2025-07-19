package com.fuel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ExportFieldAdapter extends RecyclerView.Adapter<ExportFieldAdapter.ViewHolder> {

    private List<ExportField> exportFieldList;

    public ExportFieldAdapter(List<ExportField> exportFieldList) {
        this.exportFieldList = exportFieldList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_export_field, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExportField field = exportFieldList.get(position);
        //holder.textViewFieldName.setText(field.getFieldName());
        holder.textViewFieldName.setText(field.getDisplayName());
        // ERROR in holder.textView.setText(field.getDisplayName());

        holder.checkBoxEnable.setChecked(field.isEnabled());
        holder.checkBoxEnable.setOnCheckedChangeListener((buttonView, isChecked) -> field.setEnabled(isChecked));
    }

    @Override
    public int getItemCount() {
        return exportFieldList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBoxEnable;
        TextView textViewFieldName;
        //TextView textView;
        ImageView imageViewDragHandle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxEnable = itemView.findViewById(R.id.checkBoxEnable);
            textViewFieldName = itemView.findViewById(R.id.textViewFieldName);
            //textView = itemView.findViewById(R.id.textViewTranslatedName); // ← potrebbe avere ID diverso
            imageViewDragHandle = itemView.findViewById(R.id.imageViewDragHandle);
        }
    }
}
