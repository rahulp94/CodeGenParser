package cop5556sp17;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;
import cop5556sp17.AST.Type;

import cop5556sp17.Scanner.Kind;
import cop5556sp17.AST.Dec;
import cop5556sp17.AST.FilterOpChain;

public class SymbolTable {
	int current_scope = -1;
	int next_scope = -1;
	private Stack<Integer> scopes = new Stack<Integer>();
	private HashMap<String, ArrayList<SymTable>> symbols = new HashMap<String, ArrayList<SymTable>>();

	public void enterScope() {
		current_scope = next_scope++;
		scopes.push(current_scope);
	}

	/**
	 * leaves scope
	 */
	public void leaveScope() {
		scopes.pop();
		current_scope = scopes.peek();
	}

	public boolean insert(String ident, Dec dec) {
		ArrayList<SymTable> al = new ArrayList<SymTable>();
		SymTable st = new SymTable(current_scope, dec);
		if (symbols.containsKey(ident)) {
			al = symbols.get(ident);
			for (SymTable s : al) {
				if (s.scope == current_scope)
					return false;
			}
		}
		al.add(st);
		symbols.put(ident, al);
		return true;
	}

	class SymTable {
		int scope;
		Dec dec;

		public SymTable(int temp_scope, Dec temp_dec) {
			this.scope = temp_scope;
			this.dec = temp_dec;
		}

		public int getScope() {
			return scope;
		}

		public void setScope(int scopetemp) {
			this.scope = scopetemp;
		}

		public Dec getDec() {
			return dec;
		}

		public void setDec(Dec dec) {
			this.dec = dec;
		}

	}

	public Dec lookup(String ident) {
		int m = Integer.MAX_VALUE;
		if (symbols.containsKey(ident)) {
			ArrayList<SymTable> al = symbols.get(ident);
			int index = al.size() - 1;
			for (SymTable s : al) {
				if (s.getScope() <= current_scope) {
					int dif = current_scope - s.getScope();
					if(dif<m){
					m = dif;
					index = al.indexOf(s);
					}
				}
			}
			return al.get(index).getDec();
		}
		else return null;
	}

	public SymbolTable() {
		this.current_scope = 0;
		this.next_scope = current_scope + 1;
		scopes.push(0);
	}

	@Override
	public String toString() {
		return this.toString();
	}

}
