package cop5556sp17;

import cop5556sp17.Scanner.Kind;
import static cop5556sp17.Scanner.Kind.*;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.AssignmentStatement;
import cop5556sp17.AST.BinaryChain;
import cop5556sp17.AST.BinaryExpression;
import cop5556sp17.AST.Block;
import cop5556sp17.AST.BooleanLitExpression;
import cop5556sp17.AST.Chain;
import cop5556sp17.AST.ChainElem;
import cop5556sp17.AST.ConstantExpression;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.Expression;
import cop5556sp17.AST.FilterOpChain;
import cop5556sp17.AST.FrameOpChain;
import cop5556sp17.AST.IdentChain;
import cop5556sp17.AST.IdentExpression;
import cop5556sp17.AST.IdentLValue;
import cop5556sp17.AST.IfStatement;
import cop5556sp17.AST.ImageOpChain;
import cop5556sp17.AST.IntLitExpression;
import cop5556sp17.AST.ParamDec;
import cop5556sp17.AST.Program;
import cop5556sp17.AST.SleepStatement;
import cop5556sp17.AST.Statement;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.WhileStatement;


public class Parser {

	/**
	 * Exception to be thrown if a syntax error is detected in the input.
	 * You will want to provide a useful error message.
	 *
	 */
	@SuppressWarnings("serial")
	public static class SyntaxException extends Exception {
		public SyntaxException(String message) {
			super(message);
		}
	}

	/**
	 * Useful during development to ensure unimplemented routines are
	 * not accidentally called during development.  Delete it when
	 * the Parser is finished.
	 *
	 */
	@SuppressWarnings("serial")
	public static class UnimplementedFeatureException extends RuntimeException {
		public UnimplementedFeatureException() {
			super();
		}
	}

	Scanner scanner;
	Token t;

	Parser(Scanner scanner) {
		this.scanner = scanner;
		t = scanner.nextToken();
	}

	/**
	 * parse the input using tokens from the scanner.
	 * Check for EOF (i.e. no trailing junk) when finished
	 *
	 * @throws SyntaxException
	 */
	Program parse() throws SyntaxException {
		Program p = program();
		matchEOF();
		return p;
	}

	//expression ∷= term ( relOp term)*
	Expression expression() throws SyntaxException {
		Expression e1 = term();
		Token firstTok = t;
		BinaryExpression b1 = null;
		while(checkRelOp()){//for the continuous loop
			//consume(); //after checking relOp
			Token op = relOp();
			Expression e2 = term();
			b1 = new BinaryExpression(firstTok,e1,op,e2);
			e1 = b1;
		}
		return e1;

	}

	private boolean checkRelOp() {
		if(t.kind == Kind.LT || t.kind == Kind.LE || t.kind == Kind.GT || t.kind == Kind.GE || t.kind == Kind.EQUAL || t.kind == Kind.NOTEQUAL)
			return true;
		else
		return false;
	}

	//term ∷= elem ( weakOp  elem)*
	Expression term() throws SyntaxException {
		Expression e1 = elem();
		BinaryExpression b1 = null;
		while(checkWeakOp()){// loop for relOp and term
			//consume();
			Token op = weakOp();
			Expression e2 = elem();
			b1 = new BinaryExpression(t,e1,op,e2);
			e1 = b1;
		}
		return e1;
	}

	private boolean checkWeakOp() {
		if(t.kind == Kind.PLUS || t.kind == Kind.MINUS || t.kind == Kind.OR)
			return true;
		else
		return false;
	}

	//elem ∷= factor ( strongOp factor)*
	Expression elem() throws SyntaxException {
		Expression e1 = factor();
		BinaryExpression b1 = null;
		Token firstTok =t;
		while(checkStrongOp()){
			//consume();//loop condition for strongOp and factor
			Token op = strongOp();
			Expression e2 = factor();
			b1 = new BinaryExpression(firstTok,e1,op,e2);
			e1 = b1;
		}
		return e1;
	}

