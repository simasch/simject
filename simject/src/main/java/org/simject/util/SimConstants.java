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
 * Constants used by simject
 * 
 * @author Simon Martinelli
 */
public final class SimConstants {

	/**
	 * The default directory where the resource.xml should be stored
	 */
	public final static String DEFAULT_DIRECTORY = "META-INF/";

	/**
	 * Used in HTTP communication to define the method in the HTTP header
	 */
	public final static String PARAM_METHOD = "method";

	/**
	 * Used in HTTP communication to define the method parameter types in the
	 * HTTP header
	 */
	public final static String PARAM_TYPES = "types";

	/**
	 * Used in HTTP communication to define the delimiters used to separate the
	 * parameter types in the HTTP header
	 */
	public final static String PARAM_TYPE_DELIM = ",";
}
