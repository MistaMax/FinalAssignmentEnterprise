package model;

public class BookUpdateFailedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3312572533788868513L;
	String bookTitle;
	public BookUpdateFailedException() {
		this.bookTitle=null;
	}

	public BookUpdateFailedException(String arg0) {
		this.bookTitle=arg0;
	}
}
