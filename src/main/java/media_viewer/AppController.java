package media_viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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
    		    ".ico",   // Icon Image
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
    	    List<String> tags = sql.getTags().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            
            showTags(model);
    	    model.addAttribute("mediaList", getUncategorizedFiles());
    	    model.addAttribute("imageFormats", imageFormats);
    	    model.addAttribute("videoFormats", videoFormats);
    	    model.addAttribute("tags", tags);

 
    	    return "index";
    }
    
    private void showTags(Model model) {
        // Create example hierarchical data
        // Each level is a list of TagItem objects, where each TagItem can have its own subItems

        // Level 3 (deepest level) buttons
        List<TagItem> level3List1 = Arrays.asList(new TagItem("Button 1.1.1"));
        List<TagItem> level3List2 = Arrays.asList(); // Empty for other branches

        // Level 2 buttons
        List<TagItem> level2List1 = Arrays.asList(new TagItem("Button 1.1", level3List1), new TagItem("Button 1.2"));
        List<TagItem> level2List2 = Arrays.asList(new TagItem("Button 2.1", level3List2));

        // Level 1 buttons
        List<TagItem> level1List = Arrays.asList(
            new TagItem("Button 1", level2List1),
            new TagItem("Button 2", level2List2),
            new TagItem("Button 3")
        );

        // Hierarchies as a list of lists
        List<List<TagItem>> hierarchies = new ArrayList<>();
        hierarchies.add(level1List); // Top level
        hierarchies.add(level2List1); // Second level, if needed
        hierarchies.add(level3List1); // Third level, if needed

        model.addAttribute("hierarchies", hierarchies);
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
    
    public static class TagItem {
        private String name;
        private List<TagItem> subItems;

        public TagItem(String name) {
            this.name = name;
        }

        public TagItem(String name, List<TagItem> subItems) {
            this.name = name;
            this.subItems = subItems;
        }

        public String getName() {
            return name;
        }

        public List<TagItem> getSubItems() {
            return subItems;
        }
    }
}