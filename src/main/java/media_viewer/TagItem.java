package media_viewer;

import java.util.ArrayList;
import java.util.List;

public class TagItem {
    private String name;
    private List<TagItem> subItems;

    public TagItem(String name) {
        this.name = name;
        subItems = new ArrayList<TagItem>();
    }

    public TagItem(String name, List<TagItem> subItems) {
        this.name = name;
        this.subItems = subItems;
    }

    public String getName() {
        return name;
    }

    public List<TagItem> getSubItems() {
        return subItems;
    }
}