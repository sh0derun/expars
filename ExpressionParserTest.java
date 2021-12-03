import java.util.Stack;

public class ExpressionParserTest {

    public static boolean infixToPosfixToSimulationTest(){
        System.out.println("TEST infixToPosfixToSimulationTest");
        int ko = 0, ok = 0, ret = 0;
		String[] expressions = { "(((((1*2)+(3/4))", "((1*2)+(3/4)))))))", "((1*2))))+(3/4))(((()", "((1*2)+(3/4))", "3*3+3/3-3" };
		Tokenizer tokenizer = null;
		for (String expression : expressions) {
			tokenizer = new Tokenizer(expression);
			Stack<Token> postfixNotation = ExpressionParser.toPostfixNotation(tokenizer);
			String res = "";
			System.out.print(expression + " => ");
			for (Token token : postfixNotation)
				res += token.value;
			System.out.print(res);
			try {
                int val = ExpressionParser.simulateExpression(postfixNotation);
				System.out.println(" => " + val);
                ok++;
                ret += val;
			} catch (InvalidExpressionException e) {
				System.out.println(" => cannot simulate " + e.getMessage());
                ko++;
			}
			tokenizer.reset();
		}
        if(ko == 3 && ok == 2 && ret == 9){
            return true;
        } else{
            return false;
        }
	}

}