package media_viewer;

import java.io.File;
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

	String mediaLocation = "/media_files/";
	String uncategorizedLocation = "/uncategorized/";
	String absoluteUncategorizedLocation = "E:/testing_media_viewer/uncategorized/";
	
	@Autowired
	Sql sql;

	
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
    	    List<String> tags = sql.getTagsWithNoFamilyRelations().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            
            List<String> allTags = sql.getTags().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            
            showTags(model);
    	    model.addAttribute("mediaList", getUncategorizedFiles());
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
    	List<TagItem> tagItems = sql.getTagHierarchy();
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

	
	
	//ADD HANDLING FOR SECOND EXECUTION.
    @GetMapping("/hello")
    @ResponseBody
    public String sayHi() {
    	
    	/*
    	List<String> lis = new ArrayList<>(List.of("general", "rofl", "nerd", "stupid"));
    	sql.extendTagsTableAndCreateFileTagsTablesIfNecessary(lis);
    	lis = new ArrayList<>(List.of("video", "picture"));
    	sql.extendChildTags("general", lis);
    	lis = new ArrayList<>(List.of("lol", "kek"));
    	sql.addAliases("rofl", lis);
    	lis = new ArrayList<>(List.of("pic"));
    	sql.addAliases("picture", lis);
    	lis = new ArrayList<>(List.of("nerd", "picture", "rofl"));
    	sql.addOrFindMediaFileAndAssignTagsToIt("1.jpg", lis);
    	lis = new ArrayList<>(List.of("video", "stupid", "rofl"));
    	sql.addOrFindMediaFileAndAssignTagsToIt("2.mp4", lis);
    	lis = new ArrayList<>(List.of("picture", "rofl"));
    	sql.addOrFindMediaFileAndAssignTagsToIt("3.mp4", lis);
    	lis = new ArrayList<>(List.of("picture", "rofl"));
    	sql.addOrFindMediaFileAndAssignTagsToIt("4.jpg", lis);
        */
        return "Hi";
        
    }
    @PostMapping("/test")
    public ResponseEntity<String> handlePostRequest(@RequestBody String json) {
        // Print the raw JSON data
        System.out.println(json);
        
        // Send a response
        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }

    // Static nested class
    public static class YourDataModel {

        private String name;
        private String email;

        // Default constructor
        public YourDataModel() {
        }

        // Parameterized constructor
        public YourDataModel(String name, String email) {
            this.name = name;
            this.email = email;
        }

        // Getters and Setters
        @JsonProperty("name")
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @JsonProperty("email")
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return "YourDataModel{" +
                    "name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }
}