package cop5556sp17.AST;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public abstract class Expression extends ASTNode {
	public TypeName type;

	public TypeName getType() {
		return type;
	}

	public void setType(TypeName type) throws Exception {
		if(type != null)
			this.type = type;
		else
			try {
				this.type = Type.getTypeName(firstToken);
			} catch (Exception e) {
				throw new TypeCheckException("Error");
			}

	}

	protected Expression(Token firstToken) {
		super(firstToken);
	}

	@Override
	abstract public Object visit(ASTVisitor v, Object arg) throws Exception;

}
