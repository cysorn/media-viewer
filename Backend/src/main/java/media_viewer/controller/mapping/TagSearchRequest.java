package media_viewer.controller.mapping;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TagSearchRequest  {

	private List<String> selectedTags;

    public TagSearchRequest (List<String> selectedTags) {
        this.selectedTags = selectedTags;
    }

    // Getters and Setters
    
    @JsonProperty("selectedTags")
    public List<String> getSelectedTags() {
        return selectedTags;
    }

    public void setSelectedTags(List<String> selectedTags) {
        this.selectedTags = selectedTags;
    }

    @Override
    public String toString() {
		return "TO_STRING_FUNCTION";
    }
}