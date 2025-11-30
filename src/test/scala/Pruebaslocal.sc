import Datos._
import Itinerarios._
// Ejemplo
val itsCurso = itinerarios(vuelosCurso, aeropuertosCurso)

//2.1 Aeropuertos incomunicados
val its1 = itsCurso("MID", "SVCS")
val its2 = itsCurso("CLO", "SVCS")

// 4 itinerarios CLO−SVO
val its3 = itsCurso("CLO", "SVO")

//2 itinerarios CLO−MEX
val its4 = itsCurso("CLO", "MEX")

//2 itinerarios CTG−PTY
val its5 = itsCurso("CTG","PTY")


val itsTiempoCurso = itinerariosTiempo(vuelosCurso, aeropuertosCurso)

// prueba itinerariosTiempo
val itst1 = itsTiempoCurso("MID","SVCS")
val itst2 = itsTiempoCurso("CLO","SVCS")

// 4 itinerarios CLO–SVO
val itst3 = itsTiempoCurso("CLO","SVO")

// 2 itinerarios CLO–MEX
val itst4 = itsTiempoCurso("CLO","MEX")

// 2 itinerarios CTG–PTY
val itst5 = itsTiempoCurso("CTG","PTY")


val itsEscalasCurso = itinerariosEscalas(vuelosCurso, aeropuertosCurso)

val itsc1 = itsEscalasCurso("MID","SVCS")
val itsc2 = itsEscalasCurso("CLO","SVCS")

// 4 itinerarios CLO–SVO
val itsc3 = itsEscalasCurso("CLO","SVO")

// 2 itinerarios CLO–MEX
val itsc4 = itsEscalasCurso("CLO","MEX")

// 2 itinerarios CTG–PTY
val itsc5 = itsEscalasCurso("CTG","PTY")

