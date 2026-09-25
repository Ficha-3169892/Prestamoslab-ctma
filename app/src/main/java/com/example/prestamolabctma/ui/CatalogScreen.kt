package com.example.app.ui.screens // Ajusta el package según tu proyecto

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

// ==========================================
// PALETA DE COLORES / TEMA DE LA APP
// ==========================================
val TechPrimary = Color(0xFF1E88E5)
val TechSecondary = Color(0xFF26A69A)
val TechSurface = Color(0xFFF5F5F5)
val TextDark = Color(0xFF212121)
val TextGray = Color(0xFF757575)
val StatusGreenVibrant = Color(0xFF4CAF50)
val StatusOrangeVibrant = Color(0xFFFF9800)
val StatusRedVibrant = Color(0xFFF44336)

// ==========================================
// COMPONENTE PRINCIPAL: CATALOG SCREEN
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Inventario, 1: Préstamos
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }

    // Estados para controlar los diálogos/modales
    var showAddEquipmentDialog by remember { mutableStateOf(false) }
    var equipmentToEdit by remember { mutableStateOf<Equipo?>(null) }
    var equipmentToDelete by remember { mutableStateOf<Equipo?>(null) }
    var equipmentToRequest by remember { mutableStateOf<Equipo?>(null) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Gestión de Equipos",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = if (uiState.isAdmin) "Modo Administrador" else "Modo Prestatario",
                            style = MaterialTheme.typography.bodySmall,
                            color = TechSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditProfileDialog = true }) {
                        Icon(Icons.Default.Person, contentDescription = "Perfil")
                    }
                    IconButton(onClick = {
                        viewModel.logout()
                        onNavigateToLogin()
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar Sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (uiState.isAdmin && selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddEquipmentDialog = true },
                    containerColor = TechPrimary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Equipo")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Pestañas (Tabs): Inventario vs Préstamos
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Inventario") },
                    icon = { Icon(Icons.Default.List, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Préstamos") },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                // Buscador
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar equipo...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Filtros por Categoría
                val categories = listOf("Todos", "Cámaras", "Laptops", "Aulicos", "Audio", "Otros")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }

                // Lista de Equipos
                val filteredEquipos = uiState.equipos.filter { equipo ->
                    (selectedCategory == "Todos" || equipo.categoria.equalsIgnoreCase(selectedCategory)) &&
                            (equipo.nombre.contains(searchQuery, ignoreCase = true) || equipo.descripcion.contains(searchQuery, ignoreCase = true))
                }

                if (filteredEquipos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron equipos disponibles.", color = TextGray)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredEquipos) { equipo ->
                            EquipoItemCard(
                                equipo = equipo,
                                isAdmin = uiState.isAdmin,
                                onRequest = { equipmentToRequest = equipo },
                                onEdit = { equipmentToEdit = equipo },
                                onDelete = { equipmentToDelete = equipo }
                            )
                        }
                    }
                }
            } else {
                // Sección de Préstamos
                if (uiState.isAdmin) {
                    AdminLoansList(
                        loans = uiState.prestamos,
                        equipos = uiState.equipos,
                        viewModel = viewModel,
                        onApprove = { viewModel.aprobarPrestamo(it) },
                        onReject = { viewModel.rechazarPrestamo(it) },
                        onFinalize = { viewModel.finalizarPrestamo(it) }
                    )
                } else {
                    PrestatarioLoansList(
                        loans = uiState.prestamos.filter { it.usuarioId == uiState.currentUserId },
                        equipos = uiState.equipos,
                        viewModel = viewModel,
                        onFinalize = { viewModel.finalizarPrestamo(it) }
                    )
                }
            }
        }
    }

    // ==========================================
    // MODALES Y DIÁLOGOS
    // ==========================================
    if (showAddEquipmentDialog) {
        AddOrEditEquipmentDialog(
            equipo = null,
            onDismiss = { showAddEquipmentDialog = false },
            onConfirm = { nombre, desc, cat, estado ->
                viewModel.agregarEquipo(nombre, desc, cat, estado)
                showAddEquipmentDialog = false
            }
        )
    }

    equipmentToEdit?.let { equipo ->
        AddOrEditEquipmentDialog(
            equipo = equipo,
            onDismiss = { equipmentToEdit = null },
            onConfirm = { nombre, desc, cat, estado ->
                viewModel.actualizarEquipo(equipo.id, nombre, desc, cat, estado)
                equipmentToEdit = null
            }
        )
    }

    equipmentToDelete?.let { equipo ->
        AlertDialog(
            onDismissRequest = { equipmentToDelete = null },
            title = { Text("Eliminar Equipo") },
            text = { Text("¿Estás seguro de que deseas eliminar '${equipo.nombre}' del inventario?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.eliminarEquipo(equipo.id)
                        equipmentToDelete = null
                    }
                ) { Text("Eliminar", color = StatusRedVibrant) }
            },
            dismissButton = {
                TextButton(onClick = { equipmentToDelete = null }) { Text("Cancelar") }
            }
        )
    }

    equipmentToRequest?.let { equipo ->
        RequestLoanDialog(
            equipo = equipo,
            onDismiss = { equipmentToRequest = null },
            onConfirm = { destino, proposito, horas ->
                viewModel.solicitarPrestamo(equipo.id, destino, proposito, horas)
                equipmentToRequest = null
            }
        )
    }

    if (showEditProfileDialog) {
        EditProfileDialog(
            currentName = uiState.userName,
            onDismiss = { showEditProfileDialog = false },
            onSave = { newName ->
                viewModel.actualizarPerfil(newName)
                showEditProfileDialog = false
            }
        )
    }
}

