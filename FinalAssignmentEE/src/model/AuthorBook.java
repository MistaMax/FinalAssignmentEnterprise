package model;

public class AuthorBook implements Duplicatable{
	private Author author;
	private Book book;
	private int royalty;
	private boolean newRecord;
	
	private static int multiplier = 100000;

	public AuthorBook() {
		this.author = new Author();
		this.book = new Book();
		this.royalty = -1;
		this.newRecord = true;
	}

	public AuthorBook(Author author, Book book, int royalty) {
		this.author = author;
		this.book = book;
		this.royalty = royalty;
		this.newRecord = true;
	}
	
	public AuthorBook(Book book, int royalty) {
		this.author = null;
		this.book = book;
		this.royalty = royalty;
		this.newRecord = true;
	}
	
	public AuthorBook(Author author, Book book, float royalty) {
		this.author = author;
		this.book = book;
		setRoyaltyFromFloat(royalty);
		this.newRecord = true;
	}

	public AuthorBook(Author author, Book book, float royalty, boolean newRecord) {
		this.author = author;
		this.book = book;
		setRoyaltyFromFloat(royalty);
		this.newRecord = newRecord;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public int getRoyalty() {
		return royalty;
	}

	public void setRoyalty(int royalty) {
		this.royalty = royalty;
	}

	public boolean isNewRecord() {
		return newRecord;
	}

	public void setNewRecord(boolean newRecord) {
		this.newRecord = newRecord;
	}
	
	public float getRoyaltyFloat() {
		return ((float)royalty)/multiplier;
	}
	
	public float getRoyaltyPercent() {
		return 100*((float)royalty)/multiplier;
	}
	
	public void setRoyaltyFromFloat(float royalty) {
		this.royalty = (int) (multiplier*royalty);
	}
	
	public void validate() throws ValidationFailedException{
		validateRoyaltyFloat(royalty);
		
	}
	
	public AuthorBook duplicate() {
		return new AuthorBook(this.author.duplicate(),this.book.duplicate(), royalty);
	}
	
	public static void validateRoyaltyFloat(float royalty) throws ValidationFailedException{
		if(!(royalty >= 0 && royalty <= 1))
			throw new ValidationFailedException("Royalty is out of bounds");
	}
	
	@Override
	public String toString() {
		String royaltyPercent = String.format("%s%%",getRoyaltyPercent());
		return String.format("%-120s %6s", author.toString().split(":")[1], royaltyPercent);
	}
}
