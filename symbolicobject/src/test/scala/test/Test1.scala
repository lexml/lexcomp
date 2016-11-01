package test

import br.gov.lexml.symbolicobject.impl._
import br.gov.lexml.symbolicobject.tipos.Tipos

object Test1 extends App {
  import Attributes._
  val x = ObjetoSimbolicoComplexo[Unit](
    id = 10L,
    tipo = Tipos.OsArtigo,
    data = (),
    posicoes = IndexedSeq(),
    properties = Map()
    )
  val res = caminho(x)
}
