package model;

public class Publisher {
	private int id;
	private String name;
	
	public Publisher() {
		this.id = 0;
		this.name = "";
	}
	
	public Publisher(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return Integer.toString(id) + ":" + name;
	}
}
