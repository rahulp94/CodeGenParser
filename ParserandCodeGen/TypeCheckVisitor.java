package cop5556sp17;

import cop5556sp17.AST.ASTNode;
import cop5556sp17.AST.ASTVisitor;
import cop5556sp17.AST.Tuple;
import cop5556sp17.AST.Type;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import java.util.ArrayList;
import java.util.List;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.LinePos;
import cop5556sp17.Scanner.Token;
import static cop5556sp17.AST.Type.TypeName.*;
import static cop5556sp17.Scanner.Kind.ARROW;
import static cop5556sp17.Scanner.Kind.KW_HIDE;
import static cop5556sp17.Scanner.Kind.KW_MOVE;
import static cop5556sp17.Scanner.Kind.KW_SHOW;
import static cop5556sp17.Scanner.Kind.KW_XLOC;
import static cop5556sp17.Scanner.Kind.KW_YLOC;
import static cop5556sp17.Scanner.Kind.OP_BLUR;
import static cop5556sp17.Scanner.Kind.OP_CONVOLVE;
import static cop5556sp17.Scanner.Kind.OP_GRAY;
import static cop5556sp17.Scanner.Kind.OP_HEIGHT;
import static cop5556sp17.Scanner.Kind.OP_WIDTH;
import static cop5556sp17.Scanner.Kind.*;

