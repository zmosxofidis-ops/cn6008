package com.example.smartcityapp;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartcityapp.models.CityReport;

import java.util.List;
import java.util.Locale;

/**
 * Adapter για τη διαχείριση και εμφάνιση της λίστας αναφορών στο RecyclerView.
 * Συνδέει τα δεδομένα των αναφορών με το γραφικό περιβάλλον του ιστορικού.
 */
public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<CityReport> reports;

    public ReportAdapter(List<CityReport> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Δημιουργία της όψης για κάθε αντικείμενο της λίστας
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        CityReport report = reports.get(position);
        
        // Έλεγχος εγκυρότητας δεδομένων για την αποφυγή κρασαρίσματος
        String title = report.getTitle() != null ? report.getTitle() : "Αναφορά";
        String category = report.getCategory() != null ? report.getCategory() : "Άλλο";
        String desc = report.getDescription() != null ? report.getDescription() : "";

        // Αντιστοίχιση κειμένων στα γραφικά στοιχεία
        holder.tvTitle.setText(title);
        holder.tvCategory.setText(category.toUpperCase());
        holder.tvDescription.setText(desc);
        holder.tvLocation.setText(String.format(Locale.getDefault(), "%.4f, %.4f", report.getLatitude(), report.getLongitude()));

        // Καθορισμός χρώματος ένδειξης βάσει της κατηγορίας προβλήματος
        int color = Color.GRAY;
        if (category.contains("Οδοποιία")) color = Color.parseColor("#FF9800");
        else if (category.contains("Ηλεκτροφωτισμός")) color = Color.parseColor("#FFEB3B");
        else if (category.contains("Καθαριότητα")) color = Color.parseColor("#4CAF50");
        else if (category.contains("Πράσινο")) color = Color.parseColor("#8BC34A");
        else if (category.contains("Ύδρευση")) color = Color.parseColor("#2196F3");

        try {
            // Ενημέρωση του χρώματος του στρογγυλού indicator
            GradientDrawable bg = (GradientDrawable) holder.viewIndicator.getBackground();
            if (bg != null) bg.setColor(color);
        } catch (Exception e) {
            // Προστασία σε περίπτωση που το background drawable δεν είναι GradientDrawable
        }
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    /**
     * ViewHolder που κρατάει τις αναφορές στα γραφικά στοιχεία κάθε γραμμής.
     */
    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvDescription, tvLocation;
        View viewIndicator;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_report_title);
            tvCategory = itemView.findViewById(R.id.tv_report_category);
            tvDescription = itemView.findViewById(R.id.tv_report_description);
            tvLocation = itemView.findViewById(R.id.tv_report_location);
            viewIndicator = itemView.findViewById(R.id.view_category_indicator);
        }
    }
}
