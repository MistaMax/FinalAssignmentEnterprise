package controller;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import model.Book;
import model.Publisher;
import view.ViewSwitcher;

public class PublisherTableGateway {
	private static PublisherTableGateway instance = null;
	private MysqlDataSource ds;
	Connection conn = null;

	private PublisherTableGateway() {
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

		ds.setURL(props.getProperty("MYSQL_DB_URL"));
		ds.setUser(props.getProperty("MYSQL_DB_USERNAME"));
		ds.setPassword(props.getProperty("MYSQL_DB_PASSWORD"));
		
		try {
			conn = ds.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static PublisherTableGateway getInstance() {
		if (instance == null)
			instance = new PublisherTableGateway();

		return instance;
	}
	
	public void closeConnection() {
		try {
			if(conn != null)
				conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public List<Publisher> fetchPublishers() {
		ViewSwitcher.getInstance().getLogger().info("Fetching Publishers");
		List<Publisher> publishers = new LinkedList<Publisher>();
		String query = "SELECT * FROM publisher";
		PreparedStatement smt = null;
		try {
			smt = conn.prepareStatement(query);
			ResultSet rs = smt.executeQuery(query);
			while (rs.next()) {
				publishers.add(getPublisherFromRs(rs));
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		ViewSwitcher.getInstance().getLogger().info("Done fetching Publishers");
		return publishers;
	}

	private Publisher getPublisherFromRs(ResultSet rs) throws SQLException {
		return new Publisher(rs.getInt("publisher_id"), rs.getString("name"));
	}
}
