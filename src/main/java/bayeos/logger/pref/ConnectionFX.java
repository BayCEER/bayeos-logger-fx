package bayeos.logger.pref;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import de.unibayreuth.bayeos.connection.Connection;

public class ConnectionFX {

	private StringProperty name = new SimpleStringProperty(this,"name");			
	private StringProperty host = new SimpleStringProperty(this,"host");
	private StringProperty user = new SimpleStringProperty(this,"user");
	private StringProperty password = new SimpleStringProperty(this,"password");
	
	
	public ConnectionFX(String name, String host, String user, String password){
		this.name.setValue(name);
		this.host.setValue(host);
		this.user.setValue(user);
		this.password.setValue(password);
	}
	
	public ConnectionFX(Connection con){		
		this.name.setValue(con.getName());
		this.host.setValue(con.getURL());
		this.user.setValue(con.getUserName());
		this.password.setValue(con.getPassword());
	}
	
	public final StringProperty nameProperty(){
		return name;
	}
	
	public Connection getConnection(){
		return new Connection(name.get(),host.get(),user.get(),password.get());
	}
	
	
	public final String getName() {
		return name.get();
	}

	public final void setName(String value) {
		name.set(value);
	}


	public StringProperty hostProperty(){
		return host;
	}
	
	public final String getHost() {
		return host.get();
	}

	public final void setHost(String value) {
		host.set(value);
	}
	
	public final String getUser() {
		return user.get();
	}

	public final void setUser(String value) {
		user.set(value);
	}

	
	public StringProperty userProperty(){
		return user;
	}
	
	public StringProperty passwordProperty(){
		return password;
	}
	public final String getPassword() {
		return password.get();
	}

	public final void setPassword(String value) {
		password.set(value);
	}

	
}
