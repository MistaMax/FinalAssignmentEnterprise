package model;

public class ValidationFailedException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String message;
	public ValidationFailedException(String message) {
		super();
		this.message = message;
	}
	public ValidationFailedException() {
		super();
		this.message = null;
	}
	
	public String getMessage() {
		return message;
	}
}
