package cop5556sp17;

import java.util.ArrayList;
import java.util.Arrays;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;

public class Scanner {


	/**
	 * Kind enum
	 */
	public static enum State {
		START, AFTER_EQ, AFTER_NOT, AFTER_LESS, AFTER_GREAT, AFTER_MINUS, AFTER_OR, AFTER_DIV, IN_DIGIT, IN_IDENT;
	}

	public static enum Kind {
		IDENT(""), INT_LIT(""), KW_INTEGER("integer"), KW_BOOLEAN("boolean"), KW_IMAGE("image"), KW_URL("url"), KW_FILE(
				"file"), KW_FRAME("frame"), KW_WHILE("while"), KW_IF("if"), KW_TRUE("true"), KW_FALSE("false"), SEMI(
						";"), COMMA(","), LPAREN("("), RPAREN(")"), LBRACE("{"), RBRACE("}"), ARROW("->"), BARARROW(
								"|->"), OR("|"), AND("&"), EQUAL("=="), NOTEQUAL("!="), LT("<"), GT(">"), LE("<="), GE(
										">="), PLUS("+"), MINUS("-"), TIMES("*"), DIV("/"), MOD("%"), NOT("!"), ASSIGN(
												"<-"), OP_BLUR("blur"), OP_GRAY("gray"), OP_CONVOLVE(
														"convolve"), KW_SCREENHEIGHT("screenheight"), KW_SCREENWIDTH(
																"screenwidth"), OP_WIDTH("width"), OP_HEIGHT(
																		"height"), KW_XLOC("xloc"), KW_YLOC(
																				"yloc"), KW_HIDE("hide"), KW_SHOW(
																						"show"), KW_MOVE(
																								"move"), OP_SLEEP(
																										"sleep"), KW_SCALE(
																												"scale"), EOF(
																														"eof");

		Kind(String text) {
			this.text = text;
		}

		final String text;

		String getText() {
			return text;
		}
	}

	/**
	 * Thrown by Scanner when an illegal character is encountered
	 */
	@SuppressWarnings("serial")
	public static class IllegalCharException extends Exception {
		public IllegalCharException(String message) {
			super(message);
		}
	}

	/**
	 * Thrown by Scanner when an int literal is not a value that can be
	 * represented by an int.
	 */
	@SuppressWarnings("serial")
	public static class IllegalNumberException extends Exception {
		public IllegalNumberException(String message) {
			super(message);
		}
	}

	/**
	 * Holds the line and position in the line of a token.
	 */
	static class LinePos {
		public final int line;
		public final int posInLine;

		public LinePos(int line, int posInLine) {
			super();
			this.line = line;
			this.posInLine = posInLine;
		}

		@Override
		public String toString() {
			return "LinePos [line=" + line + ", posInLine=" + posInLine + "]";
		}
	}

	public class Token {
		public final Kind kind;
		public final int pos; // position in input array
		public final int length;

		@Override
		  public int hashCode() {
		   final int prime = 31;
		   int result = 1;
		   result = prime * result + getOuterType().hashCode();
		   result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		   result = prime * result + length;
		   result = prime * result + pos;
		   return result;
		  }

		  @Override
		  public boolean equals(Object obj) {
		   if (this == obj) {
		    return true;
		   }
		   if (obj == null) {
		    return false;
		   }
		   if (!(obj instanceof Token)) {
		    return false;
		   }
		   Token other = (Token) obj;
		   if (!getOuterType().equals(other.getOuterType())) {
		    return false;
		   }
		   if (kind != other.kind) {
		    return false;
		   }
		   if (length != other.length) {
		    return false;
		   }
		   if (pos != other.pos) {
		    return false;
		   }
		   return true;
		  }



		  private Scanner getOuterType() {
		   return Scanner.this;
		  }
		public boolean isKind(Kind t){
			return kind == t;
		}
		// returns the text of this Token
		public String getText() {
			return chars.substring(pos, pos + length);
		}

		public Kind getKind() {
			return kind;
		}

