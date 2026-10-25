package com.example.damflix

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

// Rutas de la BottomBar centralizadas en una sealed class
// label es el id del texto en strings.xml (no se permiten cadenas hardcoded)
sealed class Ruta(val path: String, val label: Int, val icon: ImageVector) {
    object Inicio : Ruta(
        path = "inicio",
        label = R.string.navegacion_inicio,
        icon = Icons.Filled.Home
    )

    object Listado : Ruta(
        path = "listado",
        label = R.string.navegacion_listado,
        icon = Icons.Filled.List
    )

    object Formulario : Ruta(
        path = "alta",
        label = R.string.navegacion_anadir,
        icon = Icons.Filled.Add
    )
}