	private boolean checkStrongOp() {

		if(t.kind == Kind.TIMES || t.kind == Kind.DIV || t.kind == Kind.AND || t.kind == Kind.MOD)
			return true;
		else
		return false;
	}

	/* factor ∷= IDENT | INT_LIT | KW_TRUE | KW_FALSE |
	 *  KW_SCREENWIDTH | KW_SCREENHEIGHT | ( expression )
	 */
	Expression factor() throws SyntaxException {
		Kind kind = t.kind;
		switch (kind) {
		case IDENT: {
			Token ident = consume();
			return new IdentExpression(ident);
		}

		case INT_LIT: {
			Token intlit = consume();
			return new IntLitExpression(intlit);
		}

		case KW_TRUE:{
			Token kwtrue = consume();
			return new BooleanLitExpression(kwtrue);
		}

		case KW_FALSE: {
			Token kwfalse = consume();
			return new BooleanLitExpression(kwfalse);
		}

		case KW_SCREENWIDTH:{
			Token sw = consume();
			return new ConstantExpression(sw);
		}
		case KW_SCREENHEIGHT: {
			Token sh = consume();
			return new ConstantExpression(sh);
		}
		case LPAREN: {
			consume();
			Expression e = expression();
			match(RPAREN);
			return e;
		}
		default:
			throw new SyntaxException("Illegal Factor");
		}//switch
	}


	/* arrowOp ∷= ARROW   |   BARARROW */
	Token arrowOp() throws SyntaxException {

		Kind kind = t.kind;
		switch(kind){
		case ARROW:{
			Token t = consume();
			return t;
		}

		case BARARROW:{
			Token t = consume();
			return t;
		}
		default:
			throw new SyntaxException("Illegal Arrow operation");

		}//switch
	}


	/* strongOp ∷= TIMES | DIV | AND | MOD */
	Token strongOp() throws SyntaxException {

		Kind kind = t.kind;
		switch(kind){
		case TIMES:{
			Token t = consume();
			return t;
		}

		case DIV:{
			Token t = consume();
			return t;
		}

		case AND:{
			Token t = consume();
			return t;
		}

		case MOD:{
			Token t = consume();
			return t;
		}
		default:
			throw new SyntaxException("Illegal Strong operation");
			}//switch
	}

	/* weakOp  ∷= PLUS | MINUS | OR */
	Token weakOp() throws SyntaxException {

		Kind kind = t.kind;
		switch(kind){
		case PLUS:{
			Token t = consume();
			return t;
		}
		case MINUS:{
			Token t = consume();
			return t;
			}
		case OR:{
			Token t = consume();
			return t;
			}

		default:
			throw new SyntaxException("Illegal Weak operation");
			}//switch
	}

	/* relOp ∷=  LT | LE | GT | GE | EQUAL | NOTEQUAL */
	Token relOp() throws SyntaxException {

		Kind kind = t.kind;
		switch(kind){
		case LT:{
			Token t = consume();
			return t;
		}
		case LE:{
			Token t = consume();
			return t;
		}
		case GT:{
			Token t = consume();
			return t;
		}
		case GE:{
			Token t = consume();
			return t;

		}
		case EQUAL:{
			Token t = consume();
			return t;
		}
		case NOTEQUAL:{
			Token t = consume();
			return t;
		}
		default:
		throw new SyntaxException("Illegal Rel operation");
		}//switch
	}

	/* imageOp ::= OP_WIDTH |OP_HEIGHT | KW_SCALE */
	Token imageOp() throws SyntaxException {

		Kind kind = t.kind;
		switch(kind){
		case OP_WIDTH:{
			Token t = consume();
			return t;
		}
		case OP_HEIGHT:{
			Token t = consume();
			return t;
		}

		case KW_SCALE:{
			Token t = consume();
			return t;
		}
		default:
			throw new SyntaxException("Illegal Image operation");
		}//switch
	}

