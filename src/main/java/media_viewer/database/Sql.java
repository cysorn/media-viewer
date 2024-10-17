package media_viewer.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import media_viewer.database.mapping.TagItem;
import media_viewer.database.mapping.TagItemRow;
import media_viewer.database.mapping.TagItemRowMapper;

@Component
public class Sql {
	
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    private Integer addOrFindMediaFileAndGetId(String mediaFileName) {
    	Integer existingId = null;
        try {
        	String checkSql = "SELECT id FROM `a_media_files` WHERE `fileName` = '" + mediaFileName + "'";
            existingId = jdbcTemplate.queryForObject(checkSql, Integer.class);
        } catch (EmptyResultDataAccessException e) {
        }

        if (existingId != null) {
            return existingId;
        } else {
            String insertSql = "INSERT INTO `a_media_files`(`fileName`) VALUES ('" + mediaFileName + "')";
            jdbcTemplate.update(insertSql);

            String lastIdSql = "SELECT LAST_INSERT_ID()";
            return jdbcTemplate.queryForObject(lastIdSql, Integer.class);
        }
    }
    

    //assumed that extendTagsTableAndCreateFileTagsTables is already executed
    @Transactional
    public void addOrFindMediaFileAndAsignTagsToIt(String mediaFileName, List<String> tags) {
        // Remove duplicates
        List<String> uniqueTags = new ArrayList<>(new HashSet<>(tags));
        
        int mediaFileId = addOrFindMediaFileAndGetId(mediaFileName);

        for (String tag : uniqueTags) {
            try {
                // insert the new tag association, ignoring if it already exists
                String insertSql = "INSERT IGNORE INTO `files_tagged_" + tag + "`(`mediaFile`) VALUES (" + mediaFileId + ");";
                jdbcTemplate.update(insertSql);
            } catch (DataIntegrityViolationException e) {
            }
        }
    }
    
    @Transactional
    public void asignChildTags(String parent, List<String> children) {
    	List<String> uniqueChildren = new ArrayList<>(new HashSet<>(children));
    	
    	extendTagsTableAndCreateFileTagsTablesIfNecessary(uniqueChildren);
    	
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT IGNORE INTO `media_viewer`.`a_child_tags` (`tag`, `childTag`) ")
           .append("SELECT (SELECT id FROM a_tags WHERE tag = '")
           .append(parent)
           .append("'), id FROM a_tags WHERE tag IN ('")
           .append(String.join("', '", uniqueChildren))
           .append("');");

        jdbcTemplate.update(sql.toString());
    }
    
    @Transactional
    public void extendTagsTableAndCreateFileTagsTablesIfNecessary(List<String> tags) {
        // Remove duplicates
        List<String> uniqueTags = new ArrayList<>(new HashSet<>(tags));

        // Check if any of the tags exist in the a_tag_aliases table
        String sqlAliasCheck = "SELECT alias FROM media_viewer.a_tag_aliases WHERE alias IN (:tags)";

        Map<String, Object> aliasParams = Map.of("tags", uniqueTags);

        List<String> existingAliases = namedParameterJdbcTemplate.queryForList(
            sqlAliasCheck, aliasParams, String.class);

        // If any tags are found in the a_tag_aliases table, throw an exception
        if (!existingAliases.isEmpty()) {
            throw new IllegalArgumentException("The following tags are already used as aliases: " + existingAliases);
        }

        
        // Construct the SQL query with a parameterized IN clause
        String sqlCheck = "SELECT tag FROM media_viewer.a_tags WHERE tag IN (:tags)";
        
        Map<String, Object> params = Map.of("tags", uniqueTags);

        List<String> existingTags = namedParameterJdbcTemplate.queryForList(
            sqlCheck, params, String.class);

        uniqueTags.removeAll(existingTags);

        for (String tag : uniqueTags) {
            String sql = "INSERT INTO `media_viewer`.`a_tags`(`tag`) VALUES ('" + tag + "');";
            jdbcTemplate.update(sql);
            create_files_tagged_table(tag);
        }
    }
    
