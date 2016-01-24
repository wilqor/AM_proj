package pl.gda.pg.eti.kask.am.mobilefood.logic;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pl.gda.pg.eti.kask.am.mobilefood.R;
import pl.gda.pg.eti.kask.am.mobilefood.db.DBGetter;
import pl.gda.pg.eti.kask.am.mobilefood.db.MySQLiteHelper;
import pl.gda.pg.eti.kask.am.mobilefood.db.TagsDataSource;
import pl.gda.pg.eti.kask.am.mobilefood.model.Tag;
import pl.gda.pg.eti.kask.am.mobilefood.model.WrappedTag;

public class ObservedTagsActivity extends AppCompatActivity implements ObservedTagsActionHandler {
    private static final String TAG = "ObservedActivity";
    private TagsDataSource tagsDataSource;
    private MySQLiteHelper helper;
    private Set<String> observedTagIds;
    private ObservedTagsAdapter observedTagsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observed_tags);
    }

    @Override
    protected void onResume() {
        super.onResume();

        initializeObservedTags();
        prepareDataSource();
        populateObservedTagsListView();
    }

    private void populateObservedTagsListView() {
        List<Tag> allTags = tagsDataSource.getAllTags();
        List<WrappedTag> wrappedTags = wrapTags(allTags);
        Log.d(TAG, "Populating observed tags with: " + wrappedTags);
        ListView listView = (ListView) findViewById(R.id.observed_tags_view);
        observedTagsAdapter = new ObservedTagsAdapter(this, R.layout.checkbox_list_item, wrappedTags, this);
        listView.setAdapter(observedTagsAdapter);
    }

    @NonNull
    private List<WrappedTag> wrapTags(List<Tag> allTags) {
        List<WrappedTag> wrappedTags = new ArrayList<>();
        for (Tag tag : allTags) {
            boolean selected = false;
            if (observedTagIds.contains(String.valueOf(tag.getLocalId()))) {
                selected = true;
            }
            wrappedTags.add(new WrappedTag(tag, selected));
        }
        return wrappedTags;
    }

    private void prepareDataSource() {
        DBGetter dbGetter = new DBGetter(this);
        helper = dbGetter.getHelper();
        SQLiteDatabase db = dbGetter.getDb();
        tagsDataSource = new TagsDataSource(db);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (helper != null) {
            helper.close();
        }
    }

    private void initializeObservedTags() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        observedTagIds = new HashSet<>(pref.getStringSet(Consts.SHARED_PREF_OBSERVED_TAG_IDS_KEY, new HashSet<String>()));
        Log.d(TAG, "Initialized with observed tag ids: " + observedTagIds);
    }

    @Override
    public void onObservedTagClick(List<Long> selectedTags) {
        Log.d(TAG, "Received selection change with: " + selectedTags);
        saveSelectedTagsInPreferences(selectedTagsAsStringSet(selectedTags));
    }

    private Set<String> selectedTagsAsStringSet(List<Long> selectedTags) {
        Set<String> tagsSet = new HashSet<>();
        for (Long tag : selectedTags) {
            tagsSet.add(String.valueOf(tag));
        }
        return tagsSet;
    }

    private void saveSelectedTagsInPreferences(Set<String> selectedTags) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Consts.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putStringSet(Consts.SHARED_PREF_OBSERVED_TAG_IDS_KEY, selectedTags);
        editor.commit();
        Log.d(TAG, "Saved observed tag ids: " + selectedTags);
    }
}
