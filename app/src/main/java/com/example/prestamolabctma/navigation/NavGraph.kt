package com.example.prestamolabctma.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prestamolabctma.ui.CatalogoScreen
import com.example.prestamolabctma.ui.EquipoDetailScreen
import com.example.prestamolabctma.ui.MisSolicitudesScreen
import com.example.prestamolabctma.ui.SolicitudFormScreen
import com.example.prestamolabctma.viewmodel.PrestamoViewModel

@Composable
fun AppNavigation(viewModel: PrestamoViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(navController = navController, startDestination = "catalogo") {
        composable("catalogo") {
            CatalogoScreen(
                uiState = uiState,
                onEquipoSeleccionado = { equipoId ->
                    viewModel.seleccionarEquipo(equipoId)
                    navController.navigate("detalle")
                },
                onVerSolicitudes = { navController.navigate("solicitudes") }
            )
        }
        composable("detalle") {
            EquipoDetailScreen(
                uiState = uiState,
                onSolicitar = { navController.navigate("formulario") },
                onVolver = { navController.popBackStack() }
            )
        }
        composable("formulario") {
            SolicitudFormScreen(
                viewModel = viewModel,
                uiState = uiState,
                onVolver = { navController.popBackStack() }
            )
        }
        composable("solicitudes") {
            MisSolicitudesScreen(
                uiState = uiState,
                onCancelarSolicitud = { viewModel.cancelarSolicitud(it) },
                onVolver = { navController.popBackStack() }
            )
        }
    }
}