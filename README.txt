Este archivo describe todos los archivos entregados en el proyecto y proporciona las instrucciones
necesarias para ejecutar tanto la versión secuencial como la versión paralela del sistema de
generación de itinerarios.

================================================================================
PROYECTO: EL PROBLEMA DE LA PLANIFICACIÓN DE VUELOS ✈️
FUNDAMENTOS DE PROGRAMACIÓN FUNCIONAL Y CONCURRENTE
Profesor : Juan Francisco Díaz Frías
Monitora : Emily Núñez
Diciembre 4 del 2025
====================

1. DESCRIPCIÓN DEL PROYECTO

El proyecto consiste en la implementación de soluciones funcionales y concurrentes en **Scala** para encontrar itinerarios de vuelos que optimicen cuatro criterios principales:

    1. **Tiempo total de viaje.**
    2. **Número de escalas.**
    3. **Tiempo en el aire.**
    4. **Hora de salida más cercana.**

Se entrega el código en versiones **secuenciales** (`Itinerarios`) y **paralelas** (`ItinerariosPar`) para realizar el análisis comparativo del desempeño utilizando la biblioteca **ScalaMeter**.

---

2. ARCHIVOS ENTREGADOS 📁

El proyecto se entrega como un paquete de IntelliJ IDEA con la siguiente estructura de archivos clave:

📁 1.1. Carpeta Datos/
    Contiene las definiciones de modelos y los datos de prueba del profesor.

    * `package.scala` (o archivos separados como `Aeropuerto.scala`, `Vuelo.scala`, `Itinerario.scala`): Definiciones de las case classes `Aeropuerto` y `Vuelo`, y el alias de tipo `Itinerario = List[Vuelo]`.
    * `DatosCurso.scala` (o parte del `package.scala`): Listas de aeropuertos y vuelos de ejemplo (`aeropuertosCurso`, `vuelosCurso`, etc.).

📁 1.2. Carpeta common/
    Incluye el módulo de concurrencia y herramientas auxiliares necesarias para el paralelismo.

    * `package.scala`: Implementación de las herramientas de paralelización (`task`, `parallel`) basadas en **ForkJoinPool** para la ejecución concurrente de las funciones.

📁 1.3. Carpeta Itinerarios/
    Implementación completa y **secuencial** de las funciones de búsqueda de itinerarios.

    * `package.scala`: Contiene las funciones: `itinerarios`, `itinerariosTiempo`, `itinerariosEscalas`, `itinerariosAire` e `itinerarioSalida`.

📁 1.4. Carpeta ItinerariosPar/
    Implementación **paralela** de todas las funciones, utilizando las herramientas del módulo `common`.

    * `package.scala`: Contiene las funciones: `itinerariosPar`, `itinerariosTiempoPar`, `itinerariosEscalasPar`, `itinerariosAirePar` e `itinerarioSalidaPar`.

📁 1.5. Carpeta test/
    Incluye las pruebas de funcionalidad y rendimiento.

    * `Pruebas.sc` (o similar): Pruebas de funcionalidad para la versión **secuencial**.
    * `PruebasPar.sc` (o similar, incluyendo `Benchmark.scala`): Pruebas de funcionalidad para la versión **paralela** y el código para la medición de tiempos con **ScalaMeter**.

Estructura del proyecto:

    * .idea/          : Directorios y archivos de configuración del proyecto IntelliJ.
    * src/            : Código fuente del proyecto.

      * main/scala/ : Código de la aplicación.

        * common/         : Paquete con código común.
        * Datos/          : Paquete provisto que contiene las definiciones de
          las clases Aeropuerto y Vuelo.
        * Itinerarios/    : Paquete que contiene las implementaciones **secuenciales**
          de las funciones itinerarios, itinerariosTiempo,
          itinerariosEscalas, itinerariosAire e
          itinerarioSalida.
        * ItinerariosPar/ : Paquete que contiene las implementaciones **paralelas**
          de las funciones itinerariosPar, itinerariosTiempoPar,
          itinerariosEscalasPar, itinerariosAirePar e
          itinerarioSalidaPar.
      * test/scala/ : Código de las pruebas.

        * Pruebas.sc      : Pruebas de funcionalidad para las versiones secuenciales.
        * PruebasPar.sc   : Pruebas de funcionalidad para las versiones paralelas.
    * build.properties: Archivo de configuración para el proyecto Scala/sbt.
    * build.sbt       : Archivo de configuración de sbt (Sistema de construcción).
    * [Otros archivos del proyecto IntelliJ, como .gitignore, misc.xml, etc.]

---

3. INSTRUCCIONES PARA EJECUTAR EL PROYECTO 💻

El proyecto fue desarrollado y probado en el entorno IntelliJ Idea con soporte para Scala/sbt.

REQUISITOS:
    * Java Development Kit (JDK) instalado.
    * Scala instalado (se recomienda usar la versión definida en build.sbt).
    * sbt (Scala Build Tool) instalado (opcional, si se usa fuera del IDE).
    * IntelliJ Idea con el plugin de Scala instalado.

PASOS PARA LA EJECUCIÓN:

1. **Abrir el Proyecto en IntelliJ Idea:**

   * Descomprima el archivo entregado.
   * Abra IntelliJ Idea y seleccione "Open" (Abrir).
   * Navegue hasta la carpeta descomprimida del proyecto y ábrala. IntelliJ debería reconocer automáticamente el proyecto sbt.
   * Espere a que IntelliJ cargue el proyecto y descargue las dependencias (basadas en `build.sbt`).

