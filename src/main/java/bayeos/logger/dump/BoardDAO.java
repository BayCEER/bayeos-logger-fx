package bayeos.logger.dump;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class BoardDAO {

	private Connection con;

	public void setCon(Connection con) {
		this.con = con;
	}

	private final static Logger log = Logger.getLogger(BoardDAO.class);
		
		
   private boolean tableExists() throws SQLException {
		DatabaseMetaData metadata = null;
		metadata = con.getMetaData();
		String[] names = { "TABLE" };
		ResultSet tableNames = metadata.getTables(null, null, null, names);

		while (tableNames.next()) {
			if (tableNames.getString("TABLE_NAME").equalsIgnoreCase("board"))
				return true;
		}
		return false;
	}

	public void createTable() {
		Statement s = null;
		try {
			if (!tableExists()) {
				s = con.createStatement();
				s.execute("create table board (id INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),"
						+ "name VARCHAR(30), start_date TIMESTAMP, end_date TIMESTAMP, records INTEGER)");
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
	
	public void deleteBoard(int id){
		log.debug("Delete board:" + id);
		PreparedStatement s = null;
		try {
			s = con.prepareStatement("delete from board where id = ?");
			s.setInt(1, id);
			s.execute();
		} catch (SQLException e) {
			log.error(e.getMessage());
		} finally {
			try {
				s.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
	}
	

	public void deleteBoard(Board b) {		
		if (b == null) return;
		if (b.Id == null) return;		
		deleteBoard(b.Id);
	}
	
	public int add(Board b) {
		PreparedStatement s = null;
		try {
			s = con.prepareStatement("insert into board (name,start_date,end_date,records) values (?,?,?,?)",PreparedStatement.RETURN_GENERATED_KEYS);			
			if (b.Name==null) {
				s.setNull(1, java.sql.Types.VARCHAR);
			} else {
				s.setString(1, b.Name);				
			}			
			if(b.Start==null){
				s.setNull(2, java.sql.Types.TIMESTAMP);
			} else {
				s.setTimestamp(2, new java.sql.Timestamp(b.Start.getTime()));	
			}
			if(b.End==null){
				s.setNull(3, java.sql.Types.TIMESTAMP);
			} else {
				s.setTimestamp(3, new java.sql.Timestamp(b.End.getTime()));	
			}			
			if (b.Records==null){
				s.setNull(4, java.sql.Types.INTEGER);
			} else {
				s.setInt(4, b.Records);
			}								
			s.execute();
			ResultSet rs = s.getGeneratedKeys();
			rs.next(); 
			return rs.getInt(1);			 
		} catch (SQLException e) {
			log.error(e.getMessage());
			return 0;
		} finally {
			try {
				s.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
	}
	
	
	public void update(Board b) {
		PreparedStatement s = null;
		try {			
			s = con.prepareStatement("update board set name=?,start_date=?,end_date=?,records=? where id=?");
			if (b.Name==null) {
				s.setNull(1, java.sql.Types.VARCHAR);
			} else {
				s.setString(1, b.Name);				
			}			
			if(b.Start==null){
				s.setNull(2, java.sql.Types.TIMESTAMP);
			} else {
				s.setTimestamp(2, new java.sql.Timestamp(b.Start.getTime()));	
			}
			if(b.End==null){
				s.setNull(3, java.sql.Types.TIMESTAMP);
			} else {
				s.setTimestamp(3, new java.sql.Timestamp(b.End.getTime()));	
			}			
			if (b.Records==null){
				s.setNull(4, java.sql.Types.INTEGER);
			} else {
				s.setInt(4, b.Records);
			}
			assert(b.Id!=null);
			s.setInt(5, b.Id);
			s.execute();									 
		} catch (SQLException e) {
			log.error(e.getMessage());
		} finally {
			try {
				s.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
	}

	public List<Board> findAll() {
		List<Board> l = new ArrayList<Board>(10);
		Statement s = null;
		try {
			s = con.createStatement();
			ResultSet r = s
					.executeQuery("select id, name, start_date, end_date, records from board order by id");
			while (r.next()) {
				Board b = new Board();
				b.Id = r.getInt(1);				
				b.Name = r.getString(2);
				
				Timestamp t = r.getTimestamp(3);
				if (t!=null){
					b.Start = new Date(t.getTime());	
				} else {
					b.Start = null;
				}
				t = r.getTimestamp(4);
				if (t!=null){
					b.End = new Date(t.getTime());	
				} else {
					b.End = null;
				}
								
				b.Records = r.getInt(5);
				l.add(b);
			}
			r.close();
		} catch (SQLException e) {
			log.error(e.getMessage());
		} finally {
			try {
				s.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
		}
		return l;
	}
	
	public Board findById(Integer id) {
		
		PreparedStatement s = null;
		ResultSet r = null;
		try {
			s = con.prepareStatement("select id, name, start_date, end_date, records from board where id = ?");
			s.setInt(1, id);
			r = s.executeQuery();
			if (!r.next()){
				return null;
			} else {
				Board b = new Board();
				b.Id = r.getInt(1);				
				b.Name = r.getString(2);
				
				Timestamp t = r.getTimestamp(3);
				if (t!=null){
					b.Start = new Date(t.getTime());	
				} else {
					b.Start = null;
				}
				t = r.getTimestamp(4);
				if (t!=null){
					b.End = new Date(t.getTime());	
				} else {
					b.End = null;
				}								
				b.Records = r.getInt(5);
				return b;
			}
						
		} catch (SQLException e) {
			log.error(e.getMessage());
			return null;
		} finally {
			try {
				r.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
						
			try {
				s.close();
			} catch (SQLException e) {
				log.error(e.getMessage());
			}
			
			
		}
		
	}
	
}
