package media_viewer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonProperty;

import media_viewer.ProgramSetup;

@Controller
public class AppController {
	
    private String mediaLocation;
    private String uncategorizedLocation;
    private String deletedLocation;
    private String workingLocation;
    private String absoluteUncategorizedLocation;
    private String absoluteMediaFilesLocation;
    private String absoluteDeletedFilesLocation;
    private List<String> mediaDivContent;
    private boolean mediaDivContainsPostRequest = false;
	
    private final ProgramSetup pSetup;

    @Autowired
    public AppController(ProgramSetup pSetup) {
        this.pSetup = pSetup;
        this.workingLocation = pSetup.getWokringLocation();
        this.absoluteUncategorizedLocation = pSetup.getAbsoluteUncategorizedLocation();
        this.absoluteMediaFilesLocation = pSetup.getAbsoluteMediaFilesLocation();
        this.absoluteDeletedFilesLocation = pSetup.getAbsoluteDeletedFilesLocation();
        this.mediaLocation = pSetup.getMediaLocation();
        this.uncategorizedLocation = pSetup.getUncategorizedLocation();
        this.deletedLocation = pSetup.getAbsoluteDeletedFilesLocation();

    }

	
    @GetMapping("/")
    public String func(Model model) {


    	List<String> imageFormats = Arrays.asList(
    		    ".png",   // Portable Network Graphics
    		    ".jpg",   // JPEG Image
    		    ".jpeg",  // JPEG Image
    		    ".gif",   // Graphics Interchange Format
    		    ".bmp",   // Bitmap Image (limited support in some browsers)
    		    ".tiff",  // Tagged Image File Format (limited support, mostly Safari)
    		    ".webp",  // WebP Image
    		    ".svg",   // Scalable Vector Graphics
    		    ".jfif"   // JPEG File Interchange Format (essentially a variant of JPEG)
    		);
    	
    	List<String> videoFormats = Arrays.asList(
    		    ".mp4",    // MPEG-4 Part 14
    		    ".ogg",    // Ogg Theora
    		    ".webm",   // WebM
    		    ".mov",    // QuickTime (supported in some browsers, especially Safari)
    		    ".m4v",    // MPEG-4 Video (similar to .mp4, but used mainly by Apple)
    		    ".avi",    // AVI (supported in some browsers with limited compatibility)
    		    ".3gp"     // 3GPP (supported in most modern browsers)
    		);
    	    List<String> tags = pSetup.sql.getTagsWithNoFamilyRelations().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            
            List<String> allTags = pSetup.sql.getTags().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            
            if(mediaDivContainsPostRequest == false)
            {
        	    mediaDivContent = getUncategorizedFiles();
            }

            showTags(model);
            model.addAttribute("mediaList", mediaDivContent);
    	    model.addAttribute("imageFormats", imageFormats);
    	    model.addAttribute("videoFormats", videoFormats);
    	    model.addAttribute("tags", tags);
    	    model.addAttribute("allTags", allTags);
    	    
    	    mediaDivContainsPostRequest = false;

    	    return "index";
    }
    
    private void showTags(Model model) {
    	
    	List<TagItem> tagItems = pSetup.sql.getTagHierarchy();
        model.addAttribute("items", tagItems);
    }
    
    
    
