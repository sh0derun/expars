![Build status](https://github.com/sh0derun/expars/actions/workflows/test-install.yml/badge.svg?branch=master)

# Mathematical expression parser and simulator

Simulates the expression by transforming its infix to postfix form using [Shunting-yard algorithm](https://en.wikipedia.org/wiki/Shunting-yard_algorithm) instead of passing by AST.

Supports float and integer numbers, parenthesis, addition, substraction, multiplication, division

## Quick start

```terminal
$javac ExpressionParser*.java && java ExpressionParserTest
