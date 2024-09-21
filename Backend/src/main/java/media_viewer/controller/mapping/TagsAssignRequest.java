package media_viewer.controller.mapping;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TagsAssignRequest  {

    private List<String> selectedTags;
    private int currentFileIndex;
    private String fileLocation;

    public TagsAssignRequest (List<String> selectedTags, int currentFileIndex, String fileLocation) {
        this.selectedTags = selectedTags;
        this.currentFileIndex = currentFileIndex;
        this.fileLocation = fileLocation;
    }

    // Getters and Setters
    @JsonProperty("selectedTags")
    public List<String> getSelectedTags() {
        return selectedTags;
    }

    public void setSelectedTags(List<String> selectedTags) {
        this.selectedTags = selectedTags;
    }

    @JsonProperty("currentFileIndex")
    public int getCurrentFileIndex() {
        return currentFileIndex;
    }

    public void setCurrentFileIndex(int currentFileIndex) {
        this.currentFileIndex = currentFileIndex;
    }
    
    @JsonProperty("fileLocation")
    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    @Override
    public String toString() {
		return "TO_STRING_FUNCTION";
    }
}