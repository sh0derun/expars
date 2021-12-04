import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.Stack;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test{
    String name() default "";
    boolean run() default false;
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
        .filter(e->e.isAnnotationPresent(Test.class))
        .forEach(e->{
            Test t = e.getAnnotation(Test.class);
            if(t.run()){
                System.out.println("****** Test "+t.name()+" started ******");
                System.out.println();
                boolean testResult = false;
                try{
                    testResult = (boolean)e.invoke(test);
                } catch(Exception ex){
                    System.out.println(ex.getMessage());
                }
                System.out.println();
                if(testResult){
                    System.out.println("****** Test "+t.name()+" passed *****");
                } else{
                    System.out.println("******Test "+t.name()+" failed *****");
                }         
            }
        });
    }

    @Test(name = "infixToPosfixToSimulationTest", run = true)
    public boolean infixToPosfixToSimulationTest(){
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
        if(ko == 3 && ok == 2 && ret == 9){
            return true;
        } else{
            return false;
        }
	}

}