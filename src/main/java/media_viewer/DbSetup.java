package media_viewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import media_viewer.Sql;

@Service
public class DbSetup {
	
	public String mediaLocation = "/media_files/";
	public String uncategorizedLocation = "/uncategorized/";
	public String workingLocation = "E:/testing_media_viewer";
	public String absoluteUncategorizedLocation = "E:/testing_media_viewer/uncategorized/";
	public String absoluteMediaFilesLocation = "E:/testing_media_viewer/media_files/";
	public String absoluteDeletedFilesLocation = "E:/testing_media_viewer/deleted/";
	
	@Autowired
	public Sql sql;
	
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