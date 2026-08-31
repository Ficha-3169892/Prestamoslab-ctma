package com.example.prestamolabctma.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.ui.theme.*
import com.example.prestamolabctma.viewmodel.PrestamoViewModel
import com.example.prestamolabctma.viewmodel.PrestamoUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    viewModel: PrestamoViewModel,
    uiState: PrestamoUiState,
    onVolver: () -> Unit
) {
    val equipo = uiState.equipoSeleccionado
    val form = uiState.formulario

    Scaffold(
        containerColor = TechBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = TechPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                title = { Text("Nueva Solicitud", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            if (equipo != null) {
                // Info Card del Equipo
                Card(
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(containerColor = TechSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape,
                            color = TechPrimary.copy(alpha = 0.1f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = TechPrimary)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                equipo.nombre, 
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TechPrimary
                            )
                            BadgeEstadoModern(estado = equipo.estado)
                        }
                    }
                }

                if (equipo.estado != EstadoEquipo.DISPONIBLE) {
                    AlertNoDisponible()
                } else {
                    Text("Detalles del Préstamo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    ModernTextField(
                        value = form.ambienteDestino,
                        onValueChange = { viewModel.onAmbienteChanged(it) },
                        label = "Ambiente / Destino",
                        placeholder = "Ej. Aula 204",
                        error = form.errorAmbiente
                    )

                    ModernTextField(
                        value = form.proposito,
                        onValueChange = { viewModel.onPropositoChanged(it) },
                        label = "Propósito",
                        placeholder = "Motivo del préstamo",
                        error = form.errorProposito
                    )

                    ModernTextField(
                        value = form.duracionHoras,
                        onValueChange = { viewModel.onDuracionChanged(it) },
                        label = "Duración (Horas)",
                        placeholder = "1",
                        error = form.errorDuracion
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.guardarSolicitud()
                            onVolver()
                        },
                        enabled = form.esFormularioValido,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = StatusGreenVibrant,
                            disabledContainerColor = TechPrimary.copy(alpha = 0.1f)
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("REGISTRAR PRÉSTAMO", fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    error: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
            color = TechPrimary,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray.copy(alpha = 0.5f)) },
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TechPrimary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            supportingText = {
                if (error != null) Text(error, color = StatusRedVibrant)
            }
        )
    }
}

@Composable
fun AlertNoDisponible() {
    Card(
        colors = CardDefaults.cardColors(containerColor = StatusRedVibrant.copy(alpha = 0.1f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = StatusRedVibrant)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Este equipo no se encuentra disponible actualmente.",
                color = StatusRedVibrant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
