package media_viewer.controller.mapping;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UncategorizedDeleteRequect{
    private int currentFileIndex;
    private String fileLocation;

    // Default constructor
    public UncategorizedDeleteRequect () {
    }

    // Parameterized constructor
    public UncategorizedDeleteRequect (int currentFileIndex, String fileLocation) {
        this.currentFileIndex = currentFileIndex;
        this.fileLocation = fileLocation;
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
		return fileLocation+ " " + Integer.toString(currentFileIndex);
    }
}