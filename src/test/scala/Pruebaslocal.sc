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
