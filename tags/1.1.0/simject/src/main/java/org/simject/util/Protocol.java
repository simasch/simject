package org.simject.util;

public enum Protocol {

	Xml("xml"), Binary("bin");
	
	private String string;

	Protocol(final String string) {
		this.string = string;
	}
	
	public String getString() {
		return this.string;
	}
}