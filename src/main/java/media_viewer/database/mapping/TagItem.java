package media_viewer.database.mapping;

import java.util.ArrayList;
import java.util.List;

public class TagItem {
    private String name;
    private List<TagItem> subItems;

    public TagItem(String name) {
        this.name = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
        subItems = new ArrayList<TagItem>();
    }

    public TagItem(String name, List<TagItem> subItems) {
        this.name = Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase();
        this.subItems = subItems;
    }

    public String getName() {
        return name;
    }

    public List<TagItem> getSubItems() {
        return subItems;
    }
}