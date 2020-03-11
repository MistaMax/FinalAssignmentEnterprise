package controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.Author;
import model.AuthorBook;
import model.Book;
import model.BookNotFoundException;
import model.Publisher;
import model.ValidationFailedException;
import view.ViewSwitcher;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

/**
 * Controls the detail view of a select Book
 * 
 * @author Max Crookshanks
 *
 */
public class BookDetailViewController implements Initializable {
	Book curr;

	// Initializes the variables,
	// I know this should generally be stored in a model
	// but since this assignment is just about view, I
	// did not deem it necessary
	public BookDetailViewController(Book curr) {
		this.curr = curr;
	}

	@FXML
	private TextField titeInput;

	@FXML
	private TextArea summaryInput;

	@FXML
	private TextField yearPublishedInput;

	@FXML
	private ComboBox<Publisher> publisherIdDropdown;

	@FXML
	private TextField iSBNInput;

	@FXML
	private TextField dateAddedInput;

	@FXML
	private Button bookAuditButton;

	@FXML
	private Button saveButton;

	@FXML
	private ListView<AuthorBook> authorList;

	@FXML
	private Button addAuthorButton;

	@FXML
	private Button deleteAuthorButton;

	private boolean authorChanged;

	// Runs if the save button is pressed
	@FXML
	void runSave(ActionEvent event) {
		saveValues();
		authorChanged = false;
	}

	@FXML
	void viewBookAuditList(ActionEvent event) {
		ViewSwitcher.getInstance().getLogger().info("Audit Trail Button Pressed");
		ViewSwitcher.getInstance().switchView(3, curr.getId());
	}

	private void populatePublishers() {
		for (Publisher p : PublisherTableGateway.getInstance().fetchPublishers()) {
			this.publisherIdDropdown.getItems().add(p);

			if (curr.getId() == -1) {
				this.publisherIdDropdown.setValue(p);
				curr.setId(-2);
			}

			if (curr.getId() > -1)
				if (p.getId() == curr.getPublisherId())
					this.publisherIdDropdown.setValue(p);
		}
		if (curr.getId() == -2)
			curr.setId(-1);
	}

	private void populateAuthors() {
		for (AuthorBook aB : curr.getAuthors()) {
			this.authorList.getItems().add(aB);
		}
	}

	public void saveValues() {
		ViewSwitcher.getInstance().getLogger().info("Save Button Pressed");
		try {
			saveAuthors();
			curr.save(this.titeInput.getText(), this.summaryInput.getText(),
					Integer.parseInt(this.yearPublishedInput.getText()),
					this.publisherIdDropdown.getSelectionModel().getSelectedItem().getId(), this.iSBNInput.getText());
			BookTableGateway.getInstance().updateBook(curr);
			if (curr.getId() == -1) {
				try {
					this.curr = BookTableGateway.getInstance().findBook("title", this.titeInput.getText());
				} catch (BookNotFoundException e) {
					e.printStackTrace();
				}
			}
		} catch (ValidationFailedException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Invalid Input Error");
			alert.setHeaderText("Input Error");
			alert.setContentText("Validation test failed, please check your inputs");
			alert.showAndWait();
		}
	}

	@FXML
	void editRoyalty(ActionEvent event) {
		try {
			AuthorBook aB = authorList.getSelectionModel().getSelectedItem();

			if (aB == null)
				throw new ValidationFailedException("Please select an author");

			TextInputDialog royaltyDialog = new TextInputDialog();
			royaltyDialog.setTitle("Edit Royalty");
			royaltyDialog.setHeaderText("Please input a new royalty between 0.000000 and 1");
			royaltyDialog.setContentText("Royalty: ");

			Float royalty = Float.parseFloat(royaltyDialog.showAndWait().get());
			AuthorBook.validateRoyaltyFloat(royalty);
			authorList.getItems().remove(aB);
			aB.setRoyaltyFromFloat(royalty);
			authorList.getItems().add(aB);
			authorChanged = true;
		} catch (ValidationFailedException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Invalid Input Error");
			alert.setHeaderText(e.getMessage());
			alert.setContentText("Returning to the book detail view");
			alert.showAndWait();
		} catch (NullPointerException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Alteration Failed");
			alert.setHeaderText("Royalty is null");
			alert.setContentText("Returning to the book detail view");
			alert.showAndWait();
		} catch (NumberFormatException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Alteration Failed");
			alert.setHeaderText("Royalty is invalid");
			alert.setContentText("Returning to the book detail view");
			alert.showAndWait();
		}
	}

