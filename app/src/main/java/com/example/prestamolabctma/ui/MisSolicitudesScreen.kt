package com.example.prestamolabctma.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolabctma.model.EstadoSolicitud
import com.example.prestamolabctma.viewmodel.PrestamoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    uiState: PrestamoUiState,
    onCancelarSolicitud: (Int) -> Unit,
    onVolver: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Solicitudes") },
                navigationIcon = { TextButton(onClick = onVolver) { Text("Atrás") } }
            )
        }
    ) { padding ->
        if (uiState.solicitudes.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No tienes solicitudes registradas.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.solicitudes) { solicitud ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Solicitud #${solicitud.id}", style = MaterialTheme.typography.titleMedium)
                            Text("Ambiente: ${solicitud.ambienteDestino}")
                            Text("Propósito: ${solicitud.proposito}")
                            Text("Duración: ${solicitud.duracionHoras} hrs")
                            Text("Estado: ${solicitud.estado.name}")

                            if (solicitud.estado == EstadoSolicitud.SOLICITADA) {
                                Button(
                                    onClick = { onCancelarSolicitud(solicitud.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Cancelar Solicitud")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}