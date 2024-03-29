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
package org.simject.exception;

/**
 * This exception is thrown on configuration errors
 * 
 * @author Simon Martinelli
 */
public class SimConfigException extends SimException {

	private static final long serialVersionUID = 4480743918049637863L;

	public SimConfigException(final String message, final Exception cause) {
		super(message, cause);
	}
}
