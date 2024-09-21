package media_viewer.controller.mapping;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChildTagsAdditionRequest {
    private String tag;
    private List<String> childTags;

    public ChildTagsAdditionRequest() {
    }
    
    public ChildTagsAdditionRequest(@JsonProperty("tag")String tag, @JsonProperty("childTags")List<String> childTags) {
        this.tag = tag;
        this.childTags = childTags;
    }

    @JsonProperty("tag")
    public String getTag() {
        return tag;
    }
    
    public void setTag(String tag) {
        this.tag = tag;
    }

    @JsonProperty("childTags")
    public List<String> getChildTags() {
        return childTags;
    }
    
    public void setChildTags(List<String> childTags) {
        this.childTags = childTags;
    }

    @Override
    public String toString() {
		return "TO_STRING_FUNCTION";
    }
}
