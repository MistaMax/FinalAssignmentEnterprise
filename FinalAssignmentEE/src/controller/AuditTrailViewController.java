package controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.AuditTrailEntry;
import model.Book;
import model.BookNotFoundException;
import view.ViewSwitcher;

public class AuditTrailViewController implements Initializable {

	private Book book;
	private List<AuditTrailEntry> auditTrail;

	@FXML
	private ListView<String> AuditTrailList;

	@FXML
	private Label AuditLabel;
	
	@FXML
    private Button backButton;
	
	public AuditTrailViewController(int bookId) {
		try {
			this.book = BookTableGateway.getInstance().findBook("id", Integer.toString(bookId));
		} catch (BookNotFoundException e) {
			this.book = null;
		}
		this.auditTrail = BookTableGateway.getInstance().findAuditTrail(bookId);
	}
	
	@FXML
    void goBack(ActionEvent event) {
		try {
			ViewSwitcher.getInstance().switchView(2, book.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

	// sets all the book details to the corresponding values
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.AuditLabel.setText("Audit Trail for " + book.getTitle());
		//sorting the list
		for(int i = auditTrail.size()-1;i >= 0 ;i--) {
			this.AuditTrailList.getItems().add(auditTrail.get(i).toString());
		}
	}
}