    @Transactional
    public void addAliases(String tag, List<String> aliases) {
        List<String> uniqueAliases = new ArrayList<>(new HashSet<>(aliases));
        
        String sqlAliasCheck = "SELECT tag FROM a_tags WHERE tag IN (" 
                              + uniqueAliases.stream().map(alias -> "'" + alias + "'").collect(Collectors.joining(", ")) 
                              + ")";


        List<String> existingTags = jdbcTemplate.queryForList(sqlAliasCheck, String.class);
        if (!existingTags.isEmpty()) {
            throw new IllegalArgumentException("The following aliases are already used as tags: " + existingTags);
        }

        String sqlCheck = "SELECT alias FROM a_tag_aliases WHERE tag = (SELECT id FROM a_tags WHERE tag = '" 
                          + tag + "') AND alias IN (" 
                          + uniqueAliases.stream().map(alias -> "'" + alias + "'").collect(Collectors.joining(", ")) 
                          + ")";

        // Query the database for existing aliases
        List<String> existingAliases = jdbcTemplate.queryForList(sqlCheck, String.class);

        // Remove the existing aliases from the list of unique aliases
        uniqueAliases.removeAll(existingAliases);

        // Only proceed if there are aliases to insert
        if (!uniqueAliases.isEmpty()) {
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO a_tag_aliases (tag, alias) ");
            sql.append("SELECT tag_id.id, aliases.alias ");
            sql.append("FROM (SELECT id FROM a_tags WHERE tag = '").append(tag).append("') AS tag_id ");
            sql.append("JOIN (");

            for (int i = 0; i < uniqueAliases.size(); i++) {
                sql.append("SELECT '").append(uniqueAliases.get(i)).append("' AS alias");
                if (i < uniqueAliases.size() - 1) {
                    sql.append(" UNION ALL ");
                }
            }

            sql.append(") AS aliases;");

            jdbcTemplate.update(sql.toString());
        }
    }
    
