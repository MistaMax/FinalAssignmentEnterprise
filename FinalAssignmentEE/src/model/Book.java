package model;

import java.time.LocalDateTime;
import java.util.List;

import controller.BookTableGateway;

public class Book implements Duplicatable {
	private int id;

	private String title;

	private String summary;

	private int yearPublished;
	private int publisherId;
	private String iSBN;

	private LocalDateTime dateAdded;

	private LocalDateTime lastModified;

	public Book() {
		this.id = -1;
		this.title = "";
		this.summary = null;
		this.yearPublished = 0;
		this.publisherId = 0;
		this.iSBN = null;
		this.dateAdded = null;
		this.lastModified = null;
	}

	public Book(int id, String title, String summary, int yearPublished, int publisherId, String iSBN,
			LocalDateTime dateAdded, LocalDateTime lastModified) {
		super();
		this.id = id;
		this.title = title;
		this.summary = summary;
		this.yearPublished = yearPublished;
		this.publisherId = publisherId;
		this.iSBN = iSBN;
		this.dateAdded = dateAdded;
		this.lastModified = lastModified;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public int getYearPublished() {
		return yearPublished;
	}

	public void setYearPublished(int yearPublished) {
		this.yearPublished = yearPublished;
	}

	public int getPublisherId() {
		return publisherId;
	}

	public void setPublisherId(int publisherId) {
		this.publisherId = publisherId;
	}

	public String getiSBN() {
		return iSBN;
	}

	public void setiSBN(String iSBN) {
		this.iSBN = iSBN;
	}

	public LocalDateTime getDateAdded() {
		return dateAdded;
	}

	public LocalDateTime getLastModified() {
		return lastModified;
	}

	public void save(String title, String summary, int yearPublished, int publisherId, String iSBN)
			throws ValidationFailedException {
		if (!((validateTitle(title) && validateSummary(summary))
				&& (validateYearPublished(yearPublished) && validateISBN(iSBN))))
			throw new ValidationFailedException();
		this.title = title;
		this.summary = summary;
		this.yearPublished = yearPublished;
		this.publisherId = publisherId;
		this.iSBN = iSBN;
	}

	private boolean validateTitle(String title) {
		return (title.length() >= 1 && title.length() <= 255);
	}

	private boolean validateSummary(String summary) {
		if (summary == null)
			return true;
		return (summary.length() < 65536);
	}

	private boolean validateYearPublished(int yearPublished) {
		return (yearPublished <= 2018);
	}

	private boolean validateISBN(String iSBN) {
		if (iSBN == null)
			return true;
		return (iSBN.length() <= 13);
	}

	public boolean compareTo(Book book) {
		if (this.title.compareTo(book.getTitle()) != 0)
			return false;
		if (this.id != book.getId())
			return false;
		if (this.dateAdded.compareTo(book.getDateAdded()) != 0)
			return false;
		if (this.summary.compareTo(book.getSummary()) != 0)
			return false;
		if (this.iSBN.compareTo(book.getiSBN()) != 0)
			return false;
		if (this.publisherId != book.getPublisherId())
			return false;
		if (this.yearPublished != book.getYearPublished())
			return false;
		return true;
	}

	public String toString() {
		return title;
	}

	public List<AuthorBook> getAuthors() {
		return BookTableGateway.getInstance().getAuthorsForBook(this);
	}

	public Book duplicate() {
		return new Book(id, title, summary, yearPublished, publisherId, iSBN, dateAdded, lastModified);
	}
}
