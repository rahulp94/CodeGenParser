package cop5556sp17.AST;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import cop5556sp17.AST.Type.TypeName;
import cop5556sp17.Scanner.Token;


public abstract class Chain extends Statement {
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


	public Chain(Token firstToken) {
		super(firstToken);
	}

}
