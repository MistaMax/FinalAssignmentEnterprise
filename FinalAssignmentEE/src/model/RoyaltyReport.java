package model;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class RoyaltyReport {
	private Publisher publisher;
	private List<AuthorBook> aBList;

	public RoyaltyReport() {
		publisher = new Publisher();
		aBList = new LinkedList<AuthorBook>();
	}

	public RoyaltyReport(Publisher publisher) {
		this.publisher = publisher;
		aBList = new LinkedList<AuthorBook>();
	}

	public RoyaltyReport(Publisher publisher, List<AuthorBook> aBList) {
		this.publisher = publisher;
		this.aBList = aBList;
	}

	public Publisher getPublisher() {
		return publisher;
	}

	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

	public List<AuthorBook> getABList() {
		return aBList;
	}

	public void setABList(List<AuthorBook> aBList) {
		this.aBList = aBList;
	}

	public void addAB(AuthorBook aB) {
		aBList.add(aB);
	}

	public void generateXLSX(String fileName) {
		XSSFWorkbook workbook = new XSSFWorkbook();
		XSSFSheet sheet = workbook.createSheet("Royalty Report");
		generateHeader(sheet, new SimpleDateFormat("MM/dd/yyyy HH:mm").format(new Date()));

		int rowCount = 4;
		int currBookId = -1;
		float totalRoyalty = 0;
		Row row = null;
		Cell cell = null;
		for (int i = 0; i < aBList.size(); i++) {
			if (currBookId != aBList.get(i).getBook().getId()) {
				if (!(currBookId == -1)) {
					row = sheet.createRow(rowCount++);
					cell = row.createCell(2);
					cell.setCellValue("Total Royalty: ");
					cell = row.createCell(3);
					String royaltyPercent = String.format("%s%%",getRoyaltyPercent(totalRoyalty));
					cell.setCellValue(royaltyPercent);
					totalRoyalty = 0;
				}
				rowCount++;
				currBookId = aBList.get(i).getBook().getId();
				row = sheet.createRow(rowCount++);
				generateBookHeader(row, aBList.get(i).getBook());
			} else {
				row = sheet.createRow(rowCount++);
			}
			Author author = aBList.get(i).getAuthor();
			cell = row.createCell(2);
			cell.setCellValue(author.getFirstName() + " " + author.getLastName());
			cell = row.createCell(3);
			String royaltyPercent = String.format("%s%%",getRoyaltyPercent(aBList.get(i).getRoyaltyFloat()));
			cell.setCellValue(royaltyPercent);
			totalRoyalty += aBList.get(i).getRoyaltyFloat();
		}
		
		if (rowCount != 4) {
			row = sheet.createRow(rowCount++);
			cell = row.createCell(2);
			cell.setCellValue("Total Royalty: ");
			cell = row.createCell(3);
			String royaltyPercent = String.format("%s%%",getRoyaltyPercent(totalRoyalty));
			cell.setCellValue(royaltyPercent);
		}

		try {
			FileOutputStream outputStream = new FileOutputStream(fileName);
			workbook.write(outputStream);
			workbook.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void generateBookHeader(Row row, Book book) {
		Cell cell = row.createCell(0);
		cell.setCellValue(book.getTitle());
		cell = row.createCell(1);
		cell.setCellValue(book.getiSBN());
	}

	private void generateHeader(XSSFSheet sheet, String currDate) {
		Row row = sheet.createRow(0);
		Cell cell = row.createCell(0);

		cell.setCellValue("Royalty Report");
		row = sheet.createRow(1);
		cell = row.createCell(0);
		cell.setCellValue("Publisher: " + publisher.getName());

		row = sheet.createRow(2);
		cell = row.createCell(0);
		cell.setCellValue("Report generated on " + currDate);

		row = sheet.createRow(4);
		cell = row.createCell(0);
		cell.setCellValue("Book Title");

		cell = row.createCell(1);
		cell.setCellValue("ISBN");

		cell = row.createCell(2);
		cell.setCellValue("Author");

		cell = row.createCell(3);
		cell.setCellValue("Royalty");
	}
	
	private float getRoyaltyPercent(Float royalty) {
		return 100*royalty;
	}
}