		LinePos getLinePos() {
			// TODO IMPLEMENT THIS
			int lines = 0;
			int posInline = -1;
			int ipos = pos < chars.length() - 1 ? pos : chars.length() - 1;
			for (int i = 0; i <= ipos; i++) {
				if (chars.charAt(i) == '\n') {
					lines++;
					posInline = i;
				}
			}
			posInline = pos - posInline -1;
			LinePos lp = new LinePos(lines, posInline);
			return lp;
		}


		Token(Kind kind, int pos, int length) {
			this.kind = kind;
			this.pos = pos;
			this.length = length;
		}

		/**
		 * Precondition: kind = Kind.INT_LIT, the text can be represented with a
		 * Java int. Note that the validity of the input should have been
		 * checked when the Token was created. So the exception should never be
		 * thrown.
		 *
		 * @return int value of this token, which should represent an INT_LIT
		 * @throws NumberFormatException
		 */
		public int intVal() throws NumberFormatException {

			return Integer.parseInt(chars.substring(pos, pos + length));

		}
		@Override
		public String toString(){
			return this.getText();
		}

	}

	Scanner(String chars) {
		this.chars = chars;
		tokens = new ArrayList<Token>();
		linePosition = new ArrayList<Integer>();
	}


	public static int binarySearch(int[] linePosArray, int lowerbound, int upperbound, int key) {
		int position;
		int lb, ub;
		lb = lowerbound;
		ub = upperbound;
		// To start, find the subscript of the middle position.
		position = (ub + lb) / 2;

		while (ub > lb) {

			if (key >= linePosArray[lb] && key < linePosArray[ub]) {
				return linePosArray[lb];
			}

			else if (linePosArray[position] < key) {
				lb = position;

			}

			else if (linePosArray[position] > key) {
				ub = position;
			} else
				return linePosArray[position];
		}

		position = (lb + ub) / 2;
		return 0;
	}