	/* frameOp ::= KW_SHOW | KW_HIDE | KW_MOVE | KW_XLOC |KW_YLOC */
	Token frameOp() throws SyntaxException {

		Kind kind = t.kind;
		switch(kind){
		case KW_SHOW:{
			Token t = consume();
			return t;
		}
		case KW_HIDE:{
			Token t = consume();
			return t;
		}
		case KW_MOVE:{
			Token t = consume();
			return t;
		}
		case KW_XLOC:{
			Token t = consume();
			return t;
		}
		case KW_YLOC:{
			Token t = consume();
			return t;
		}
		default:
			throw new SyntaxException("illegal frame operation");
		}//switch
	}

	/* whileStatement ::= KW_WHILE ( expression ) block */
	WhileStatement whileStatement() throws SyntaxException {

		if(checkWhileStatement()){
		Token firstTok = match(KW_WHILE);
		match(LPAREN);
		Expression e = expression();
		match(RPAREN);
		Block b = block();
		return new WhileStatement(firstTok,e,b);
		}
		else{
		throw new SyntaxException("illegal While Operation");
		}
	}

	/* ifStatement ::= KW_IF ( expression ) block */
	IfStatement ifStatement() throws SyntaxException {

		if(checkIfStatement()){
			Token t = match(KW_IF);
			match(LPAREN);
			Expression e = expression();
			match(RPAREN);
			Block b = block();
			return new IfStatement(t,e,b);
		}
		else{
		throw new SyntaxException("illegal if operation");
		}
	}

	/* filterOp ::= OP_BLUR |OP_GRAY | OP_CONVOLVE */
	Token filterOp() throws SyntaxException {

		Kind kind = t.kind;
		switch(kind){
		case OP_BLUR:{
			Token t =  consume();
			return t;
		}

		case OP_GRAY:{
			Token t = consume();
			return t;
		}

		case OP_CONVOLVE:{
			Token t = consume();
			return t;
		}
		default:
			throw new SyntaxException("illegal filter operation");
		}//switch
	}

	/* block ::= { ( dec | statement) * } */
	Block block() throws SyntaxException {
		if(checkBlock()){
			ArrayList<Dec> dec = new ArrayList<>();
			ArrayList<Statement> stm = new ArrayList<>();
			Token firstTok = match(LBRACE);
			while(checkDec() || checkStatement()){
				if(checkDec()){
					dec.add(dec());
					//return Block();
				}
				else if(checkStatement()){
					stm.add(statement());
					//return Statement();
				}
				continue;
			}
			match(RBRACE);
			return new Block(firstTok,dec,stm);
		}
		else{
		throw new SyntaxException("Illegal Block");
		}
	}

	private boolean checkStatement() {
		if(t.kind.equals(OP_SLEEP) || checkWhileStatement() || checkIfStatement() || checkChainElem() || checkAssign())
			return true;
		else
		return false;
	}

	/* program ::=  IDENT param_dec ( , param_dec )*  block */
	Program program() throws SyntaxException {
		Kind kind = t.kind;
		if(kind.equals(IDENT)){
			Token t = consume();
			if(checkBlock()){
				Block b = block();
				return new Program(t,new ArrayList<ParamDec>(),b);
			}
			else if(checkParamDec()){
				ArrayList<ParamDec> p = new ArrayList<>();
				p.add(paramDec());
				while(this.t.kind.equals(COMMA)){
				consume();
				p.add(paramDec());
				}//while
				Block b = block();
				return new Program(t,p,b);
			}//elseif
		}//if
		//{
		throw new SyntaxException("Illegal Program");
		//}
	}

	private boolean checkBlock() {
		if(t.kind.equals(LBRACE))
			return true;
		else
			return false;
	}

	/* assign ::= IDENT ASSIGN expression */
	AssignmentStatement assign() throws SyntaxException {
		if(checkAssign()){
			Token t = match(IDENT);
			IdentLValue n = new IdentLValue(t);
			match(ASSIGN);
			Expression e = expression();
			return new AssignmentStatement(t,n,e);
		}
		else{
		throw new SyntaxException("Illegal Assign");
		}
	}

