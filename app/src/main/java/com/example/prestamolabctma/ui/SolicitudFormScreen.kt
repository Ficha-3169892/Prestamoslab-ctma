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
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.ui.theme.AccentGreen
import com.example.prestamolabctma.ui.theme.PrimaryBlue
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent),
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
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(equipo.nombre, style = MaterialTheme.typography.titleMedium)
                            BadgeEstadoModern(estado = equipo.estado)
                        }
                    }
                }

                if (equipo.estado != EstadoEquipo.DISPONIBLE) {
                    AlertNoDisponible()
                } else {
                    Text("Detalles del Préstamo", style = MaterialTheme.typography.titleMedium)

                    ModernTextField(
                        value = form.ambienteDestino,
                        onValueChange = { viewModel.onAmbienteChanged(it) },
                        label = "Ambiente / Destino",
                        error = form.errorAmbiente
                    )

                    ModernTextField(
                        value = form.proposito,
                        onValueChange = { viewModel.onPropositoChanged(it) },
                        label = "Propósito",
                        error = form.errorProposito
                    )

                    ModernTextField(
                        value = form.duracionHoras,
                        onValueChange = { viewModel.onDuracionChanged(it) },
                        label = "Duración (Horas)",
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
                            .height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGreen,
                            disabledContainerColor = AccentGreen.copy(alpha = 0.3f)
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirmar Préstamo", fontWeight = FontWeight.Bold)
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
    error: String?
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            isError = error != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = PrimaryBlue,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            supportingText = {
                if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
            }
        )
    }
}

@Composable
fun AlertNoDisponible() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Este equipo no se encuentra disponible actualmente.",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
