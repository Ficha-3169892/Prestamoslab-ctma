package com.example.prestamolabctma.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolabctma.model.EstadoSolicitud
import com.example.prestamolabctma.ui.theme.*
import com.example.prestamolabctma.viewmodel.PrestamoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisSolicitudesScreen(
    uiState: PrestamoUiState,
    onCancelarSolicitud: (Int) -> Unit,
    onAprobarSolicitud: (Int) -> Unit,
    onRechazarSolicitud: (Int) -> Unit,
    onFinalizarPrestamo: (Int) -> Unit
) {
    Scaffold(
        containerColor = TechBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechPrimary,
                    titleContentColor = Color.White
                ),
                title = { Text("Gestión de Solicitudes", fontWeight = FontWeight.Bold) }
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
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                items(uiState.solicitudes) { solicitud ->
                    val equipo = uiState.equipos.find { it.id == solicitud.equipoId }
                    val nombreEquipo = equipo?.nombre ?: "Equipo desconocido"
                    
                    SolicitudModernItem(
                        solicitud = solicitud,
                        nombreEquipo = nombreEquipo,
                        onCancelarSolicitud = onCancelarSolicitud,
                        onAprobarSolicitud = onAprobarSolicitud,
                        onRechazarSolicitud = onRechazarSolicitud,
                        onFinalizarPrestamo = onFinalizarPrestamo
                    )
                }
            }
        }
    }
}

@Composable
fun SolicitudModernItem(
    solicitud: com.example.prestamolabctma.model.SolicitudPrestamo,
    nombreEquipo: String,
    onCancelarSolicitud: (Int) -> Unit,
    onAprobarSolicitud: (Int) -> Unit,
    onRechazarSolicitud: (Int) -> Unit,
    onFinalizarPrestamo: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nombreEquipo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TechPrimary
                    )
                    Text(
                        "Orden #${solicitud.id} • ${solicitud.ambienteDestino}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextGray
                    )
                }
                BadgeEstadoSolicitud(solicitud.estado)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.3f))

            DetailRow("Propósito", solicitud.proposito)
            DetailRow("Duración", "${solicitud.duracionHoras}h de préstamo")

            if (solicitud.estado == EstadoSolicitud.SOLICITADA) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAprobarSolicitud(solicitud.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusGreenVibrant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("APROBAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    
                    Button(
                        onClick = { onRechazarSolicitud(solicitud.id) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRedVibrant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RECHAZAR", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                
                TextButton(
                    onClick = { onCancelarSolicitud(solicitud.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = TextGray)
                ) {
                    Text("Eliminar Solicitud", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            if (solicitud.estado == EstadoSolicitud.APROBADA) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onFinalizarPrestamo(solicitud.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = TechPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("MARCAR COMO DEVUELTO", fontWeight = FontWeight.Bold)
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
            color = TextDark
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            color = TextGray
        )
    }
}

@Composable
fun BadgeEstadoSolicitud(estado: EstadoSolicitud) {
    val (color, label) = when (estado) {
        EstadoSolicitud.SOLICITADA -> TechPrimary to "Pendiente"
        EstadoSolicitud.APROBADA -> StatusGreenVibrant to "Activo"
        EstadoSolicitud.RECHAZADA -> StatusRedVibrant to "Rechazado"
        EstadoSolicitud.CANCELADA -> Color.Gray to "Cancelado"
        EstadoSolicitud.DEVUELTA -> Color.DarkGray to "Devuelto"
        else -> StatusOrangeVibrant to estado.name
    }
    Surface(
        color = color,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Black,
            color = Color.White,
            fontSize = 9.sp
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
            tint = TechPrimary.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Sin solicitudes activas",
            style = MaterialTheme.typography.titleMedium,
            color = TextGray
        )
    }
}
