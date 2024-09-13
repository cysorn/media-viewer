package media_viewer;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import media_viewer.TagItem;


@Service
public class Sql {
	
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /*
    public void addMediaFile(String mediaFileName) {
        String sql = "INSERT INTO `media_files`(`" + mediaFileName + "`) VALUES ('')";
        int result = jdbcTemplate.queryForObject(sql, Integer.class);
        System.out.println("Database connection test result: " + result);
    }
    */
    
    //Done after POST request
    private Integer addOrFindMediaFileAndGetId(String mediaFileName) {
        // Check if the mediaFileName already exists
    	Integer existingId = null;
        try {
        	String checkSql = "SELECT id FROM `a_media_files` WHERE `fileName` = '" + mediaFileName + "'";
            existingId = jdbcTemplate.queryForObject(checkSql, Integer.class);
        } catch (EmptyResultDataAccessException e) {
            // Handle the case where no existing ID is found
            // This is expected when the fileName is not yet in the database
        }

        if (existingId != null) {
            // Return the existing ID if the media file is already in the database
            return existingId;
        } else {
            // Insert the new media file and return the new ID
            String insertSql = "INSERT INTO `a_media_files`(`fileName`) VALUES ('" + mediaFileName + "')";
            jdbcTemplate.update(insertSql);

            String lastIdSql = "SELECT LAST_INSERT_ID()";
            return jdbcTemplate.queryForObject(lastIdSql, Integer.class);
        }
    }
    
    
    //Executed after POST request
    //THIS ASSUMES THAT extendTagsTableAndCreateFileTagsTables WAS ALREADY EXECUTED
    @Transactional
    public void addOrFindMediaFileAndAsignTagsToIt(String mediaFileName, List<String> tags) {
        // Remove duplicates from the list of tags
        List<String> uniqueTags = new ArrayList<>(new HashSet<>(tags));
        
        // Get the media file ID, adding it if necessary
        int mediaFileId = addOrFindMediaFileAndGetId(mediaFileName);

        // Iterate through each tag
        for (String tag : uniqueTags) {
            try {
                // Attempt to insert the new tag association, ignoring if it already exists
                String insertSql = "INSERT IGNORE INTO `files_tagged_" + tag + "`(`mediaFile`) VALUES (" + mediaFileId + ");";
                jdbcTemplate.update(insertSql);
            } catch (DataIntegrityViolationException e) {
                // Handle any integrity violations here, if necessary
            }
        }
    }
    
    
    
  //Executed by admin
    /*
    @Transactional
    public void addChildTags(String parent, List<String> children) {
    	String sql = "SELECT id FROM a_tags WHERE tag = " + parent + ";";
    	int parentTagId = jdbcTemplate.queryForObject(sql, Integer.class);
    	
    	StringBuilder sqlChildren = new StringBuilder();
    	for(String child: children) {
    		sqlChildren.append("INSERT INTO `a_child_tags`(`tag`, `childTag`) VALUES ('" + parentTagId + "','" + child + "');");
    	}
    	jdbcTemplate.update(sqlChildren.toString());
    }
    */
    
    //Executed by admin BY AI
    @Transactional
    public void asignChildTags(String parent, List<String> children) {
    	List<String> uniqueChildren = new ArrayList<>(new HashSet<>(children));
    	
    	extendTagsTableAndCreateFileTagsTablesIfNecessary(uniqueChildren);
    	
        // Build the SQL query
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT IGNORE INTO `media_viewer`.`a_child_tags` (`tag`, `childTag`) ")
           .append("SELECT (SELECT id FROM a_tags WHERE tag = '")
           .append(parent)
           .append("'), id FROM a_tags WHERE tag IN ('")
           .append(String.join("', '", uniqueChildren))
           .append("');");

        // Execute the final SQL query with a single jdbcTemplate call
        jdbcTemplate.update(sql.toString());
    }
    

    
    //Executed by admin
    @Transactional
    public void extendTagsTableAndCreateFileTagsTablesIfNecessary(List<String> tags) {
        // Remove duplicates from the input list by converting it to a Set and back to a List
        List<String> uniqueTags = new ArrayList<>(new HashSet<>(tags));

     // Check if any of the tags exist in the a_tag_aliases table
        String sqlAliasCheck = "SELECT alias FROM media_viewer.a_tag_aliases WHERE alias IN (:tags)";

        // Prepare the SQL parameters
        Map<String, Object> aliasParams = Map.of("tags", uniqueTags);

        // Query the database for existing aliases
        List<String> existingAliases = namedParameterJdbcTemplate.queryForList(
            sqlAliasCheck, aliasParams, String.class);

        // If any tags are found in the a_tag_aliases table, throw an exception
        if (!existingAliases.isEmpty()) {
            throw new IllegalArgumentException("The following tags are already used as aliases: " + existingAliases);
        }

        
        // Construct the SQL query with a parameterized IN clause
        String sqlCheck = "SELECT tag FROM media_viewer.a_tags WHERE tag IN (:tags)";
        
        // Prepare the SQL parameters
        Map<String, Object> params = Map.of("tags", uniqueTags);

        // Query the database for existing tags
        List<String> existingTags = namedParameterJdbcTemplate.queryForList(
            sqlCheck, params, String.class);

        // Remove the existing tags from the list of unique tags
        uniqueTags.removeAll(existingTags);

        // Insert only the new tags and create associated tables
        for (String tag : uniqueTags) {
            String sql = "INSERT INTO `media_viewer`.`a_tags`(`tag`) VALUES ('" + tag + "');";
            jdbcTemplate.update(sql);
            create_files_tagged_table(tag);
        }
    }
    
