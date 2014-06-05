package bayeos.logger.dump;

import java.util.Date;


public class Board {

	public Integer Id;
	
	public Board(String name) {
		this.Name = name;
	}

	public Board() {		
	}

	public Integer getId() {
		return Id;
	}

	public void setId(Integer id) {
		Id = id;
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public Date getStart() {
		return Start;
	}

	public void setStart(Date start) {
		Start = start;
	}

	public Date getEnd() {
		return End;
	}

	public void setEnd(Date end) {
		End = end;
	}

	public Integer getRecords() {
		return Records;
	}

	public void setRecords(Integer records) {
		Records = records;
	}

	public String Name;
	public Date Start;
	public Date End;
	public Integer Records;

	public Object get(int index) {
		switch (index) {		
		case 0:
			return Name;
		case 1:
			return Start;
		case 2:
			return End;
		case 3:
			return Records;
		case 4:
			return Id;
		}
		return null;

	}
	
	@Override
	public String toString() {
		return "Name:" + Name + " Start:" + Start + " End:" + End + " Records:" + Records + " Id:" + Id;		
	
	}

}