	/**
	 * Initializes Scanner object by traversing chars and adding tokens to
	 * tokens list.
	 *
	 * @return this scanner
	 * @throws IllegalCharException
	 * @throws IllegalNumberException
	 */
	public Scanner scan() throws IllegalCharException, IllegalNumberException {

		int pos = 0;
		int length = chars.length();
		State state = State.START;
		int startPos = 0;
		int ch;
		while (pos <= length) {
			ch = pos < length ? chars.charAt(pos) : -1;
			switch (state) {
			case START: {
				pos = skipWhiteSpace(pos);
				ch = pos < length ? chars.charAt(pos) : -1;
				startPos = pos;
				switch (ch) {
				case -1: {
					tokens.add(new Token(Kind.EOF, pos, 0));
					pos++;
				}
					break;
				case '+': {
					tokens.add(new Token(Kind.PLUS, startPos, 1));
					pos++;
				}
					break;
				case '*': {
					tokens.add(new Token(Kind.TIMES, startPos, 1));
					pos++;
				}
					break;
				case '=': {
					state = State.AFTER_EQ;
					pos++;
				}
					break;
				case '0': {
					tokens.add(new Token(Kind.INT_LIT, startPos, 1));
					pos++;
				}
					break;
				case ';': {
					tokens.add(new Token(Kind.SEMI, startPos, 1));
					pos++;
				}
					break;
				case ',': {
					tokens.add(new Token(Kind.COMMA, startPos, 1));
					pos++;
				}
					break;
				case '(': {
					tokens.add(new Token(Kind.LPAREN, startPos, 1));
					pos++;
				}
					break;
				case ')': {
					tokens.add(new Token(Kind.RPAREN, startPos, 1));
					pos++;
				}
					break;
				case '{': {
					tokens.add(new Token(Kind.LBRACE, startPos, 1));
					pos++;
				}
					break;
				case '}': {
					tokens.add(new Token(Kind.RBRACE, startPos, 1));
					pos++;
				}
					break;
				case '|': {
					state = State.AFTER_OR;
					pos++;
				}
					break;
				case '&': {
					tokens.add(new Token(Kind.AND, startPos, 1));
					pos++;
				}
					break;
				case '<': {
					state = State.AFTER_LESS;
					pos++;
				}
					break;
				case '>': {
					state = State.AFTER_GREAT;
					pos++;
				}
					break;
				case '-': {
					state = State.AFTER_MINUS;
					pos++;
				}
					break;
				case '/': {
					state = State.AFTER_DIV;
					pos++;
				}
					break;
				case '%': {
					tokens.add(new Token(Kind.MOD, startPos, 1));
					pos++;
				}
					break;
				case '!': {
					state = State.AFTER_NOT;
					pos++;
				}
					break;
				default: {
					if (ch != -1 && Character.isDigit(ch)) {
						state = State.IN_DIGIT;
						pos++;
					} else if (ch != -1 && Character.isJavaIdentifierStart(ch)) {
						state = State.IN_IDENT;
						pos++;
					} else {
						throw new IllegalCharException("illegal char " + ch + " at pos " + pos);
					}
				}
				} // switch (ch)
			}
				break;
			case IN_DIGIT: {
				if (ch != -1 && Character.isDigit(ch)) {
					pos++;
				} else {
					String intTemp = chars.substring(startPos, pos);
					try {
						Integer.parseInt(intTemp);
					} catch (NumberFormatException ex) {
						throw new IllegalNumberException(" Number exceeded range");
					}
					tokens.add(new Token(Kind.INT_LIT, startPos, pos - startPos));
					state = State.START;
				}
			}
				break;
			case IN_IDENT: {
				if (ch != -1 && Character.isJavaIdentifierPart(ch)) {
					pos++;
				} else {

					String strTemp = chars.substring(startPos, pos);
					Kind value = null;
					switch (strTemp) {
					case "integer":
						value = Kind.KW_INTEGER;
						break;
					case "boolean":
						value = Kind.KW_BOOLEAN;
						break;
					case "image":
						value = Kind.KW_IMAGE;
						break;
					case "url":
						value = Kind.KW_URL;
						break;
					case "file":
						value = Kind.KW_FILE;
						break;
					case "frame":
						value = Kind.KW_FRAME;
						break;
					case "while":
						value = Kind.KW_WHILE;
						break;
					case "if":
						value = Kind.KW_IF;
						break;
					case "true":
						value = Kind.KW_TRUE;
						break;
					case "false":
						value = Kind.KW_FALSE;
						break;
					case "blur":
						value = Kind.OP_BLUR;
						break;
					case "gray":
						value = Kind.OP_GRAY;
						break;
					case "convolve":
						value = Kind.OP_CONVOLVE;
						break;
					case "screenheight":
						value = Kind.KW_SCREENHEIGHT;
						break;
					case "screenwidth":
						value = Kind.KW_SCREENWIDTH;
						break;
					case "width":
						value = Kind.OP_WIDTH;
						break;
					case "height":
						value = Kind.OP_HEIGHT;
						break;
					case "xloc":
						value = Kind.KW_XLOC;
						break;
					case "yloc":
						value = Kind.KW_YLOC;
						break;
					case "hide":
						value = Kind.KW_HIDE;
						break;
					case "show":
						value = Kind.KW_SHOW;
						break;
					case "move":
						value = Kind.KW_MOVE;
						break;
					case "sleep":
						value = Kind.OP_SLEEP;
						break;
					case "scale":
						value = Kind.KW_SCALE;
						break;
					default:
						value = Kind.IDENT;
						state = State.START;
					}

					tokens.add(new Token(value, startPos, pos - startPos));
					// pos++;
					state = State.START;

				}

			}
				break;
			case AFTER_EQ: {

				if (ch != -1 && chars.charAt(pos) == '=') {
					tokens.add(new Token(Kind.EQUAL, startPos, 2));
					pos++;
					state = State.START;
				}
				else {
					throw new IllegalCharException("illegal char " + ch + " at pos " + pos);
				}
			}
				break;

			case AFTER_NOT: {
				if (ch != -1 && chars.charAt(pos) == '=') {
					tokens.add(new Token(Kind.NOTEQUAL, startPos, 2));
					pos++;
					state = State.START;
				} else {
					tokens.add(new Token(Kind.NOT, startPos, 1));
					//pos++;
					state = State.START;
				}
			}
				break;

			case AFTER_LESS: {
				if (ch != -1 && chars.charAt(pos) == '=') {
					tokens.add(new Token(Kind.LE, startPos, 2));
					pos++;
					state = State.START;
				} else if (ch != -1 && chars.charAt(pos) == '-') {
					tokens.add(new Token(Kind.ASSIGN, startPos, 2));
					pos++;
					state = State.START;
				} else {
					tokens.add(new Token(Kind.LT, startPos, 1));
					//pos++;
					state = State.START;
				}
			}
				break;

			case AFTER_GREAT: {

				if (ch != -1 && chars.charAt(pos) == '=') {
					tokens.add(new Token(Kind.GE, startPos, 2));
					pos++;
					state = State.START;
				} else {
					tokens.add(new Token(Kind.GT, startPos, 1));
					//pos++;
					state = State.START;
				}
			}
				break;

			case AFTER_MINUS: {
				if (ch != -1 && chars.charAt(pos) == '>') {
					tokens.add(new Token(Kind.ARROW, startPos, 2));
					pos++;
					state = State.START;
				} else {
					tokens.add(new Token(Kind.MINUS, startPos, 1));
					//pos++;
					state = State.START;
				}
			}
				break;

			case AFTER_OR: {
				if (ch != -1 && chars.charAt(pos) == '-' && chars.charAt(pos + 1) == '>') {
					tokens.add(new Token(Kind.BARARROW, startPos, 3));
					pos = pos + 2;
					state = State.START;
				} else if (ch != -1 && chars.charAt(pos) == '-' && chars.charAt(pos + 1) != '>') {
					tokens.add(new Token(Kind.OR, startPos, 1));
					tokens.add(new Token(Kind.MINUS, startPos, 2));
					pos = pos++;
					state = State.START;
				} else {
					tokens.add(new Token(Kind.OR, startPos, 1));
					//pos++;
					state = State.START;
				}
			}
				break;

			case AFTER_DIV: {// comments
				int posTemp = pos;
				if (ch != -1 && chars.charAt(posTemp) == '*') {
					posTemp++;
					while ((posTemp + 1 < chars.length())
							&& (chars.charAt(posTemp) != '*' && chars.charAt(posTemp + 1) != '/')) {
						if (posTemp == '\n') {
							linePosition.add(posTemp + 1);
						}

						posTemp++;

					}

					pos = posTemp + 2;
					state = State.START;

				}

				else {
					tokens.add(new Token(Kind.DIV, startPos, 1));
					state = State.START;
				}
			}
				break;

			default:
				assert false;
			}// switch(state)
		} // while
			// tokens.add(new Token(Kind.EOF,pos,0));
		return this;

	}

	// tokens.add(new Token(Kind.EOF,pos,0));
	// return this;

	private int skipWhiteSpace(int pos) {

		int strTemp = pos;
		while ((strTemp < chars.length()) && Character.isWhitespace(chars.charAt(strTemp))) {
			if (chars.charAt(strTemp) == '\n') {
				linePosition.add(strTemp + 1);
			}
			strTemp++;
		}

		return strTemp;
	}

	final ArrayList<Token> tokens;
	final ArrayList<Integer> linePosition;
	final String chars;
	int tokenNum;

	/*
	 * Return the next token in the token list and update the state so that the
	 * next call will return the Token..
	 */
	public Token nextToken() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum++);
	}

	/*
	 * Return the next token in the token list without updating the state. (So
	 * the following call to next will return the same token.)
	 */
	public Token peek() {
		if (tokenNum >= tokens.size())
			return null;
		return tokens.get(tokenNum);
	}

	/**
	 * Returns a LinePos object containing the line and position in line of the
	 * given token.
	 *
	 * Line numbers start counting at 0
	 *
	 * @param t
	 * @return
	 */
	public LinePos getLinePos(Token t) {

		return t.getLinePos();
	}

}
