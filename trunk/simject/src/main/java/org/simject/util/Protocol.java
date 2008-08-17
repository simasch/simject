/*
 * Copyright 2008 Simon Martinelli, Rebenweg 32, 3236 Gampelen, Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.simject.util;

/**
 * Enumeration containing the available protocol types
 * 
 * @author Simon Martinelli
 */
public enum Protocol {

	Xml("xml", "text/xml"), Binary("bin",
			"application/x-java-serialized-object");

	/**
	 * Holds the name of the protocol
	 */
	private String string;

	/**
	 * Holds the content type
	 */
	private String contentType;

	/**
	 * Constructor taking the name and the content type
	 * 
	 * @param string
	 * @param contentType
	 */
	private Protocol(final String string, final String contentType) {
		this.string = string;
		this.contentType = contentType;
	}

	/**
	 * Returns the protocol as string
	 * 
	 * @return
	 */
	public String getString() {
		return this.string;
	}

	/**
	 * Returns the content typ of the protcol
	 * 
	 * @return
	 */
	public String getContentType() {
		return this.contentType;
	}
}
