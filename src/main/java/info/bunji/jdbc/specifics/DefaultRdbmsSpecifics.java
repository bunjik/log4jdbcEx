/*
 * Copyright 2016 Fumiharu Kinoshita
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package info.bunji.jdbc.specifics;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author f.kinoshita
 */
public class DefaultRdbmsSpecifics implements RdbmsSpecifics {

	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss.SSS");

	public DefaultRdbmsSpecifics() {
		// do nothing.
	}

	/* (非 Javadoc)
	 * @see info.bunji.jdbc.specifics.RdbmsSpecifics#formatParameterObject(java.lang.Object)
	 */
	@Override
	public String formatParameterObject(Object object) {
		if (object == null) {
			return "NULL";
		} else {
			if (object instanceof String) {
				return "'" + escapeString((String) object) + "'";
			} else if (object instanceof Date) {
				synchronized (dateFormat) {
					return "'" + dateFormat.format(object) + "'";
				}
			} else {
				return object.toString();
			}
		}
	}

	/**
	 * Make sure string is escaped properly so that it will run in a SQL query analyzer tool.
	 * At this time all we do is double any single tick marks.
	 * Do not call this with a null string or else an exception will occur.
	 *
	 * @return the input String, escaped.
	 */
	String escapeString(String in) {
		StringBuilder out = new StringBuilder();
		for (int i = 0, j = in.length(); i < j; i++) {
			char c = in.charAt(i);
			if (c == '\'') {
				out.append(c);
			}
			out.append(c);
		}
		return out.toString();
	}
}
