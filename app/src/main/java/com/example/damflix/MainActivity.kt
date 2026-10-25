package com.example.damflix

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.damflix.data.DAMFlixDbHelper
import com.example.damflix.data.PeliculaRepository
import com.example.damflix.ui.screens.FormularioAgregarPeliculaScreen
import com.example.damflix.ui.screens.ListaPeliculasScreen
import com.example.damflix.ui.viewmodel.PeliculaViewModel


// Aquí se crean las capas (base de datos -> repositorio -> ViewModel) y se lanza la pantalla principal.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // actividad 5: Cada capa recibe la anterior
        val dbHelper = DAMFlixDbHelper(this)   // Crea y abre la base de datos SQLite
        val repository = PeliculaRepository(dbHelper)  // Lee y escribe películas en la base de datos
        val viewModel = PeliculaViewModel(repository)  // Guarda el estado que ven las pantallas

        setContent {
            MaterialTheme {
                PantallaCatalogoDAMFlix(viewModel = viewModel)
            }
        }
    }
}

// actividad 4
// Pantalla principal: Scaffold con barra superior, barra inferior y NavHost.
// El NavHost cambia entre Inicio, Listado y Formulario según la ruta.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaCatalogoDAMFlix(viewModel: PeliculaViewModel) {
    // El NavController controla la navegación entre pantallas
    val navController = rememberNavController()

    // Observamos la ruta actual del BackStack
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    // El título de la barra superior cambia según la pantalla
    val titulo = when (rutaActual) {
        Ruta.Listado.path -> stringResource(R.string.titulo_catalogo)
        Ruta.Formulario.path -> stringResource(R.string.titulo_formulario)
        else -> stringResource(R.string.titulo_inicio)
    }

    Scaffold(
        // Barra superior con el título de la pantalla
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        // Barra inferior con un botón por cada ruta
        bottomBar = {
            NavigationBar {
                // Definimos una lista inmutable con las rutas
                listOf(
                    Ruta.Inicio,
                    Ruta.Listado,
                    Ruta.Formulario
                ).forEach { ruta ->
                    NavigationBarItem(
                        selected = rutaActual == ruta.path,
                        onClick = {
                            if (rutaActual != ruta.path) {
                                navController.navigate(ruta.path) {
                                    // Limpia la pila hasta el inicio y guarda el estado
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    // No apila copias de la misma pantalla
                                    launchSingleTop = true
                                    // Restaura el estado al volver a una pestaña
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(ruta.icon, contentDescription = stringResource(ruta.label)) },
                        label = { Text(stringResource(ruta.label)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        // Es obligatorio aplicar innerPadding al NavHost para no superponer vistas
        NavHost(
            navController = navController,
            startDestination = Ruta.Inicio.path,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Ruta.Inicio.path) {
                PantallaInicio()
            }
            // Hace un listado de catálogo de películas leído de la base de datos
            composable(Ruta.Listado.path) {
                ListaPeliculasScreen(viewModel = viewModel)
            }
            // Aqui se añade un formulario que guarda la película en la base de datos
            composable(Ruta.Formulario.path) {
                FormularioAgregarPeliculaScreen(viewModel = viewModel)
            }
        }
    }
}