package pl.gda.pg.eti.kask.am.mobilefood.logic;

import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.model.WrappedTag;

/**
 * Created by Kuba on 2016-01-24.
 */
public interface ObservedTagsActionHandler {
    void onObservedTagClick(List<Long> selectedTags);
}
