package model;

public class Page {
	private int pageSize, currPage, maxEntries;

	public Page() {
		pageSize = 50;
		currPage = 1;
		maxEntries = 50;
	}

	public Page(int pageSize, int currPage, int maxEntries) {
		super();
		this.pageSize = pageSize;
		this.currPage = currPage;
		this.maxEntries = maxEntries;
	}

	public Page(int maxEntries) {
		this.maxEntries = maxEntries;
		pageSize = 50;
		currPage = 1;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrPage() {
		return currPage;
	}

	public void setCurrPage(int currPage) {
		this.currPage = currPage;
	}

	public int getMaxEntries() {
		return maxEntries;
	}

	public void setMaxEntries(int maxEntries) {
		this.maxEntries = maxEntries;
	}
	
	public int getPageMin() {
		return (getCurrPage() - 1) * getPageSize();
	}

	public int getPageMax() {
		return (getCurrPage() - 1) * getPageSize() + getPageSize();
	}

	public String getLimit() {
		if(maxEntries < pageSize)
			return" LIMIT " + Integer.toString(getPageMin()) + "," + Integer.toString(maxEntries) + ";";
		return " LIMIT " + Integer.toString(getPageMin()) + "," + Integer.toString(getPageSize()) + ";";
	}

	@Override
	public String toString() {
		if(maxEntries < getPageMax())
			return String.format("Entries: %d to %d from %d", getPageMin() + 1, maxEntries, getMaxEntries());
		return String.format("Entries: %d to %d from %d", getPageMin() + 1, getPageMax(), getMaxEntries());
	}
}
