package com.example.prestamolabctma.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.viewmodel.PrestamoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolicitudFormScreen(
    viewModel: PrestamoViewModel,
    uiState: com.example.prestamolabctma.viewmodel.PrestamoUiState,
    onVolver: () -> Unit
) {
    val equipo = uiState.equipoSeleccionado
    val form = uiState.formulario

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Solicitar Préstamo") },
                navigationIcon = {
                    TextButton(onClick = onVolver) { Text("Atrás") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (equipo != null) {
                Text("Equipo: ${equipo.nombre}", style = MaterialTheme.typography.titleLarge)
                BadgeEstado(estado = equipo.estado)

                if (equipo.estado != EstadoEquipo.DISPONIBLE) {
                    Text(
                        "Este equipo no está disponible para préstamo en este momento.",
                        color = MaterialTheme.colorScheme.error
                    )
                } else {
                    OutlinedTextField(
                        value = form.ambienteDestino,
                        onValueChange = { viewModel.onAmbienteChanged(it) },
                        label = { Text("Ambiente / Salón de destino") },
                        isError = form.errorAmbiente != null,
                        supportingText = { form.errorAmbiente?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.proposito,
                        onValueChange = { viewModel.onPropositoChanged(it) },
                        label = { Text("Propósito (10 - 180 caracteres)") },
                        isError = form.errorProposito != null,
                        supportingText = { form.errorProposito?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = form.duracionHoras,
                        onValueChange = { viewModel.onDuracionChanged(it) },
                        label = { Text("Duración en horas (1 a 8)") },
                        isError = form.errorDuracion != null,
                        supportingText = { form.errorDuracion?.let { Text(it) } },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            viewModel.guardarSolicitud()
                            onVolver()
                        },
                        enabled = form.esFormularioValido,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Confirmar Solicitud")
                    }
                }
            } else {
                Text("No se seleccionó ningún equipo.")
            }
        }
    }
}