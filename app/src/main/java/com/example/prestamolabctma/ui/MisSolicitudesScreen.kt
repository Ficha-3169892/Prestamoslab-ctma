package com.example.prestamolabctma.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.prestamolabctma.model.EstadoSolicitud
import com.example.prestamolabctma.ui.theme.*
import com.example.prestamolabctma.viewmodel.PrestamoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    uiState: PrestamoUiState,
    onCancelarSolicitud: (Int) -> Unit,
    onVolver: () -> Unit
) {
    var solicitudACancelar by androidx.compose.runtime.remember { 
        androidx.compose.runtime.mutableStateOf<Int?>(null) 
    }

    Scaffold(
        containerColor = BackgroundSlate,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
                title = { Text("Mis Solicitudes", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.solicitudes.isEmpty()) {
            EmptySolicitudes(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                items(uiState.solicitudes) { solicitud ->
                    SolicitudModernItem(solicitud, onCancelarClick = { solicitudACancelar = it })
                }
            }
        }

        // Dialogo de Confirmación
        solicitudACancelar?.let { id ->
            AlertDialog(
                onDismissRequest = { solicitudACancelar = null },
                confirmButton = {
                    Button(
                        onClick = {
                            onCancelarSolicitud(id)
                            solicitudACancelar = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                    ) {
                        Text("Confirmar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { solicitudACancelar = null }) {
                        Text("Cancelar")
                    }
                },
                title = { Text("¿Cancelar solicitud?") },
                text = { Text("Esta acción no se puede deshacer y el equipo volverá a estar disponible.") },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun SolicitudModernItem(
    solicitud: com.example.prestamolabctma.model.SolicitudPrestamo,
    onCancelarClick: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(PrimaryBlueLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Solicitud #${solicitud.id}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark
                        )
                        Text(
                            solicitud.ambienteDestino,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextGray
                        )
                    }
                }
                BadgeEstadoSolicitud(solicitud.estado)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )

            DetailRow("Propósito", solicitud.proposito)
            DetailRow("Duración", "${solicitud.duracionHoras} horas")

            if (solicitud.estado == EstadoSolicitud.SOLICITADA) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { onCancelarClick(solicitud.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(StatusRed))
                ) {
                    Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar Solicitud", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = TextGray
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = TextDark
        )
    }
}

@Composable
fun BadgeEstadoSolicitud(estado: EstadoSolicitud) {
    val color = when (estado) {
        EstadoSolicitud.SOLICITADA -> PrimaryBlue
        EstadoSolicitud.APROBADA -> StatusGreen
        EstadoSolicitud.CANCELADA, EstadoSolicitud.RECHAZADA -> StatusRed
        else -> StatusOrange
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = estado.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@Composable
fun EmptySolicitudes(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = TextGray.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Sin solicitudes activas",
            style = MaterialTheme.typography.titleMedium,
            color = TextGray
        )
    }
}
