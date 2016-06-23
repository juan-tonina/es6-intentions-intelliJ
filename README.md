# es6-intentions-intelliJ
Proof of concept, testing jetbrains plugin development

Version 0.2.1

This plugin adds refactor options for:
 - Convert to/from arrow function.(It performs a few validations and only takes care of the syntax change.)
 - Convert to template string (buggy when there are operators without parenthesis: be sure to double check the outcome)

If converting to template string does not work as intended, you can add parenthesis and its content will be escaped
with ${}.

Use at your own risk! 
Possible outcomes include: broken code, stupid code, fire, death.
