package cop5556sp17;

import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.ASTVisitor;
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
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.AST.WhileStatement;

import static cop5556sp17.AST.Type.TypeName.FRAME;
import static cop5556sp17.AST.Type.TypeName.IMAGE;
import static cop5556sp17.AST.Type.TypeName.URL;
import static cop5556sp17.Scanner.Kind.*;

public class CodeGenVisitor implements ASTVisitor, Opcodes {

	/**
	 * @param DEVEL
	 *            used as parameter to genPrint and genPrintTOS
	 * @param GRADE
	 *            used as parameter to genPrint and genPrintTOS
	 * @param sourceFileName
	 *            name of source file, may be null.
	 */
	public CodeGenVisitor(boolean DEVEL, boolean GRADE, String sourceFileName) {
		super();
		this.DEVEL = DEVEL;
		this.GRADE = GRADE;
		this.sourceFileName = sourceFileName;
		counterStack = 1;
		slots = new Stack<Integer>();
	}

	ClassWriter cw;
	String className;
	String classDesc;
	String sourceFileName;
	int paramDecCounter = 0;
	int counterStack = 0;
	Stack<Integer> slots;

	MethodVisitor mv; // visitor of method currently under construction

	/** Indicates whether genPrint and genPrintTOS should generate code. */
	final boolean DEVEL;
	final boolean GRADE;


	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		className = program.getName();
		classDesc = "L" + className + ";";
		String sourceFileName = (String) arg;
		cw.visit(52, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
				new String[] { "java/lang/Runnable" });
		cw.visitSource(sourceFileName, null);