	Token paramDecKw() throws SyntaxException{//keyword class for param_dec
		Kind kind = t.kind;
		switch(kind){
		case KW_URL:{
			Token t = consume();
			return t;
		}
		case KW_FILE:{
			Token t = consume();
			return t;
		}

		case KW_INTEGER:{
			Token t = consume();
			return t;
		}

		case KW_BOOLEAN:{
			Token t = consume();
			return t;
		}
		default:
			throw new SyntaxException("Illegal ParamDec keyword operation");
		}//switch

	}

	/* paramDec ::= ( KW_URL | KW_FILE | KW_INTEGER | KW_BOOLEAN)  IDENT */
	ParamDec paramDec() throws SyntaxException {
		if(checkParamDec()){
			Token t = paramDecKw();
			Token ident = match(IDENT);
			return new ParamDec(t,ident);
		}
		else
			throw new SyntaxException("Illegal paramdec");
	}

	private boolean checkParamDec() {
		if(t.kind == Kind.KW_URL || t.kind == Kind.KW_FILE || t.kind == Kind.KW_INTEGER || t.kind == Kind.KW_BOOLEAN)
			return true;
		else
			return false;
	}

	Token decKw() throws SyntaxException{//keyword class for dec
		Kind kind = t.kind;
		switch(kind){
		case KW_INTEGER:{
			Token t = consume();
			return t;
		}

		case KW_BOOLEAN:{
			Token t = consume();
			return t;
		}

		case KW_IMAGE:{
			Token t = consume();
			return t;
		}

		case KW_FRAME:{
			Token t = consume();
			return t;
		}
		default:
			throw new SyntaxException("Illegal Dec Keyword");
		}//switch

	}
	/* dec ::= (  KW_INTEGER | KW_BOOLEAN
	 * | KW_IMAGE | KW_FRAME)    IDENT */
	Dec dec() throws SyntaxException {
			if(checkDec()){
				Token t = decKw();
				Token ident = match(IDENT);
				return new Dec(t,ident);
			}

			else
				throw new SyntaxException("Illegal Dec Operation");
	}

	private boolean checkDec() {
		if(t.kind == Kind.KW_INTEGER || t.kind == Kind.KW_BOOLEAN || t.kind == Kind.KW_IMAGE || t.kind == Kind.KW_FRAME)
			return true;
		else
		return false;
	}

	/* statement ::=   OP_SLEEP expression ; | whileStatement
	 * | ifStatement | chain ; | assign ; */
	Statement statement() throws SyntaxException {
		Token firstTok = t;
		if(t.kind == Kind.OP_SLEEP){
			match(OP_SLEEP);
			Expression e = expression();
			match(SEMI);
			return new SleepStatement(t,e);
		}
		else if(checkWhileStatement()){
			return whileStatement();
		}
		else if(checkIfStatement()){
			return ifStatement();

		}
		else if(!(scanner.peek().isKind(ASSIGN)) && checkChainElem()){
			Chain c1 = chain();
			match(SEMI);
			return c1;
		}
		else if(checkAssign()){
			AssignmentStatement a1 = assign();
			match(SEMI);
			return a1;
		}
		//exception
		else{
		throw new SyntaxException("Illegal Statement");
		}
	}

	private boolean checkAssign() {
		if(t.kind.equals(IDENT))
			return true;
		else
			return false;
	}

	private boolean checkIfStatement() {
		if(t.kind.equals(KW_IF))
			return true;
		else
			return false;
	}

	private boolean checkWhileStatement() {
		if(t.kind.equals(KW_WHILE))
			return true;
		else
			return false;
	}

	/* chain ::=  chainElem arrowOp chainElem ( arrowOp  chainElem)* */
	Chain chain() throws SyntaxException {
		if(checkChainElem()){
			Chain c1 = chainElem();
			Token arrow = arrowOp();
			ChainElem c2 = chainElem();
			BinaryChain b1 = new BinaryChain(t,c1,arrow,c2);
			BinaryChain temp = null;
			while(checkArrowOp()){// loop for arrowOp chainElem
				Token arrow1 = arrowOp();
				ChainElem c3 = chainElem();
				temp = b1;
				b1 = new BinaryChain(t,temp,arrow1,c3);
			}
			return b1;
		}
		else{
		throw new SyntaxException("Illegal Chain");
		}
	}

