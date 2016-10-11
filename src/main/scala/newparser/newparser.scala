package newparser

import syntax._
import scala.util.parsing.combinator._
import scala.util.parsing.input.CharSequenceReader

class Parser extends Parsers with PackratParsers {

  override type Elem = Token

  def name: Parser [ Name ] =
    accept ( "IDENT" , { case IDENT ( n ) => Name ( ??? ) } )

  lazy val proc: PackratParser [ Proc ] = par | end | snd | rcv | res

  lazy val par: PackratParser [ Proc ] = proc ~ BAR ~ proc ^^ {
    case p ~ _ ~ q => Parallel ( p , q )
  }

  def snd: Parser [ Proc ] =
    SEND ~ name ~ COLON ~ name ~ DOT ~ proc ^^ {
      case _ ~ c ~ _ ~ m ~ _ ~ p => Send ( Variable ( c ) , Variable ( m ) , p )
    }

  def rcv: Parser [ Proc ] =
    RECEIVE ~ name ~ COLON ~ name ~ DOT ~ proc ^^ {
      case _ ~ c ~ _ ~ b ~ _ ~ p => Receive ( false , Variable ( c ) , b , p )
    }

  def res: Parser [ Proc ] = NEW ~ name ~ DOT ~ proc ^^ {
    case _ ~ n ~ _ ~ p => New ( n , p )
  }

  def end: Parser [ Proc ] = END ^^ { _ => End }

  lazy val exp: PackratParser [ Exp ] =
    variable | intLiteral | trueLiteral | falseLiteral | chanLiteral | pair |
    binExp | unExp | LPAREN ~> exp <~ RPAREN

  def variable: Parser [ Exp ] = name ^^ { Variable ( _ ) }

  def intLiteral: Parser [ Exp ] =
    accept ( "INT" , { case INT ( i ) => IntLiteral ( i.toInt ) } )

  def trueLiteral: Parser [ Exp ] = TRUE ^^ { _ => BoolLiteral ( true ) }

  def falseLiteral: Parser [ Exp ] = FALSE ^^ { _ => BoolLiteral ( false ) }

  def chanLiteral: Parser [ Exp ] =
    accept ( "CHAN" , { case CHAN ( c ) => ChanLiteral ( Name ( ??? ) ) } )

  def pair: Parser [ Exp ] = LCURLY ~ exp ~ COMMA ~ exp ~ RCURLY ^^ {
    case _ ~ l ~ _ ~ r ~ _ => Pair ( l , r )
  }

  lazy val binExp: PackratParser [ Exp ] = exp ~ binOpTy ~ exp ^^ {
    case l ~ op ~ r => BinExp ( op , l , r )
  }

  def unExp: Parser [ Exp ] = unOpTy ~ exp ^^ {
    case op ~ e => UnExp ( op , e )
  }

  def uMinus: Parser [ Exp ] = DASH ~ exp ^^ {
    case _ ~ e => BinExp ( Sub , IntLiteral ( 0 ) , e )
  }

  def binOpTy: Parser [ BinOp ] =
    PLUS   ^^ { _ => Add       } |
    DASH   ^^ { _ => Sub       } |
    STAR   ^^ { _ => Mul       } |
    FSLASH ^^ { _ => Div       } |
    PERC   ^^ { _ => Mod       } |
    EQEQ   ^^ { _ => Equal     } |
    NEQ    ^^ { _ => NotEqual  } |
    LESS   ^^ { _ => Less      } |
    LESSEQ ^^ { _ => LessEq    } |
    GRTR   ^^ { _ => Greater   } |
    GRTREQ ^^ { _ => GreaterEq } |
    AND    ^^ { _ => And       } |
    OR     ^^ { _ => Or        }

  def unOpTy: Parser [ UnOp ] =
    BANG   ^^ { _ => Not    } |
    LARROW ^^ { _ => PLeft  } |
    RARROW ^^ { _ => PRight }
}

object Lexer extends RegexParsers {

  override def skipWhitespace = true
  override val whiteSpace = """[ \t\r\f\n]+""".r