    /*
    
    private Integer addTag(String tag) {
        String sql = "INSERT INTO `a_tags`(`tag`) VALUES ('" + tag + "');";
        jdbcTemplate.update(sql.toString());
        sql = "SELECT LAST_INSERT_ID()";
    	return jdbcTemplate.queryForObject(sql, Integer.class);
    }
    
    //Executed by the file indexing
    private Integer addTagAndGetId(String tag) {
        String sql = "INSERT INTO `a_tags`(`tag`) VALUES ('" + tag + "');";
        jdbcTemplate.update(sql.toString());
        sql = "SELECT LAST_INSERT_ID()";
    	return jdbcTemplate.queryForObject(sql, Integer.class);
    }
    */
    
    
    
    
    
    
    
   
    //Executed by the admin
    @Transactional
    public void addAliases(String tag, List<String> aliases) {
        // Remove duplicates from the input list
        List<String> uniqueAliases = new ArrayList<>(new HashSet<>(aliases));
        
     // Construct the SQL query to check for existing aliases in the a_tags table
        String sqlAliasCheck = "SELECT tag FROM a_tags WHERE tag IN (" 
                              + uniqueAliases.stream().map(alias -> "'" + alias + "'").collect(Collectors.joining(", ")) 
                              + ")";

        // Query the database for existing tags
        List<String> existingTags = jdbcTemplate.queryForList(sqlAliasCheck, String.class);

        // If any aliases are found in the a_tags table, throw an exception
        if (!existingTags.isEmpty()) {
            throw new IllegalArgumentException("The following aliases are already used as tags: " + existingTags);
        }

        // Construct the SQL query to check for existing aliases in the database
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

            // Execute the SQL query
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
    /*
    //AI Version
    public void addAliases(String tag, List<String> aliases) {
        if (!aliases.isEmpty()) {
            String sqlTemplate = "INSERT INTO a_tag_aliases (tag, alias) SELECT id, '%s' FROM a_tags WHERE tag = '%s';";
            StringBuilder sql = new StringBuilder();

            for (String alias : aliases) {
                sql.append(String.format(sqlTemplate, alias, tag));
            }

            // Execute the combined SQL statements as a single batch update
            jdbcTemplate.update(sql.toString());
        }
    }
    */
    
    
    
    
    
    
    
    
    
    
    
    
    /*
    public List<String> getExcludedFromIndexingRelativePaths() {
        String sql = "SELECT storageRelativePath FROM categories";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("storageRelativePath"));
    }
    */
    //Triggered via POST request from user
    
    
    /*
    
    public void assignTagsToMediaFile(String mediaFileName, List<String> tags) {
    	int mediaFileId = addMediaFileAndGetId(mediaFileName);
    }*/
    
    
    
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
        
        // Execute query
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
        // Query to get all tags and child tags
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
        
        return result;  // Return only parent tags with children
    }
	
	    private static class TagItemRow {
	        private final int parentId;
	        private final String parentTag;
	        private final Integer childId;
	        private final String childTag;
	
	        public TagItemRow(int parentId, String parentTag, Integer childId, String childTag) {
	            this.parentId = parentId;
	            this.parentTag = parentTag;
	            this.childId = childId;
	            this.childTag = childTag;
	        }
	
	        public int getParentId() {
	            return parentId;
	        }
	
	        public String getParentTag() {
	            return parentTag;
	        }
	
	        public Integer getChildId() {
	            return childId;
	        }
	
	        public String getChildTag() {
	            return childTag;
	        }
	    }
	
	    private static class TagItemRowMapper implements RowMapper<TagItemRow> {
	        @Override
	        public TagItemRow mapRow(ResultSet rs, int rowNum) throws SQLException {
	            int parentId = rs.getInt("parentId");
	            String parentTag = rs.getString("parentTag");
	            Integer childId = (rs.getObject("childId") != null) ? rs.getInt("childId") : null;
	            String childTag = (rs.getObject("childTag") != null) ? rs.getString("childTag") : null;
	            return new TagItemRow(parentId, parentTag, childId, childTag);
	        }
	    }
	}