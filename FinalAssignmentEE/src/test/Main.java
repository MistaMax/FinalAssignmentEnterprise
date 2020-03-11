package test;

import java.sql.SQLException;

import controller.*;
import model.*;

public class Main {

	public static void main(String[] args) {
		try {
			System.out.println(BookTableGateway.getInstance().findAuthor("id", "1").toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