    private List<String> getUncategorizedFiles(){
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

	private int getNextFreeFileName(String folderToCheck){
		
        File mediaFiles = new File(folderToCheck);
        int highestFileName = 0;
        int fileNameNumber = 0;
        
        // Check if directory exists and is a directory
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
	
	private String moveFileFromUncategorizedAndGetName(File file, String targetLocation) throws IOException {
        // Check if file exists
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }

        // Extract the file extension
        int nextFreeFileName = getNextFreeFileName(targetLocation);
        String fileName = file.getName();
        String fileExtension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            fileExtension = fileName.substring(dotIndex); // Includes the dot
        }

        // Rename the file to 133713371337 with the original extension
        String tempFileName = "13371337" + fileExtension;
        Path tempFilePath = Paths.get(absoluteUncategorizedLocation, tempFileName);
        Files.move(file.toPath(), tempFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Move the renamed file to the new location
        Path movedFilePath = Paths.get(targetLocation, tempFileName);
        Files.move(tempFilePath, movedFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Rename the file to the new name with the original extension
        String finalFileName = nextFreeFileName + fileExtension;
        Path finalFilePath = movedFilePath.resolveSibling(finalFileName);
        Files.move(movedFilePath, finalFilePath, StandardCopyOption.REPLACE_EXISTING);
        
        return finalFileName;
    }
	
    @GetMapping("/setup")
    @ResponseBody
    public String setupProgram() {
    	
    	//setupDb
    	
    	StringBuilder output = pSetup.setupDb();
    	
    	output.append(pSetup.createProgramDirectories());
    	
        return output.toString().replace("\n", "<br/>");
    }
 
    @PostMapping("/sendTags")
    public ResponseEntity<String> handleTagsPostRequest(@RequestBody TagRequest tagRequest) {
        // Print the raw JSON data
    	
    	String receivedMediaFileName = tagRequest.getFileLocation();
    	//String uncatMediaFileName = receivedMediaFileName.substring(receivedMediaFileName.lastIndexOf('/'));
    	String uncatMediaFileName = workingLocation + receivedMediaFileName;

    	String newFileName;
    	try {
			newFileName = moveFileFromUncategorizedAndGetName(new File(uncatMediaFileName), absoluteMediaFilesLocation);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    	
    	//System.out.println(tagRequest.getCurrentFileIndex());
    	//System.out.println(tagRequest.getSelectedTags());
    	//System.out.println(tagRequest.getFileLocation());
    	pSetup.sql.addOrFindMediaFileAndAsignTagsToIt(newFileName, tagRequest.getSelectedTags());
        
        // Send a response
        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    @DeleteMapping("/deleteUncategorizedMedia")
    public ResponseEntity<String> handleUncategorizedDelete(@RequestBody UncategorizedDeleteRequect tagRequest) {
    	
    	String receivedMediaFileName = tagRequest.getFileLocation();
    	//String uncatMediaFileName = receivedMediaFileName.substring(receivedMediaFileName.lastIndexOf('/'));
    	String toDeleteMediaFileName = workingLocation + receivedMediaFileName;
    	
    	try {
			moveFileFromUncategorizedAndGetName(new File( toDeleteMediaFileName), absoluteDeletedFilesLocation);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

        // Send a response
        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    @DeleteMapping("/deleteMediaFromGalery")
    public ResponseEntity<String> handleGaleryDelete(@RequestBody TagRequest tagRequest) {

        // Send a response
        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    
    
    
    @PostMapping("/sendSearchTags")
    public String handleSearchPostRequest(@RequestBody TagSearchRequest tagSearchRequest, Model model) {
        // Print the raw JSON data
    	//System.out.println(tagSearchRequest.getSelectedTags());
    	
    	List<String> res = tagSearchRequest.getSelectedTags();
    	if (!res.get(0).isBlank())
    	{
    		List<String> files = pSetup.sql.getFilesByTags(res)
    				.stream()
    				.map(fName -> mediaLocation + fName)
    				.collect(Collectors.toList());
    		
            Collections.shuffle(files);
    		mediaDivContent = files;
    		mediaDivContainsPostRequest = true;
    	}
    	/*
    	System.out.println(files);
    	model.addAttribute("mediaList", files);
    	System.out.println("Attribute: " + model.getAttribute("mediaList"));
       */
    	
        // Send a response
    	return "index";
    	//return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    public static class TagRequest  {

        private List<String> selectedTags;
        private int currentFileIndex;
        private String fileLocation;

        // Default constructor
        public TagRequest () {
        }

        // Parameterized constructor
        public TagRequest (List<String> selectedTags, int currentFileIndex, String fileLocation) {
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
    
    // Static nested class
    public static class TagSearchRequest  {

    	private List<String> selectedTags;

        // Default constructor
        public TagSearchRequest () {
        }
        
        // Parameterized constructor
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
    
    
    public static class UncategorizedDeleteRequect{
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
}