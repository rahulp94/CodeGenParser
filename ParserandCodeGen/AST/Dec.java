package cop5556sp17.AST;
import cop5556sp17.TypeCheckVisitor.TypeCheckException;
import cop5556sp17.Scanner.Token;
import cop5556sp17.AST.Type.TypeName;

public class Dec extends ASTNode {
	final Token ident;
	public TypeName type;
	int slotCounter;
	boolean bool;

	public int getDecCount() {
		return slotCounter;
	}

	public void setDecCount(int i) {
		slotCounter = i;
	}

	public Dec(Token firstToken, Token ident) {
		super(firstToken);

		this.ident = ident;
	}

	public Token getType() {
		return firstToken;
	}

	public TypeName getType2(){
		return type;
	}

	public boolean getBoolValue(){
		return bool;
	}

	public void setBoolValue(boolean bool){
		this.bool = bool;
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

	public Token getIdent() {
		return ident;
	}

	@Override
	public String toString() {
		return "Dec [ident=" + ident + ", firstToken=" + firstToken + "]";
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((ident == null) ? 0 : ident.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof Dec)) {
			return false;
		}
		Dec other = (Dec) obj;
		if (ident == null) {
			if (other.ident != null) {
				return false;
			}
		} else if (!ident.equals(other.ident)) {
			return false;
		}
		return true;
	}

	@Override
	public Object visit(ASTVisitor v, Object arg) throws Exception {
		return v.visitDec(this,arg);
	}

}
