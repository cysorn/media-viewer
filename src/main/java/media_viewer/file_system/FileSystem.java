package media_viewer.file_system;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import media_viewer.config.CustomProperties;

@Component
public class FileSystem {
	
	private String workingLocation;
	private String mediaLocation;
	private String uncategorizedLocation;
	private String absoluteUncategorizedLocation;
	private String absoluteMediaFilesLocation;
	private String absoluteDeletedFilesLocation;
	
    @Autowired
    public FileSystem(CustomProperties customProperties) {
        this.workingLocation = customProperties.getWorkingLocationPath();
        mediaLocation = "/media_files/";
    	uncategorizedLocation = "/uncategorized/";
    	absoluteUncategorizedLocation = workingLocation + "/uncategorized/";
    	absoluteMediaFilesLocation = workingLocation + "/media_files/";
    	absoluteDeletedFilesLocation = workingLocation + "/deleted/";
    }
	
	public String createProgramDirectories() {
		StringBuilder output = new StringBuilder();
		output.append(ensurePathIsExisting(absoluteUncategorizedLocation));
		output.append(ensurePathIsExisting(absoluteMediaFilesLocation));
		output.append(ensurePathIsExisting(absoluteDeletedFilesLocation));
		
		return output.toString();
	}
	
	private String ensurePathIsExisting(String path) {
		Path folderPath = Paths.get(path);

        try {
            // Create the directory
            Files.createDirectories(folderPath);
            return path + " is prepared!\n";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to create " + path;
        }
	}

    public List<String> getUncategorizedFiles(){
   	    List<String> fileNames = new ArrayList<>();
        File uncategorized = new File(absoluteUncategorizedLocation);

        // Check if directory exists and is a directory
        if (uncategorized.exists() && uncategorized.isDirectory()) {
            File[] files = uncategorized.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) { // Only get files, not directories
                        fileNames.add(uncategorizedLocation + file.getName().toLowerCase());
                    }
                }
            }
        }
        Collections.shuffle(fileNames);
        return fileNames;
    }

	
	public String moveFileFromUncategorizedAndGetName(File file, String targetLocation) throws IOException {
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }

        // Extract the file extension
        int nextFreeFileName = getNextFreeFileName(targetLocation);
        String fileName = file.getName();
        String fileExtension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            fileExtension = fileName.substring(dotIndex);
        }

        String tempFileName = "13371337" + fileExtension;
        Path tempFilePath = Paths.get(absoluteUncategorizedLocation, tempFileName);
        Files.move(file.toPath(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);

        Path movedFilePath = Paths.get(targetLocation, tempFileName);
        Files.move(tempFilePath, movedFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Rename the file to the new name with the original extension
        String finalFileName = nextFreeFileName + fileExtension;
        Path finalFilePath = movedFilePath.resolveSibling(finalFileName);
        Files.move(movedFilePath, finalFilePath, StandardCopyOption.REPLACE_EXISTING);
        
        return finalFileName;
    }
	
	
	private int getNextFreeFileName(String folderToCheck){
		
		File mediaFiles = new File(folderToCheck);
		int highestFileName = 0;
		int fileNameNumber = 0;
		
		if (mediaFiles.exists() && mediaFiles.isDirectory()) {
			File[] files = mediaFiles.listFiles();
			if (files != null) {
				for (File file : files) {
					String fileName = file.getName();
					fileName = file.getName().substring(0, fileName.lastIndexOf('.')); // Includes the dot
					if (file.isFile()) { // Only get files, not directories
						fileNameNumber = Integer.parseInt(fileName);
						if(highestFileName < fileNameNumber)
						{
							highestFileName = fileNameNumber;
						}
					}
				}
			}
		}
		
		return highestFileName + 1;
	}
	
	
	public String getWorkingLocation() {
		return workingLocation;
	}

	public void setWokringLocation(String workingLocation) {
		this.workingLocation = workingLocation;
	}
	
	public String getMediaLocation() {
		return mediaLocation;
	}

	public void setMediaLocation(String mediaLocation) {
		this.mediaLocation = mediaLocation;
	}

	public String getUncategorizedLocation() {
		return uncategorizedLocation;
	}

	public void setUncategorizedLocation(String uncategorizedLocation) {
		this.uncategorizedLocation = uncategorizedLocation;
	}

	public String getAbsoluteUncategorizedLocation() {
		return absoluteUncategorizedLocation;
	}

	public void setAbsoluteUncategorizedLocation(String absoluteUncategorizedLocation) {
		this.absoluteUncategorizedLocation = absoluteUncategorizedLocation;
	}

	public String getAbsoluteMediaFilesLocation() {
		return absoluteMediaFilesLocation;
	}

	public void setAbsoluteMediaFilesLocation(String absoluteMediaFilesLocation) {
		this.absoluteMediaFilesLocation = absoluteMediaFilesLocation;
	}

	public String getAbsoluteDeletedFilesLocation() {
		return absoluteDeletedFilesLocation;
	}

	public void setAbsoluteDeletedFilesLocation(String absoluteDeletedFilesLocation) {
		this.absoluteDeletedFilesLocation = absoluteDeletedFilesLocation;
	}
}