	@FXML
	void addAuthor(ActionEvent event) {
		// run dialog box
		try {
			ChoiceDialog<Author> authorDialog = new ChoiceDialog<Author>();
			authorDialog.setTitle("Add Author");
			authorDialog.setHeaderText("Select an Author to Add to the Book");
			authorDialog.setContentText("Author: ");
			for (Author a : BookTableGateway.getInstance().getAuthorList()) {
				authorDialog.getItems().add(a);
				authorDialog.setSelectedItem(a);
			}

			Author a = authorDialog.showAndWait().get();

			if (a == null) {
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Invalid Input Error");
				alert.setHeaderText("No author selected");
				alert.setContentText("Returning to detail the book detail view");
				alert.showAndWait();
				return;
			}

			for (AuthorBook aB : authorList.getItems()) {
				if (aB.getAuthor().compareTo(a)) {
					throw new ValidationFailedException("Author already exists for this book");
				}
			}

			TextInputDialog royaltyDialog = new TextInputDialog();
			royaltyDialog.setTitle("Input Royalty");
			royaltyDialog.setHeaderText("Please input a royalty between 0.000000 and 1");
			royaltyDialog.setContentText("Royalty: ");

			Float royalty = Float.parseFloat(royaltyDialog.showAndWait().get());
			AuthorBook.validateRoyaltyFloat(royalty);
			AuthorBook aB = new AuthorBook(a, curr, royalty);
			authorList.getItems().add(aB);
			authorChanged = true;
		} catch (ValidationFailedException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Invalid Input Error");
			alert.setHeaderText(e.getMessage());
			alert.setContentText("Returning to detail the book detail view");
			alert.showAndWait();
		} catch (NullPointerException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Invalid Input Error");
			alert.setHeaderText("Royalty is null");
			alert.setContentText("Returning to detail the book detail view");
			alert.showAndWait();
		} catch (NumberFormatException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Invalid Input Error");
			alert.setHeaderText("Royalty input is invalid");
			alert.setContentText("Returning to detail the book detail view");
			alert.showAndWait();
		}
	}

	@FXML
	void deleteAuthor(ActionEvent event) {
		try {
			authorList.getItems().remove(authorList.getSelectionModel().getSelectedItem());
			authorChanged = true;
		} catch (NullPointerException e) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Invalid Input Error");
			alert.setHeaderText("No author selected");
			alert.setContentText("Returning to detail the book detail view");
			alert.showAndWait();
		}
	}

	void saveAuthors() {
		// add all of the new records
		List<AuthorBook> dbAB = BookTableGateway.getInstance().getAuthorsForBook(curr);
		for (AuthorBook aB : authorList.getItems()) {
			if (aB.isNewRecord())
				BookTableGateway.getInstance().addAuthorToBook(aB);
			else {
				for (AuthorBook tmp : dbAB) {
					if (aB.getAuthor().getId() == tmp.getAuthor().getId()) {
						if (aB.getRoyalty() != tmp.getRoyalty()) {
							System.out.println("Updating royalty");
							BookTableGateway.getInstance().updateRoyalty(aB);
						}
						break;
					}
				}
			}
		}
		// deletions
		for (AuthorBook aB : dbAB) {
			boolean shouldDelete = true;
			for(AuthorBook tempAB:authorList.getItems()) {
				if(tempAB.getAuthor().getId() == aB.getAuthor().getId()) {
					shouldDelete = false;
					break;
				}
			}
			if (shouldDelete) {
				BookTableGateway.getInstance().deleteAuthorFromBook(aB);
			}
		}
	}

	public boolean isChanged() {
		if (curr.getId() == -1)
			return true;
		if (this.curr.getTitle().compareTo(this.titeInput.getText()) != 0)
			return true;
		if (this.curr.getSummary().compareTo(this.summaryInput.getText()) != 0)
			return true;
		if (this.curr.getiSBN().compareTo(this.iSBNInput.getText()) != 0)
			return true;
		if (this.curr.getPublisherId() != this.publisherIdDropdown.getSelectionModel().getSelectedItem().getId())
			return true;
		if (this.curr.getYearPublished() != Integer.parseInt(this.yearPublishedInput.getText()))
			return true;
		if (authorChanged)
			return true;
		return false;
	}

	// sets all the book details to the corresponding values
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if (curr.getId() != -1) {
			this.titeInput.setText(curr.getTitle());
			this.summaryInput.setText(curr.getSummary());
			this.yearPublishedInput.setText(Integer.toString(curr.getYearPublished()));
			this.iSBNInput.setText(curr.getiSBN());
			this.dateAddedInput.setText(curr.getDateAdded().toString());
		} else {
			this.bookAuditButton.setDisable(true);
		}
		populatePublishers();
		populateAuthors();
		this.dateAddedInput.setDisable(true);
		authorChanged = false;
	}

}