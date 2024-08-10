package media_viewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DbSetup1 {
	
	String mediaLocation = "/media_files/";
	String uncategorizedLocation = "/uncategorized/";
	String workingLocation = "E:/testing_media_viewer";
	String absoluteUncategorizedLocation = "E:/testing_media_viewer/uncategorized/";
	String absoluteMediaFilesLocation = "E:/testing_media_viewer/media_files/";
	
	@Autowired
	Sql sql;
	
	void setupDb1() {
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
	}
	
}