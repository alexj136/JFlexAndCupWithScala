package test

import syntax._
import org.scalacheck._
import Gen._
import Arbitrary.arbitrary
import org.scalacheck.Prop.BooleanOperators

object ArbitraryTypes {

  val genSend: Gen[Proc] = for {
    ch   <- genExp
    msg  <- genExp
    next <- genProc
  } yield Send(ch, msg, next)

  val genReceive: Gen[Proc] = for {
    srv  <- arbitrary[Boolean]
    ch   <- genExp
    bind <- genName
    next <- genProc
  } yield Receive(srv, ch, bind, next)

  val genNew: Gen[Proc] = for {
    bind <- genName
    next <- genProc
  } yield New(bind, next)

  val genParallel: Gen[Proc] = for {
    l <- genProc
    r <- genProc
  } yield Parallel(l, r)

  val genLetIn: Gen[Proc] = for {
    bind <- genName
    exp  <- genExp
    next <- genProc
  } yield LetIn(bind, exp, next)

  val genIfThenElse: Gen[Proc] = for {
    cond <- genExp
    t    <- genProc
    f    <- genProc
  } yield IfThenElse(cond, t, f)

  val genEnd: Gen[Proc] = const(End)

  def genProc: Gen[Proc] = lzy(frequency(
    (  2 , genSend       ) ,
    (  2 , genReceive    ) ,
    (  1 , genNew        ) ,
    (  1 , genParallel   ) ,
    (  1 , genLetIn      ) ,
    (  1 , genIfThenElse ) ,
    ( 20 , genEnd        ) )
  )

  val genVariable: Gen[Exp] = for {
    id <- genName
  } yield Variable(id)

  val genIntLiteral: Gen[Exp] = for {
    n <- arbitrary[Int]
  } yield IntLiteral(n)

  val genBoolLiteral: Gen[Exp] = for {
    b <- arbitrary[Boolean]
  } yield BoolLiteral(b)

  val genChanLiteral: Gen[Exp] = for {
    c <- genName
  } yield ChanLiteral(c)

  val genPair: Gen[Exp] = for {
    l <- genExp
    r <- genExp
  } yield Pair(l, r)

  val genUnExp: Gen[Exp] = for {
    op  <- oneOf(
      const( Not    ),
      const( PLeft  ),
      const( PRight ))
    exp <- genExp
  } yield UnExp(op, exp)

  val genBinExp: Gen[Exp] = for {
    op <- oneOf(
      const ( Add       ),
      const ( Sub       ),
      const ( Mul       ),
      const ( Div       ),
      const ( Mod       ),
      const ( Equal     ),
      const ( NotEqual  ),
      const ( Less      ),
      const ( LessEq    ),
      const ( Greater   ),
      const ( GreaterEq ),
      const ( And       ),
      const ( Or        ))
    l  <- genExp
    r  <- genExp
  } yield BinExp(op, l, r)

  def genExp: Gen[Exp] = lzy(frequency(
    ( 10 , genVariable    ) ,
    ( 10 , genIntLiteral  ) ,
    ( 10 , genBoolLiteral ) ,
    ( 10 , genChanLiteral ) ,
    (  3 , genPair        ) ,
    (  6 , genUnExp       ) ,
    (  3 , genBinExp      ) )
  )

  def genName: Gen[Name] = for {
    id <- lzy(arbitrary[Int])
  } yield Name(id)

  implicit val arbitraryProc: Arbitrary[Proc] = Arbitrary(genProc)
  implicit val arbitraryExp : Arbitrary[Exp ] = Arbitrary(genExp )
  implicit val arbitraryName: Arbitrary[Name] = Arbitrary(genName)
}

object ProcProperties extends Properties("Proc") {
  import ArbitraryTypes._

  property("alphaEquivIsReflexive") = Prop.forAll { ( p: Proc ) => {
    (p alphaEquiv p).nonEmpty
  }}

  property("alphaEquivSimple") = Prop.forAll {
    ( n0: Name, n1: Name, n2: Name ) => { (n0 != n1 && n0 != n2) ==>
      (Send(ChanLiteral(n0), Variable(n1), End).alphaEquiv(
        Send(ChanLiteral(n1), Variable(n2), End))).nonEmpty
    }
  }
}
