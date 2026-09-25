package com.example.prestamolabctma.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.prestamolabctma.data.repository.EquipmentRepository
import com.example.prestamolabctma.model.CategoriaEquipo
import com.example.prestamolabctma.model.Equipo
import com.example.prestamolabctma.model.EstadoEquipo
import com.example.prestamolabctma.model.EstadoSolicitud
import com.example.prestamolabctma.model.SolicitudPrestamo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Estado unificado para la UI
data class CatalogUiStateData(
    val equipos: List<Equipo> = emptyList(),
    val prestamos: List<SolicitudPrestamo> = emptyList(),
    val isAdmin: Boolean = false,
    val currentUserId: Int = 1,
    val userName: String = "Usuario PréstamoLab"
)

class CatalogViewModel(
    private val repository: EquipmentRepository
) : ViewModel() {

    private val _userRole = MutableStateFlow("PRESTATARIO")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userName = MutableStateFlow("Usuario PréstamoLab")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _currentUserId = MutableStateFlow(1)

    val uiState: StateFlow<CatalogUiStateData> = combine(
        repository.getEquipments(),
        repository.getAllLoansAdmin(),
        _userRole,
        _userName
    ) { equipments, loans, role, name ->
        CatalogUiStateData(
            equipos = equipments,
            prestamos = loans,
            isAdmin = role.equals("ADMIN", ignoreCase = true) || role.equals("DUEÑO", ignoreCase = true),
            currentUserId = _currentUserId.value,
            userName = name
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        CatalogUiStateData()
    )

    fun logout() {
        // Lógica de cierre de sesión
    }

    fun actualizarPerfil(newName: String) {
        _userName.value = newName
    }

    // --- ACCIONES DE PRÉSTAMO ---

    fun solicitarPrestamo(equipoId: Int, ambiente: String, proposito: String, duracion: Int) {
        viewModelScope.launch {
            val solicitud = SolicitudPrestamo(
                id = 0,
                equipoId = equipoId,
                ambienteDestino = ambiente,
                proposito = proposito,
                duracionHoras = duracion,
                estado = EstadoSolicitud.SOLICITADA,
                borrowerName = _userName.value,
                usuarioId = _currentUserId.value
            )
            repository.createLoanRequest(solicitud, null)
            refreshData()
        }
    }

    fun aprobarPrestamo(loanId: Int) {
        changeLoanStatus(loanId, EstadoSolicitud.APROBADA.name)
    }

    fun rechazarPrestamo(loanId: Int) {
        changeLoanStatus(loanId, EstadoSolicitud.RECHAZADA.name)
    }

    fun finalizarPrestamo(loanId: Int) {
        changeLoanStatus(loanId, "DEVUELTA")
    }

    private fun changeLoanStatus(loanId: Int, status: String) {
        viewModelScope.launch {
            repository.updateLoanStatus(loanId, status)
            refreshData()
        }
    }

    // --- ACCIONES DE EQUIPO (ADMIN) ---

    fun agregarEquipo(nombre: String, descripcion: String, categoriaStr: String, disponible: Boolean) {
        viewModelScope.launch {
            val cat = parseCategoria(categoriaStr)
            val nuevo = Equipo(
                id = 0,
                nombre = nombre,
                categoria = cat,
                estado = if (disponible) EstadoEquipo.DISPONIBLE else EstadoEquipo.MANTENIMIENTO,
                descripcion = descripcion
            )
            repository.addEquipment(nuevo)
            refreshData()
        }
    }

    fun actualizarEquipo(id: Int, nombre: String, descripcion: String, categoriaStr: String, disponible: Boolean) {
        viewModelScope.launch {
            val cat = parseCategoria(categoriaStr)
            val modificado = Equipo(
                id = id,
                nombre = nombre,
                categoria = cat,
                estado = if (disponible) EstadoEquipo.DISPONIBLE else EstadoEquipo.MANTENIMIENTO,
                descripcion = descripcion
            )
            repository.updateEquipment(modificado)
            refreshData()
        }
    }

    fun eliminarEquipo(id: Int) {
        viewModelScope.launch {
            repository.deleteEquipment(id)
            refreshData()
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.refreshData()
        }
    }

    // --- UTILIDADES ---

    private fun parseCategoria(categoriaStr: String): CategoriaEquipo {
        return try {
            CategoriaEquipo.valueOf(categoriaStr.uppercase())
        } catch (e: Exception) {
            CategoriaEquipo.OTROS
        }
    }

    fun getRemainingTime(loan: SolicitudPrestamo): String {
        if (loan.estado != EstadoSolicitud.APROBADA && loan.estado.name != "ENTREGADA") return "N/A"
        val createdAtStr = loan.createdAt ?: return "Calculando..."

        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")

            val dateCreated = sdf.parse(createdAtStr) ?: return "Error formato"
            val calendar = Calendar.getInstance()
            calendar.time = dateCreated
            calendar.add(Calendar.HOUR, loan.duracionHoras)

            val endTime = calendar.timeInMillis
            val currentTime = System.currentTimeMillis()
            val diff = endTime - currentTime

            if (diff <= 0) "Expirado"
            else {
                val hours = diff / (1000 * 60 * 60)
                val minutes = (diff / (1000 * 60)) % 60
                String.format(Locale.getDefault(), "%02dh %02dm", hours, minutes)
            }
        } catch (e: Exception) {
            "..."
        }
    }
}