  def lex : Parser [ List [ Token ] ] = phrase ( rep1 (
    """[a-z_]+""".r      ^^ { IDENT ( _ )  } |||
    """\$[a-z_]+""".r    ^^ { CHAN  ( _ )  } |||
    """(0|[1-9]\d*)""".r ^^ { INT   ( _ )  } |||
    "!"                  ^^ { _ => BANG    } |||
    "*"                  ^^ { _ => STAR    } |||
    "."                  ^^ { _ => DOT     } |||
    ":"                  ^^ { _ => COLON   } |||
    "let"                ^^ { _ => LET     } |||
    "new"                ^^ { _ => NEW     } |||
    "if"                 ^^ { _ => IF      } |||
    "then"               ^^ { _ => THEN    } |||
    "else"               ^^ { _ => ELSE    } |||
    "endif"              ^^ { _ => ENDIF   } |||
    "send"               ^^ { _ => SEND    } |||
    "receive"            ^^ { _ => RECEIVE } |||
    "server"             ^^ { _ => SERVER  } |||
    "|"                  ^^ { _ => BAR     } |||
    "end"                ^^ { _ => END     } |||
    "("                  ^^ { _ => LPAREN  } |||
    ")"                  ^^ { _ => RPAREN  } |||
    "{"                  ^^ { _ => LCURLY  } |||
    "}"                  ^^ { _ => RCURLY  } |||
    ","                  ^^ { _ => COMMA   } |||
    "<-"                 ^^ { _ => LARROW  } |||
    "->"                 ^^ { _ => RARROW  } |||
    "true"               ^^ { _ => TRUE    } |||
    "false"              ^^ { _ => FALSE   } |||
    "+"                  ^^ { _ => PLUS    } |||
    "-"                  ^^ { _ => DASH    } |||
    "/"                  ^^ { _ => FSLASH  } |||
    "%"                  ^^ { _ => PERC    } |||
    "="                  ^^ { _ => EQUAL   } |||
    "=="                 ^^ { _ => EQEQ    } |||
    "!="                 ^^ { _ => NEQ     } |||
    "<"                  ^^ { _ => LESS    } |||
    "<="                 ^^ { _ => LESSEQ  } |||
    ">"                  ^^ { _ => GRTR    } |||
    ">="                 ^^ { _ => GRTREQ  } |||
    "&&"                 ^^ { _ => AND     } |||
    "||"                 ^^ { _ => OR      } ) )
}

sealed abstract class Token
sealed abstract class InfoToken ( text: String ) extends Token
sealed abstract class KeyWdToken extends Token { val text: String }

case class  IDENT ( text: String ) extends InfoToken ( text )
case class  CHAN  ( text: String ) extends InfoToken ( text )
case class  INT   ( text: String ) extends InfoToken ( text )
case class  ERROR ( text: String ) extends InfoToken ( text )

case object BANG    extends KeyWdToken { val text: String = "!"       }
case object STAR    extends KeyWdToken { val text: String = "*"       }
case object DOT     extends KeyWdToken { val text: String = "."       }
case object COLON   extends KeyWdToken { val text: String = ":"       }
case object LET     extends KeyWdToken { val text: String = "let"     }
case object NEW     extends KeyWdToken { val text: String = "new"     }
case object IF      extends KeyWdToken { val text: String = "if"      }
case object THEN    extends KeyWdToken { val text: String = "then"    }
case object ELSE    extends KeyWdToken { val text: String = "else"    }
case object ENDIF   extends KeyWdToken { val text: String = "endif"   }
case object SEND    extends KeyWdToken { val text: String = "send"    }
case object RECEIVE extends KeyWdToken { val text: String = "receive" }
case object SERVER  extends KeyWdToken { val text: String = "server"  }
case object BAR     extends KeyWdToken { val text: String = "|"       }
case object END     extends KeyWdToken { val text: String = "end"     }
case object LPAREN  extends KeyWdToken { val text: String = "("       }
case object RPAREN  extends KeyWdToken { val text: String = ")"       }
case object LCURLY  extends KeyWdToken { val text: String = "{"       }
case object RCURLY  extends KeyWdToken { val text: String = "}"       }
case object COMMA   extends KeyWdToken { val text: String = ","       }
case object LARROW  extends KeyWdToken { val text: String = "<-"      }
case object RARROW  extends KeyWdToken { val text: String = "->"      }
case object TRUE    extends KeyWdToken { val text: String = "true"    }
case object FALSE   extends KeyWdToken { val text: String = "false"   }
case object PLUS    extends KeyWdToken { val text: String = "+"       }
case object DASH    extends KeyWdToken { val text: String = "-"       }
case object FSLASH  extends KeyWdToken { val text: String = "/"       }
case object PERC    extends KeyWdToken { val text: String = "%"       }
case object EQUAL   extends KeyWdToken { val text: String = "="       }
case object EQEQ    extends KeyWdToken { val text: String = "=="      }
case object NEQ     extends KeyWdToken { val text: String = "!="      }
case object LESS    extends KeyWdToken { val text: String = "<"       }
case object LESSEQ  extends KeyWdToken { val text: String = "<="      }
case object GRTR    extends KeyWdToken { val text: String = ">"       }
case object GRTREQ  extends KeyWdToken { val text: String = ">="      }
case object AND     extends KeyWdToken { val text: String = "&&"      }
case object OR      extends KeyWdToken { val text: String = "||"      }
