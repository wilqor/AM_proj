package pl.gda.pg.eti.kask.am.mobilefood.logic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.R;
import pl.gda.pg.eti.kask.am.mobilefood.model.WrappedTag;

/**
 * Created by Kuba on 2016-01-24.
 */
public class ObservedTagsAdapter extends ArrayAdapter<WrappedTag> {
    private final List<WrappedTag> wrappedTags;
    private final ObservedTagsActionHandler handler;

    public ObservedTagsAdapter(Context context, int textViewResourceId, List<WrappedTag> wrappedTags, ObservedTagsActionHandler handler) {
        super(context, textViewResourceId, wrappedTags);
        this.wrappedTags = wrappedTags;
        this.handler = handler;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.checkbox_list_item, null);
        }
        final WrappedTag tag = wrappedTags.get(position);
        if (tag != null) {
            TextView tagItemText = (TextView) view.findViewById(R.id.list_item_string);
            tagItemText.setText(tag.getName());
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.list_item_checkbox);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    tag.setSelected(isChecked);
                    handler.onObservedTagClick(checkedTagIds());
                }
            });
            checkBox.setChecked(tag.isSelected());
        }
        return view;
    }

    private List<Long> checkedTagIds() {
        List<Long> checked = new ArrayList<>();
        for (WrappedTag tag : wrappedTags) {
            if (tag.isSelected()) {
                checked.add(tag.getTag().getLocalId());
            }
        }
        return checked;
    }
}
