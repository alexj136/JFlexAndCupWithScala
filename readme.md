# Nodes

## About
Nodes is a concurrent programming language, based on the Pi-Calculus. It is
implemented in scala, both sequentially and concurrently. The sequential
implementation is based on the algorithm given in David N. Turner's PhD thesis,
while the concurrent implementation is a true-to-semantics implementation using 
Akka, scala's concurrency library. 

## Source language syntax
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

## State
At present, the project is in a pre-alpha state. A fairly solid lexer and parser
have been built using JFlex and CUP, with nice error messages.
The implementation uses dynamic typing, with no attempt to give nice error
messages.

## Todos
The main priorities for this project are:
- To implement a static type system
- To implement more sophisticated data types, with ML style type inference
- To provide a nicer command-line interface, allowing the user to specify
  which implementation to use