    public List<String> getTags() {
    	String sql = "SELECT tag FROM `a_tags`";
    	return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("tag"));
    }
    
    public List<String> getAllParentTags(){
    	String sql = "SELECT DISTINCT t.tag FROM `a_tags` t LEFT JOIN `a_child_tags` ct ON t.id = ct.tag WHERE ct.tag IS NOT NULL;";
    	return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("tag")); 
    }
    
    public List<String> giveTagsWithNoParents(){
    	String sql = "SELECT DISTINCT t.tag FROM `a_tags` t LEFT JOIN `a_child_tags` ct ON t.id = ct.childTag WHERE ct.childTag IS NULL;";
    	return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("tag"));
    }
    
    public List<String> getTagsWithNoFamilyRelations(){
    	String sql = "SELECT t.tag FROM `a_tags` t WHERE t.id NOT IN (SELECT tag FROM `a_child_tags`) AND t.id NOT IN (SELECT childTag FROM `a_child_tags`);";
    	return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("tag")); 
    }
    
    
    public List<String> getFilesByTags(List<String> tags) {
        // Base SQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT fileName FROM a_media_files mf ");
        
        // Add JOINs for each tag
        for (String tag : tags) {
            // Sanitize the tag to ensure it's a valid SQL identifier
            String sanitizedTag = tag.replace("`", "``"); // Escape backticks
            
            sql.append("JOIN `files_tagged_")
               .append(sanitizedTag)
               .append("` ON mf.id = `files_tagged_")
               .append(sanitizedTag)
               .append("`.mediaFile ");
        }
        
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> rs.getString("fileName"));
    }
    
    
    private void create_files_tagged_table(String tag) {
    	StringBuilder sql = new StringBuilder();
    	sql.append("CREATE TABLE `media_viewer`.`files_tagged_" + tag + "` (");
    	sql.append("`id` INT NOT NULL AUTO_INCREMENT,");
    	sql.append("  `mediaFile` INT NOT NULL,");
    	sql.append("  PRIMARY KEY (`id`),");
    	sql.append("  UNIQUE (`mediaFile`),");
    	sql.append("  FOREIGN KEY (`mediaFile`) REFERENCES `a_media_files`(`id`) ON DELETE RESTRICT ON UPDATE RESTRICT");
    	sql.append(") ENGINE = InnoDB;");
    	jdbcTemplate.update(sql.toString());
    }


    public List<TagItem> getTagHierarchy() {
        // get all tags and child tags
        String query = "SELECT t1.id AS parentId, t1.tag AS parentTag, t2.id AS childId, t2.tag AS childTag " +
                       "FROM a_tags t1 " +
                       "LEFT JOIN a_child_tags ct ON t1.id = ct.tag " +
                       "LEFT JOIN a_tags t2 ON ct.childTag = t2.id " +
                       "WHERE ct.childTag IS NOT NULL " + 
                       "ORDER BY parentTag, childTag;";  // Only include tags that have children

        List<TagItemRow> rows = jdbcTemplate.query(query, new TagItemRowMapper()); 

        // Maps to store tags and their children
        Map<Integer, TagItem> tagMap = new HashMap<>();
        Map<Integer, List<TagItem>> childMap = new HashMap<>();

        // Populate the maps
        for (TagItemRow row : rows) {
            int parentId = row.getParentId();
            String parentTag = row.getParentTag();
            Integer childId = row.getChildId();
            String childTag = row.getChildTag();

            
            tagMap.putIfAbsent(parentId, new TagItem(parentTag));
            
            if (childId != null) {
                tagMap.putIfAbsent(childId, new TagItem(childTag));
                childMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(tagMap.get(childId));
            }
        }

        // Build the hierarchical structure
        List<TagItem> result = new ArrayList<>();
        for (Map.Entry<Integer, List<TagItem>> entry : childMap.entrySet()) {
            TagItem parent = tagMap.get(entry.getKey());
            if (parent != null) {
                parent.getSubItems().addAll(entry.getValue());
                result.add(parent);  // Only add parent tags that have children
            }
        }


        result = result.stream()
                .filter(tagItem -> {
                    // Capitalize the first letter of each string in the output of giveTagsWithNoParents()
                    List<String> capitalizedTags = giveTagsWithNoParents().stream()
                         .map(tag -> Character.toUpperCase(tag.charAt(0)) + tag.substring(1))
                         .collect(Collectors.toList());
                    // Check if the capitalized name of the TagItem is in the list
                    return capitalizedTags.contains(tagItem.getName());
                })
                .collect(Collectors.toList());
        
        return result;
    }
	
    //setup db
	@Transactional
	public void createDbStructureIfNecessary() {
		//FIXME
		String sqlCreateDatabase = "CREATE DATABASE IF NOT EXISTS media_viewer";
		jdbcTemplate.update(sqlCreateDatabase);
			    	
		String sqlCreateAMediaFiles = "CREATE TABLE IF NOT EXISTS media_viewer.a_media_files (id INT NOT NULL AUTO_INCREMENT , fileName TEXT NOT NULL , PRIMARY KEY (id)) ENGINE = InnoDB;";
					
		String sqlCreateATags = "CREATE TABLE IF NOT EXISTS media_viewer.a_tags (id INT NOT NULL AUTO_INCREMENT , tag TEXT NOT NULL , PRIMARY KEY (id), UNIQUE (tag)) ENGINE = InnoDB;";
					
		StringBuilder sqlCreateATagAliases = new StringBuilder();
		sqlCreateATagAliases.append("CREATE TABLE IF NOT EXISTS media_viewer.a_tag_aliases");
		sqlCreateATagAliases.append("(id INT NOT NULL AUTO_INCREMENT, tag INT NOT NULL, alias TEXT NOT NULL,");
		sqlCreateATagAliases.append("PRIMARY KEY (id), UNIQUE (alias), FOREIGN KEY (tag)");
		sqlCreateATagAliases.append("REFERENCES a_tags(id) ON DELETE RESTRICT ON UPDATE RESTRICT) ENGINE = InnoDB;");
					
		StringBuilder sqlCreateAChildTags = new StringBuilder();
							
		sqlCreateAChildTags.append("CREATE TABLE IF NOT EXISTS media_viewer.a_child_tags (");
		sqlCreateAChildTags.append("id INT NOT NULL AUTO_INCREMENT,");
		sqlCreateAChildTags.append(" tag INT NOT NULL,");
		sqlCreateAChildTags.append(" childTag INT NOT NULL,");
		sqlCreateAChildTags.append(" PRIMARY KEY (id),");
		sqlCreateAChildTags.append(" UNIQUE KEY unique_tag_childTag (tag, childTag),");
		sqlCreateAChildTags.append(" CONSTRAINT fk_tag FOREIGN KEY (tag) REFERENCES a_tags(id) ON DELETE RESTRICT ON UPDATE RESTRICT,");
		sqlCreateAChildTags.append(" CONSTRAINT fk_childTag FOREIGN KEY (childTag) REFERENCES a_tags(id) ON DELETE RESTRICT ON UPDATE RESTRICT");
		sqlCreateAChildTags.append(") ENGINE=InnoDB;");
					
		jdbcTemplate.update(sqlCreateAMediaFiles);
		jdbcTemplate.update(sqlCreateATags);
		jdbcTemplate.update(sqlCreateATagAliases.toString());
		jdbcTemplate.update(sqlCreateAChildTags.toString());
	}
}
