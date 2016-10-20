# Nodes

## About
This project demonstrates the use of the JFlex lexer generator and CUP parser
generator for Java, with Scala datatypes.

## Source language syntax
The source language is a pi-calculus like language with the following grammar:
#### Processes
    P, Q ::= send E : E . P             Output
        | receive E : X . P             Input
        | server E : X . P              Replicated input
        | let X = E . P                 Local abstraction
        | if E then P else Q endif      Conditional
        | P | Q                         Parallel composition
        | new X . P                     Channel creation
        | end                           Null process
        | (P)                           Parenthesised process
#### Expressions
    E, F ::= X                          Variables ([a-z_]+)
        | C                             Channel literals ($[a-z_]+)
        | I                             Integers (0|[1-9][0-9]*)
        | true | false                  Booleans
        | E + F                         Addition
        | E - F                         Subtraction
        | E * F                         Multiplucation
        | E / F                         Integer division
        | E % F                         Modulo (remainder after integer division)
        | E == F                        Equality
        | E != F                        Inequality
        | E < F                         Less-than
        | E <= F                        Less-or-equal
        | E > F                         Greater-than
        | E >= F                        Greater-or-equal
        | E && F                        Logical and
        | E || F                        Logical or
        | ! E                           Logical not
        | { E , F }                     Tuple constructor
        | <- E                          Left-hand tuple destructor
        | -> E                          Right-hand tuple destructor
        | (E)                           Parenthesised expression

## Dependencies
Just SBT and a JRE8. SBT will download the required scala compiler and
libraries.
