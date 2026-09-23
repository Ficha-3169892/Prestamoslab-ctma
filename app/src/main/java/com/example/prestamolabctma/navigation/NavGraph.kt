package com.example.prestamolabctma.navigation

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.prestamolabctma.PrestamoLabApp
import com.example.prestamolabctma.data.remote.SupabaseProvider
import com.example.prestamolabctma.ui.CatalogScreen
import com.example.prestamolabctma.ui.EquipmentDetailScreen
import com.example.prestamolabctma.ui.LoanRequestScreen
import com.example.prestamolabctma.ui.MyLoansScreen
import com.example.prestamolabctma.ui.auth.AuthViewModel
import com.example.prestamolabctma.ui.auth.LoginScreen
import com.example.prestamolabctma.ui.auth.RegisterScreen
import com.example.prestamolabctma.viewmodel.CatalogViewModel
import com.example.prestamolabctma.viewmodel.CatalogViewModelFactory

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val app = context.applicationContext as PrestamoLabApp

    val catalogViewModel: CatalogViewModel = viewModel(
        factory = CatalogViewModelFactory(app.repository)
    )

    val supabaseClient = SupabaseProvider.client
    val authViewModel: AuthViewModel = viewModel {
        AuthViewModel(supabaseClient)
    }

    val navController = rememberNavController()

    val uiState by catalogViewModel.uiState.collectAsStateWithLifecycle()
    val query by catalogViewModel.queryBusqueda.collectAsStateWithLifecycle()
    val category by catalogViewModel.categoriaSeleccionada.collectAsStateWithLifecycle()
    val equipmentDetail by catalogViewModel.equipmentDetail.collectAsStateWithLifecycle()
    val myLoans by catalogViewModel.myLoans.collectAsStateWithLifecycle()
    val isRequesting by catalogViewModel.isRequesting.collectAsStateWithLifecycle()
    val currentUserName by authViewModel.userName.collectAsStateWithLifecycle()

    LaunchedEffect(currentUserName) {
        if (currentUserName.isNotBlank()) {
            catalogViewModel.updateProfile(currentUserName, null)
        }
    }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            Surface(modifier = Modifier.fillMaxSize()) {
                LoginScreen(
                    viewModel = authViewModel,
                    onLoginSuccess = { role ->
                        Toast.makeText(context, "Bienvenido. Rol: $role", Toast.LENGTH_SHORT).show()
                        catalogViewModel.setRole(role)
                        navController.navigate("catalogo") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate("registro")
                    }
                )
            }
        }

        composable("registro") {
            Surface(modifier = Modifier.fillMaxSize()) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onRegisterSuccess = { role ->
                        Toast.makeText(context, "¡Registro exitoso! Rol: $role", Toast.LENGTH_LONG).show()
                        catalogViewModel.setRole(role)
                        navController.navigate("catalogo") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }
        }

        composable("catalogo") {
            CatalogScreen(
                viewModel = catalogViewModel,
                onEquipmentClick = { id ->
                    catalogViewModel.selectEquipment(id)
                    navController.navigate("detalle")
                },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("detalle") {
            EquipmentDetailScreen(
                equipo = equipmentDetail,
                onSolicitar = { navController.navigate("solicitud") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("solicitud") {
            LoanRequestScreen(
                equipo = equipmentDetail,
                isRequesting = isRequesting,
                onConfirmRequest = { ambiente, proposito, duracion, photoUrl ->
                    catalogViewModel.createLoan(ambiente, proposito, duracion, photoUrl)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("mis_prestamos") {
            MyLoansScreen(
                loans = myLoans,
                onBack = { navController.popBackStack() }
            )
        }
    }
}