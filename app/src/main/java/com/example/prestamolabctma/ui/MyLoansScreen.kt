package com.example.prestamolabctma.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prestamolabctma.model.SolicitudPrestamo
import com.example.prestamolabctma.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyLoansScreen(
    loans: List<SolicitudPrestamo>,
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
                title = { Text("Mis Préstamos") }
            )
        }
    ) { padding ->
        if (loans.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Aún no tienes préstamos registrados", color = TextGray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(loans) { loan ->
                    LoanItem(loan = loan)
                }
            }
        }
    }
}

@Composable
fun LoanItem(loan: SolicitudPrestamo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = TechSurface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Assignment, contentDescription = null, tint = TechSecondary, modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Ambiente: ${loan.ambienteDestino}", fontWeight = FontWeight.Bold)
                Text("Propósito: ${loan.proposito}", style = MaterialTheme.typography.bodySmall, color = TextGray)
            }
            Surface(
                color = TechSecondary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = loan.estado.name,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = TechSecondary,
                    fontSize = 10.sp
                )
            }
        }
    }
}
