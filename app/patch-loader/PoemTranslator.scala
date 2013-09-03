package com.rumblesan.metapiano



object PoemTranslator {

  def translatePoem(poem: String): String = {
    poem.toLowerCase.map(c => translateSymbol(c) ++ " ").mkString
  }

  def translateSymbol(symbol: Char): String = {
    (symbol match {
      case ' ' => 0
      case 'z' => 1
      case 'x' => 2
      case 'c' => 3
      case 'v' => 4
      case 'b' => 5
      case 'n' => 6
      case 'm' => 7
      case ',' => 8
      case '.' => 9
      case '/' => 10
      case 'a' => 11
      case 's' => 12
      case 'd' => 13
      case 'f' => 14
      case 'g' => 15
      case 'h' => 16
      case 'j' => 17
      case 'k' => 18
      case 'l' => 19
      case ';' => 20
      case '\'' => 21
      case 'q' => 22
      case 'w' => 23
      case 'e' => 24
      case 'r' => 25
      case 't' => 26
      case 'y' => 27
      case 'u' => 28
      case 'i' => 29
      case 'o' => 30
      case 'p' => 31
      case '`' => 32

      case '1' => 33
      case '2' => 34
      case '3' => 35
      case '4' => 36
      case '5' => 37
      case '6' => 38
      case '7' => 39
      case '8' => 40
      case '9' => 41
      case '0' => 42

      case '\n' => '\n'

      case _ => 0
    }).toString
  }


}


