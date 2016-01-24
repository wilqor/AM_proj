package pl.gda.pg.eti.kask.am.mobilefood.model;

/**
 * Created by Kuba on 2016-01-24.
 */
public class WrappedTag {
    private final Tag tag;
    private boolean selected;

    public WrappedTag(Tag tag, boolean selected) {
        this.tag = tag;
        this.selected = selected;
    }

    public String getName() {
        return tag.getName();
    }

    public Tag getTag() {
        return tag;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "WrappedTag{" +
                "tag=" + tag +
                ", selected=" + selected +
                '}';
    }
}
