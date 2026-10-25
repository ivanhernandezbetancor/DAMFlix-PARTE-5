<div align="center">

# 🎬 DAMFlix · Parte 5

**Aplicación Android para almacenar y consultar películas**

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Material 3](https://img.shields.io/badge/Material%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white)

**Módulo:** Programación Multimedia y Dispositivos Móviles  
**Unidad de Trabajo Nº2:** Desarrollo de aplicaciones Android  
**Parte 5 · Actividad 5:** Persistencia Local con SQLite Nativo y MVVM  

2º DAM · CIFP Villa de Agüimes

</div>

---

## 🎯 Objetivo

Implementar la persistencia local de datos en la aplicación **DAMFlix** utilizando **SQLite nativo** mediante la API `SQLiteOpenHelper` de Android, e integrar la arquitectura **MVVM (Model-View-ViewModel)** para independizar la gestión de la base de datos de la interfaz de usuario en Jetpack Compose.

## ✅ Requisitos funcionales

| Requisito | Cómo se cumple |
|---|---|
| **1. Gestor de base de datos (SQLiteOpenHelper)** | La clase `DAMFlixDbHelper` crea la tabla `peliculas` y controla la versión de la base de datos |
| **2. Capa de dominio y datos (Repositorio)** | `PeliculaRepository` ejecuta `SELECT`, `INSERT` y `UPDATE` con lectura manual mediante `Cursor` y escritura con `ContentValues` |
| **3. Gestión asíncrona (Corrutinas) y ViewModel** | Todas las operaciones usan `withContext(Dispatchers.IO)`. `PeliculaViewModel` expone el estado con `MutableStateFlow` y lo refresca tras cada modificación |
| **4. Integración reactiva en Jetpack Compose** | El formulario y la lista se vinculan al ViewModel, y la lista lee el estado con `collectAsStateWithLifecycle()` |
| **5. Prohibición de cadenas hardcoded** | Todos los textos de la interfaz salen de `strings.xml` mediante `stringResource()` |

## 🏗️ Arquitectura

```
┌──────────────────────────────────────────────┐
│  UI / Jetpack Compose (Lista y Formulario)   │
└──────────────────────┬───────────────────────┘
                       │ Observa StateFlow
                       ▼
┌──────────────────────────────────────────────┐
│  PeliculaViewModel (estado de la interfaz)   │
└──────────────────────┬───────────────────────┘
                       │ Funciones suspend
                       ▼
┌──────────────────────────────────────────────┐
│  PeliculaRepository (Cursor y ContentValues) │
└──────────────────────┬───────────────────────┘
                       │ Consultas SQL
                       ▼
┌──────────────────────────────────────────────┐
│  DAMFlixDbHelper (SQLiteOpenHelper)          │
└──────────────────────────────────────────────┘
```

## ⚙️ Funcionamiento

### Dependencias

En `app/build.gradle.kts`, dentro de `dependencies`:

```kotlin
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
```

SQLite nativo no necesita ninguna librería de terceros.

### Modelo

Cada película es un objeto de la clase `Pelicula`:

```kotlin
data class Pelicula(
    val id: Int = 0,
    val titulo: String,
    val director: String,
    val anio: Int,
    val esVista: Boolean = false,
    val esFavorita: Boolean = false
)
```

### Base de datos: DAMFlixDbHelper

`DAMFlixDbHelper` extiende `SQLiteOpenHelper`. En `onCreate` crea la tabla y en `onUpgrade` la recrea si cambia la versión:

```kotlin
CREATE TABLE peliculas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo TEXT NOT NULL,
    director TEXT NOT NULL,
    anio INTEGER NOT NULL,
    es_vista INTEGER NOT NULL DEFAULT 0,
    es_favorita INTEGER NOT NULL DEFAULT 0
)
```

SQLite no tiene tipo booleano, por eso `es_vista` y `es_favorita` se guardan como `0` o `1`.

### Repositorio: PeliculaRepository

| Función | Qué hace |
|---|---|
| `obtenerTodas()` | `SELECT` de todas las películas, ordenadas por título. Lee cada fila con un `Cursor` |
| `insertar(pelicula)` | `INSERT` de una película nueva con `ContentValues` |
| `actualizar(pelicula)` | `UPDATE` de una película por su `id`, por ejemplo al marcarla como favorita |

Las tres son funciones `suspend` y trabajan fuera del hilo principal:

```kotlin
suspend fun obtenerTodas(): List<Pelicula> = withContext(Dispatchers.IO) {
    // consulta con Cursor
}
```

### ViewModel: PeliculaViewModel

Guarda la lista de películas en un `MutableStateFlow` y la expone a la interfaz como `StateFlow`:

```kotlin
private val _listaPeliculas = MutableStateFlow<List<Pelicula>>(emptyList())
val listaPeliculas: StateFlow<List<Pelicula>> = _listaPeliculas.asStateFlow()
```

| Función | Qué hace |
|---|---|
| `cargarPeliculas()` | Lee la base de datos y actualiza el estado |
| `agregarPelicula(...)` | Inserta una película y recarga la lista |
| `conmutarFavorito(pelicula)` | Cambia el favorito en la base de datos y recarga la lista |

Tras cada modificación se vuelve a llamar a `cargarPeliculas()`, y así la vista se entera del cambio.

### Creación de las capas

En `MainActivity.kt`, cada capa recibe la anterior:

```kotlin
val dbHelper = DAMFlixDbHelper(this)
val repository = PeliculaRepository(dbHelper)
val viewModel = PeliculaViewModel(repository)
```

El `viewModel` se pasa a la pantalla principal y de ahí a las dos pantallas del `NavHost`.

### Pantallas

| Pantalla | Archivo | Qué hace |
|---|---|---|
| **Listado** | `ListaPeliculasScreen` | Lee la lista con `collectAsStateWithLifecycle()` y la muestra en un `LazyColumn`. El corazón llama a `conmutarFavorito` |
| **Añadir** | `FormularioAgregarPeliculaScreen` | Formulario con título, director, año y "vista". Al guardar llama a `agregarPelicula` |

La lista se actualiza sola cuando cambia el estado del ViewModel:

```kotlin
val peliculas by viewModel.listaPeliculas.collectAsStateWithLifecycle()
```

El botón "Guardar Película" solo se activa si el título y el director no están vacíos y el año es un número.

### Textos en strings.xml

Ningún texto de la interfaz está escrito en el código. Todos se consumen con `stringResource()`, incluidos los de la barra inferior y los títulos de la barra superior. En la clase `Ruta`, la etiqueta (`label`) guarda el identificador del texto en lugar del texto:

```kotlin
sealed class Ruta(val path: String, val label: Int, val icon: ImageVector) {
    object Inicio : Ruta(path = "inicio", label = R.string.navegacion_inicio, icon = Icons.Filled.Home)
    // ...
}
```

Los títulos y directores de las películas no están en `strings.xml` porque son datos de la base de datos, no textos fijos de la interfaz.

## 🗂️ Estructura

```
app/src/main
├── java/com/example/damflix
│   ├── MainActivity.kt                          # Crea las capas y define Scaffold, BottomBar y NavHost
│   ├── Ruta.kt                                  # sealed class Ruta con las rutas de la BottomBar
│   ├── Pantallas.kt                             # PantallaInicio y perfil de usuario
│   ├── data
│   │   ├── DAMFlixDbHelper.kt                   # SQLiteOpenHelper: crea la tabla peliculas
│   │   └── PeliculaRepository.kt                # SELECT, INSERT y UPDATE con Cursor y ContentValues
│   ├── model
│   │   └── Pelicula.kt                          # Entidad de datos
│   └── ui
│       ├── viewmodel
│       │   └── PeliculaViewModel.kt             # Estado con MutableStateFlow
│       └── screens
│           ├── FormularioAgregarPeliculaScreen.kt
│           └── ListaPeliculasScreen.kt
└── res
    └── values
        └── strings.xml                          # Todos los textos de la interfaz
```

## 🧪 Cómo probar la persistencia

1. Al abrir la app por primera vez, el listado está vacío, porque la base de datos no tiene películas.
2. Pulsa "Añadir": el botón "Guardar Película" está deshabilitado.
3. Escribe un título, un director y un año: el botón se activa.
4. Pulsa "Guardar Película": los campos se vacían.
5. Pulsa "Listado": aparece la película con su director y su año.
6. Pulsa el corazón: se pone rojo. Púlsalo otra vez y vuelve a gris.
7. Marca una película como favorita, **cierra la app del todo** y vuelve a abrirla: la película sigue en el listado y el corazón sigue rojo.

## 🚀 Cómo ejecutarlo

1. Clona el repositorio:
```bash
git clone https://github.com/ivanhernandezbetancor/DAMflix_PARTE_5.git
```
2. Ábrelo con **Android Studio**.
3. Espera a que termine la sincronización de Gradle.
4. Ejecuta la app en un emulador o en un dispositivo físico.

## 🛠️ Tecnologías

- Kotlin
- Jetpack Compose
- Material 3
- Navigation Compose
- SQLite nativo (`SQLiteOpenHelper`)
- Corrutinas
- ViewModel y `StateFlow`

### Dependencias necesarias

En `dependencies` de `app/build.gradle.kts`:

```kotlin
implementation("androidx.navigation:navigation-compose:2.10.2")
implementation("androidx.compose.material:material-icons-core")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.11.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.11.0")
```
