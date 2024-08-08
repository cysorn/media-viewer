package media_viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
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


    	    List<String> imageFormats = Arrays.asList(".png", ".jpg", ".jpeg", ".gif");
    	    List<String> videoFormats = Arrays.asList(".mp4", ".ogg", ".webm");
    	    List<String> tags = sql.getTags().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());

    	    model.addAttribute("mediaList", getUncategorizedFiles());
    	    model.addAttribute("imageFormats", imageFormats);
    	    model.addAttribute("videoFormats", videoFormats);
    	    model.addAttribute("tags", tags);

 
    	    return "index";
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
}