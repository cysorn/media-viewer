package media_viewer.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import media_viewer.ProgramSetup;
import media_viewer.controller.mapping.TagsAssignRequest;
import media_viewer.controller.mapping.ChildTagsAdditionRequest;
import media_viewer.controller.mapping.TagSearchRequest;
import media_viewer.controller.mapping.TagsAdditionRequest;
import media_viewer.controller.mapping.UncategorizedDeleteRequest;
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
    
    //FIXME
    @GetMapping("/error")
    @ResponseBody
    public String errorFunc() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Troubleshooting:\n");
    	sb.append("1. Open application.properties and ensure that you specified custom.workingLocationPath.\n");
    	sb.append("2. Make sure your database is running and that the credentials are correct.\n");
    	sb.append("3. Open localhost:8080/setup; it should display a message indicating that everything is set up.\n");
    	sb.append("4. Try to open localhost:8080 again\n");
    	
    	return prepareStringToDisplayOnPage(sb.toString());
    }
    
	
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> func(Model model) {

    	    List<String> tags = sql.getTagsWithNoFamilyRelations().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(tags);
            
            
            List<String> allTags = sql.getTags().stream()
                    .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1).toLowerCase())
                    .collect(Collectors.toList());
            Collections.sort(allTags);
            
            if(mediaDivContainsPostRequest == false)
            {
        	    mediaDivContent = fileSystem.getUncategorizedFiles();
            }
            
    	 // Create response map
            Map<String, Object> response = new HashMap<>();
            response.put("mediaList", mediaDivContent);
            response.put("imageFormats", imageFormats);
            response.put("videoFormats", videoFormats);
            response.put("tags", tags);
            response.put("allTags", allTags);

            mediaDivContainsPostRequest = false;

            return new ResponseEntity<>(response, HttpStatus.OK);
            
    	    //return "index";
    }
	
    @GetMapping("/setup")
    @ResponseBody
    public String setupProgram() {
    	
    	StringBuilder output = pSetup.setupDb();
    	
    	output.append(fileSystem.createProgramDirectories());
    	
        return prepareStringToDisplayOnPage(output.toString());
    }
    
    private String prepareStringToDisplayOnPage(String str) {
    	return str.replace("\n", "<br/>");
    }
    
    
    @PostMapping("/sendTags")
    public ResponseEntity<String> handleTagsPostRequest(@RequestBody TagsAssignRequest tagRequest) {
    	
    	String receivedMediaFileName = tagRequest.getFileLocation();
    	String uncatMediaFileName = fileSystem.getWorkingLocation() + receivedMediaFileName;

    	String newFileName;
    	try {
			newFileName = fileSystem.moveFileFromUncategorizedAndGetName(new File(uncatMediaFileName), fileSystem.getAbsoluteMediaFilesLocation());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    	
    	sql.addOrFindMediaFileAndAsignTagsToIt(newFileName, tagRequest.getSelectedTags());

        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    @DeleteMapping("/deleteUncategorizedMedia")
    public ResponseEntity<String> handleUncategorizedDelete(@RequestBody UncategorizedDeleteRequest tagRequest) {
    	
    	String receivedMediaFileName = tagRequest.getFileLocation();
    	String toDeleteMediaFileName = fileSystem.getWorkingLocation() + receivedMediaFileName;
    	
    	try {
    		fileSystem.moveFileFromUncategorizedAndGetName(new File( toDeleteMediaFileName), fileSystem.getAbsoluteDeletedFilesLocation());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    	
        return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    @DeleteMapping("/deleteMediaFromGallery")
    public ResponseEntity<String> handleGaleryDelete(@RequestBody TagsAssignRequest tagRequest) {

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
    	
    	return "index";
    	//return new ResponseEntity<>("JSON received successfully", HttpStatus.OK);
    }
    
    @PostMapping("/addTags")
    public ResponseEntity<String> addTags(@RequestBody TagsAdditionRequest tagsAdditionRequest){
    	
    	sql.extendTagsTableAndCreateFileTagsTablesIfNecessary(tagsAdditionRequest.getTags());
    	
    	return new ResponseEntity<>("Tags added succesfully.", HttpStatus.OK);
    }
    
    @PostMapping("/addChildTags")
    public ResponseEntity<String> addChildTags(@RequestBody ChildTagsAdditionRequest childTagsAdditionRequest){
    	
    	sql.asignChildTags(childTagsAdditionRequest.getTag(), childTagsAdditionRequest.getChildTags());
    	
    	return new ResponseEntity<>("Tags added succesfully.", HttpStatus.OK);
    }
    
    @DeleteMapping("/deleteTags")
    public ResponseEntity<String> deleteTags(@RequestBody TagsAdditionRequest tagsToDelete){
    	
    	return new ResponseEntity<>("Tags deleted succesfully.", HttpStatus.OK);
    }
}