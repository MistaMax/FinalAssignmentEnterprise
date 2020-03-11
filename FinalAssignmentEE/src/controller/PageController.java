package controller;

import model.Page;

public class PageController {
	private static PageController instance = null;
	// private int pageSize, currPage;
	private Page page;

	private PageController() {
		page = new Page(BookTableGateway.getInstance().getOverallEntries());
	}

	public static PageController getInstance() {
		if (instance == null)
			instance = new PageController();
		return instance;
	}

	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	public void incCurrPage() {
		page.setCurrPage(page.getCurrPage() + 1);

	}

	public void decCurrPage() {
		page.setCurrPage(page.getCurrPage() - 1);
	}

}
