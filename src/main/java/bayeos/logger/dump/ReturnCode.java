package bayeos.logger.dump;

class ReturnCode {
	
	private int responseCode;
	private String msg;
	
	
	public ReturnCode(int responseCode, String msg) {
			this.setResponseCode(responseCode);
			this.setMsg(msg);
	}


	public int getResponseCode() {
		return responseCode;
	}


	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}


	public String getMsg() {
		return msg;
	}


	public void setMsg(String msg) {
		this.msg = msg;
	}

	public static final ReturnCode BAD_REQUEST = new ReturnCode(400, "Bad request, please check your parameters.");
	public static final ReturnCode UN_AUTHORIZED  = new ReturnCode(401, "Not authorized, please check your password.");
	public static final ReturnCode NOT_IMPLEMENTED  = new ReturnCode(405, "Method not implemented.");
	public static final ReturnCode OK  = new ReturnCode(200, "ok");
	
	
		
}