		// generate constructor code
		// get a MethodVisitor
		mv = cw.visitMethod(ACC_PUBLIC, "<init>", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		// Create label at start of code
		Label constructorStart = new Label();
		mv.visitLabel(constructorStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering <init>");
		// generate code to call superclass constructor
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		// visit parameter decs to add each as field to the class
		// pass in mv so decs can add their initialization code to the
		// constructor.
		ArrayList<ParamDec> params = program.getParams();
		int count=0;
		for (ParamDec dec : params){
			dec.setDecCount(count);
			cw.visitField(0, dec.getIdent().getText(),
					dec.getType2().getJVMTypeDesc(), null, null);
			dec.visit(this, mv);
		}
		mv.visitInsn(RETURN);
		// create label at end of code
		Label constructorEnd = new Label();
		mv.visitLabel(constructorEnd);
		// finish up by visiting local vars of constructor
		// the fourth and fifth arguments are the region of code where the local
		// variable is defined as represented by the labels we inserted.
		mv.visitLocalVariable("this", classDesc, null, constructorStart, constructorEnd, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, constructorStart, constructorEnd, 1);
		// indicates the max stack size for the method.
		// because we used the COMPUTE_FRAMES parameter in the classwriter
		// constructor, asm
		// will do this for us. The parameters to visitMaxs don't matter, but
		// the method must
		// be called.
		mv.visitMaxs(1, 1);
		// finish up code generation for this method.
		mv.visitEnd();
		// end of constructor

		// create main method which does the following
		// 1. instantiate an instance of the class being generated, passing the
		// String[] with command line arguments
		// 2. invoke the run method.
		mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null,
				null);
		mv.visitCode();
		Label mainStart = new Label();
		mv.visitLabel(mainStart);
		// this is for convenience during development--you can see that the code
		// is doing something.
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering main");
		mv.visitTypeInsn(NEW, className);
		mv.visitInsn(DUP);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, className, "<init>", "([Ljava/lang/String;)V", false);
		mv.visitMethodInsn(INVOKEVIRTUAL, className, "run", "()V", false);
		mv.visitInsn(RETURN);
		Label mainEnd = new Label();
		mv.visitLabel(mainEnd);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, mainStart, mainEnd, 0);
		mv.visitLocalVariable("instance", classDesc, null, mainStart, mainEnd, 1);
		mv.visitMaxs(0, 0);
		mv.visitEnd();

		// create run method
		mv = cw.visitMethod(ACC_PUBLIC, "run", "()V", null, null);
		mv.visitCode();
		Label startRun = new Label();
		mv.visitLabel(startRun);
		CodeGenUtils.genPrint(DEVEL, mv, "\nentering run");
		program.getB().visit(this, null);
		mv.visitInsn(RETURN);
		Label endRun = new Label();
		mv.visitLabel(endRun);
		mv.visitLocalVariable("this", classDesc, null, startRun, endRun, 0);
		mv.visitLocalVariable("args", "[Ljava/lang/String;", null, startRun, endRun, 1);
		mv.visitMaxs(1, 1);
		mv.visitEnd(); // end of run method


		cw.visitEnd();//end of class

		//generate classfile and return it
		return cw.toByteArray();
	}



	@Override
	public Object visitAssignmentStatement(AssignmentStatement assignStatement, Object arg) throws Exception {
		assignStatement.getE().visit(this, arg);
		CodeGenUtils.genPrint(DEVEL, mv, "\nassignment: " + assignStatement.var.getText() + "=");
		CodeGenUtils.genPrintTOS(GRADE, mv, assignStatement.getE().getType());
		assignStatement.getVar().visit(this, arg);
		return null;
	}

	@Override
	public Object visitBinaryChain(BinaryChain binaryChain, Object arg) throws Exception {
		binaryChain.getE0().visit(this, false);

		if (binaryChain.getE0().getType() == TypeName.URL) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromURL",
					PLPRuntimeImageIO.readFromURLSig, false);

		} else if (binaryChain.getE0().getType() == TypeName.FILE) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "readFromFile",
					PLPRuntimeImageIO.readFromFileDesc, false);
		}

		if(binaryChain.getE1() instanceof FilterOpChain){
			if(binaryChain.getArrow().kind == BARARROW && binaryChain.getE1().firstToken.kind == OP_GRAY){
				mv.visitInsn(DUP);
			}else{
				mv.visitInsn(ACONST_NULL);
			}
		}
		binaryChain.getE1().visit(this, true);

		return null;
	}

	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression, Object arg) throws Exception {

		TypeName tn0 = binaryExpression.getE0().getType();
		TypeName tn1 = binaryExpression.getE1().getType();

		Token t = binaryExpression.getOp();

		binaryExpression.getE0().visit(this, arg);
		binaryExpression.getE1().visit(this, arg);
		if(t.kind == Kind.PLUS){
			if (tn0 == TypeName.INTEGER) {
				mv.visitInsn(IADD);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"add", PLPRuntimeImageOps.addSig, false);
			}
		}
		else if(t.kind == Kind.MINUS){
			if (tn0 == TypeName.INTEGER) {
				mv.visitInsn(ISUB);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"sub", PLPRuntimeImageOps.subSig, false);
			}
		}
		else if(t.kind == Kind.TIMES){
			if ((tn0 == TypeName.INTEGER) && (tn1 == TypeName.INTEGER)) {
				mv.visitInsn(IMUL);
			} else if ((tn0 == TypeName.INTEGER) && (tn1 == TypeName.IMAGE)) {
				mv.visitInsn(SWAP);
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"mul", PLPRuntimeImageOps.mulSig, false);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"mul", PLPRuntimeImageOps.mulSig, false);
			}
		}
		else if(t.kind == Kind.DIV){
			if ((tn0 == TypeName.INTEGER) && (tn1 == TypeName.INTEGER)) {
				mv.visitInsn(IDIV);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"div", PLPRuntimeImageOps.divSig, false);
				}
		}
		else if(t.kind == Kind.MOD){
			if ((tn0 == TypeName.INTEGER) && (tn1 == TypeName.INTEGER)) {
				mv.visitInsn(IREM);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
						"mod", PLPRuntimeImageOps.modSig, false);
			}
		}
		else if(t.kind == Kind.OR){
			Label startOr = new Label();
			Label endOr = new Label();
			mv.visitJumpInsn(IFNE, startOr);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, endOr);
			mv.visitLabel(startOr);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(endOr);
		}

		else if(t.kind == Kind.AND){
			Label startAnd = new Label();
			Label endAnd = new Label();
			mv.visitJumpInsn(IFEQ, startAnd);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, endAnd);
			mv.visitLabel(startAnd);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(endAnd);
		}
		else if(t.kind == Kind.LT){
			Label startLT = new Label();
			Label endLT = new Label();
			mv.visitJumpInsn(IF_ICMPGE, startLT);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, endLT);
			mv.visitLabel(startLT);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(endLT);
		}
		else if(t.kind == Kind.LE){
			Label startLE = new Label();
			Label endLE = new Label();
			mv.visitJumpInsn(IF_ICMPGT, startLE);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, endLE);
			mv.visitLabel(startLE);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(endLE);
		}
		else if(t.kind == Kind.GT){
			Label startGT = new Label();
			Label endGT = new Label();
			mv.visitJumpInsn(IF_ICMPLE, startGT);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, endGT);
			mv.visitLabel(startGT);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(endGT);
		}

		else if(t.kind == Kind.GE){
			Label startGE = new Label();
			Label endGE = new Label();
			mv.visitJumpInsn(IF_ICMPLT, startGE);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, endGE);
			mv.visitLabel(startGE);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(endGE);
		}
		else if(t.kind == Kind.EQUAL){
			if(tn0 == TypeName.INTEGER || tn0 == TypeName.BOOLEAN){
			Label startEq = new Label();
			Label endEq = new Label();
			mv.visitJumpInsn(IF_ICMPNE, startEq);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, endEq);
			mv.visitLabel(startEq);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(endEq);
			}
			else{
			Label startEq = new Label();
			Label endEq = new Label();
			mv.visitJumpInsn(IF_ACMPNE, startEq);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, endEq);
			mv.visitLabel(startEq);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(endEq);
			}
		}
		else if(t.kind == Kind.NOTEQUAL){
			if(tn0 == TypeName.INTEGER || tn0 == TypeName.BOOLEAN){
			Label startNot = new Label();
			Label endNot = new Label();
			mv.visitJumpInsn(IF_ICMPEQ, startNot);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, endNot);
			mv.visitLabel(startNot);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(endNot);
			}
			else{
			Label startNot = new Label();
			Label endNot = new Label();
			mv.visitJumpInsn(IF_ACMPEQ, startNot);
			mv.visitInsn(ICONST_1);
			mv.visitJumpInsn(GOTO, endNot);
			mv.visitLabel(startNot);
			mv.visitInsn(ICONST_0);
			mv.visitLabel(endNot);
			}
		}

		return null;

	}

	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {

		Label startBlock = new Label();
		Label endBlock = new Label();

		mv.visitLabel(startBlock);
		ArrayList<Dec> d = block.getDecs();
		ArrayList<Statement> s = block.getStatements();
		for(Dec dec : d){
			dec.visit(this, mv);
		}

		for(Statement statement : s){
			statement.visit(this, mv);
			if (statement instanceof BinaryChain) {
				mv.visitInsn(POP);
			}
		}
		mv.visitLabel(endBlock);
		for (Dec dec : d) {
			mv.visitLocalVariable(dec.getIdent().getText(), dec.getType2().getJVMTypeDesc(),
					null, startBlock ,endBlock, dec.getDecCount());
			counterStack--;
			slots.pop();
		}
		return null;
	}

	@Override
	public Object visitBooleanLitExpression(BooleanLitExpression booleanLitExpression, Object arg) throws Exception {
		if (booleanLitExpression.getValue()) {
			mv.visitInsn(ICONST_1);
		} else {
			mv.visitInsn(ICONST_0);
		}
		return null;
	}

	@Override
	public Object visitConstantExpression(ConstantExpression constantExpression, Object arg) {
		if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENHEIGHT)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenHeight",
					PLPRuntimeFrame.getScreenHeightSig, false);
		} else if (constantExpression.getFirstToken().isKind(Kind.KW_SCREENWIDTH)) {
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "getScreenWidth",
					PLPRuntimeFrame.getScreenWidthSig, false);
		}
		return null;
	}

	@Override
	public Object visitDec(Dec declaration, Object arg) throws Exception {
		slots.push(counterStack++);
		declaration.setDecCount(slots.peek());
		if(declaration.getType2().equals(IMAGE) || declaration.getType2().equals(FRAME)){
			mv.visitInsn(ACONST_NULL);
			mv.visitVarInsn(ASTORE, declaration.getDecCount());
		}
		return null;
	}

	@Override
	public Object visitFilterOpChain(FilterOpChain filterOpChain, Object arg) throws Exception {
		Kind k = filterOpChain.getFirstToken().kind;

		if(k.equals(OP_BLUR)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName,
					"blurOp", PLPRuntimeFilterOps.opSig, false);
		}

		else if(k.equals(OP_CONVOLVE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName,
					"convolveOp", PLPRuntimeFilterOps.opSig,false);
		}

		else if(k.equals(OP_GRAY)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFilterOps.JVMName,
					"grayOp", PLPRuntimeFilterOps.opSig, false);
		}
		else{
			throw new Exception("Illegal FilterOP");
		}
		return null;
	}

	@Override
	public Object visitFrameOpChain(FrameOpChain frameOpChain, Object arg) throws Exception {
		frameOpChain.getArg().visit(this, arg);
		Kind k = frameOpChain.getFirstToken().kind;
		if(k.equals(KW_SHOW)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"showImage", PLPRuntimeFrame.showImageDesc,false);
		}
		else if(k.equals(KW_HIDE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"hideImage", PLPRuntimeFrame.hideImageDesc,false);
		}
		else if(k.equals(KW_MOVE)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"moveFrame", PLPRuntimeFrame.moveFrameDesc,false);
		}

		else if(k.equals(KW_XLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"getXVal", PLPRuntimeFrame.getXValDesc,false);
		}

		else if(k.equals(KW_YLOC)){
			mv.visitMethodInsn(INVOKEVIRTUAL, PLPRuntimeFrame.JVMClassName,
					"getYVal", PLPRuntimeFrame.getYValDesc,false);
		}
		return null;
	}

	@Override
	public Object visitIdentChain(IdentChain identChain, Object arg) throws Exception {
		Boolean dir = Boolean.valueOf((boolean) arg);
		if (dir) {
			//right
			if (identChain.getDec() instanceof ParamDec) {
				if(identChain.getDec().getType2().equals(TypeName.FILE)){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getType2().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getDec().setBoolValue(true);
				}
				else if(identChain.getDec().getType2().equals(TypeName.INTEGER)){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(SWAP);
					mv.visitFieldInsn(PUTFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getType2().getJVMTypeDesc());
					identChain.getDec().setBoolValue(true);
				}
			} else {
				if(identChain.getDec().getType2().equals(TypeName.FILE)){
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
							identChain.getDec().getType2().getJVMTypeDesc());
					mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "write",
							PLPRuntimeImageIO.writeImageDesc, false);
					identChain.getDec().setBoolValue(true);
				}
				else if(identChain.getDec().getType2().equals(TypeName.INTEGER)){
					mv.visitInsn(DUP);
					mv.visitVarInsn(ISTORE, identChain.getDec().getDecCount());
					identChain.getDec().setBoolValue(true);
				}
				else if(identChain.getDec().getType2().equals(TypeName.IMAGE)){
					mv.visitInsn(DUP);
					mv.visitVarInsn(ASTORE, identChain.getDec().getDecCount());
					identChain.getDec().setBoolValue(true);
				}
				else if(identChain.getDec().getType2().equals(TypeName.FRAME)){
//					if (identChain.getDec().getBoolValue()) {
						mv.visitVarInsn(ALOAD, identChain.getDec().getDecCount());
						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
								PLPRuntimeFrame.createOrSetFrameSig, false);
//					} else {
//						mv.visitInsn(ACONST_NULL);
//						mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeFrame.JVMClassName, "createOrSetFrame",
//								PLPRuntimeFrame.createOrSetFrameSig, false);
						mv.visitInsn(DUP);
						mv.visitVarInsn(ASTORE, identChain.getDec().getDecCount());
						identChain.getDec().setBoolValue(true);
//					}
				}
				}
		} else {
			//left
			if (identChain.getDec() instanceof ParamDec) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, className, identChain.getDec().getIdent().getText(),
						identChain.getDec().getType2().getJVMTypeDesc());
			} else {
				if (identChain.getDec().getType2() == FRAME) {
					if (identChain.getDec().getBoolValue()) {
						mv.visitVarInsn(ALOAD, identChain.getDec().getDecCount());
					} else {
						mv.visitInsn(ACONST_NULL);
					}

				} else if (identChain.getDec().getType2() == IMAGE){
					mv.visitVarInsn(ALOAD, identChain.getDec().getDecCount());
				} else{
					mv.visitVarInsn(ILOAD, identChain.getDec().getDecCount());
				}

			}

		}
		return null;
	}

	@Override
	public Object visitIdentExpression(IdentExpression identExpression, Object arg) throws Exception {
		if(identExpression.getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, className, identExpression.getDec().getIdent().getText(),
				identExpression.getDec().getType2().getJVMTypeDesc());
		}
		else{
			if(identExpression.getType() == TypeName.INTEGER
					|| identExpression.getType() == TypeName.BOOLEAN){
				mv.visitVarInsn(ILOAD, identExpression.getDec().getDecCount());
			}
			else{
				mv.visitVarInsn(ALOAD, identExpression.getDec().getDecCount());
			}
		}
		return null;
	}

	@Override
	public Object visitIdentLValue(IdentLValue identX, Object arg) throws Exception {
		if(identX.getDec() instanceof ParamDec){
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(SWAP);
			mv.visitFieldInsn(PUTFIELD, className, identX.getDec().getIdent().getText(),
					identX.getDec().getType2().getJVMTypeDesc());
		}
		else{
			if (identX.getDec().getType2() == IMAGE) {
				mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName, "copyImage",
						PLPRuntimeImageOps.copyImageSig, false);
				mv.visitVarInsn(ASTORE, identX.getDec().getDecCount());
				identX.getDec().setBoolValue(true);
			} else if (identX.getDec().getType2() == FRAME){
				mv.visitVarInsn(ASTORE, identX.getDec().getDecCount());
				identX.getDec().setBoolValue(true);
			} else {
				mv.visitVarInsn(ISTORE, identX.getDec().getDecCount());
				identX.getDec().setBoolValue(true);
			}
		}
		return null;

	}

	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg) throws Exception {
		ifStatement.getE().visit(this, arg);
		Label startAfter = new Label();
		Label endAfter = new Label();
		mv.visitJumpInsn(IFEQ, startAfter);
		mv.visitLabel(endAfter);
		ifStatement.getB().visit(this, arg);
		mv.visitLabel(startAfter);
		return null;
	}

	@Override
	public Object visitImageOpChain(ImageOpChain imageOpChain, Object arg) throws Exception {
		imageOpChain.getArg().visit(this, arg);
		Kind k = imageOpChain.getFirstToken().getKind();
		if(k.equals(OP_HEIGHT)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage",
					"getHeight", "()I", false);
		}
		else if(k.equals(KW_SCALE)){
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageOps.JVMName,
					"scale", PLPRuntimeImageOps.scaleSig, false);
		}
		else if(k.equals(OP_WIDTH)){
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/awt/image/BufferedImage",
					"getWidth", "()I", false);
		}
		else{
			throw new Exception("Invalid ImageOP");
		}
		return null;

	}

	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression, Object arg) throws Exception {
		mv.visitLdcInsn(intLitExpression.value);
		return null;
	}


	@Override
	public Object visitParamDec(ParamDec paramDec, Object arg) throws Exception {
		MethodVisitor mv = (MethodVisitor) arg;
		TypeName t1 = paramDec.getType2();
		switch (t1) {

		case FRAME:
			break;

		case IMAGE:
			break;

		case NONE:
			break;

		case INTEGER:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
//			findStackPosition(paramDec.getDecCount(), mv);
			mv.visitLdcInsn(paramDecCounter);
			paramDecCounter++;
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "I");
			break;

		case BOOLEAN:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
//			findStackPosition(paramDec.getDecCount(), mv);
			mv.visitLdcInsn(paramDecCounter);
			paramDecCounter++;
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "parseBoolean", "(Ljava/lang/String;)Z", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Z");
			break;

		case FILE:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitTypeInsn(NEW, "java/io/File");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDecCounter);
			paramDecCounter++;
			mv.visitInsn(AALOAD);
			mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/io/File;");
			break;

		case URL:
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitLdcInsn(paramDecCounter);
			paramDecCounter++;
			mv.visitMethodInsn(INVOKESTATIC, PLPRuntimeImageIO.className, "getURL", PLPRuntimeImageIO.getURLSig, false);
			mv.visitFieldInsn(PUTFIELD, className, paramDec.getIdent().getText(), "Ljava/net/URL;");
			break;

		default:
			break;
		}
		return null;
	}

	@Override
	public Object visitSleepStatement(SleepStatement sleepStatement, Object arg) throws Exception {
		sleepStatement.getE().visit(this, arg);
		mv.visitInsn(I2L);
		mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "sleep", "(J)V", false);
		return null;
	}

	@Override
	public Object visitTuple(Tuple tuple, Object arg) throws Exception {
		for (Expression e : tuple.getExprList()) {
			e.visit(this, arg);
		}
		return null;
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg) throws Exception {
		Label startGuard = new Label();
		Label endGuard = new Label();
		Label startBody = new Label();
		Label endBody = new Label();

		mv.visitJumpInsn(GOTO, startGuard);

		mv.visitLabel(startBody);
		whileStatement.getB().visit(this, arg);
		mv.visitLabel(endBody);

		mv.visitLabel(startGuard);
		whileStatement.getE().visit(this, arg);
		mv.visitJumpInsn(IFNE, startBody);
		mv.visitLabel(endGuard);

		return null;
	}

}