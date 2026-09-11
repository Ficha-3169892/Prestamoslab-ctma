package com.example.prestamolabctma.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.prestamolabctma.model.EstadoSolicitud
import com.example.prestamolabctma.ui.CatalogoScreen
import com.example.prestamolabctma.ui.EquipoDetailScreen
import com.example.prestamolabctma.ui.MisSolicitudesScreen
import com.example.prestamolabctma.ui.SolicitudFormScreen
import com.example.prestamolabctma.ui.theme.*
import com.example.prestamolabctma.viewmodel.PrestamoViewModel

@Composable
fun AppNavigation(viewModel: PrestamoViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val screens = listOf("catalogo", "solicitudes")
    val showBottomBar = currentDestination?.route in screens

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = TechPrimary,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        icon = { 
                            BadgedBox(
                                badge = {
                                    val pendientes = uiState.solicitudes.count { it.estado == EstadoSolicitud.SOLICITADA }
                                    if (pendientes > 0) {
                                        Badge(containerColor = TechAccent, contentColor = Color.White) { 
                                            Text(pendientes.toString()) 
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Inventory, contentDescription = null)
                            }
                        },
                        label = { Text("Inventario", fontWeight = FontWeight.Bold) },
                        selected = currentDestination?.hierarchy?.any { it.route == "catalogo" } == true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TechPrimary,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.White
                        ),
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
                        icon = { 
                            BadgedBox(
                                badge = {
                                    val pendientes = uiState.solicitudes.count { it.estado == EstadoSolicitud.SOLICITADA }
                                    if (pendientes > 0) {
                                        Badge(containerColor = TechAccent, contentColor = Color.White) { 
                                            Text(pendientes.toString()) 
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.History, contentDescription = null)
                            }
                        },
                        label = { Text("Solicitudes", fontWeight = FontWeight.Bold) },
                        selected = currentDestination?.hierarchy?.any { it.route == "solicitudes" } == true,
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TechPrimary,
                            selectedTextColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            unselectedTextColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = Color.White
                        ),
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
                    onEquipoSeleccionado = { equipoId ->
                        viewModel.seleccionarEquipo(equipoId)
                        navController.navigate("detalle")
                    },
                    onToggleEstado = { viewModel.toggleEstadoEquipo(it) },
                    onEliminarEquipo = { viewModel.eliminarEquipo(it) },
                    onMostrarDialogoNuevo = { viewModel.mostrarDialogoNuevoEquipo(it) },
                    onNombreNuevoChanged = { viewModel.onNombreEquipoChanged(it) },
                    onCategoriaNuevaChanged = { viewModel.onCategoriaEquipoChanged(it) },
                    onAgregarEquipo = { viewModel.agregarEquipo() },
                    onQueryChanged = { viewModel.onQueryBusquedaChanged(it) },
                    onCategoriaFilterChanged = { viewModel.onCategoriaSelected(it) }
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
                    onAprobarSolicitud = { viewModel.aprobarSolicitud(it) },
                    onRechazarSolicitud = { viewModel.rechazarSolicitud(it) },
                    onFinalizarPrestamo = { viewModel.finalizarPrestamo(it) }
                )
            }
        }
    }
}
