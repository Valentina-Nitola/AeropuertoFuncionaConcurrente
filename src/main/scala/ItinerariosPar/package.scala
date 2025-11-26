import Datos._
import common._
import Itinerarios._

import scala.collection.parallel.CollectionConverters._
import scala.collection.parallel.ParSeq._

package object ItinerariosPar {
  def itinerariosPar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    // Igual que antes: preprocesamos los vuelos por aeropuerto de origen
    val vuelosDesde: Map[String, List[Vuelo]] =
      vuelos.groupBy(_.Org)

    // Búsqueda de todos los itinerarios, pero paralelizando la exploración de las salidas
    def buscar(origen: String, destino: String, visitados: Set[String]): List[Itinerario] = {
      if (origen == destino)
        List(Nil)
      else {

        val salidas: List[Vuelo] =
          vuelosDesde.getOrElse(origen, Nil)
            .filter(v => !visitados.contains(v.Dst))

        // AQUÍ está el paralelismo:
        // usamos .par para explorar cada vuelo de salida en paralelo
        val itinerariosParalelos =
          salidas.par
            .flatMap { vuelo =>
              val subIts: List[Itinerario] =
                buscar(vuelo.Dst, destino, visitados + vuelo.Dst)

              subIts.map(it => vuelo :: it)
            }

        // Convertimos de vuelta a List secuencial para mantener el mismo tipo externo
        itinerariosParalelos.toList
      }
    }

    (cod1: String, cod2: String) =>
      buscar(cod1, cod2, Set(cod1))
  }
}
