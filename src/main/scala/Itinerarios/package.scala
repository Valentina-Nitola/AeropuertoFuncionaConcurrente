import Datos._

package object Itinerarios {

  // --------------------------------------------------
  // función base: itinerarios
  // --------------------------------------------------
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

  // --------------------------------------------------
  // Helpers de tiempo (puras, privadas)
  // --------------------------------------------------

  private def minutoDelDia(hora: Int, minuto: Int): Int =
    hora * 60 + minuto

  private def toUTCmin(localMin: Int, gmt: Int): Int = {
    val gmtMinutes = if (Math.abs(gmt) > 24) gmt else gmt * 60
    localMin - gmtMinutes
  }

  private def diffMinutosNormalizado(tInicioUTC: Int, tFinUTC: Int): Int = {
    val diff = tFinUTC - tInicioUTC
    if (diff >= 0) diff else diff + 24 * 60
  }

  private def duracionVueloMin(v: Vuelo, mapaAer: Map[String, Aeropuerto]): Int = {
    val depLocal = minutoDelDia(v.HS, v.MS)
    val arrLocal = minutoDelDia(v.HL, v.ML)

    val gmtOrg = mapaAer.getOrElse(v.Org,
      throw new NoSuchElementException(s"Aeropuerto ${v.Org} no encontrado")).GMT
    val gmtDst = mapaAer.getOrElse(v.Dst,
      throw new NoSuchElementException(s"Aeropuerto ${v.Dst} no encontrado")).GMT

    val depUTC = toUTCmin(depLocal, gmtOrg)
    val arrUTC = toUTCmin(arrLocal, gmtDst)

    diffMinutosNormalizado(depUTC, arrUTC)
  }

  private def esperaEntre(v1: Vuelo, v2: Vuelo, mapaAer: Map[String, Aeropuerto]): Int = {
    // llegada de v1 en su aeropuerto (v1.Dst)
    val arrLocal1 = minutoDelDia(v1.HL, v1.ML)
    val gmt1 = mapaAer.getOrElse(v1.Dst,
      throw new NoSuchElementException(s"Aeropuerto ${v1.Dst} no encontrado")).GMT
    val arrUTC1 = toUTCmin(arrLocal1, gmt1)

    // salida de v2 en su aeropuerto (v2.Org)
    val depLocal2 = minutoDelDia(v2.HS, v2.MS)
    val gmt2 = mapaAer.getOrElse(v2.Org,
      throw new NoSuchElementException(s"Aeropuerto ${v2.Org} no encontrado")).GMT
    val depUTC2 = toUTCmin(depLocal2, gmt2)

    diffMinutosNormalizado(arrUTC1, depUTC2)
  }

  // --------------------------------------------------
  // Conversiones y helpers públicos solicitados
  // --------------------------------------------------

  /** Convierte una cantidad de minutos a (horas, minutos) */
  def minutosAhoras(min: Int): (Int, Int) = {
    val h = min / 60
    val m = min % 60
    (h, m)
  }

  /** Duración real de un vuelo (public wrapper) */
  def tiempoDeVuelo(mapaAer: List[Aeropuerto])(vuelo: Vuelo): Int = {
    val mapa = mapaAer.map(a => a.Cod -> a).toMap
    duracionVueloMin(vuelo, mapa) // ya calcula correctamente con GMT
  }

  /** Tiempo total de vuelo (solo aire) de un itinerario */
  def tiempoEnAireIt(mapaAer: List[Aeropuerto])(it: Itinerario): Int = {
    val mapa = mapaAer.map(a => a.Cod -> a).toMap
    it.map(v => duracionVueloMin(v, mapa)).sum
  }

  /** Tiempo total incluyendo vuelos y esperas entre conexiones */
  def tiempoDeVueloIt(mapaAer: List[Aeropuerto])(it: Itinerario): Int = {
    val mapa = mapaAer.map(a => a.Cod -> a).toMap

    val aire = it.map(v => duracionVueloMin(v, mapa)).sum

    val esperas =
      it.zip(it.drop(1))
        .map { case (v1, v2) => esperaEntre(v1, v2, mapa) }
        .sum

    aire + esperas
  }


  // --------------------------------------------------
  // itinerariosTiempo
  // --------------------------------------------------

  def itinerariosTiempo(vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]):
  (String, String) => List[Itinerario] = {

    val its = itinerarios(vuelos, aeropuertos)
    val mapaAer: Map[String, Aeropuerto] = aeropuertos.map(a => a.Cod -> a).toMap

    (o: String, d: String) => {
      val todos: List[Itinerario] = its(o, d)

      // calcular tiempo total en minutos para cada itinerario
      val conTiempos: List[(Itinerario, Int)] =
        todos.map { it =>
          val tiempoEnAire = it.map(v => duracionVueloMin(v, mapaAer)).sum
          val tiemposConexiones = it.zip(it.drop(1)).map { case (v1, v2) =>
            esperaEntre(v1, v2, mapaAer)
          }.sum
          (it, tiempoEnAire + tiemposConexiones)
        }

      // ordenar por tiempo total y devolver solo los itinerarios (los 3 mejores)
      conTiempos.sortBy(_._2).take(3).map(_._1)
    }
  }

  def itinerariosEscalas( vuelos: List[Vuelo], aeropuertos: List[Aeropuerto]):
  (String, String) => List[Itinerario] = {val its = itinerarios(vuelos, aeropuertos)

    (o: String, d: String) => {
      val todos: List[Itinerario] = its(o, d)

      // calculamos: totalEscalas = sum(v.Esc) + (numConexiones)
      val conEscalas: List[(Itinerario, Int, Int)] =
        todos.map { it =>
          val sumEscInternas = it.map(_.Esc).sum // escalas internas
          val numConexiones = Math.max(0, it.length - 1) // cambios de avión
          val totalEsc = sumEscInternas + numConexiones
          (it, totalEsc, numConexiones)
        }

      // ordenamos por (totalEsc, numConexiones) ascendente; stable sort preserva orden original en empates finales
      conEscalas
        .sortBy { case (_, totalEsc, numConexiones) => (totalEsc, numConexiones) }
        .take(3)
        .map(_._1)
    }
  }

  def itinerariosAire(
                       vuelos: List[Vuelo],
                       aeropuertos: List[Aeropuerto]
                     ): (String, String) => List[Itinerario] = {

    val its = itinerarios(vuelos, aeropuertos)

    // mapa para acceder a GMT de cada aeropuerto
    val mapaAer: Map[String, Aeropuerto] =
      aeropuertos.map(a => a.Cod -> a).toMap

    (o: String, d: String) => {

      val todos: List[Itinerario] = its(o, d)

      // Para cada itinerario calculamos:
      // (itinerario, tiempoEnAire, conexiones)
      val conTiempo: List[(Itinerario, Int, Int)] =
        todos.map { it =>

          // tiempo total en aire usando tu helper duracionVueloMin
          val tiempoAire: Int =
            it.map(v => duracionVueloMin(v, mapaAer)).sum

          // conexiones = vuelos - 1 (para criterio de desempate)
          val conexiones: Int =
            Math.max(0, it.length - 1)

          (it, tiempoAire, conexiones)
        }

      // Orden: menos tiempo en aire, luego menos conexiones
      conTiempo
        .sortBy { case (_, aire, conexiones) => (aire, conexiones) }
        .take(3)
        .map(_._1)
    }
  }

  def itinerarioSalida(
                        vuelos: List[Vuelo],
                        aeropuertos: List[Aeropuerto]
                      ): (String, String, Int, Int) => Itinerario = {

    val its = itinerarios(vuelos, aeropuertos)
    val mapaAer = aeropuertos.map(a => a.Cod -> a).toMap

    (o: String, d: String, h: Int, m: Int) => {

      val todos = its(o, d)

      if (todos.isEmpty) {
        // NO usamos return. Devolvemos directamente Nil como Itinerario vacío
        Nil
      } else {

        val citaLocal = minutoDelDia(h, m)
        val gmtDst = mapaAer(d).GMT
        val citaUTC = toUTCmin(citaLocal, gmtDst)

        def llegadaUTC(it: Itinerario): Int = {
          val ult = it.last
          val arrLocal = minutoDelDia(ult.HL, ult.ML)
          val gmt = mapaAer(ult.Dst).GMT
          toUTCmin(arrLocal, gmt)
        }

        def salidaLocal(it: Itinerario): Int = {
          val v0 = it.head
          minutoDelDia(v0.HS, v0.MS)
        }

        // díasAntes = cuántos días antes de la cita equivale llegar
        def diasAntes(arr: Int): Int = {
          ((citaUTC - arr) / (24 * 60.0)).floor.toInt * -1
        }

        todos
          .map { it =>
            val arr = llegadaUTC(it)
            val dAntes = diasAntes(arr)
            (it, dAntes, salidaLocal(it))
          }
          .sortBy { case (_, dAntes, salida) => (dAntes, -salida) }
          .head
          ._1
      }
    }
  }


}