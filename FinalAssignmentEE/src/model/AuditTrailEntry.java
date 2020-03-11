package model;

import java.util.Date;

public class AuditTrailEntry {
	private int id;
	private Date dateAdded;
	private String message;
	
	public AuditTrailEntry(int id, Date dateAdded, String message) {
		super();
		this.id = id;
		this.dateAdded = dateAdded;
		this.message = message;
	}
	
	public int getId() {
		return id;
	}
	
	public Date getDateAdded() {
		return dateAdded;
	}
	
	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return dateAdded.toString() + ": " + message;
	}
}
