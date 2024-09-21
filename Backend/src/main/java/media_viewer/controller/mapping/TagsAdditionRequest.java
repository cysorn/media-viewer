package media_viewer.controller.mapping;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TagsAdditionRequest {
    private List<String> tags;

    //public TagsAdditionRequest() {
    //    this.tags = new ArrayList<String>();
    //}
    
    
    public TagsAdditionRequest(@JsonProperty("tags") List<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("tags")
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
		return "TO_STRING_FUNCTION";
    }
}
