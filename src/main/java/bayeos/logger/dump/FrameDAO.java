package bayeos.logger.dump;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class FrameDAO {
	
	private Connection con;

	
	public void setCon(Connection con) {
		this.con = con;
	}

	private final static Logger log = Logger.getLogger(FrameDAO.class);
	
	PreparedStatement sInsert = null;
	PreparedStatement sSelect = null;
	
	
	

	public FrameDAO() {
			
	}
	
	public void createTable() {
		Statement s = null;
		try {
			if (!tableExists()) {
				s = con.createStatement();
				s.execute("create table frame (id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), value LONG VARCHAR, board_id INTEGER, " +
						"CONSTRAINT fk_frame_board FOREIGN KEY (board_id) REFERENCES board (id) ON DELETE CASCADE)");					
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		} finally {
			try {
				if (s != null)
					s.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	private boolean tableExists() throws SQLException {
		DatabaseMetaData metadata = null;
		metadata = con.getMetaData();
		String[] names = { "TABLE" };
		ResultSet tableNames = metadata.getTables(null, null, null, names);

		while (tableNames.next()) {
			if (tableNames.getString("TABLE_NAME").equalsIgnoreCase("frame"))
				return true;
		}
		return false;
	}
	
	public void addFrames(List<String> values, Integer boardId) {
		try {
			sInsert = con.prepareStatement("insert into frame (value,board_id) values (?,?)");								
			assert(values!=null);
			assert(boardId!=null);
			sInsert.setInt(2, boardId);	
			for (String f : values) {
				sInsert.setString(1, f);				
				sInsert.addBatch();
			}												
			sInsert.executeBatch();						 
		} catch (SQLException e) {
			log.error(e.getMessage());
			
		} finally {
			try {
				sInsert.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
		
	}
	
	public void addFrame(String value, Integer boardId) {		
		try {
			sInsert = con.prepareStatement("insert into frame (value,board_id) values (?,?)");								
			assert(value!=null);
			assert(boardId!=null);
			sInsert.setString(1, value);
			sInsert.setInt(2, boardId);										
			sInsert.execute();						 
		} catch (SQLException e) {
			log.error(e.getMessage());
			
		} finally {
			try {
				sInsert.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	public List<String> getFrames(Integer boardId,int offset, int limit){
		
		try {
			sSelect = con.prepareStatement("select value from frame where board_id = ? order by id OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");																			
			assert(boardId!=null);			
			sSelect.setInt(1, boardId);
			sSelect.setInt(2, offset);
			sSelect.setInt(3, limit);						
			ResultSet rs = sSelect.executeQuery();			
			List<String> ret = new ArrayList<String>(limit);			
			while (rs.next()) {
				String f = rs.getString(1);
				// log.info(ByteUtils.toStringList(ByteUtils.toIntArray(Base64.decodeBase64(f), 0)));
				ret.add(f);				
			}
			return ret;									
		} catch (SQLException e) {
			log.error(e.getMessage());
			return null;
		} finally {
			try {
				sSelect.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
		
		
		
	}
	
	
	
	
	

}
