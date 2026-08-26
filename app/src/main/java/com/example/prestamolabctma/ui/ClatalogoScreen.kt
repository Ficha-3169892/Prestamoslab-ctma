package com.example.prestamolabctma.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.viewmodel.PrestamoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    uiState: PrestamoUiState,
    onEquipoSeleccionado: (Int) -> Unit,
    onVerSolicitudes: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PréstamoLab CTMA") },
                actions = {
                    TextButton(onClick = onVerSolicitudes) {
                        Text("Mis Solicitudes")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.equipos) { equipo ->
                EquipoItem(equipo = equipo, onClick = { onEquipoSeleccionado(equipo.id) })
            }
        }
    }
}

@Composable
fun EquipoItem(equipo: Equipo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = equipo.nombre, style = MaterialTheme.typography.titleMedium)
                Text(text = equipo.categoria.name, style = MaterialTheme.typography.bodySmall)
            }
            BadgeEstado(estado = equipo.estado)
        }
    }
}

@Composable
fun BadgeEstado(estado: EstadoEquipo) {
    val color = when (estado) {
        EstadoEquipo.DISPONIBLE -> Color(0xFF4CAF50)
        EstadoEquipo.RESERVADO -> Color(0xFFFF9800)
        EstadoEquipo.PRESTADO -> Color(0xFFF44336)
    }
    Surface(
        color = color.copy(alpha = 0.2f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = estado.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}