	private boolean checkChainElem() {
		if(t.kind.equals(IDENT) || checkFilterOp() || checkImageOp() || checkFrameOp())
			return true;
		else
			return false;
	}

	private boolean checkArrowOp() {
		if(t.kind == Kind.ARROW || t.kind == Kind.BARARROW)
			return true;
		else
		return false;
	}

	/* chainElem ::= IDENT | filterOp arg | frameOp arg | imageOp arg */
	ChainElem chainElem() throws SyntaxException {
		Kind kind = t.kind;
		if(kind.equals(IDENT)){
			Token id =  consume();
			return new IdentChain(id);
		}
		else if(checkFilterOp()){
			Token filterOp = filterOp();
			Tuple arg = arg();
			return new FilterOpChain(filterOp,arg);
		}
		else if(checkFrameOp()){
			Token frameOp = frameOp();
			Tuple arg = arg();
			return new FrameOpChain(frameOp,arg);
		}
		else if(checkImageOp()){
			Token imageOp = imageOp();
			Tuple arg = arg();
			return new ImageOpChain(imageOp,arg);
		}
		else{
		throw new SyntaxException("illegal chain element");
		}
	}

	private boolean checkFrameOp() {
		if(t.kind == Kind.KW_SHOW || t.kind == Kind.KW_HIDE || t.kind == Kind.KW_MOVE || t.kind == Kind.KW_XLOC || t.kind == Kind.KW_YLOC)
			return true;
		else
		return false;
	}

	private boolean checkImageOp() {
		if(t.kind == Kind.OP_WIDTH || t.kind == Kind.OP_HEIGHT || t.kind == Kind.KW_SCALE)
			return true;
		else
		return false;
	}

	private boolean checkFilterOp() {

		if(t.kind == Kind.OP_BLUR || t.kind == Kind.OP_GRAY || t.kind == Kind.OP_CONVOLVE)
			return true;
		else
		return false;
	}

	/* arg ::= ε | ( expression (   ,expression)* ) */
	Tuple arg() throws SyntaxException {
		Kind kind = t.kind;
		List<Expression> arg = new ArrayList<>();
		if(kind.equals(LPAREN)){
			Token t = match(LPAREN);
			arg.add(expression());
			while(this.t.kind.equals(COMMA)){
				consume();
				arg.add(expression());
			}
			match(RPAREN);
			return new Tuple(t,arg);
		}
		else{
		//throw new SyntaxException("Illegal Arg");
		return new Tuple(t,arg);
		}
	}

	/**
	 * Checks whether the current token is the EOF token. If not, a
	 * SyntaxException is thrown.
	 *
	 * @return
	 * @throws SyntaxException
	 */
	private Token matchEOF() throws SyntaxException {
		if (t.isKind(EOF)) {
			return t;
		}
		throw new SyntaxException("expected EOF");
	}

	/**
	 * Checks if the current token has the given kind. If so, the current token
	 * is consumed and returned. If not, a SyntaxException is thrown.
	 *
	 * Precondition: kind != EOF
	 *
	 * @param kind
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind kind) throws SyntaxException {
		if (t.isKind(kind)) {
			return consume();
		}
		throw new SyntaxException("saw " + t.kind + "expected " + kind);
	}

	/**
	 * Checks if the current token has one of the given kinds. If so, the
	 * current token is consumed and returned. If not, a SyntaxException is
	 * thrown.
	 *
	 * * Precondition: for all given kinds, kind != EOF
	 *
	 * @param kinds
	 *            list of kinds, matches any one
	 * @return
	 * @throws SyntaxException
	 */
	private Token match(Kind... kinds) throws SyntaxException {
		// TODO. Optional but handy
		return null; //replace this statement
	}

	/**
	 * Gets the next token and returns the consumed token.
	 *
	 * Precondition: t.kind != EOF
	 *
	 * @return
	 *
	 */
	private Token consume() throws SyntaxException {
		Token tmp = t;
		t = scanner.nextToken();
		return tmp;
	}

}