2. **Ejecutar las Pruebas de Funcionalidad:**

   * Las pruebas de funcionalidad están ubicadas en `src/test/scala/Pruebas.sc` y `src/test/scala/PruebasPar.sc`.
   * Para ejecutar las pruebas secuenciales, abra `Pruebas.sc` y ejecute los ejemplos (e.g., haciendo clic derecho y seleccionando "Run" si está configurado como hoja de cálculo de Scala).
   * Para ejecutar las pruebas paralelas, abra `PruebasPar.sc` y ejecute los ejemplos.

3. **Realizar la Evaluación Comparativa:**

   * Para la evaluación comparativa , se utiliza la biblioteca `org.scalameter`.
   * Los archivos de código fuente que contienen las llamadas a `org.scalameter` (si no están ya en `Pruebas.sc` o `PruebasPar.sc`) deben ejecutarse para generar los datos tabulados que se presentan en el informe.

4. **Generación del Informe y Entrega:**

   * Asegúrese de que también se incluye el informe en formato PDF.
   * Asegúrese de que todo el contenido (código, informe y este `Readme.txt`) esté empaquetado en un solo archivo con el formato [Apellidos]..

5. ESTRUCTURA COMPLETA DEL PROYECTO

A continuación, se documenta detalladamente cada carpeta, archivo y módulo que componen el proyecto, siguiendo un formato académico y estructurado. Esta sección facilita la comprensión del funcionamiento interno del sistema y el rol de cada componente.

    ---

    ## 5.1 Carpeta `/common`

    La carpeta **common** contiene la infraestructura para la ejecución paralela basada en `ForkJoinPool`, fundamental para las funciones paralelas usadas en este proyecto.

    ### Archivos contenidos:

    ### `package object common`

    Incluye los elementos esenciales para gestionar tareas en paralelo:

    * **`forkJoinPool`**: Pool de hilos global que gestiona las tareas paralelas.
    * **Clase abstracta `TaskScheduler`**: Define la estructura de un programador de tareas concurrentes.
    * **`DefaultTaskScheduler`**: Implementación por defecto usando `RecursiveTask`.
    * **`task[T](...)`**: Envía una tarea para ser ejecutada en paralelo.
    * **`parallel(...)`**: Ejecuta dos o cuatro tareas simultáneamente, retornando los resultados.

    **Propósito académico:**
    Permitir el uso de paralelismo de datos y facilitar la implementación de funciones paralelas mediante el patrón **divide and conquer**.

    ---

    ## 5.2 Carpeta `/Datos`

    Esta carpeta define todos los datos utilizados en el proyecto, incluyendo aeropuertos, vuelos y estructuras auxiliares.

    ### Archivos contenidos:

    ### **`package object Datos`**

    Contiene:

    * **Caso de clase `Aeropuerto`**: Modelo de un aeropuerto con código, coordenadas y zona horaria.
    * **Caso de clase `Vuelo`**: Modelo detallado de un vuelo individual.
    * **Tipo `Itinerario`**: Representa una lista ordenada de vuelos.
    * **`aeropuertosCurso`**: Conjunto principal de aeropuertos del curso.
    * **`vuelosCurso`**: Conjunto oficial de vuelos del curso.
    * **Listas adicionales de aeropuertos y vuelos**: Para pruebas con datasets de mayor tamaño (A1, B1, C1, C2).

    **Propósito académico:**
    Simular redes aéreas reales para probar algoritmos de búsqueda, optimización y programación paralela.

    ---

    ## 5.3 Archivo `/Pruebas.sc`

    Archivo principal para la validación secuencial. Contiene las pruebas oficiales descritas en el PDF del taller:

    * Pruebas de aeropuertos incomunicados.
    * Pruebas de itinerarios CLO–SVO, CLO–MEX, CTG–PTY.
    * Pruebas de itinerarios por tiempo.
    * Pruebas de itinerarios por número de escalas.
    * Pruebas de itinerarios por tiempo en aire.
    * Pruebas de itinerario con restricción de hora de salida.
    * Pruebas con datasets A1, B1, C1 y combinaciones ampliadas.

    **Propósito académico:**
    Confirmar la corrección de las funciones secuenciales antes de realizar mediciones de desempeño.

    ---

    ## 5.4 Archivo `/PruebasLocal.sc`

    Archivo auxiliar usado para pruebas manuales. Permite descomentar secciones para validar partes específicas sin ejecutar todas las pruebas del taller.

    **Propósito académico:**
    Permitir pruebas incrementales durante el desarrollo.

    ---

    ## 5.5 Archivo `/PruebasPar.sc`

    Archivo dedicado a la validación de las funciones **paralelas**. Incluye:

    * Importación de **ScalaMeter** (`import org.scalameter._`).
    * Uso de la función `tiempoDe()` para medición de tiempos.
    * Pruebas paralelas equivalentes a las secuenciales.
    * Comparación de resultados entre versiones secuenciales y paralelas.
    * Evaluación del desempeño con datasets A1, B1, C1 y C2.

Ejecuta:

* `itinerariosPar`
* `itinerariosTiempoPar`
* `itinerariosEscalasPar`
* `itinerariosAirePar`
* `itinerarioSalidaPar`

**Propósito académico:**
Medir el rendimiento, escalabilidad y eficiencia del algoritmo paralelo respecto al secuencial.
---

4. AUTORES

Este proyecto fue desarrollado por:

* **Cristian Camilo Pavas Ríos**
* **Valentina Nitola**
* **Juan Jose Flores**
* **Daniel Arteaga**
---

# FIN DEL README
