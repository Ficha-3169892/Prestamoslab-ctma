package com.example.prestamolabctma.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolabctma.model.CategoriaEquipo
import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.ui.theme.*
import com.example.prestamolabctma.viewmodel.PrestamoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    uiState: PrestamoUiState,
    onEquipoSeleccionado: (Int) -> Unit,
    onToggleEstado: (Int) -> Unit,
    onEliminarEquipo: (Int) -> Unit,
    onMostrarDialogoNuevo: (Boolean) -> Unit,
    onNombreNuevoChanged: (String) -> Unit,
    onCategoriaNuevaChanged: (CategoriaEquipo) -> Unit,
    onAgregarEquipo: () -> Unit,
    onQueryChanged: (String) -> Unit
) {
    val equiposFiltrados = remember(uiState.equipos, uiState.queryBusqueda) {
        if (uiState.queryBusqueda.isBlank()) {
            uiState.equipos
        } else {
            val queryNormalizada = uiState.queryBusqueda.trim().lowercase()
            uiState.equipos.filter { it.nombre.lowercase().contains(queryNormalizada) }
        }
    }

    Scaffold(
        containerColor = TechBackground,
        floatingActionButton = {
            if (uiState.formularioEquipo.mostrarDialogo.not()) {
                FloatingActionButton(
                    onClick = { onMostrarDialogoNuevo(true) },
                    containerColor = TechSecondary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Equipo")
                }
            }
        },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechPrimary,
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        "PréstamoLab CTMA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header Colorido
            ResumenDashboard(uiState.equipos.count { it.estado == EstadoEquipo.DISPONIBLE })

            // Campo de Búsqueda
            OutlinedTextField(
                value = uiState.queryBusqueda,
                onValueChange = onQueryChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                placeholder = { Text("Buscar equipo por nombre...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.queryBusqueda.isNotEmpty()) {
                        IconButton(onClick = { onQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TechPrimary,
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )

            Text(
                "Catálogo de Equipos",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            if (equiposFiltrados.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No hay resultados para la búsqueda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(equiposFiltrados) { equipo ->
                        EquipoModernItem(
                            equipo = equipo,
                            onClick = { onEquipoSeleccionado(equipo.id) },
                            onToggleEstado = { onToggleEstado(equipo.id) },
                            onEliminar = { onEliminarEquipo(equipo.id) }
                        )
                    }
                }
            }
        }

        if (uiState.formularioEquipo.mostrarDialogo) {
            NuevoEquipoDialog(
                state = uiState.formularioEquipo,
                onNombreChanged = onNombreNuevoChanged,
                onCategoriaChanged = onCategoriaNuevaChanged,
                onConfirmar = onAgregarEquipo,
                onDismiss = { onMostrarDialogoNuevo(false) }
            )
        }
    }
}

@Composable
fun ResumenDashboard(disponibles: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = TechPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(TechPrimary, TechSecondary)
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "EQUIPOS DISPONIBLES",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        disponibles.toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "unidades listas\npara préstamo",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 18.sp
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
fun EquipoModernItem(
    equipo: Equipo,
    onClick: () -> Unit,
    onToggleEstado: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = when(equipo.categoria) {
                        CategoriaEquipo.MEDICION -> CatMedicion.copy(alpha = 0.1f)
                        CategoriaEquipo.ELECTRONICA -> CatElectronica.copy(alpha = 0.1f)
                        CategoriaEquipo.COMPUTO -> CatComputo.copy(alpha = 0.1f)
                        else -> TechPrimary.copy(alpha = 0.1f)
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = TechPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = equipo.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = equipo.categoria.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = TechPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                BadgeEstadoModern(estado = equipo.estado)
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Color.LightGray.copy(alpha = 0.5f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onToggleEstado,
                    colors = ButtonDefaults.textButtonColors(contentColor = TechPrimary)
                ) {
                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("ESTADO", fontWeight = FontWeight.Bold)
                }
                
                IconButton(onClick = onEliminar) {
                    Icon(
                        imageVector = Icons.Default.Delete, 
                        contentDescription = "Borrar", 
                        tint = StatusRedVibrant.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun BadgeEstadoModern(estado: EstadoEquipo) {
    val (color, label) = when (estado) {
        EstadoEquipo.DISPONIBLE -> StatusGreenVibrant to "Disponible"
        EstadoEquipo.RESERVADO -> StatusOrangeVibrant to "Reservado"
        EstadoEquipo.PRESTADO -> StatusRedVibrant to "Prestado"
        EstadoEquipo.NO_DISPONIBLE -> Color.Gray to "No Disponible"
    }

    Surface(
        color = color,
        shape = CircleShape
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Color.White,
            fontSize = 9.sp
        )
    }
}

@Composable
fun NuevoEquipoDialog(
    state: com.example.prestamolabctma.viewmodel.FormularioEquipoState,
    onNombreChanged: (String) -> Unit,
    onCategoriaChanged: (CategoriaEquipo) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Nuevo Equipo", fontWeight = FontWeight.Bold) },
        containerColor = TechSurface,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.nombre,
                    onValueChange = onNombreChanged,
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Text("Categoría:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                Column {
                    CategoriaEquipo.entries.forEach { categoria ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCategoriaChanged(categoria) }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = state.categoria == categoria,
                                onClick = { onCategoriaChanged(categoria) }
                            )
                            Text(categoria.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmar, 
                enabled = state.nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}
