package com.mobiledev.androidstudio.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mobiledev.androidstudio.R;
import com.mobiledev.androidstudio.models.Template;

import java.util.List;

public class TemplateAdapter extends RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder> {
    
    private final Context context;
    private final List<Template> templates;
    private int selectedPosition = -1;
    private final OnTemplateSelectedListener listener;
    
    public interface OnTemplateSelectedListener {
        void onTemplateSelected(Template template);
    }
    
    public TemplateAdapter(Context context, List<Template> templates, OnTemplateSelectedListener listener) {
        this.context = context;
        this.templates = templates;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TemplateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_template, parent, false);
        return new TemplateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TemplateViewHolder holder, int position) {
        Template template = templates.get(position);
        
        holder.tvTemplateName.setText(template.getName());
        holder.tvTemplateDescription.setText(template.getDescription());
        holder.rbTemplate.setChecked(position == selectedPosition);
        
        View.OnClickListener clickListener = v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            
            if (listener != null) {
                listener.onTemplateSelected(template);
            }
        };
        
        holder.itemView.setOnClickListener(clickListener);
        holder.rbTemplate.setOnClickListener(clickListener);
    }

    @Override
    public int getItemCount() {
        return templates.size();
    }
    
    public Template getSelectedTemplate() {
        if (selectedPosition >= 0 && selectedPosition < templates.size()) {
            return templates.get(selectedPosition);
        }
        return null;
    }
    
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
        notifyDataSetChanged();
    }
    
    static class TemplateViewHolder extends RecyclerView.ViewHolder {
        RadioButton rbTemplate;
        TextView tvTemplateName;
        TextView tvTemplateDescription;
        
        TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            rbTemplate = itemView.findViewById(R.id.rbTemplate);
            tvTemplateName = itemView.findViewById(R.id.tvTemplateName);
            tvTemplateDescription = itemView.findViewById(R.id.tvTemplateDescription);
        }
    }
}