package edu.perphy.enger.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;

/**
 * Created by perphy on 2016/3/23 0023.
 * 内置词典的单词列表作为AutoCompleteTextView的数据源
 */
public class WordAutoCompleteArrayAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> fullList, mOriginalLists;
    private ArrayFilter mFilter;

    public WordAutoCompleteArrayAdapter(Context context, int resource, int textViewResourceId, ArrayList<String> words) {
        super(context, resource, textViewResourceId, words);

        fullList = words;
        mOriginalLists = new ArrayList<>(fullList);
    }

    @Override
    public String getItem(int position) {
        return fullList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    @Override
    public int getCount() {
        return fullList.size();
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mOriginalLists == null) {
                mOriginalLists = new ArrayList<>(fullList);
            }

            if (TextUtils.isEmpty(prefix)) {
                ArrayList<String> list = new ArrayList<>(mOriginalLists);
                results.values = list;
                results.count = list.size();
            } else {
                final String prefixString = prefix.toString().toLowerCase();
                ArrayList<String> list = mOriginalLists;
                int count = list.size();

                ArrayList<String> newList = new ArrayList<>(count);

                for (int i = 0; i < count; ++i) {
                    String item = list.get(i);
                    if (item.toLowerCase().startsWith(prefixString)) {
                        newList.add(item);
                    }
                }

                results.values = newList;
                results.count = newList.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results.values != null) {
                fullList = (ArrayList<String>) results.values;
            } else {
                fullList = new ArrayList<>();
            }
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
