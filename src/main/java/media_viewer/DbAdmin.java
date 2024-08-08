package media_viewer;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DbAdmin {
	
	@Autowired
	Sql sql;
	
	public void test() {
		
		
	}
	
	@Transactional
	public void addTagsAndAliases() {
		//sql.addTags(List.of("",""));
		
		sql.addAliases("", List.of("", ""));
	}
}
