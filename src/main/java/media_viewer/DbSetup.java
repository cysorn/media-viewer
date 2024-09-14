package media_viewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class DbSetup {
	
	
	private String workingLocation = "E:/testing_media_viewer";
	
	private String mediaLocation = "/media_files/";
	private String uncategorizedLocation = "/uncategorized/";
	private String absoluteUncategorizedLocation = workingLocation + "/uncategorized/";
	private String absoluteMediaFilesLocation = workingLocation + "/media_files/";
	private String absoluteDeletedFilesLocation = workingLocation + "/deleted/";
	
	
	//TODO
	/*
    @Autowired
    public DbSetup(CustomProperties customProperties) {
    	System.out.println(customProperties.getWorkinLocationPath());
        this.workingLocation = customProperties.getWorkinLocationPath();
    }
    */
	
	@Autowired
	public Sql sql;
	
	public void setupDb() {
		
		System.out.println("in setup db func");
		sql.createDbStructureIfNecessary();
		System.out.println("after setup db");
		
    	List<String> lis = new ArrayList<>(List.of("animals", "ai", "video"));
    	sql.extendTagsTableAndCreateFileTagsTablesIfNecessary(lis);
    
    	lis = new ArrayList<>(List.of("cats", "dogs"));
    	sql.asignChildTags("animals", lis);
    	
    	lis = new ArrayList<>(List.of("gif"));
    	sql.addAliases("video", lis);
    	
    	//lis = new ArrayList<>(List.of("tag"));
    	//sql.addOrFindMediaFileAndAsignTagsToIt("1.jpg", lis);
	}

	public String getWokringLocation() {
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