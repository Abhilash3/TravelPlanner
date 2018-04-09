package com.travelplanner.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.travelplanner.R;
import com.travelplanner.api.GoogleApis;
import com.travelplanner.common.Constants;
import com.travelplanner.common.Utils;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class AutoCompleteAdapter extends BaseAdapter implements Filterable {

    private final Context context;
    private List<String> resultList = Collections.emptyList();

    public AutoCompleteAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return resultList.size();
    }

    @Override
    public String getItem(int index) {
        return resultList.get(index);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = Objects.requireNonNull(inflater).inflate(R.layout.search_result, parent, false);
        }

        TextView view = convertView.findViewById(R.id.search_result_text);
        view.setText(getItem(position));

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(final CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() > 0) {
                    List<String> searchLocations = Collections.emptyList();
                    try {
                        searchLocations = GoogleApis.withLimit(5, TimeUnit.SECONDS).placesAutoComplete(constraint.toString());
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        e.printStackTrace();
                        Utils.toast(context, Constants.Message.AUTOCOMPLETE_FAILURE, Toast.LENGTH_LONG);
                    }

                    results.values = searchLocations;
                    results.count = searchLocations.size();
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.values != null) {
                    resultList = (List<String>) results.values;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
