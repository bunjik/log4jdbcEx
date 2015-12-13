/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package info.bunji.jdbc.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

//import org.apache.openjpa.lib.util.J2DoPrivHelper;

/*
 * Lots of this could be abstracted out into a word-wrapping class.
 */

/**
 * Converts single-line SQL strings into nicely-formatted multi-line, indented
 * statements. Example: from PERSON t0, COMPANY t1 WHERE t0.ID = 10 AND \
 * t0.COMPANY_ID = t1.ID AND t1.NAME = 'OpenJPA'</code> becomes
 * <code>SELECT * FROM PERSON t0, COMPANY t1
 * WHERE t0.ID = 10 AND t0.COMPANY_ID = t1.ID AND t1.NAME = 'OpenJPA'\
 * </code> and
 * <code>INSERT INTO PERSON VALUES('Patrick', 'Linskey', 'OpenJPA', \
 * '202 595 2064 x1111')</code> becomes
 * <code>INSERT INTO PERSON VALUES('Patrick', 'Linskey', 'OpenJPA', '202
 * 595 2064 x1111')</code> etc.
 *
 * @author Patrick Linskey
 */
public class SQLFormatter {

	private boolean multiLine = false;
	private boolean doubleSpace = true;
	private String newline = System.getProperty("line.separator");
//	private int lineLength = -1;
	private int lineLength = 9999;
	private String wrapIndent = "    ";
	private String clauseIndent = "  ";

	private static List<String> SQL_KEYWORDS = Arrays.asList(
			"SELECT",
			"FROM",
			"WHERE",
			"AND",
			"OR",
			"IN",
			"NOT NULL",
			"HAVING",
			"ORDER BY",
			"GROUP BY",
			"VALUES",
			"UNION",
			"ALTER",
			"SET",
			"MINUS"
			);

	private static final String[] selectSeparators = new String[] {
		"FROM ", "WHERE ", "ORDER BY ", // ### is this order correct?
		"GROUP BY ", "HAVING ", "AND ", "OR " };

	private static final String[] insertSeparators = new String[] {
		"VALUES ", };

	private static final String[] updateSeparators = new String[] {
		"SET ", "WHERE ", };

	private static final String[] deleteSeparators = new String[] {
		"WHERE ", };

	private static final String[] createTableSeparators = new String[] {
		"( ", };

	private static final String[] createIndexSeparators = new String[] {
		"ON ", "( ", };

	public void setNewline(String val) {
		newline = val;
	}

	public String getNewline() {
		return newline;
	}

	public void setLineLength(int val) {
		lineLength = val;
	}

	public int getLineLength() {
		return lineLength;
	}

	public void setWrapIndent(String val) {
		wrapIndent = val;
	}

	public String getWrapIndent() {
		return wrapIndent;
	}

	public void setClauseIndent(String val) {
		clauseIndent = val;
	}

	public String getClauseIndent() {
		return clauseIndent;
	}

	/**
	 * If true, then try to parse multi-line SQL statements.
	 */
	public void setMultiLine(boolean multiLine) {
		this.multiLine = multiLine;
	}

	/**
	 * If true, then try to parse multi-line SQL statements.
	 */
	public boolean getMultiLine() {
		return this.multiLine;
	}

	/**
	 * If true, then output two lines after multi-line statements.
	 */
	public void setDoubleSpace(boolean doubleSpace) {
		this.doubleSpace = doubleSpace;
	}

	/**
	 * If true, then output two lines after multi-line statements.
	 */
	public boolean getDoubleSpace() {
		return this.doubleSpace;
	}

	public Object prettyPrint(Object sqlObject) {
		if (!multiLine) {
			return prettyPrintLine(sqlObject);
		} else {
			StringBuilder sql = new StringBuilder(sqlObject.toString());
			StringBuilder buf = new StringBuilder(sql.length());

			while (sql.length() > 0) {
				String line = null;

				int index = Math.max(sql.toString().indexOf(";\n"), sql.toString().indexOf(";\r"));
				if (index == -1)
					line = sql.toString();
				else
					line = sql.substring(0, index + 2);

				// remove the current line from the sql buffer
				sql.delete(0, line.length());

				buf.append(prettyPrintLine(line));
				for (int i = 0; i < 1 + (getDoubleSpace() ? 1 : 0); i++)
					buf.append(newline);
			}

			return buf.toString();
		}
	}

	private static final Pattern regex1 = Pattern.compile("\\/\\*.*?\\*\\/", Pattern.DOTALL);
	private static final Pattern regex2 = Pattern.compile("--.*$", Pattern.MULTILINE);
//	private static final Pattern regex3 = Pattern.compile("^\\n", Pattern.MULTILINE);
	private static final Pattern regex3 = Pattern.compile("\\r|\\n", Pattern.MULTILINE);

