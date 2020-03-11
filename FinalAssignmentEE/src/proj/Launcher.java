package proj;

import java.net.URL;

import controller.BookListViewController;
import controller.BookTableGateway;
import controller.MainViewController;
import controller.PublisherTableGateway;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import view.ViewSwitcher;

/**
 * CS4743 Final Project by Max Crookshanks
 * 
 * @author Max Crookshanks
 *
 */
public class Launcher extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		// loads in the Main view that contains the menu at the top of the application
		URL fxmlFile = this.getClass().getResource("../view/MainView.fxml");
		FXMLLoader loader = new FXMLLoader(fxmlFile);
		MainViewController main = new MainViewController();
		loader.setController(main);
		BorderPane rootNode = loader.load();
		ViewSwitcher.getInstance().setRootNode(rootNode);
		main.setRootNode((BorderPane) rootNode);
		Scene scene = new Scene(rootNode, 600, 400);
		stage.setTitle("Book Checker");
		stage.setScene(scene);
		stage.show();
		// Loads in the book list
		BookListViewController controller = new BookListViewController(BookTableGateway.getInstance().getBooks());
		controller.setRootNode((BorderPane) rootNode);

		fxmlFile = this.getClass().getResource("../view/BookListView.fxml");
		loader = new FXMLLoader(fxmlFile);

		loader.setController(controller);

		Parent contentView = loader.load();

		rootNode.setCenter(contentView);
	}

	@Override
	public void stop() throws Exception {
		ViewSwitcher.getInstance().checkForSave();
		BookTableGateway.getInstance().closeConnection();
		PublisherTableGateway.getInstance().closeConnection();
		super.stop();
	}

	public static void main(String[] args) {
		launch(args);

	}

}
