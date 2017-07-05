package org.rootio.tools.utils;

public class FrequencyUnit {

	private int id;
	private String title;

	public FrequencyUnit(int id, String title) {
		this.id = id;
		this.title = title;
	}

	public int getId() {
		return this.id;
	}

	String getTitle() {
		return this.title;
	}

	@Override
	public String toString() {
		return this.title;
	}
}