	// sql を構文単位に分割
	// "'"による文字列を考慮する必要がある
	public String format(String sqlStr) {
		String sql = sqlStr.trim();

		// 複数行コメントの除去
		sql = regex1.matcher(sql).replaceAll("").trim();
		// 単一行コメントの除去
		sql = regex2.matcher(sql).replaceAll("").trim();
		// 空行の削除
		sql = regex3.matcher(sql).replaceAll("").trim();

System.out.println(SQL_KEYWORDS);

		int start = 0;
		int end = -1;
		StringBuilder formatSql = new StringBuilder(sql.length());
		String upperSql = sql.toUpperCase();
System.out.println(upperSql);
try {
		while(true) {
			for (String keyword : SQL_KEYWORDS) {
				end = upperSql.indexOf(keyword + " ", start);
System.out.println("check " + keyword + " " + start + "/" + end);
				if (end != -1) {
System.out.println("keyword="  + keyword + " [" + 	sql.substring(start, keyword.length() + 1) + "]" + start + "/" + end);
					formatSql.append(sql.substring(start, keyword.length() + 1));
System.out.println("[" + formatSql + "]");
					start = end + keyword.length() + 1;
//					break;
				}
			}
			if (end == -1) {
				break;
			}
		}
} catch(Exception e) {
	System.out.println(start + " - " + end);
	e.printStackTrace();
}
 		return formatSql.toString();
	}

	private Object prettyPrintLine(Object sqlObject) {
		String sql = sqlObject.toString().trim();
		String lowerCaseSql = sql.toLowerCase();

		String[] separators;
		if (lowerCaseSql.startsWith("select"))
			separators = selectSeparators;
		else if (lowerCaseSql.startsWith("insert"))
			separators = insertSeparators;
		else if (lowerCaseSql.startsWith("update"))
			separators = updateSeparators;
		else if (lowerCaseSql.startsWith("delete"))
			separators = deleteSeparators;
		else if (lowerCaseSql.startsWith("create table"))
			separators = createTableSeparators;
		else if (lowerCaseSql.startsWith("create index"))
			separators = createIndexSeparators;
		else
			separators = new String[0];

		String indent = clauseIndent;
		if (lineLength == -1) indent = " ";

		int start = 0;
		int end = -1;
		StringBuilder clause;
		List<StringBuilder> clauses = new ArrayList<StringBuilder>();
		clauses.add(new StringBuilder());
		for (int i = 0; i < separators.length; i++) {
			end = lowerCaseSql.indexOf(" " + separators[i].toLowerCase(), start);
			if (end == -1)
				break;

			clause = clauses.get(clauses.size() - 1);
			clause.append(sql.substring(start, end));

			clause = new StringBuilder();
			clauses.add(clause);
			clause.append(indent);
			clause.append(separators[i]);

			start = end + 1 + separators[i].length();
		}

		clause = clauses.get(clauses.size() - 1);
		clause.append(sql.substring(start));

		StringBuilder pp = new StringBuilder(sql.length());
		for (Iterator<StringBuilder> iter = clauses.iterator(); iter.hasNext();) {
			String buf = iter.next().toString();
//System.out.println(buf);
			if (lineLength != -1) {
				pp.append(wrapLine(buf));
				if (iter.hasNext()) {
					pp.append(newline);
				}
			} else {
				pp.append(buf);
			}
		}

		return pp.toString().trim();
	}

	private String wrapLine(String line) {
		StringBuilder lines = new StringBuilder(line.length());

		// ensure that any leading whitespace is preserved.
		for (int i = 0; i < line.length()
				&& (line.charAt(i) == ' ' || line.charAt(i) == '\t'); i++) {
			lines.append(line.charAt(i));
		}

		StringTokenizer tok = new StringTokenizer(line);
		int length = 0;
		String elem;
		while (tok.hasMoreTokens()) {
			elem = tok.nextToken();
			length += elem.length();

			// if we would have exceeded the max, write out a newline
			// before writing the elem.
			if (length >= lineLength) {
				lines.append(newline);
				lines.append(wrapIndent);
				lines.append(elem);
				lines.append(' ');
				length = wrapIndent.length() + elem.length() + 1;
				continue;
			}

			// if the current length is greater than the max, then the
			// last word alone was too long, so just write out a
			// newline and move on.
			if (elem.length() >= lineLength) {
				lines.append(elem);
				if (tok.hasMoreTokens())
					lines.append(newline);
				lines.append(wrapIndent);
				length = wrapIndent.length();
				continue;
			}

			lines.append(elem);
			lines.append(' ');
			length++;
		}

		return lines.toString();
	}

//	public static void main(String[] args) {
//		SQLFormatter formatter = new SQLFormatter();
//		for (int i = 0; i < args.length; i++) {
//			// START - ALLOW PRINT STATEMENTS
//			System.out.println(formatter.prettyPrint(args[i]));
//			// STOP - ALLOW PRINT STATEMENTS
//		}
//	}
}