import java.beans.Expression;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface TestCase{
    String name() default "";
    boolean run() default false;
    class NoException extends RuntimeException{}
    Class<? extends RuntimeException> expectedException() default NoException.class;
}

class AssertionException extends RuntimeException{
    enum AssertionType{
        TRUE, EQUAL
    }
    AssertionException(AssertionType type, Object actual, Object expected){
        super(type + " assertion failed : expected " + expected + " but was " + actual);
    }
}

public class ExpressionParserTest {

    ExpressionParser expressionParser = new ExpressionParser();
    static boolean PASSED = true;

    /** Test main method 
     * @param args Test cases to run
    */
	public static void main(String[] args) {
        runTests();
	}

    private static void runTests(){
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
                    Throwable throwable = getCausedBy(Optional.of(ex));
                    String ieen = InvalidExpressionException.class.getName();
                    throwable.printStackTrace();
                    System.out.println();
                    if(ieen.equals(throwable.getClass().getName()) && ieen.equals(t.expectedException().getName())){
                        System.out.println("******Test "+t.name()+" passed *****");
                        System.out.println();
                        return;
                    }
                    System.out.println("******Test "+t.name()+" failed *****");
                    ExpressionParserTest.PASSED = false;
                    return;
                }
                System.out.println();
                System.out.println("****** Test "+t.name()+" passed *****");
                System.out.println();
            }
        });
        if(!ExpressionParserTest.PASSED){
            //TODO: To be reviewed either exit(1) or exit with exception
            System.exit(1);
        }
    }

    private static Throwable getCausedBy(Optional<Throwable> throwable) {
        Throwable rootCause = null;
        for (rootCause = throwable.orElseGet(()->new NullPointerException());
             rootCause.getCause() != null && rootCause.getCause() != rootCause;
             rootCause = rootCause.getCause());
        return rootCause;
    }

    @TestCase(name = "InvalidExpressionExceptionTest", run = true, expectedException = InvalidExpressionException.class)
    public void InvalidExpressionExceptionTest(){
        String expression = "+-+0*";
        Stack<Token> postfixNotation = expressionParser.toPostfixNotation(new Tokenizer(expression));
        System.out.println(expression + " => " + String.join(" ", postfixNotation.stream().map(x->x.value).collect(Collectors.toList())));
        float res = expressionParser.simulateExpression(postfixNotation);
    }
    
    @TestCase(name = "simulateFloatWithIntegerTest", run = true)
    public void simulateFloatWithIntegerTest(){
        String expression = "(111.111+222.222)/333.333";
        Stack<Token> postfixNotation = expressionParser.toPostfixNotation(new Tokenizer(expression));
        float res = expressionParser.simulateExpression(postfixNotation);
		System.out.println(expression + " => " + String.join(" ", postfixNotation.stream().map(x->x.value).collect(Collectors.toList())) + " => " + res);
        assertEquals(res, 2.0f);
    }

    @TestCase(name = "infixToPosfixToSimulationTest", run = true)
    public void infixToPosfixToSimulationTest(){
        int ko = 0, ok = 0, ret = 0;
		String[] expressions = { "(((((1*2)+(3/4))", "((1*2)+(3/4)))))))", "((1*2))))+(3/4))(((()", "((1*2)+(3/4))", "3*3+3/3-3" };
		Tokenizer tokenizer = null;
		for (String expression : expressions) {
			tokenizer = new Tokenizer(expression);
			Stack<Token> postfixNotation = expressionParser.toPostfixNotation(tokenizer);
			System.out.print(expression + " => " + String.join(" ", postfixNotation.stream().map(x->x.value).collect(Collectors.toList())));
			try {
                float val = expressionParser.simulateExpression(postfixNotation);
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

    private void assertEquals(float actual, float expected) throws AssertionException{
        if(actual != expected)
            throw new AssertionException(AssertionException.AssertionType.EQUAL, actual, expected);
    }

}