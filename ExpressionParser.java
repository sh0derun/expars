import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tokens that be used by the tokenizer
 */
enum TokenInfo {
	ADD(2, 1, Associv.LEFT), 
	SUB(2, 2, Associv.LEFT), 
	MULT(3, 3, Associv.LEFT), 
	DIV(3, 4, Associv.LEFT), 
	INTEGER(9999, 5, Associv.NONE),
	FLOAT(9999, 5, Associv.NONE), 
	LEFTPAR(9999, 6, Associv.NONE), 
	RIGHTPAR(9999, 7, Associv.NONE), 
	INVALID(9999, 8, Associv.NONE);

	/**
	 * Associativity values
	 */
	enum Associv{
		LEFT, RIGHT, NONE
	}

	/** Token precedence */
	public int prec;
	/** Token id */
	public int id;
	/** Token associativity */
	public Associv associv;

	TokenInfo(int prec, int id, Associv associv) {
		this.prec = prec;
		this.id = id;
		this.associv = associv;
	}
}

/**
 * Token structure that holds token information and its value
 */
class Token {
	TokenInfo info;
	String value = "";
}

/**
 * Tokenizer that tokenize expression into tokens
 */
class Tokenizer {
	/** Tokenizer current position */
	int index;
	/** Expression that will be tokenized */
	String expression;

	/** Lookup table that maps operator to it's token */
	private static final Map<Character, TokenInfo> lookup = new HashMap<Character, TokenInfo>();

	static {
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

	/** Check if there is a next token */
	public boolean hasNextToken() {
		return index < expression.length();
	}

	/** Reseting the tokenizer position */
	public void reset() {
		this.index = 0;
	}

	/** Advance tokenizer by one character */
	private void forwardByChar(Token token, TokenInfo info, char value) {
		token.info = info;
		token.value += value;
		index++;
	}

	/**
	 * Consume and return next token
	 * @return {@link Token}
	 */
	public Token consume() {
		String res = expression.substring(index);
		Token token = new Token();
		char firstChar = res.charAt(0);

		if (Character.isDigit(firstChar)) {
			Pattern pattern = Pattern.compile("[0-9]+(\\.[0-9]+)?");
			Matcher matcher = pattern.matcher(res);
			if (matcher.find()) {
				String group = matcher.group();
				token.value += group;
				token.info = group.contains(".") ? TokenInfo.FLOAT : TokenInfo.INTEGER;
				index += group.length();
			}
		} else {
			forwardByChar(token, Tokenizer.lookup.get(firstChar), firstChar);
		}
		return token;
	}
}

/** Invalid postfix expression flag */
class InvalidExpressionException extends RuntimeException{
	public InvalidExpressionException(){
		super("Invalid postfix expression !");
	}
}

public class ExpressionParser {

	/** 
	 * Evaluates the postfix Expression
	 * @param postfixExpression postfix expression stack tokens
	 * @return result from evaluating the expression
	 */
	float simulateExpression(Stack<Token> postfixExpression){
		Stack<Token> copy = new Stack<Token>();
		copy.addAll(postfixExpression);
		Collections.reverse(copy);
		if(Arrays.asList(TokenInfo.ADD,TokenInfo.SUB,TokenInfo.MULT,TokenInfo.DIV,TokenInfo.INVALID).contains(copy.peek().info)){
			throw new InvalidExpressionException();
		}
		Stack<Float> stack = new Stack<>();
		while(!copy.isEmpty()){
			switch(copy.peek().info){
				case INTEGER:
					stack.push((float)Integer.parseInt(copy.pop().value));
				break;
				case FLOAT:
					stack.push(Float.parseFloat(copy.pop().value));
				break;
				case ADD:
					stack.push(stack.pop() + stack.pop());
					copy.pop();
					break;
				case SUB:{
					float a = stack.pop();
					float b = stack.pop();
					stack.push(b - a);
					copy.pop();
				}
					break;
				case MULT:
					stack.push(stack.pop() * stack.pop());
					copy.pop();
					break;
				case DIV:
					float a = stack.pop();
					float b = stack.pop();
					stack.push(b / a);
					copy.pop();
					break;
				default:
					throw new InvalidExpressionException();
			}
		}
		return stack.pop();
	}

	/** Tokenize infix expression and converts it to tokenized postfix expression
	 * @param tokenizer
	 * @return Postfix expression tokens stack
	 */
	Stack<Token> toPostfixNotation(Tokenizer tokenizer) {
		Stack<Token> stack = new Stack<>();
		Stack<Token> stackOps = new Stack<>();
		Runnable invalidExpressionBloc = () -> {
			stack.clear();
			Token invalid = new Token();
			invalid.info = TokenInfo.INVALID;
			invalid.value = "invalid expression !";
			stack.push(invalid);
		};
		Supplier<Boolean> stackOpsToStack = () -> {
			while (!stackOps.isEmpty()) {
				if (TokenInfo.LEFTPAR.equals(stackOps.peek().info)) {
					return true;
				}
				stack.push(stackOps.pop());
			}
			return false;
		};
		tokenIterator:
		while (tokenizer.hasNextToken()) {
			Token token = tokenizer.consume();
			switch (token.info) {
				case INTEGER:
				case FLOAT:
					stack.push(token);
					break;
				case ADD:
				case SUB:
				case MULT:
				case DIV:
					if (stackOps.isEmpty()){
						stackOps.push(token);
						continue tokenIterator;
					}
					while(!stackOps.isEmpty() &&!TokenInfo.LEFTPAR.equals(stackOps.peek().info) && 
							(stackOps.peek().info.prec > token.info.prec || 
							(stackOps.peek().info.prec == token.info.prec && TokenInfo.Associv.LEFT.equals(token.info.associv)))){
						stack.push(stackOps.pop());
					}
					stackOps.push(token);
					break;
				case LEFTPAR:
					stackOps.push(token);
					break;
				case RIGHTPAR:
					if (stackOps.isEmpty()) {
						invalidExpressionBloc.run();
						return stack;
					}
					while (!stackOps.isEmpty() && !stackOps.peek().info.equals(TokenInfo.LEFTPAR)) {
						stack.push(stackOps.pop());
					}
					if (stackOps.isEmpty() || !TokenInfo.LEFTPAR.equals(stackOps.peek().info)) {
						invalidExpressionBloc.run();
						return stack;
					}
					stackOps.pop();
					break;
				default:
					System.out.println("Unsupported token during postfix transformation!");
					break tokenIterator;
			}
		}
		if (stackOpsToStack.get()) {
			invalidExpressionBloc.run();
		}
		return stack;
	}

}
