package com.example.prestamolabctma.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.prestamolabctma.model.CategoriaEquipo
import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.model.EstadoSolicitud
import com.example.prestamolabctma.model.SolicitudPrestamo
import com.example.prestamolabctma.ui.theme.*
import com.example.prestamolabctma.viewmodel.CatalogUiState
import com.example.prestamolabctma.viewmodel.CatalogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: CatalogViewModel,
    onEquipmentClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val queryBusqueda by viewModel.queryBusqueda.collectAsState()
    val categoriaSeleccionada by viewModel.categoriaSeleccionada.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val userName by viewModel.userName.collectAsState()
    val allLoansAdmin by viewModel.allLoansAdmin.collectAsState()
    val myLoans by viewModel.myLoans.collectAsState()

    var showProfileDialog by remember { mutableStateOf(false) }
    var showAddEquipmentDialog by remember { mutableStateOf(false) }
    var showEditEquipmentDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedEquipmentToEdit by remember { mutableStateOf<Equipo?>(null) }
    var equipmentToDelete by remember { mutableStateOf<Equipo?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) } 

    var editName by remember(userName) { mutableStateOf(userName) }

    Scaffold(
        containerColor = TechBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechPrimary,
                    titleContentColor = Color.White
                ),
                title = {
                    Text(
                        text = if (userRole == "DUEÑO") "Panel Administrativo" else "PréstamoLab CTMA",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                },
                actions = {
                    IconButton(onClick = { 
                        editName = userName
                        showProfileDialog = true 
                    }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            if (userRole == "DUEÑO" && selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddEquipmentDialog = true },
                    containerColor = TechSecondary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Dispositivo")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(TechBackground)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab, 
                containerColor = TechSurface,
                contentColor = TechPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TechSecondary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Inventario", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { 
                        val count = if(userRole == "DUEÑO") allLoansAdmin.count { it.estado == EstadoSolicitud.SOLICITADA || it.estado == EstadoSolicitud.APROBADA } else myLoans.size
                        val label = if(userRole == "DUEÑO") "Préstamos ($count)" else "Mis Préstamos ($count)"
                        Text(text = label, fontWeight = FontWeight.Bold) 
                    }
                )
            }

            if (selectedTab == 1) {
                val equiposList = (uiState as? CatalogUiState.Success)?.equipos ?: emptyList()
                if (userRole == "DUEÑO") {
                    AdminLoansList(
                        loans = allLoansAdmin,
                        equipos = equiposList,
                        viewModel = viewModel,
                        onApprove = { viewModel.changeLoanStatus(it, "APROBADA") },
                        onReject = { viewModel.changeLoanStatus(it, "RECHAZADA") },
                        onFinalize = { viewModel.finalizeLoan(it) }
                    )
                } else {
                    PrestatarioLoansList(
                        loans = myLoans,
                        equipos = equiposList,
                        viewModel = viewModel,
                        onFinalize = { viewModel.finalizeLoan(it) }
                    )
                }
            } else {
                Column {
                    OutlinedTextField(
                        value = queryBusqueda,
                        onValueChange = { viewModel.onQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        placeholder = { Text("Buscar dispositivo...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TechPrimary) },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = categoriaSeleccionada == null,
                                onClick = { viewModel.onCategorySelected(null) },
                                label = { Text("Todas") }
                            )
                        }
                        items(CategoriaEquipo.entries) { cat ->
                            FilterChip(
                                selected = categoriaSeleccionada == cat,
                                onClick = { viewModel.onCategorySelected(cat) },
                                label = { Text(cat.name) }
                            )
                        }
                    }

                    when (uiState) {
                        is CatalogUiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = TechPrimary)
                            }
                        }
                        is CatalogUiState.Empty -> {
                            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(Icons.Default.Inventory, contentDescription = null, tint = TechSecondary, modifier = Modifier.size(56.dp))
                                    Text("No hay equipos registrados en el inventario.", fontWeight = FontWeight.Bold, color = TextDark, textAlign = TextAlign.Center)
                                }
                            }
                        }
                        is CatalogUiState.Error -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Error de conexión", color = Color.Red)
                            }
                        }
                        is CatalogUiState.Success -> {
                            val equiposList = (uiState as CatalogUiState.Success).equipos
                            if (equiposList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.SearchOff,
                                            contentDescription = null,
                                            tint = TechSecondary,
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Text(
                                            text = when {
                                                categoriaSeleccionada != null -> "No hay dispositivos registrados en la categoría '${categoriaSeleccionada?.name}'"
                                                queryBusqueda.isNotBlank() -> "No se encontraron resultados para '$queryBusqueda'"
                                                else -> "No se encontraron dispositivos"
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            color = TextDark
                                        )
                                        Text(
                                            text = "Intenta buscar con otros términos o selecciona otra categoría.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.onCategorySelected(null)
                                                viewModel.onQueryChanged("")
                                            },
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Icon(Icons.Default.FilterAltOff, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Limpiar filtros")
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(equiposList, key = { it.id }) { equipo ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(16.dp),
                                            colors = CardDefaults.cardColors(containerColor = TechSurface),
                                            elevation = CardDefaults.cardElevation(2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .then(
                                                        if (userRole != "DUEÑO") {
                                                            Modifier.clickable { onEquipmentClick(equipo.id) }
                                                        } else Modifier
                                                    )
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Surface(
                                                    modifier = Modifier.size(44.dp),
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = TechPrimary.copy(alpha = 0.1f)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = TechPrimary)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.width(16.dp))

                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(equipo.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextDark)
                                                    Text(equipo.categoria.name, style = MaterialTheme.typography.bodySmall, color = Color.Gray, fontWeight = FontWeight.Medium)
                                                }

                                                if (userRole == "DUEÑO") {
                                                    IconButton(onClick = {
                                                        selectedEquipmentToEdit = equipo
                                                        showEditEquipmentDialog = true
                                                    }) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TechSecondary)
                                                    }
                                                    IconButton(onClick = {
                                                        equipmentToDelete = equipo
                                                        showDeleteConfirmDialog = true
                                                    }) {
                                                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = StatusRedVibrant)
                                                    }
                                                } else {
                                                    Surface(
                                                        color = if (equipo.estado == EstadoEquipo.DISPONIBLE) StatusGreenVibrant.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f),
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Text(
                                                            text = equipo.estado.name,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (equipo.estado == EstadoEquipo.DISPONIBLE) StatusGreenVibrant else Color.Gray
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // MODAL DE PERFIL
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            confirmButton = {},
            containerColor = TechSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = TechPrimary, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Cuenta", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.padding(vertical = 8.dp)) {
                        Surface(
                            modifier = Modifier.size(90.dp).border(2.dp, TechPrimary, CircleShape),
                            shape = CircleShape,
                            color = TechPrimary.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = TechPrimary, modifier = Modifier.size(48.dp))
                            }
                        }
                    }

                    Surface(
                        color = TechSecondary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ROL: $userRole",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.labelSmall,
                            color = TechSecondary
                        )
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = TechPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.updateProfile(editName, null)
                            showProfileDialog = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar Perfil", fontWeight = FontWeight.Bold)
                    }

                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

                    Button(
                        onClick = {
                            showProfileDialog = false
                            onLogout() 
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRedVibrant)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar Sesión", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        )
    }

    // DIÁLOGOS DE AGREGAR/EDITAR
    if (showAddEquipmentDialog) {
        var newName by remember { mutableStateOf("") }
        var newDesc by remember { mutableStateOf("") }
        var selectedCat by remember { mutableStateOf(CategoriaEquipo.ELECTRONICA) }

        AlertDialog(
            onDismissRequest = { showAddEquipmentDialog = false },
            title = { Text("Nuevo Dispositivo", fontWeight = FontWeight.Bold) },
            containerColor = TechSurface,
            shape = RoundedCornerShape(24.dp),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newDesc,
                        onValueChange = { newDesc = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text("Categoría:", fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoriaEquipo.entries.forEach { cat ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedCat = cat }
                                    .border(1.dp, if (selectedCat == cat) TechPrimary else Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedCat == cat) TechPrimary.copy(alpha = 0.1f) else Color.Transparent
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = selectedCat == cat, onClick = { selectedCat = cat })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat.name, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            viewModel.addNewEquipment(newName, selectedCat, newDesc)
                            showAddEquipmentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                ) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEquipmentDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showEditEquipmentDialog && selectedEquipmentToEdit != null) {
        var editNameEq by remember { mutableStateOf(selectedEquipmentToEdit!!.nombre) }
        var editDescEq by remember { mutableStateOf(selectedEquipmentToEdit!!.descripcion) }
        var editCatEq by remember { mutableStateOf(selectedEquipmentToEdit!!.categoria) }

        AlertDialog(
            onDismissRequest = { showEditEquipmentDialog = false },
            title = { Text("Editar Dispositivo", fontWeight = FontWeight.Bold) },
            containerColor = TechSurface,
            shape = RoundedCornerShape(24.dp),
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editNameEq,
                        onValueChange = { editNameEq = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editDescEq,
                        onValueChange = { editDescEq = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text("Categoría:", fontWeight = FontWeight.Bold)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        CategoriaEquipo.entries.forEach { cat ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { editCatEq = cat }
                                    .border(1.dp, if (editCatEq == cat) TechPrimary else Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                                shape = RoundedCornerShape(10.dp),
                                color = if (editCatEq == cat) TechPrimary.copy(alpha = 0.1f) else Color.Transparent
                            ) {
                                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = editCatEq == cat, onClick = { editCatEq = cat })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(cat.name, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNameEq.isNotBlank()) {
                            viewModel.updateEquipmentAdmin(
                                id = selectedEquipmentToEdit!!.id,
                                nombre = editNameEq,
                                categoria = editCatEq,
                                descripcion = editDescEq,
                                estado = selectedEquipmentToEdit!!.estado
                            )
                            showEditEquipmentDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditEquipmentDialog = false }) { Text("Cancelar") }
            }
        )
    }

    if (showDeleteConfirmDialog && equipmentToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRedVibrant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Dispositivo", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar '${equipmentToDelete!!.nombre}'? Esta acción no se puede deshacer.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            containerColor = TechSurface,
            shape = RoundedCornerShape(24.dp),
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.removeEquipment(equipmentToDelete!!.id)
                        showDeleteConfirmDialog = false
                        equipmentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRedVibrant)
                ) {
                    Text("Eliminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

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