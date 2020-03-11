package controller;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.AuditTrailEntry;
import model.Author;
import model.AuthorBook;
import model.Book;
import model.BookNotFoundException;
import model.Page;
import model.Publisher;
import view.ViewSwitcher;

public class BookTableGateway {
	private static BookTableGateway instance = null;
	private MysqlDataSource ds;
	private Connection conn = null;
	private String currSearch;

	private BookTableGateway() {
		MysqlDataSource ds = new MysqlDataSource();
		Properties props = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream("db.properties");
			props.load(fis);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// create the datasource
		ds.setURL(props.getProperty("MYSQL_DB_URL"));
		ds.setUser(props.getProperty("MYSQL_DB_USERNAME"));
		ds.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));

		// create the connection
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		
		}

		currSearch = "%%";
	}

	public static BookTableGateway getInstance() {
		if (instance == null) {
			instance = new BookTableGateway();
		}
		return instance;
	}

	public void closeConnection() {
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void openConnection() {
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Book> getBooks() {
		ViewSwitcher.getInstance().getLogger().info("Pulling books");
		List<Book> books = new ArrayList<Book>();
		String query = "SELECT * FROM books WHERE title LIKE '" + currSearch + "'"
				+ PageController.getInstance().getPage().getLimit();
		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			ResultSet rs = smt.executeQuery(query);
			while (rs.next()) {
				books.add(getBookFromRs(rs));
			}
		} catch (SQLException e) {
			displaySQLError();
		}
		return books;
	}

	public void updatePageController(String search) {
		search = "%" + search + "%";
		String query = "SELECT COUNT(*) FROM books WHERE title LIKE '" + search + "';";
		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			ResultSet rs = smt.executeQuery(query);
			rs.next();
			PageController.getInstance().setPage(new Page(rs.getInt(1)));
			currSearch = search;
		} catch (SQLException e) {

		}
	}

	public Book findBook(String prop, String val) throws BookNotFoundException {
		ViewSwitcher.getInstance().getLogger().info("Finding book");
		Book curr = null;
		String query = "SELECT * FROM books WHERE " + prop + "='" + val + "';";

		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			ResultSet rs = smt.executeQuery(query);
			if (rs.next())
				curr = getBookFromRs(rs);
			else
				throw new BookNotFoundException();
		} catch (SQLException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("SQL Exception Thrown");
			alert.setHeaderText("SQL Error");
			alert.setContentText("Error finding information in the database");
			alert.showAndWait();
		}
		return curr;
	}

	public void updateBookVariable(int bookId, String var, String val) {
		String query = null;
		if (val == null)
			query = "UPDATE books SET " + var + "=NULL WHERE books.id=" + Integer.toString(bookId);
		else
			query = "UPDATE books SET " + var + "='" + val + "' WHERE books.id=" + Integer.toString(bookId);

		PreparedStatement smt = null;

		try {
			smt = conn.prepareStatement(query);
			smt.executeUpdate(query);
			query = "UPDATE books SET last_modified=CURRENT_TIMESTAMP WHERE books.id=" + bookId;
			smt = conn.prepareStatement(query);
			smt.executeUpdate(query);
		} catch (SQLException e) {
			displaySQLError();
		}
	}

	public void updateBook(Book book) {
		if (book.getId() == -1) {
			ViewSwitcher.getInstance().getLogger().info("Running insert");
			insertBook(book);
		} else {
			Book updateCheck;
			try {
				updateCheck = BookTableGateway.getInstance().findBook("id", Integer.toString(book.getId()));
				if (book.getLastModified().compareTo(updateCheck.getLastModified()) != 0) {
					ViewSwitcher.getInstance().getLogger().info("Updated Version out");
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("Newer Version of the Document Out");
					alert.setHeaderText("Please Reload This Record");
					alert.setContentText("Someone has already eddited and commited their changes before you");
					alert.showAndWait();
					return;
				}
				ViewSwitcher.getInstance().getLogger().info("Updating a book");

				if (updateCheck.getTitle().compareTo(book.getTitle()) != 0) {
					updateBookVariable(book.getId(), "title", book.getTitle());
					addAudit(book.getId(), "title", updateCheck.getTitle(), book.getTitle());
				}

				if (updateCheck.getSummary().compareTo(book.getSummary()) != 0) {
					updateBookVariable(book.getId(), "summary", book.getSummary());
					addAudit(book.getId(), "summary", updateCheck.getSummary(), book.getSummary());
				}

				if (updateCheck.getYearPublished() != book.getYearPublished()) {
					updateBookVariable(book.getId(), "year_published", Integer.toString(book.getYearPublished()));
					addAudit(book.getId(), "year_published", Integer.toString(updateCheck.getYearPublished()),
							Integer.toString(book.getYearPublished()));
				}

				if (updateCheck.getPublisherId() != book.getPublisherId()) {
					updateBookVariable(book.getId(), "publisher_id", Integer.toString(book.getPublisherId()));
					addAudit(book.getId(), "publisher_id", Integer.toString(updateCheck.getPublisherId()),
							Integer.toString(book.getPublisherId()));
				}

				if (updateCheck.getiSBN().compareTo(book.getiSBN()) != 0) {
					updateBookVariable(book.getId(), "isbn", book.getiSBN());
					addAudit(book.getId(), "isbn", updateCheck.getiSBN(), book.getiSBN());
				}
			} catch (BookNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private void addAudit(int bookId, String field, String oldValue, String newValue) {
		String query = "INSERT INTO book_audit_trail(id,book_id,date_added,entry_msg) VALUES (NULL, '"
				+ Integer.toString(bookId) + "',CURRENT_TIMESTAMP,'" + field + " changed from " + oldValue + " to "
				+ newValue + "')";
		try {
			runUpdateQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void addAudit(int bookId, String message) {
		String query = "INSERT INTO book_audit_trail(id,book_id,date_added,entry_msg) VALUES (NULL, '"
				+ Integer.toString(bookId) + "',CURRENT_TIMESTAMP,'" + message + "')";
		try {
			runUpdateQuery(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteBook(int id) {
		ViewSwitcher.getInstance().getLogger().info("Deleting book from the database");
		String query = "DELETE FROM books WHERE id='" + Integer.toString(id) + "';";
		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			smt.executeUpdate(query);
		} catch (SQLException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("SQL Exception Thrown");
			alert.setHeaderText("SQL Error");
			alert.setContentText("Error updating information in the database");
			alert.showAndWait();
		}
		ViewSwitcher.getInstance().switchView(1, 0);
	}

	public void insertBook(Book book) {
		String query = "INSERT INTO books (id,title,summary,year_published,publisher_id,isbn,date_added,last_modified) VALUES"
				+ "(NULL,'" + book.getTitle() + "','" + book.getSummary() + "','"
				+ Integer.toString(book.getYearPublished()) + "','" + Integer.toString(book.getPublisherId()) + "','"
				+ book.getiSBN() + "',CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
		try {
			runUpdateQuery(query);
			book = findBook("title", book.getTitle());
			query = "INSERT INTO book_audit_trail(id,book_id,date_added,entry_msg) VALUES (NULL, '"
					+ Integer.toString(book.getId()) + "',CURRENT_TIMESTAMP,'Book added')";
		} catch (SQLException e) {
			ViewSwitcher.getInstance().getLogger().info("Failed to insert");
			e.printStackTrace();
		} catch (BookNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Book getBookFromRs(ResultSet rs) throws SQLException {
		return new Book(rs.getInt("id"), rs.getString("title"), rs.getString("summary"), rs.getInt("year_published"),
				rs.getInt("publisher_id"), rs.getString("isbn"), rs.getTimestamp("date_added").toLocalDateTime(),
				rs.getTimestamp("last_modified").toLocalDateTime());
	}

	public List<AuditTrailEntry> findAuditTrail(int bookId) {
		List<AuditTrailEntry> entries = new LinkedList<AuditTrailEntry>();
		String query = "SELECT a.id, a.date_added, a.entry_msg FROM book_audit_trail a, books b WHERE a.book_id = b.id AND b.id="
				+ Integer.toString(bookId);
		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			ResultSet rs = smt.executeQuery(query);
			while (rs.next()) {
				entries.add(getAuditTrailEntryFromRs(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return entries;
	}

	private AuditTrailEntry getAuditTrailEntryFromRs(ResultSet rs) throws SQLException {
		return new AuditTrailEntry(rs.getInt("id"), new Date(rs.getTimestamp("date_added").getTime()),
				rs.getString("entry_msg"));
	}

	private Author getAuthorFromRs(ResultSet rs) throws SQLException {
		return new Author(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),
				rs.getDate("dob").toLocalDate(), rs.getString("gender"), rs.getString("web_site"));
	}

	public List<AuthorBook> getAuthorsForBook(Book book) {
		List<AuthorBook> bookAuthors = new LinkedList<AuthorBook>();
		String query = "SELECT ab.royalty, a.* FROM author_book ab, author a WHERE ab.book_id = " + book.getId()
				+ " AND ab.author_id=a.id;";
		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			ResultSet rs = smt.executeQuery(query);
			while (rs.next()) {
				float royalty = rs.getFloat("royalty");
				bookAuthors.add(new AuthorBook(getAuthorFromRs(rs), book, royalty, false));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return bookAuthors;
	}

	private void runUpdateQuery(String query) throws SQLException {
		PreparedStatement smt = null;
		smt = conn.prepareStatement(query);
		smt.executeUpdate(query);
	}

	public void displaySQLError() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle("SQL Exception Thrown");
		alert.setHeaderText("SQL Error");
		alert.setContentText("Error updating information in the database");
		alert.showAndWait();
	}

	public void deleteAuthorFromBook(AuthorBook author) {
		ViewSwitcher.getInstance().getLogger().info("Deleting Book Author entry from the database");
		String query = "DELETE FROM author_book WHERE author_id='" + Integer.toString(author.getAuthor().getId())
				+ "' AND book_id='" + Integer.toString(author.getBook().getId()) + "';";
		try {
			runUpdateQuery(query);
			addAudit(author.getBook().getId(), "Removed Author by ID " + Integer.toString(author.getAuthor().getId()));
		} catch (SQLException e) {
			displaySQLError();
		}
	}

	public void addAuthorToBook(AuthorBook author) {
		String query = "INSERT INTO author_book(author_id, book_id, royalty, last_modified) VALUES ('"
				+ author.getAuthor().getId() + "', '" + Integer.toString(author.getBook().getId()) + "', '"
				+ Float.toString(author.getRoyaltyFloat()) + "', CURRENT_TIMESTAMP)";
		try {
			runUpdateQuery(query);
			addAudit(author.getBook().getId(), "Added Author by ID " + Integer.toString(author.getAuthor().getId())
					+ " with royalty " + Float.toString(author.getRoyaltyFloat()));
		} catch (SQLException e) {
			displaySQLError();
		}
	}

	public void updateRoyalty(AuthorBook aB) {
		String query = "UPDATE author_book SET royalty=" + Float.toString(aB.getRoyaltyFloat()) + " WHERE author_id="
				+ Integer.toString(aB.getAuthor().getId()) + " AND book_id=" + Integer.toString(aB.getBook().getId());
		try {
			runUpdateQuery(query);
			addAudit(aB.getBook().getId(), "Author ID " + Integer.toString(aB.getAuthor().getId())
					+ " royalty changed to" + Float.toString(aB.getRoyaltyFloat()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Author> getAuthorList() {
		List<Author> auth = new LinkedList<Author>();
		String query = "SELECT * FROM author;";
		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			ResultSet rs = smt.executeQuery(query);
			while (rs.next()) {
				auth.add(getAuthorFromRs(rs));
			}
		} catch (SQLException e) {
			displaySQLError();
		}
		return auth;
	}

	public Connection getConn() {
		return conn;
	}

	public int getOverallEntries() {
		String query = "SELECT COUNT(*) FROM books;";
		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			ResultSet rs = smt.executeQuery(query);
			rs.next();
			return rs.getInt(1);
		} catch (SQLException e) {
			return -1;
		}
	}

	public Author findAuthor(String prop, String val) throws SQLException {
		String query = "SELECT * FROM author WHERE " + prop + "='" + val + "';";
		PreparedStatement smt = conn.prepareStatement(query);
		ResultSet rs = smt.executeQuery(query);
		rs.next();
		return getAuthorFromRs(rs);
	}

	public List<AuthorBook> getAuthorBooksFromPublisher(Publisher publisher) {
		String query = "SELECT ab.book_id, ab.author_id, ab.royalty FROM books b, author_book ab WHERE b.publisher_id="
				+ Integer.toString(publisher.getId())
				+ " AND ab.book_id = b.id ORDER BY ab.book_id ASC, ab.author_id ASC";
		List<AuthorBook> aBList = new LinkedList<AuthorBook>();
		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			ResultSet rs = smt.executeQuery(query);

			while (rs.next()) {
				Book book = findBook("id", Integer.toString(rs.getInt(1)));
				Author author = findAuthor("id", Integer.toString(rs.getInt(2)));
				aBList.add(new AuthorBook(author, book, rs.getFloat(3), false));
			}
			return aBList;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (BookNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
