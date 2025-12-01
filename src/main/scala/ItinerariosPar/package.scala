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
  
  def itinerariosTiempoPar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    // Reutilizamos la versión paralela de itinerarios.
    val its = itinerariosPar(vuelos, aeropuertos)

    // Creamos un mapa de código de aeropuerto → GMT (zona horaria)
    val gmtPorCod: Map[String, Int] =
      aeropuertos.map(a => a.Cod -> a.GMT).toMap

    val minutosDia = 24 * 60

    // Convierte una hora local a minutos GMT.
    def aMinutosGMT(h: Int, m: Int, cod: String): Int =
      (h - gmtPorCod(cod)) * 60 + m

    // Ajusta una hora que atraviesa medianoche hacia adelante.
    def ajustarForward(base: Int, valor: Int): Int =
      if valor >= base then valor else ajustarForward(base, valor + minutosDia)

    // Calcula duración total de un itinerario.
    def tiempoItinerario(it: Itinerario): Int = it match {
      case Nil => 0

      case primer :: _ =>
        val inicio = aMinutosGMT(primer.HS, primer.MS, primer.Org)

        val fin = it.foldLeft(inicio) { (tActual, vuelo) =>
          // Hora de salida en GMT ajustada
          val dep0 = aMinutosGMT(vuelo.HS, vuelo.MS, vuelo.Org)
          val dep = ajustarForward(tActual, dep0)

          // Hora de llegada en GMT ajustada
          val arr0 = aMinutosGMT(vuelo.HL, vuelo.ML, vuelo.Dst)
          val arr = ajustarForward(dep, arr0)

          arr
        }
        fin - inicio
    }

    // Función final
    (cod1: String, cod2: String) => {
      val todos = its(cod1, cod2)

      // Ordenamos en paralelo por tiempo total.
      todos.par
        .toList
        .sortBy(tiempoItinerario)
        .take(3)
    }
  }

  def itinerariosEscalasPar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    // Reutilizamos itinerariosPar para obtener todas las rutas en paralelo.
    val its = itinerariosPar(vuelos, aeropuertos)

    // El número de cambios de avión es (vuelos - 1)
    def cambios(it: Itinerario): Int =
      if it.isEmpty then 0 else it.length - 1

    (cod1: String, cod2: String) => {
      val todos = its(cod1, cod2)

      //  Ordenamos en paralelo por menor número de escalas.
      todos.par
        .toList
        .sortBy(cambios)
        .take(3)
    }
  }


  def itinerariosAirePar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]): (String, String) => List[Itinerario] = {

    // Obtener itinerarios paralelos
    val its = itinerariosPar(vuelos, aeropuertos)

    // Mapa de código → coordenadas (X,Y) del aeropuerto
    val coords: Map[String, (Int, Int)] =
      aeropuertos.map(a => a.Cod -> (a.X, a.Y)).toMap

    // Distancia euclidiana entre dos aeropuertos (aproxima tiempo de vuelo)
    def dist(a: String, b: String): Double = {
      val (x1, y1) = coords(a)
      val (x2, y2) = coords(b)
      math.hypot(x2 - x1, y2 - y1)
    }

    // Tiempo total sumando distancias de cada vuelo.
    def tiempoAire(it: Itinerario): Double =
      it.map(v => dist(v.Org, v.Dst)).sum

    (cod1: String, cod2: String) => {
      val todos = its(cod1, cod2)

      //  Ordenamos en paralelo por menor distancia total
      todos.par
        .toList
        .sortBy(tiempoAire)
        .take(3)
    }
  }



  def itinerarioSalidaPar(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]):
        (String, String, Int, Int) => Itinerario = {

    // Reutilizamos itinerariosTiempoPar
    val itsTimePar = itinerariosTiempoPar(vuelos, aeropuertos)

    val gmtPorCod = aeropuertos.map(a => a.Cod -> a.GMT).toMap
    val minutosDia = 24 * 60

    def aMinutosGMT(h: Int, m: Int, cod: String): Int =
      (h - gmtPorCod(cod)) * 60 + m

    def ajustarForward(base: Int, valor: Int): Int =
      if valor >= base then valor else ajustarForward(base, valor + minutosDia)

    (cod1: String, cod2: String, hCita: Int, mCita: Int) => {

      // Convertimos la hora de cita a minutos GMT
      val citaGMT = aMinutosGMT(hCita, mCita, cod2)

      // Obtenemos los itinerarios válidos
      val itinerarios = itsTimePar(cod1, cod2)

      //  Seleccionamos en paralelo el itinerario que permite salir más tarde
      val mejor = itinerarios.par.maxBy { it =>
        it match
          case Nil => -999999 // evitar elegir un vacío

          case _ =>
            // Hora de llegada del último vuelo
            val ultimo = it.last
            val arrGMT = aMinutosGMT(ultimo.HL, ultimo.ML, ultimo.Dst)
            val arrReal = ajustarForward(citaGMT, arrGMT)

            // Duración del itinerario
            val inicio = aMinutosGMT(it.head.HS, it.head.MS, it.head.Org)
            val fin = ajustarForward(inicio, arrGMT)
            val duracion = fin - inicio

            // Hora máxima de salida posible
            arrReal - duracion
      }

      mejor
    }
  }


}
