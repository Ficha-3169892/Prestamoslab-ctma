package com.example.prestamolabctma.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.ui.theme.*
import com.example.prestamolabctma.viewmodel.PrestamoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    uiState: PrestamoUiState,
    onEquipoSeleccionado: (Int) -> Unit,
    onVerSolicitudes: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                ),
                title = {
                    Text(
                        "PréstamoLab",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                },
                actions = {
                    IconButton(
                        onClick = onVerSolicitudes,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Resumen (Dashboard Style)
            ResumenDashboard(uiState.equipos.count { it.estado == EstadoEquipo.DISPONIBLE })

            Text(
                "Catálogo de Equipos",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                style = MaterialTheme.typography.titleLarge
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(uiState.equipos) { equipo ->
                    EquipoModernItem(equipo = equipo, onClick = { onEquipoSeleccionado(equipo.id) })
                }
            }
        }
    }
}

@Composable
fun ResumenDashboard(disponibles: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(PrimaryBlue, Color(0xFF3B82F6))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Equipos Disponibles",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        disponibles.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 42.sp
                    )
                    Text(
                        " unidades",
                        color = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(64.dp)
                    .graphicsLayer(alpha = 0.2f),
                tint = Color.White
            )
        }
    }
}

@Composable
fun EquipoModernItem(equipo: Equipo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = equipo.categoria.name,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            BadgeEstadoModern(estado = equipo.estado)
        }
    }
}

@Composable
fun BadgeEstadoModern(estado: EstadoEquipo) {
    val (color, label) = when (estado) {
        EstadoEquipo.DISPONIBLE -> StatusGreen to "Disponible"
        EstadoEquipo.RESERVADO -> StatusOrange to "Reservado"
        EstadoEquipo.PRESTADO -> StatusRed to "Prestado"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = CircleShape
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}