// ==========================================
// COMPONENTES SECUNDARIOS DE UI
// ==========================================

@Composable
fun EquipoItemCard(
    equipo: Equipo,
    isAdmin: Boolean,
    onRequest: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = equipo.nombre,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextDark
                )
                Text(
                    text = equipo.categoria,
                    style = MaterialTheme.typography.labelMedium,
                    color = TechPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = equipo.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (equipo.disponible) StatusGreenVibrant.copy(alpha = 0.1f) else StatusRedVibrant.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (equipo.disponible) "Disponible" else "Ocupado",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (equipo.disponible) StatusGreenVibrant else StatusRedVibrant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (isAdmin) {
                    Row {
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TechPrimary)
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StatusRedVibrant)
                        }
                    }
                } else if (equipo.disponible) {
                    Button(
                        onClick = onRequest,
                        colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("Solicitar", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RequestLoanDialog(
    equipo: Equipo,
    onDismiss: () -> Unit,
    onConfirm: (destino: String, proposito: String, horas: Int) -> Unit
) {
    var destino by remember { mutableStateOf("") }
    var proposito by remember { mutableStateOf("") }
    var horasText by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Solicitar Préstamo: ${equipo.nombre}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = destino,
                    onValueChange = { destino = it },
                    label = { Text("Ambiente de Destino (ej. Aula 102)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = proposito,
                    onValueChange = { proposito = it },
                    label = { Text("Propósito del Préstamo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = horasText,
                    onValueChange = { horasText = it },
                    label = { Text("Duración estimada (Horas)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val horas = horasText.toIntOrNull() ?: 1
                    if (destino.isNotBlank()) {
                        onConfirm(destino, proposito, horas)
                    }
                }
            ) { Text("Enviar Solicitud") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AddOrEditEquipmentDialog(
    equipo: Equipo?,
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, descripcion: String, categoria: String, disponible: Boolean) -> Unit
) {
    var nombre by remember { mutableStateOf(equipo?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(equipo?.descripcion ?: "") }
    var categoria by remember { mutableStateOf(equipo?.categoria ?: "Cámaras") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (equipo == null) "Agregar Nuevo Equipo" else "Editar Equipo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Equipo") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text("Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank()) {
                        onConfirm(nombre, descripcion, categoria, equipo?.disponible ?: true)
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun EditProfileDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre de Usuario") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onSave(name) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

// Extension helper para comparar cadenas
fun String.equalsIgnoreCase(other: String): Boolean {
    return this.equals(other, ignoreCase = true)
}

// ==========================================
// VISTAS Y LISTAS DE PRÉSTAMOS
// ==========================================

@Composable
fun PrestatarioLoansList(
    loans: List<SolicitudPrestamo>,
    equipos: List<Equipo>,
    viewModel: CatalogViewModel,
    onFinalize: (Int) -> Unit
) {
    if (loans.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aún no has solicitado préstamos.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(loans) { loan ->
                val equipoNombre = equipos.find { it.id == loan.equipoId }?.nombre ?: "Dispositivo #${loan.equipoId}"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TechSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(equipoNombre, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = TechPrimary)
                                Text("Solicitud #${loan.id}", style = MaterialTheme.typography.bodySmall, color = TextGray)
                            }
                            Surface(
                                color = when(loan.estado) {
                                    EstadoSolicitud.APROBADA -> StatusGreenVibrant.copy(alpha = 0.1f)
                                    EstadoSolicitud.SOLICITADA -> StatusOrangeVibrant.copy(alpha = 0.1f)
                                    EstadoSolicitud.RECHAZADA -> StatusRedVibrant.copy(alpha = 0.1f)
                                    else -> Color.Gray.copy(alpha = 0.1f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = loan.estado.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when(loan.estado) {
                                        EstadoSolicitud.APROBADA -> StatusGreenVibrant
                                        EstadoSolicitud.SOLICITADA -> StatusOrangeVibrant
                                        EstadoSolicitud.RECHAZADA -> StatusRedVibrant
                                        else -> Color.Gray
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Destino: ${loan.ambienteDestino}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        if (loan.proposito.isNotBlank()) {
                            Text("Propósito: ${loan.proposito}", style = MaterialTheme.typography.bodySmall, color = TextGray)
                        }
                        Text("Duración: ${loan.duracionHoras}h", style = MaterialTheme.typography.bodySmall, color = TextGray)

                        if (loan.estado == EstadoSolicitud.APROBADA) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tiempo restante: ${viewModel.getRemainingTime(loan)}",
                                fontWeight = FontWeight.Bold,
                                color = TechSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onFinalize(loan.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Finalizar Préstamo")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminLoansList(
    loans: List<SolicitudPrestamo>,
    equipos: List<Equipo>,
    viewModel: CatalogViewModel,
    onApprove: (Int) -> Unit,
    onReject: (Int) -> Unit,
    onFinalize: (Int) -> Unit
) {
    val activeLoans = loans.filter { it.estado == EstadoSolicitud.SOLICITADA || it.estado == EstadoSolicitud.APROBADA }

    if (activeLoans.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Sin préstamos activos.", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(activeLoans) { loan ->
                val equipoNombre = equipos.find { it.id == loan.equipoId }?.nombre ?: "Dispositivo #${loan.equipoId}"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TechSurface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = loan.borrowerName ?: "Solicitante",
                                    fontWeight = FontWeight.Bold,
                                    color = TechPrimary,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Equipo: $equipoNombre",
                                    fontWeight = FontWeight.SemiBold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextDark
                                )
                            }
                            Surface(
                                color = when(loan.estado) {
                                    EstadoSolicitud.SOLICITADA -> StatusOrangeVibrant.copy(alpha = 0.1f)
                                    EstadoSolicitud.APROBADA -> StatusGreenVibrant.copy(alpha = 0.1f)
                                    else -> Color.Gray.copy(alpha = 0.1f)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = loan.estado.name,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when(loan.estado) {
                                        EstadoSolicitud.SOLICITADA -> StatusOrangeVibrant
                                        EstadoSolicitud.APROBADA -> StatusGreenVibrant
                                        else -> Color.Gray
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Destino: ${loan.ambienteDestino}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        if (loan.proposito.isNotBlank()) {
                            Text("Propósito: ${loan.proposito}", style = MaterialTheme.typography.bodySmall, color = TextGray)
                        }
                        Text("Duración: ${loan.duracionHoras}h", style = MaterialTheme.typography.bodySmall, color = TextGray)

                        if (loan.estado == EstadoSolicitud.APROBADA) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tiempo restante: ${viewModel.getRemainingTime(loan)}",
                                fontWeight = FontWeight.Bold,
                                color = TechSecondary,
                                fontSize = 12.sp
                            )
                        }

                        if (!loan.fotoValidacionUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Foto de verificación:", style = MaterialTheme.typography.labelSmall, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Image(
                                painter = rememberAsyncImagePainter(loan.fotoValidacionUrl),
                                contentDescription = "Foto de validación",
                                modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        if (loan.estado == EstadoSolicitud.SOLICITADA) {
                            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { onApprove(loan.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreenVibrant)
                                ) {
                                    Text("Aprobar")
                                }
                                Button(
                                    onClick = { onReject(loan.id) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusRedVibrant)
                                ) {
                                    Text("Rechazar")
                                }
                            }
                        } else if (loan.estado == EstadoSolicitud.APROBADA) {
                            Button(
                                onClick = { onFinalize(loan.id) },
                                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                            ) {
                                Text("Finalizar Préstamo")
                            }
                        }
                    }
                }
            }
        }
    }
}