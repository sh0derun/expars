import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Optional;
import java.util.Stack;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface TestCase{
    String name() default "";
    boolean run() default false;
}

class AssertionException extends RuntimeException{
    enum AssertionType{
        TRUE, EQUAL
    }
    AssertionException(AssertionType type, boolean actual, boolean expected){
        super(type + " assertion failed : expected " + expected + " but was " + actual);
    }
}

public class ExpressionParserTest {

    ExpressionParser expressionParser = new ExpressionParser();

    /** Test main method 
     * @param args Test cases to run
    */
	public static void main(String[] args) {
        runTests();
	}

    public static void runTests(){
        ExpressionParserTest test = new ExpressionParserTest();
        Class<ExpressionParserTest> clazz = ExpressionParserTest.class;
        Arrays.stream(clazz.getDeclaredMethods())
        .filter(e->e.isAnnotationPresent(TestCase.class))
        .forEach(e->{
            TestCase t = e.getAnnotation(TestCase.class);
            if(t.run()){
                System.out.println("****** Test "+t.name()+" started ******");
                System.out.println();
                try{
                    e.invoke(test);
                } catch(IllegalAccessException ex){
                    System.out.println("Can't access to invoked method");
                    return;
                } catch(Exception ex){
                    getCausedBy(Optional.of(ex)).printStackTrace();
                    System.out.println("******Test "+t.name()+" failed *****");
                    return;
                }
                System.out.println();
                System.out.println("****** Test "+t.name()+" passed *****");
            }
        });
    }

    private static Throwable getCausedBy(Optional<Throwable> throwable) {
        Throwable rootCause = throwable.orElseGet(()->new NullPointerException());
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }

    @TestCase(name = "infixToPosfixToSimulationTest", run = true)
    public void infixToPosfixToSimulationTest(){
        int ko = 0, ok = 0, ret = 0;
		String[] expressions = { "(((((1*2)+(3/4))", "((1*2)+(3/4)))))))", "((1*2))))+(3/4))(((()", "((1*2)+(3/4))", "3*3+3/3-3" };
		Tokenizer tokenizer = null;
		for (String expression : expressions) {
			tokenizer = new Tokenizer(expression);
			Stack<Token> postfixNotation = expressionParser.toPostfixNotation(tokenizer);
			String res = "";
			System.out.print(expression + " => ");
			for (Token token : postfixNotation)
				res += token.value;
			System.out.print(res);
			try {
                int val = expressionParser.simulateExpression(postfixNotation);
				System.out.println(" => " + val);
                ok++;
                ret += val;
			} catch (InvalidExpressionException e) {
				System.out.println(" => cannot simulate " + e.getMessage());
                ko++;
			}
			tokenizer.reset();
		}
        assertTrue(ko == 3 && ok == 2 && ret == 9);
	}

    private void assertTrue(boolean flag) throws AssertionException{
        if(!flag)
            throw new AssertionException(AssertionException.AssertionType.TRUE, flag, true);
    }

}