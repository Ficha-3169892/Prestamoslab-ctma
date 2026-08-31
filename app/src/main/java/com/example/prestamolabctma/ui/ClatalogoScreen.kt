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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onBusquedaChanged: (String) -> Unit,
    onEquipoSeleccionado: (Int) -> Unit,
    onAgregarEquipo: () -> Unit,
    onEditarEquipo: (com.example.prestamolabctma.model.Equipo) -> Unit,
    onEliminarEquipo: (Int) -> Unit
) {
    var equipoMostrarDetalle by remember { 
        mutableStateOf<Equipo?>(null) 
    }

    var equipoAEliminar by remember {
        mutableStateOf<Int?>(null)
    }

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
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarEquipo,
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Equipo")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Buscador Moderno
            SearchBarModern(
                query = uiState.queryBusqueda,
                onQueryChange = onBusquedaChanged
            )

            // Header Resumen (Dashboard Style)
            ResumenDashboard(uiState.equipos.count { it.estado == EstadoEquipo.DISPONIBLE })

            Text(
                "Catálogo de Equipos",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                style = MaterialTheme.typography.titleLarge
            )

            if (uiState.equiposFiltrados.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No se encontraron equipos", color = TextGray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(uiState.equiposFiltrados) { equipo ->
                        EquipoModernItem(equipo = equipo, onClick = { equipoMostrarDetalle = equipo })
                    }
                }
            }
        }

        // Dialogo de Detalle
        equipoMostrarDetalle?.let { equipo ->
            DetalleEquipoDialog(
                equipo = equipo,
                onDismiss = { equipoMostrarDetalle = null },
                onConfirmar = {
                    equipoMostrarDetalle = null
                    onEquipoSeleccionado(equipo.id)
                },
                onEditar = {
                    equipoMostrarDetalle = null
                    onEditarEquipo(equipo)
                },
                onEliminar = {
                    equipoMostrarDetalle = null
                    equipoAEliminar = equipo.id
                }
            )
        }

        // Dialogo de Confirmación de Eliminación
        equipoAEliminar?.let { id ->
            AlertDialog(
                onDismissRequest = { equipoAEliminar = null },
                confirmButton = {
                    Button(
                        onClick = {
                            onEliminarEquipo(id)
                            equipoAEliminar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { equipoAEliminar = null }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("¿Eliminar equipo?") },
                text = { Text("Esta acción eliminará el equipo del catálogo permanentemente.") },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun DetalleEquipoDialog(
    equipo: Equipo,
    onDismiss: () -> Unit,
    onConfirmar: () -> Unit,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirmar,
                enabled = equipo.estado == EstadoEquipo.DISPONIBLE,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (equipo.estado == EstadoEquipo.DISPONIBLE) "Solicitar Préstamo" else "No Disponible")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(equipo.nombre, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Row {
                    IconButton(onClick = onEditar) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = PrimaryBlue)
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StatusRed)
                    }
                }
            }
        },
        text = {
            Column {
                BadgeEstadoModern(estado = equipo.estado)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = equipo.descripcion.ifBlank { "Sin descripción disponible." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Categoría: ${equipo.categoria.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextGray
                )
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = Color.White
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBarModern(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        placeholder = { Text("Buscar equipo o categoría...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        ),
        singleLine = true
    )
}

@Composable
fun ResumenDashboard(disponibles: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(PrimaryBlue, Color(0xFF4F46E5))
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "Estado del Inventario",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        disponibles.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    ) {
                        Text(
                            "Disponibles",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Default.Inventory2,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(80.dp)
                    .graphicsLayer(alpha = 0.15f, rotationZ = -15f),
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconBackground = when (equipo.estado) {
                EstadoEquipo.DISPONIBLE -> PrimaryBlueLight
                else -> Color(0xFFF1F5F9)
            }
            
            val iconTint = when (equipo.estado) {
                EstadoEquipo.DISPONIBLE -> PrimaryBlue
                else -> TextGray
            }

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = equipo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextDark
                )
                Text(
                    text = equipo.categoria.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
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
