package view;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import controller.AuditTrailViewController;
import controller.BookDetailViewController;
import controller.BookListViewController;
import controller.BookTableGateway;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import model.Book;
import model.BookNotFoundException;

/**
 * Controls the switching between views
 * 
 * @author Max Crookshanks
 *
 */
public class ViewSwitcher {
	private static ViewSwitcher instance = null;
	private static BookDetailViewController detailInstance = null;
	private BorderPane rootNode;
	private static Logger logger = LogManager.getLogger(ViewSwitcher.class);
	private static int currView = 1;

	private ViewSwitcher() {
		rootNode = null;
	}
	
	public int checkForSave() {
		if(currView == 2) {
			if(detailInstance.isChanged()) {
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Leaving Without Saving");
				alert.setHeaderText("Would you like to save you progress?");
				alert.setContentText("You have unsaved changes");
				
				ButtonType yesButton = new ButtonType("Yes");
				ButtonType noButton = new ButtonType("No");
				ButtonType cancelButton = new ButtonType("Cancel");
				
				alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);
				
				Optional<ButtonType> result = alert.showAndWait();
				
				if(result.get() == yesButton) {
					detailInstance.saveValues();
					return 0;
				}
				else if(result.get() == noButton) {
					return 0;
				}else
					return 1;
					
			}
			else
				return 0;
		}
		return 0;
	}

	public void switchView(int viewType, int id) {
		if(checkForSave() == 1)
			return;
		
		switch (viewType) {
		case 1:
			try {
				viewBookList(BookTableGateway.getInstance().getBooks());
				currView = viewType;
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 2:
			try {
				logger.info("Switching to Book Detail View");
				BookDetailViewController controller = null;
				if (id < 0)
					controller = new BookDetailViewController(new Book());
				else {
					try {
						controller = new BookDetailViewController(
								BookTableGateway.getInstance().findBook("id", Integer.toString(id)));
					} catch (BookNotFoundException e) {
						e.printStackTrace();
					}
				}
				// controller.setRootNode(rootNode);
				URL fxmlFile = this.getClass().getResource("BookDetailView.fxml");
				FXMLLoader loader = new FXMLLoader(fxmlFile);

				loader.setController(controller);

				Parent contentView = loader.load();

				// get rid of reference to previous content view
				rootNode.setCenter(null);

				rootNode.setCenter(contentView);
				detailInstance = controller;
				currView = viewType;
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		case 3:
			try {
				logger.info("Switching to Book Audit View");
				AuditTrailViewController controller = null;
				controller = new AuditTrailViewController(id);

				// controller.setRootNode(rootNode);

				URL fxmlFile = this.getClass().getResource("AuditTrailView.fxml");
				FXMLLoader loader = new FXMLLoader(fxmlFile);

				loader.setController(controller);

				Parent contentView = loader.load();

				// get rid of reference to previous content view
				rootNode.setCenter(null);

				rootNode.setCenter(contentView);
				currView = viewType;
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		}
	}
	
	private void viewBookList(List<Book> books) throws IOException{
		askForUpdate();
		logger.info("Switching to Book List View");
		BookListViewController controller = new BookListViewController(books);
		controller.setRootNode(rootNode);

		URL fxmlFile = this.getClass().getResource("BookListView.fxml");
		FXMLLoader loader = new FXMLLoader(fxmlFile);

		loader.setController(controller);

		Parent contentView = loader.load();

		// get rid of reference to previous content view
		rootNode.setCenter(null);

		rootNode.setCenter(contentView);
	}
	
	public boolean askForUpdate() {
		// put asking for update code here
		return true;
	}

	public BorderPane getRootNode() {
		return rootNode;
	}

	public void setRootNode(BorderPane rootNode) {
		this.rootNode = rootNode;
	}

	public Logger getLogger() {
		return logger;
	}

	public static ViewSwitcher getInstance() {
		if (instance == null) {
			instance = new ViewSwitcher();
		}
		return instance;
	}
}
