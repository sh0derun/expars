import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

enum TokenInfo{
	ADD(2, 1),
	SUB(2, 2),
	MULT(3, 3), 
	DIV(3, 4),
	NUMBER(9999, 5),
	LEFTPAR(9999, 6),
	RIGHTPAR(9999, 7),
	INVALID(9999, 8);

	public int prec, id;

	TokenInfo(int prec, int id){
		this.prec = prec;
		this.id = id;
	}
}

class Token{
	TokenInfo info;
	String value = "";
}

class Tokenizer{
	int index;
	String expression;
	private static final Map<Character, TokenInfo> lookup = new HashMap<Character,TokenInfo>();
	
	static{
		lookup.put('+', TokenInfo.ADD);
		lookup.put('-', TokenInfo.SUB);
		lookup.put('*', TokenInfo.MULT);
		lookup.put('/', TokenInfo.DIV);
		lookup.put('(', TokenInfo.LEFTPAR);
		lookup.put(')', TokenInfo.RIGHTPAR);
	} 
    
	public Tokenizer(String expression) {
		index = 0;
		this.expression = expression;
    }

	public boolean hasNextToken(){
		return index < expression.length();
	}

	public void reset(){
		this.index = 0;
	}

	private void forwardByChar(Token token, TokenInfo info, char value){
		token.info = info;
		token.value += value;
		index++;
	}

	public Token consume() {
		String res = expression.substring(index);
		Token token = new Token();
		char firstChar = res.charAt(0);
		
		if(Character.isDigit(firstChar)){
			token.info = TokenInfo.NUMBER;
			Pattern pattern = Pattern.compile("[0-9]{1,}");
			Matcher matcher = pattern.matcher(res);
			if(matcher.find()){
				String group = matcher.group();
				token.value += group;
				index += group.length();
			}
		}
		else{
			forwardByChar(token, Tokenizer.lookup.get(firstChar), firstChar);
		}
		return token;
	}
}

public class ExpressionParser {

	public static void main(String[] args) {
		String[] expressions = {"(((((1*2)+(3/4))",
								"((1*2)+(3/4)))))))",
								"((1*2))))+(3/4))(((()",
								"((1*2)+(3/4))"};
		Tokenizer tokenizer = null;
		Stack<Token> postfixNotation = null;
		for(String expression : expressions){
			tokenizer = new Tokenizer(expression);
			postfixNotation = toPostfixNotation(tokenizer);
			String res = "";
			System.out.print(expression+" => ");
			for(Token token : postfixNotation)res += token.value;
			System.out.println(res);
			tokenizer.reset();
		}
	}

	static Stack<Token> toPostfixNotation(Tokenizer tokenizer){
		Stack<Token> stack = new Stack<>();
		Stack<Token> stackOps = new Stack<>();
		Runnable invalidExpressionBloc = ()->{
			stack.clear();
			Token invalid = new Token();
			invalid.info = TokenInfo.INVALID;
			invalid.value = "invalid expression !";
			stack.push(invalid);
		};
		UnaryOperator<Boolean> stackOpsToStack = (flag) -> {
			while(!stackOps.isEmpty()){
				if(flag && TokenInfo.LEFTPAR.equals(stackOps.peek().info)){
					return true;
				}
				stack.push(stackOps.pop());
			}
			return false;
		};
		while(tokenizer.hasNextToken()){
			Token token = tokenizer.consume();
			switch(token.info){
				case NUMBER: 
					stack.push(token);
					break;
				case ADD:
				case SUB: 
				case MULT: 
				case DIV:
					if(stackOps.isEmpty() || token.info.prec > stackOps.peek().info.prec || stackOps.peek().info.equals(TokenInfo.LEFTPAR)){
						stackOps.push(token);
					}
					else if(token.info.prec == stackOps.peek().info.prec){
						stack.push(stackOps.pop());
						stackOps.push(token);
					}
					else{
						stackOpsToStack.apply(false);
						stackOps.push(token);
					}
					break;
				case LEFTPAR:
					stackOps.push(token);
					break;
				case RIGHTPAR:
					if(stackOps.isEmpty()){
						invalidExpressionBloc.run();
						return stack;
					}
					while(!stackOps.isEmpty() && !stackOps.peek().info.equals(TokenInfo.LEFTPAR)){
						stack.push(stackOps.pop());
					}
					if(!stackOps.isEmpty() && !TokenInfo.LEFTPAR.equals(stackOps.peek().info)){
						invalidExpressionBloc.run();
						return stack;
					}
					stackOps.pop();
					break;
				default:
					System.out.println("Unsupported token !");
			}
		}
		if(stackOpsToStack.apply(true)){
			invalidExpressionBloc.run();
		}
		return stack;
	}

}
