package controller;

import java.util.List;
import java.util.ResourceBundle;
import java.io.File;
import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import model.Book;
import model.Publisher;
import model.RoyaltyReport;
import view.ViewSwitcher;
import javafx.scene.input.MouseEvent;

/**
 * Controls the Book list
 * 
 * @author Max Crookshanks
 *
 */
public class BookListViewController implements Initializable {

	// private BorderPane rootNode;
	private List<Book> curr;
	@FXML
	private ListView<Book> bookList;

	@FXML
	private TextField searchTextField;

	@FXML
	private Button deleteButton;

	@FXML
	private Button prevButton;

	@FXML
	private Button nextButton;

	@FXML
	private Button searchBookButton;

	@FXML
	private Label pageLabel;

	public BookListViewController(List<Book> curr) {
		this.curr = curr;
	}

	@FXML
	void goToBook(MouseEvent event) {
		if (event.getClickCount() == 2) {
			ViewSwitcher.getInstance().switchView(2, bookList.getSelectionModel().getSelectedItem().getId());
		}
	}

	@FXML
	void findBook(ActionEvent event) {
		BookTableGateway.getInstance().updatePageController(searchTextField.getText());
		ViewSwitcher.getInstance().switchView(1, -1);
	}

	public void setRootNode(BorderPane rootNode) {
		// this.rootNode = rootNode;
	}

	@FXML
	void genRoyaltyReport(ActionEvent event) {
		try {
			//choose the publisher
			ChoiceDialog<Publisher> publisherDialog = new ChoiceDialog<Publisher>();
			publisherDialog.setTitle("Royalty Generator");
			publisherDialog.setHeaderText("Select a publisher that needs their royalty report");
			publisherDialog.setContentText("Publisher:");
			
			for (Publisher p : PublisherTableGateway.getInstance().fetchPublishers()) {
				publisherDialog.getItems().add(p);
				publisherDialog.setSelectedItem(p);
			}

			Publisher publisher = publisherDialog.showAndWait().get();
			RoyaltyReport rR = new RoyaltyReport(publisher,
					BookTableGateway.getInstance().getAuthorBooksFromPublisher(publisher));
			//choose the folder
			DirectoryChooser excelFileChooser = new DirectoryChooser();
			File excelFile = excelFileChooser.showDialog(null);
			String path = excelFile.getAbsolutePath();
			//choose the name
			TextInputDialog fileNameDialog = new TextInputDialog(".xlsx");
			fileNameDialog.setTitle("File Name Input");
			fileNameDialog.setHeaderText("What would you like to call this file?");
			fileNameDialog.setContentText("Royalty Report Name: ");
			String fileName = fileNameDialog.showAndWait().get();
			path = path + fileName;
			
			//runs the built in generate function that creates the excel document according to the path
			rR.generateXLSX(path);
		} catch (NullPointerException e) {

		} catch (Exception e) {
			
		}
	}

	@FXML
	void handleDelete(ActionEvent event) {
		BookTableGateway.getInstance().deleteBook(bookList.getSelectionModel().getSelectedItem().getId());
	}

	@FXML
	void nextPage(ActionEvent event) {
		PageController.getInstance().incCurrPage();
		ViewSwitcher.getInstance().switchView(1, -1);
	}

	@FXML
	void prevPage(ActionEvent event) {
		PageController.getInstance().decCurrPage();
		ViewSwitcher.getInstance().switchView(1, -1);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		for (int i = 0; i < curr.size(); i++) {
			this.bookList.getItems().add(bookList.getItems().size(), curr.get(i));
		}
		if (PageController.getInstance().getPage().getCurrPage() == 1)
			prevButton.setDisable(true);
		if (PageController.getInstance().getPage().getMaxEntries() <= PageController.getInstance().getPage()
				.getPageMax())
			nextButton.setDisable(true);
		pageLabel.setText(PageController.getInstance().getPage().toString());
	}
}
