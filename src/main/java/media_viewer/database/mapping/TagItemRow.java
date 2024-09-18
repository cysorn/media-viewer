package media_viewer.database.mapping;

public class TagItemRow {
    private final int parentId;
    private final String parentTag;
    private final Integer childId;
    private final String childTag;

    public TagItemRow(int parentId, String parentTag, Integer childId, String childTag) {
        this.parentId = parentId;
        this.parentTag = parentTag;
        this.childId = childId;
        this.childTag = childTag;
    }

    public int getParentId() {
        return parentId;
    }

    public String getParentTag() {
        return parentTag;
    }

    public Integer getChildId() {
        return childId;
    }

    public String getChildTag() {
        return childTag;
    }
}