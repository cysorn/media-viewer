package media_viewer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ProgramSetup {
	
	
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
    	System.out.println(customProperties.getWorkingLocationPath());
        this.workingLocation = customProperties.getWorkingLocationPath();
    }
    */
	
	@Autowired
	public Sql sql;
	
	public StringBuilder setupDb() {
		
		sql.createDbStructureIfNecessary();
		
    	List<String> lis = new ArrayList<>(List.of("animals", "ai", "video"));
    	sql.extendTagsTableAndCreateFileTagsTablesIfNecessary(lis);
    
    	lis = new ArrayList<>(List.of("cats", "dogs"));
    	sql.asignChildTags("animals", lis);
    	
    	lis = new ArrayList<>(List.of("gif"));
    	sql.addAliases("video", lis);
    	
    	//lis = new ArrayList<>(List.of("tag"));
    	//sql.addOrFindMediaFileAndAsignTagsToIt("1.jpg", lis);
    	
    	return new StringBuilder("DB is ready to work.\n");
	}
	
	public String createProgramDirectories() {
		StringBuilder output = new StringBuilder();
		output.append(ensurePathIsExisting(absoluteUncategorizedLocation));
		output.append(ensurePathIsExisting(absoluteMediaFilesLocation));
		output.append(ensurePathIsExisting(absoluteDeletedFilesLocation));
		
		return output.toString();
	}
	
	private String ensurePathIsExisting(String path) {
		Path folderPath = Paths.get(path);

        try {
            // Create the directory
            Files.createDirectories(folderPath);
            return path + " is prepared!\n";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed to create " + path;
        }
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