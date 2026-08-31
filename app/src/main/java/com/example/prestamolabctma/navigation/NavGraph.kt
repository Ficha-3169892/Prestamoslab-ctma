package com.example.prestamolabctma.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.prestamolabctma.ui.CatalogoScreen
import com.example.prestamolabctma.ui.MisSolicitudesScreen
import com.example.prestamolabctma.ui.SolicitudFormScreen
import com.example.prestamolabctma.viewmodel.PrestamoViewModel

@Composable
fun AppNavigation(viewModel: PrestamoViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.route in listOf("catalogo", "solicitudes")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.mensajeExito, uiState.mensajeError) {
        uiState.mensajeExito?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
        uiState.mensajeError?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Inicio") },
                        selected = currentDestination?.hierarchy?.any { it.route == "catalogo" } == true,
                        onClick = {
                            navController.navigate("catalogo") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.History, contentDescription = null) },
                        label = { Text("Mis Préstamos") },
                        selected = currentDestination?.hierarchy?.any { it.route == "solicitudes" } == true,
                        onClick = {
                            navController.navigate("solicitudes") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "catalogo",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("catalogo") {
                CatalogoScreen(
                    uiState = uiState,
                    onBusquedaChanged = { viewModel.onBusquedaChanged(it) },
                    onEquipoSeleccionado = { equipoId ->
                        viewModel.seleccionarEquipo(equipoId)
                        navController.navigate("formulario")
                    },
                    onVerSolicitudes = { navController.navigate("solicitudes") }
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
}
