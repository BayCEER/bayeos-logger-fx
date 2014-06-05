package bayeos.logger.dump;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class DAO {
	
	private static Logger log = Logger.getLogger(DAO.class);
		
	private static String userHomeDir = System.getProperty("user.home", ".");
	private static String systemDir = userHomeDir + File.separatorChar + ".bayeos-logger";	
	private static File dbFolder = new File(systemDir + File.separatorChar + "loggerData");
		  
	private static Connection con;	
	private static BoardDAO boardDAO = new BoardDAO();
	private static FrameDAO frameDAO = new FrameDAO();
	
		
	static {
		System.setProperty("derby.stream.error.file", systemDir	+ File.separatorChar + "derby.log");		
		File f = new File(systemDir);
		if (!f.exists()) {
			f.mkdir();
		}		
	}
	
	public static BoardDAO getBoardDAO() throws SQLException {
		boardDAO.setCon(getConnection());
		return boardDAO;		
	}
	
	
	public static FrameDAO getFrameDAO() throws SQLException {
		frameDAO.setCon(getConnection());
		return frameDAO;		
	}
	
	public static Connection getConnection() throws SQLException {
		if (con == null || con.isClosed()){
			con = createConnection();
		}
		return con;						
	}
	
	
	public static void close() {
		try {
			DriverManager.getConnection("jdbc:derby:" + dbFolder.getPath()
					+ ";shutdown=true");
		} catch (SQLException e) {
			
		}
	}
	
				
	private static Connection createConnection() throws SQLException {		 		
		if (dbFolder.exists()) {
			// DB Already there
			return  DriverManager.getConnection("jdbc:derby:" + dbFolder.getPath());
	
		} else {// First run
			log.info("Initializing embedded database in " + dbFolder.getAbsolutePath());
			return  DriverManager.getConnection("jdbc:derby:" + dbFolder.getPath() + ";create=true");									
		}
		
	}
	
	
	
	
	
	

}
