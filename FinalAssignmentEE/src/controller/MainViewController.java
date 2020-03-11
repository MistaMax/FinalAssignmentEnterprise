package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import view.ViewSwitcher;

/**
 * Controls the overall application view
 * 
 * @author Max Crookshanks
 *
 */
public class MainViewController implements Initializable {

	// private BorderPane rootNode;
	@FXML
	private MenuBar menuBar;

	@FXML
	private Menu menuOptionFile;

	@FXML
	private MenuItem menuItemBooks;

	@FXML
	private MenuItem menuItemClose;
	@FXML
	private MenuItem menuItemAddBooks;
	@FXML
	private MenuItem menuItemPublishers;

	@FXML
	private void handleMenuAction(ActionEvent event) throws IOException {
		if (event.getSource() == menuItemBooks) {
			BookTableGateway.getInstance().updatePageController("");
			ViewSwitcher.getInstance().switchView(1, 0);
		} else if (event.getSource() == menuItemClose) {
			ViewSwitcher.getInstance().getLogger().info("Quiting Application based on the quit function");
			System.exit(0);
		} else if (event.getSource() == menuItemAddBooks) {
			ViewSwitcher.getInstance().getLogger().info("Adding book to database");
			ViewSwitcher.getInstance().switchView(2, -1);
		}
	}

	public MainViewController() {
	}

	public void setRootNode(BorderPane rootNode) {
		// this.rootNode = rootNode;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ViewSwitcher.getInstance().getLogger().info("Initialized Main Viewer");
	}

}
