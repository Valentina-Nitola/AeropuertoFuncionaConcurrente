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

  def itinerariosTiempo (vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {
    val its = itinerarios(vuelos, aeropuertos)

    val gmtPorCod: Map[String, Int] =
      aeropuertos.map(a => a.Cod -> a.GMT).toMap

    val minutosDia = 24 * 60

    def aMinutosGMT(h: Int, m: Int, codAeropuerto: String): Int = {
      val gmt = gmtPorCod(codAeropuerto)
      (h - gmt) * 60 + m
    }

    def ajustarForward(base: Int, valor: Int): Int =
      if valor >= base then valor
      else ajustarForward(base, valor + minutosDia)

    def tiempoItinerario(it: Itinerario): Int = it match {
      case Nil => 0
      case primer :: _ =>
        val inicio = aMinutosGMT(primer.HS, primer.MS, primer.Org)

        val fin = it.foldLeft(inicio) { (tActual, vuelo) =>
          val dep0 = aMinutosGMT(vuelo.HS, vuelo.MS, vuelo.Org)
          val dep = ajustarForward(tActual, dep0)

          val arr0 = aMinutosGMT(vuelo.HL, vuelo.ML, vuelo.Dst)
          val arr = ajustarForward(dep, arr0)

          arr
        }

        fin - inicio
    }

    (cod1: String, cod2: String) => {
      val todos = its(cod1, cod2)
      val ordenados = todos.sortBy(tiempoItinerario)
      ordenados.take(3)
    }
  }

  def itinerariosEscalas(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    // Reutilizamos la función que genera TODOS los itinerarios posibles
    val its = itinerarios(vuelos, aeropuertos)

    // Número de cambios de avión en un itinerario
    // (vuelos - 1; si no hay vuelos, 0)
    def cambios(it: Itinerario): Int =
      it match {
        case Nil => 0
        case _ => it.length - 1
      }

    // Función que se devuelve: de (cod1, cod2) a los 3 itinerarios con menos escalas
    (cod1: String, cod2: String) => {
      val todos: List[Itinerario] = its(cod1, cod2)

      val ordenados: List[Itinerario] =
        todos.sortBy(cambios) // ordena por menor número de escalas

      ordenados.take(3) // devuelve los tres (si los hay)
    }
  }
}