public class TypeCheckVisitor implements ASTVisitor {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		public TypeCheckException(String message) {
			super(message);
		}
	}

	SymbolTable symtab = new SymbolTable();

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		Chain bc1 = (Chain) binaryChain.getE0().visit(this, null);
		Chain bc2 = (Chain) binaryChain.getE1().visit(this, null);

		Kind bcToken = binaryChain.getE1().firstToken.kind;

		if (binaryChain.getArrow().kind.equals(ARROW)) {

			if (bc1.getType().equals(URL) && bc2.getType().equals(IMAGE))
			{
				binaryChain.type = IMAGE;
			}

			else if (bc1.getType().equals(FILE) && bc2.getType().equals(IMAGE))
			{
				binaryChain.type = IMAGE;
			}

			else if (bc1.getType().equals(FRAME) && binaryChain.getE1() instanceof FrameOpChain)
			{
				if (bcToken.equals(KW_XLOC) || bcToken.equals(KW_YLOC))
				{
					binaryChain.type = INTEGER;
				}

				else if (bcToken.equals(KW_SHOW) || bcToken.equals(KW_HIDE) || bcToken.equals(KW_MOVE))
				{
					binaryChain.type = FRAME;
				}

				else
				{
					throw new TypeCheckException("Error ");
				}

			}

			else if (bc1.getType().equals(IMAGE) && binaryChain.getE1() instanceof ImageOpChain)
			{
				if (bcToken.equals(OP_WIDTH) || bcToken.equals(OP_HEIGHT))
				{
					binaryChain.type = INTEGER;
				} else if (bcToken.equals(KW_SCALE)){
					binaryChain.type = IMAGE;
				}
				else
				{
					throw new TypeCheckException("Error ");
				}
			}

			else if (bc1.getType().equals(IMAGE) && bc2.getType().equals(FRAME))
			{
				binaryChain.type = FRAME;
			}

			else if (bc1.getType().equals(IMAGE) && bc2.getType().equals(FILE))
			{
				binaryChain.type = NONE;
			}

			else if (bc1.getType().equals(IMAGE) && binaryChain.getE1() instanceof IdentChain)
			{
				if(((IdentChain) binaryChain.getE1()).getType() == TypeName.IMAGE){
				binaryChain.type = IMAGE;
				}
			}

			else if (bc1.getType().equals(INTEGER) && binaryChain.getE1() instanceof IdentChain)
			{
				if(((IdentChain) binaryChain.getE1()).getType() == TypeName.INTEGER){
				binaryChain.type = INTEGER;
				}
			}

			else if (binaryChain.getArrow().kind.equals(ARROW) || binaryChain.getArrow().kind.equals(BARARROW))
			{

				if (bc1.getType().equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain)

				{

					if (bcToken.equals(OP_GRAY) || bcToken.equals(OP_BLUR) || bcToken.equals(OP_CONVOLVE))

					{
						binaryChain.type = IMAGE;
					}

					else
					{
						throw new TypeCheckException("Error ");
					}
				}
				else
				{
					throw new TypeCheckException("Error Bararrow");
				}
			}

			else
			{
				throw new TypeCheckException("Error ");
			}
		}
		else if (binaryChain.getArrow().kind.equals(BARARROW))
		{

			if (bc1.getType().equals(IMAGE) && binaryChain.getE1() instanceof FilterOpChain)

			{

				if (bcToken.equals(OP_GRAY) || bcToken.equals(OP_BLUR) || bcToken.equals(OP_CONVOLVE))

				{
					binaryChain.type = IMAGE;
				}

				else
				{
					throw new TypeCheckException("Error ");
				}
			}
			else
			{
				throw new TypeCheckException("Error Bararrow");
			}
		}
		else
		{
			throw new TypeCheckException(" Error");
		}

		return binaryChain;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {
		Expression be1 = (Expression) binaryExpression.getE0().visit(this, null);
		Expression be2 = (Expression) binaryExpression.getE1().visit(this, null);

		//Token beToken = binaryExpression.getE1().firstToken;

		if(binaryExpression.getOp().kind.equals(PLUS) || binaryExpression.getOp().kind.equals(MINUS)){
			if(be1.getType().equals(INTEGER) && be2.getType().equals(INTEGER)){
				binaryExpression.type = INTEGER;
			}
			else if(be1.getType().equals(IMAGE) && be2.getType().equals(IMAGE)){
				binaryExpression.type = IMAGE;
			}
			else{
				throw new TypeCheckException("Error" + binaryExpression.firstToken.getText());
			}
		}

		else if(binaryExpression.getOp().kind.equals(TIMES) || binaryExpression.getOp().kind.equals(DIV)){
			if(be1.getType().equals(INTEGER) && be2.getType().equals(INTEGER)){
				binaryExpression.type = INTEGER;
			}

			else{
				throw new TypeCheckException("Error" + binaryExpression.firstToken.getText());			}
		}

		else if(binaryExpression.getOp().kind.equals(TIMES)){
			if(be1.getType().equals(INTEGER) && be2.getType().equals(IMAGE)){
				binaryExpression.type = IMAGE;
			}
			else if(be1.getType().equals(IMAGE) && be2.getType().equals(INTEGER)){
				binaryExpression.type = IMAGE;
			}
			else{
				throw new TypeCheckException("Error" + binaryExpression.firstToken.getText());			}
		}

		else if(binaryExpression.getOp().kind.equals(TIMES) || binaryExpression.getOp().kind.equals(DIV)
				|| binaryExpression.getOp().kind.equals(MOD)){
			if(be1.getType().equals(IMAGE) && be2.getType().equals(INTEGER)){
				binaryExpression.type = IMAGE;
			}
		}

		else if(binaryExpression.getOp().kind.equals(AND) && binaryExpression.getOp().kind.equals(OR)){
			if(be1.getType().equals(BOOLEAN) && be2.getType().equals(BOOLEAN)){
				binaryExpression.type = TypeName.BOOLEAN;
			}
		}

		else if(binaryExpression.getOp().kind.equals(LT) || binaryExpression.getOp().kind.equals(GT)
				|| binaryExpression.getOp().kind.equals(LE) || binaryExpression.getOp().kind.equals(GE)){
			if(be1.getType().equals(INTEGER) && be2.getType().equals(INTEGER)){
				binaryExpression.type = BOOLEAN;
			}
			else if(be1.getType().equals(BOOLEAN) && be2.getType().equals(BOOLEAN)){
				binaryExpression.type = BOOLEAN;
			}
			else{
				throw new TypeCheckException("Error" + binaryExpression.firstToken.getText());			}
		}

		else if(binaryExpression.getOp().kind.equals(EQUAL) || binaryExpression.getOp().kind.equals(NOTEQUAL)){
			if(be1.getType().equals(be2.getType())){
				binaryExpression.type = BOOLEAN;
			}

			else{
				throw new TypeCheckException("Error" + binaryExpression.firstToken.getText());			}
		}

		else {
			throw new TypeCheckException("Error" + binaryExpression.firstToken.getText());
			}
		return binaryExpression;
	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		symtab.enterScope();
		List<Dec> decList = block.getDecs();
		List<Statement> stmList = block.getStatements();
		for (Dec d : decList) {
			d.visit(this, null);
		}
		for (Statement s : stmList) {
			s.visit(this, null);
		}
		symtab.leaveScope();
		return block;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		booleanLitExpression.type = BOOLEAN;

		return booleanLitExpression;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		if (filterOpChain.getArg().getExprList().size() == 0) {
			filterOpChain.type = IMAGE;
		}
		else{
			throw new TypeCheckException("Error " + filterOpChain.firstToken.getText());
		}
		return filterOpChain;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, null);
		Kind frameOp = frameOpChain.getFirstToken().kind;
		if(frameOp.equals(KW_SHOW) || frameOp.equals(KW_HIDE)){
			if (frameOpChain.getArg().getExprList().size() == 0) {
				frameOpChain.type = NONE;
			}
			else{
				throw new TypeCheckException("Error " + frameOpChain.firstToken.getText());
			}

		}
		else if(frameOp.equals(KW_XLOC) || frameOp.equals(KW_YLOC)){
			if (frameOpChain.getArg().getExprList().size() == 0) {
				frameOpChain.type = INTEGER;
			}
			else{
				throw new TypeCheckException("Error " + frameOpChain.firstToken.getText());
			}
		}
		else if(frameOp.equals(KW_MOVE)){
			if (frameOpChain.getArg().getExprList().size() == 2) {
				frameOpChain.getArg().visit(this, arg);
				frameOpChain.type = NONE;
			}
			else{
				throw new TypeCheckException("Error " + frameOpChain.firstToken.getText());
			}
		}
		else{
			throw new TypeCheckException("Error " + frameOpChain.firstToken.getText());
		}

		return frameOpChain;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Dec ident = symtab.lookup(identChain.firstToken.getText());
		//identChain.setType(Type.getTypeName(identChain.firstToken));
		if(!ident.equals(null)){
			ident.setType(Type.getTypeName(ident.firstToken));
			identChain.setType(ident.getType2());
			identChain.setDec(ident);
			return identChain;
		}
		else{
			throw new TypeCheckException("Error ");
		}
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		Dec ident = symtab.lookup(identExpression.firstToken.getText());
		if(!ident.equals(null))
		{
			ident.setType(Type.getTypeName(ident.firstToken));
			identExpression.setDec(ident);
			identExpression.setType(ident.getType2());
			return identExpression;

		}
		else{
			throw new TypeCheckException(" Error Identlvalue");
		}
	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.getB().visit(this, null);
		Expression i1 = (Expression) ifStatement.getE().visit(this, null);
		if(i1.getType().equals(BOOLEAN)){
				return ifStatement;
			}

		else{
			throw new TypeCheckException("Error if");
		}
	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		intLitExpression.type = INTEGER;

		return intLitExpression;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		Expression s1 = (Expression) sleepStatement.getE().visit(this, null);
		if(s1.getType().equals(INTEGER)){
			return sleepStatement;
			}

		else{
			throw new TypeCheckException(" Error intlit");
		}
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		whileStatement.getB().visit(this, null);
		Expression w1 = (Expression) whileStatement.getE().visit(this, null);
		if(w1.getType().equals(BOOLEAN)){
			return whileStatement;
			}

		else{
			throw new TypeCheckException("Error while");
		}

	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		declaration.setType(Type.getTypeName(declaration.firstToken));
		boolean isInserted = symtab.insert(declaration.getIdent().getText(), declaration);
		if(isInserted){
		return declaration;
		}else{
			throw new TypeCheckException("Error");
		}
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
			List<ParamDec> params = program.getParams();
			for(ParamDec p : params){
				p.visit(this, arg);
			}
			program.getB().visit(this, null);
			return program;
	}

	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getVar().visit(this, null);
		TypeName t1 = assignStatement.getVar().getDec().getType2() ;
		assignStatement.getE().visit(this, null);
		TypeName t2 = assignStatement.getE().getType();
		if(t1.equals(t2)){
			return assignStatement;
			}

		else{
			throw new TypeCheckException("Error assign");
		}
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		Dec ident = symtab.lookup(identX.firstToken.getText());
		if(!(ident == null))
		{
			ident.setType(Type.getTypeName(ident.firstToken));
			identX.setDec(ident);
			return identX;

		}
		else{
			throw new TypeCheckException(" Error Identlvalue");
		}
	}

	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		paramDec.setType(Type.getTypeName(paramDec.firstToken));
		boolean isInserted = symtab.insert(paramDec.getIdent().getText(), paramDec);
		if(isInserted){
			return paramDec;
			}else{
				throw new TypeCheckException("Error");
			}

	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {

		constantExpression.type = INTEGER;
		return constantExpression;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, null);
		Kind imageOp = imageOpChain.getFirstToken().kind;
		if(imageOp.equals(OP_WIDTH) || imageOp.equals(OP_HEIGHT)){
			if (imageOpChain.getArg().getExprList().size() == 0) {
				imageOpChain.type = INTEGER;
			}
			else{
				throw new TypeCheckException("Error " + imageOpChain.firstToken.getText());
			}
		}
		else if(imageOp.equals(KW_SCALE)){
			if (imageOpChain.getArg().getExprList().size() == 1) {
				imageOpChain.getArg().visit(this, arg);
				imageOpChain.type = IMAGE;
			}
			else{
				throw new TypeCheckException("Error " + imageOpChain.firstToken.getText());
			}
		}
		else{
			throw new TypeCheckException("Error " + imageOpChain.firstToken.getText());
		}

		return imageOpChain;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		List<Expression> exprList = tuple.getExprList();
		for(Expression e : exprList){
			e.type = INTEGER;
		}
		return tuple;
	}

}
