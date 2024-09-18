package media_viewer;

import java.io.File;
import java.io.IOException;
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

import media_viewer.database.Sql;
import media_viewer.file_system.FileSystem;


@Controller
public class AppController {
	
	@Autowired
	private Sql sql;
	
	@Autowired
	private FileSystem fileSystem;
	
    private List<String> mediaDivContent;
    private boolean mediaDivContainsPostRequest = false;
	
    private final ProgramSetup pSetup;
    
    private List<String> imageFormats;
    private List<String> videoFormats;

    @Autowired
    public AppController(ProgramSetup pSetup) {

        this.pSetup = pSetup;
        
    	imageFormats = Arrays.asList(
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
    	
    	videoFormats = Arrays.asList(
    		    ".mp4",    // MPEG-4 Part 14
    		    ".ogg",    // Ogg Theora
    		    ".webm",   // WebM
    		    ".mov",    // QuickTime (supported in some browsers, especially Safari)
    		    ".m4v",    // MPEG-4 Video (similar to .mp4, but used mainly by Apple)
    		    ".avi",    // AVI (supported in some browsers with limited compatibility)
    		    ".3gp"     // 3GPP (supported in most modern browsers)
    		);
    }
	
    @GetMapping("/")
    public String func(Model model) {

    	    List<String> tags = sql.getTagsWithNoFamilyRelations().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            
            List<String> allTags = sql.getTags().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            if(mediaDivContainsPostRequest == false)
            {
        	    mediaDivContent = fileSystem.getUncategorizedFiles();
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
    	
    	List<TagItem> tagItems = sql.getTagHierarchy();
        model.addAttribute("items", tagItems);
    }
	
    @GetMapping("/setup")
    @ResponseBody
    public String setupProgram() {
    	
    	//setupDb
    	
    	StringBuilder output = pSetup.setupDb();
    	
    	output.append(fileSystem.createProgramDirectories());
    	
        return output.toString().replace("\n", "<br/>");
    }
 
    @PostMapping("/sendTags")
    public ResponseEntity<String> handleTagsPostRequest(@RequestBody TagRequest tagRequest) {
        // Print the raw JSON data
    	
    	String receivedMediaFileName = tagRequest.getFileLocation();
    	//String uncatMediaFileName = receivedMediaFileName.substring(receivedMediaFileName.lastIndexOf('/'));
    	String uncatMediaFileName = fileSystem.getWorkingLocation() + receivedMediaFileName;

    	String newFileName;
    	try {
			newFileName = fileSystem.moveFileFromUncategorizedAndGetName(new File(uncatMediaFileName), fileSystem.getAbsoluteMediaFilesLocation());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    	
    	sql.addOrFindMediaFileAndAsignTagsToIt(newFileName, tagRequest.getSelectedTags());
        
        // Send a response
        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    @DeleteMapping("/deleteUncategorizedMedia")
    public ResponseEntity<String> handleUncategorizedDelete(@RequestBody UncategorizedDeleteRequect tagRequest) {
    	
    	String receivedMediaFileName = tagRequest.getFileLocation();
    	//String uncatMediaFileName = receivedMediaFileName.substring(receivedMediaFileName.lastIndexOf('/'));
    	String toDeleteMediaFileName = fileSystem.getWorkingLocation() + receivedMediaFileName;
    	
    	try {
    		fileSystem.moveFileFromUncategorizedAndGetName(new File( toDeleteMediaFileName), fileSystem.getAbsoluteDeletedFilesLocation());
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
    		List<String> files = sql.getFilesByTags(res)
    				.stream()
    				.map(fName -> fileSystem.getMediaLocation() + fName)
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