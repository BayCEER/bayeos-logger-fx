package bayeos.logger.dump;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javafx.concurrent.Task;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import bayeos.logger.TaskController;
import de.unibayreuth.bayeos.connection.Connection;
import frame.parser.DataReader;



public class UploadBoardTask extends Task<Boolean> {
	
	private long posts = 0;
	private Board board;
	private Connection con;
	private Date startDate;
	private Date endDate;
	private final static int FRAMES_PER_POST = 10000;
	private static final Logger log = Logger.getLogger(UploadBoardTask.class);
	
	public UploadBoardTask(Board b, Map<String, Object> ret) {
		this.board = b;
		this.con = (Connection) ret.get("con");	
		this.startDate = (Date) ret.get("startDate");
		this.endDate = (Date) ret.get("endDate");
		
		posts = (long) Math.ceil(b.getRecords() / FRAMES_PER_POST) + 1;		
	}
	
	@Override
	protected Boolean call() throws Exception {
		updateTitle("Uploading data to " + con.getURL());
		log.debug("Start upload task");		
		long startTime = new Date().getTime();
		
		SimpleHTTPClient client = null;
		try {
		client = new SimpleHTTPClient(con.getURL(),con.getUserName(),con.getPassword());
		updateProgress(0, posts);
		updateMessage(TaskController.getUpdateMsg("Upload:", 0, posts, startTime));
		int offset = 0;
		List<String> frames;
		
		int post = 0;
		while ((frames = DAO.getFrameDAO().getFrames(board.getId(), offset, FRAMES_PER_POST)).size() > 0) {
			if (isCancelled()) {				
				updateMessage("Cancelled");	
				return false;
			}
		
			List<String> expFrames = new ArrayList<String>(FRAMES_PER_POST);
			for(int i=0;i<frames.size();i++) {
				
				DataReader reader = new DataReader();
				Map<String, Object> ret = reader.read(Base64.decodeBase64(frames.get(i)), board.getName(), new Date());
				Date t = (Date) ret.get("result_time");
												
				if (!(t.before(startDate)) || t.after(endDate)) {
					expFrames.add(frames.get(i));					
				}
										
				
			}
			
			ReturnCode ret = ReturnCode.OK;
			if (expFrames.size()>0) {
				ret = client.postFrames(expFrames,board.getName());				
			}			
			if (ret.getResponseCode() == ReturnCode.OK.getResponseCode()) {					
				offset = ++post * FRAMES_PER_POST;
				updateProgress(post, posts);				
				updateMessage(TaskController.getUpdateMsg("Upload:", post, posts, startTime));								
			} else {								
				return false;
			}															 
		}
		return true;
		} catch (Exception e) {
			log.error(e);			
			return false;
		} 
		
	}

}
