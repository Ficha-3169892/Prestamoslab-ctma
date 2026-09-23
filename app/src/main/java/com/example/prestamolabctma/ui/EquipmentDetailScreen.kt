package com.example.prestamolabctma.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipmentDetailScreen(
    equipo: Equipo?,
    onSolicitar: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = TechBackground,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TechPrimary, titleContentColor = Color.White),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                title = { Text("Detalle del Equipo") }
            )
        }
    ) { padding ->
        if (equipo == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Equipo no encontrado", color = TextGray)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header con Icono
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Surface(
                        modifier = Modifier.size(120.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = TechPrimary.copy(alpha = 0.1f)
                    ) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = TechPrimary, modifier = Modifier.padding(24.dp))
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(equipo.nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = TextDark)
                    Text(equipo.categoria.name, style = MaterialTheme.typography.bodyLarge, color = TechSecondary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    BadgeEstado(estado = equipo.estado)
                }

                // Descripción
                Card(colors = CardDefaults.cardColors(containerColor = TechSurface), shape = MaterialTheme.shapes.medium) {
                    Column(Modifier.padding(16.dp).fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = TechPrimary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Descripción", fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(equipo.descripcion, style = MaterialTheme.typography.bodyMedium, color = TextDark)
                    }
                }

                // Especificaciones
                if (equipo.especificaciones.isNotEmpty()) {
                    Text("Especificaciones Técnicas", fontWeight = FontWeight.Bold)
                    equipo.especificaciones.forEach { spec ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("•", Modifier.padding(end = 8.dp), fontWeight = FontWeight.Bold, color = TechPrimary)
                            Text(spec, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onSolicitar,
                    enabled = equipo.estado == EstadoEquipo.DISPONIBLE,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TechPrimary)
                ) {
                    Text("SOLICITAR PRÉSTAMO", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}


@Composable
fun BadgeEstado(
    estado: EstadoEquipo,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor) = when (estado) {
        EstadoEquipo.DISPONIBLE -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        EstadoEquipo.PRESTADO -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        EstadoEquipo.RESERVADO -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        else -> Color(0xFFE0E0E0) to Color(0xFF616161) // Cubre NO_DISPONIBLE y cualquier otro estado futuro
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Text(
            text = estado.name,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}