package media_viewer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import media_viewer.database.Sql;


@Service
public class ProgramSetup {

	@Autowired
	private Sql sql;
	
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
}