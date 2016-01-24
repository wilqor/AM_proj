package pl.gda.pg.eti.kask.am.mobilefood.logic;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.R;
import pl.gda.pg.eti.kask.am.mobilefood.model.Tag;

/**
 * Created by Kuba on 2016-01-24.
 */
public class ProductTagListAdapter extends ArrayAdapter<Tag> {
    private final List<Tag> productTags;
    private final ProductTagListActionHandler handler;

    public ProductTagListAdapter(Context context, int textViewResourceId, List<Tag> productTags, ProductTagListActionHandler handler) {
        super(context, textViewResourceId, productTags);
        this.productTags = productTags;
        this.handler = handler;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.deletable_list_item, null);
        }
        final Tag tag = productTags.get(position);
        if (tag != null) {
            TextView tagItemText = (TextView) view.findViewById(R.id.list_item_string);
            tagItemText.setText(tag.getName());
            ImageButton deleteBtn = (ImageButton) view.findViewById(R.id.delete_btn);
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.onProductTagDeleteClick(tag);
                }
            });
        }
        return view;
    }
}
