import Datos._
package object Itinerarios {

  def itinerarios(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    val vuelosDesde: Map[String, List[Vuelo]] =
      vuelos.groupBy(_.Org)

    def buscar(origen: String, destino: String, visitados: Set[String]): List[Itinerario] = {
      if (origen == destino)

        List(Nil)
      else {

        val salidas: List[Vuelo] =
          vuelosDesde.getOrElse(origen, Nil)
            .filter(v => !visitados.contains(v.Dst))


        salidas.flatMap { vuelo =>
          val subIts = buscar(vuelo.Dst, destino, visitados + vuelo.Dst)

          subIts.map(it => vuelo :: it)
        }
      }
    }

    (cod1: String, cod2: String) =>
      buscar(cod1, cod2, Set(cod1))
  }


}
