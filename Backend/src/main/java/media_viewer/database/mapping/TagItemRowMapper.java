
package media_viewer.database.mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class TagItemRowMapper implements RowMapper<TagItemRow> {
    @Override
    public TagItemRow mapRow(ResultSet rs, int rowNum) throws SQLException {
        int parentId = rs.getInt("parentId");
        String parentTag = rs.getString("parentTag");
        Integer childId = (rs.getObject("childId") != null) ? rs.getInt("childId") : null;
        String childTag = (rs.getObject("childTag") != null) ? rs.getString("childTag") : null;
        return new TagItemRow(parentId, parentTag, childId, childTag);
    }
}