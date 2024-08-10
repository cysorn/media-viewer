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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonProperty;

@Controller
public class AppController {

	/*
	String mediaLocation = "/media_files/";
	String uncategorizedLocation = "/uncategorized/";
	String workingLocation = "E:/testing_media_viewer";
	String absoluteUncategorizedLocation = "E:/testing_media_viewer/uncategorized/";
	String absoluteMediaFilesLocation = "E:/testing_media_viewer/media_files/";
	*/
	
	
	
	
	@Autowired
	DbSetup dbSetup;

	String mediaLocation = "/media_files/";
	String uncategorizedLocation = "/uncategorized/";
	String workingLocation;
	String absoluteUncategorizedLocation;
	String absoluteMediaFilesLocation;

	
	List<String> mediaDivContent;
	AppController(){
		
	}
	
    @GetMapping("/")
    public String func(Model model) {

    	//This must be in the constructor
    	workingLocation = dbSetup.workingLocation;
    	absoluteUncategorizedLocation = dbSetup.absoluteUncategorizedLocation;
    	absoluteMediaFilesLocation = dbSetup.absoluteMediaFilesLocation;
    	mediaDivContent = getUncategorizedFiles();

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
    	    List<String> tags = dbSetup.sql.getTagsWithNoFamilyRelations().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            
            List<String> allTags = dbSetup.sql.getTags().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            
            showTags(model);
            model.addAttribute("mediaList", mediaDivContent);
    	    model.addAttribute("imageFormats", imageFormats);
    	    model.addAttribute("videoFormats", videoFormats);
    	    model.addAttribute("tags", tags);
    	    model.addAttribute("allTags", allTags);

 
    	    return "index";
    }
    
    private void showTags(Model model) {
    	
    	/*
        // Level 3 (deepest level) buttons
        List<TagItem> level3List1 = Arrays.asList(
            new TagItem("Button 1.1.1")
        );

        // Level 2 buttons with nested level 3
        List<TagItem> level2List1 = Arrays.asList(
            new TagItem("Button 1.1", level3List1),
            new TagItem("Button 1.2")
        );

        // Level 1 buttons with nested level 2
        List<TagItem> level1List = Arrays.asList(
            new TagItem("Button 1", level2List1),
            new TagItem("Button 2"),
            new TagItem("Button 3")
        );

        // Pass the top-level list to the model
        */
    	List<TagItem> tagItems = dbSetup.sql.getTagHierarchy();
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
        return fileNames;
    }

	private int getNextFreeFileName(){
		
        File mediaFiles = new File(absoluteMediaFilesLocation);
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
	
	private String moveFileToMediaFolderAndGetName(File file) throws IOException {
        // Check if file exists
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }

        // Extract the file extension
        int nextFreeFileName = getNextFreeFileName();
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
        Path movedFilePath = Paths.get(absoluteMediaFilesLocation, tempFileName);
        Files.move(tempFilePath, movedFilePath, StandardCopyOption.REPLACE_EXISTING);

        // Rename the file to the new name with the original extension
        String finalFileName = nextFreeFileName + fileExtension;
        Path finalFilePath = movedFilePath.resolveSibling(finalFileName);
        Files.move(movedFilePath, finalFilePath, StandardCopyOption.REPLACE_EXISTING);
        
        return finalFileName;
    }
	
	//ADD HANDLING FOR SECOND EXECUTION.
    @GetMapping("/hello")
    @ResponseBody
    public String sayHi() {
    	
    	//setupDb1
    	dbSetup.setupDb();
    	/*
    	List<String> lis = new ArrayList<>(List.of("general", "rofl", "nerd", "stupid"));
    	sql.extendTagsTableAndCreateFileTagsTablesIfNecessary(lis);
    
    	lis = new ArrayList<>(List.of("video", "picture"));
    	sql.asignChildTags("general", lis);
    	
    	lis = new ArrayList<>(List.of("lol", "kek"));
    	sql.addAliases("rofl", lis);
    	
    	lis = new ArrayList<>(List.of("nerd", "picture", "rofl"));
    	sql.addOrFindMediaFileAndAsignTagsToIt("1.jpg", lis);
        */
    	
        return "Hi";
    }
 
    @PostMapping("/sendTags")
    public ResponseEntity<String> handleTagsPostRequest(@RequestBody TagRequest tagRequest) {
        // Print the raw JSON data
    	
    	String receivedMediaFileName = tagRequest.getFileLocation();
    	//String uncatMediaFileName = receivedMediaFileName.substring(receivedMediaFileName.lastIndexOf('/'));
    	String uncatMediaFileName = workingLocation + receivedMediaFileName;

    	String newFileName;
    	try {
			newFileName = moveFileToMediaFolderAndGetName(new File(uncatMediaFileName));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    	
    	//System.out.println(tagRequest.getCurrentFileIndex());
    	//System.out.println(tagRequest.getSelectedTags());
    	//System.out.println(tagRequest.getFileLocation());
    	dbSetup.sql.addOrFindMediaFileAndAsignTagsToIt(newFileName, tagRequest.getSelectedTags());
        
        // Send a response
        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    @PostMapping("/sendSearchTags")
    public String handleSearchPostRequest(@RequestBody TagSearchRequest tagSearchRequest, Model model) {
        // Print the raw JSON data
    	//System.out.println(tagSearchRequest.getSelectedTags());
    	
    	List<String> res = tagSearchRequest.getSelectedTags();
    	List<String> files = dbSetup.sql.getFilesByTags(res)
    			.stream()
                .map(fName -> mediaLocation + fName)
                .collect(Collectors.toList());
    	
    	mediaDivContent = files;
    	/*
    	System.out.println(files);
    	model.addAttribute("mediaList", files);
    	System.out.println("Attribute: " + model.getAttribute("mediaList"));
       */
    	
        // Send a response
    	return "index";
    	//return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    
    /*
    
    
    @PostMapping("/sendTags")
    public ResponseEntity<String> handleTagsPostRequest(@RequestBody String json) {
        // Print the raw JSON data
        System.out.println(json);
        
        // Send a response
        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    @PostMapping("/sendSearchTags")
    public ResponseEntity<String> handleSearchPostRequest(@RequestBody String json) {
        // Print the raw JSON data
        System.out.println(json);
        
        // Send a response
        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
	*/
    // Static nested class
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
}