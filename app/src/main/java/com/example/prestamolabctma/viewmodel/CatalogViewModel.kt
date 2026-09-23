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

class CatalogViewModel(
    private val repository: EquipmentRepository
) : ViewModel() {

    private val _queryBusqueda = MutableStateFlow("")
    val queryBusqueda: StateFlow<String> = _queryBusqueda.asStateFlow()

    private val _categoriaSeleccionada = MutableStateFlow<CategoriaEquipo?>(null)
    val categoriaSeleccionada: StateFlow<CategoriaEquipo?> = _categoriaSeleccionada.asStateFlow()

    val uiState: StateFlow<CatalogUiState> = combine(
        repository.getEquipments(),
        _queryBusqueda,
        _categoriaSeleccionada
    ) { equipments, query, category ->
        val filtered = equipments.filter { equipo ->
            (query.isBlank() || equipo.nombre.contains(query, ignoreCase = true)) &&
            (category == null || equipo.categoria == category)
        }
        if (equipments.isEmpty()) CatalogUiState.Empty 
        else CatalogUiState.Success(filtered)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CatalogUiState.Loading)

    private val _equipmentDetail = MutableStateFlow<Equipo?>(null)
    val equipmentDetail: StateFlow<Equipo?> = _equipmentDetail.asStateFlow()

    private val _myLoans = repository.getMyLoans()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val myLoans: StateFlow<List<SolicitudPrestamo>> = _myLoans

    // Flujo para el Dueño
    val allLoansAdmin: StateFlow<List<SolicitudPrestamo>> = repository.getAllLoansAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRequesting = MutableStateFlow(false)
    val isRequesting: StateFlow<Boolean> = _isRequesting.asStateFlow()

    // Perfil del Usuario
    private val _userRole = MutableStateFlow("PRESTATARIO")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userName = MutableStateFlow("Usuario PréstamoLab")
    val userName: StateFlow<String> = _userName.asStateFlow()

    fun setRole(role: String) {
        _userRole.value = role.uppercase().trim()
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            repository.refreshData()
        }
    }

    fun updateProfile(name: String, photoUrl: String?) {
        _userName.value = name
    }

    fun onQueryChanged(newQuery: String) {
        _queryBusqueda.value = newQuery
    }

    fun onCategorySelected(category: CategoriaEquipo?) {
        _categoriaSeleccionada.value = category
    }

    fun selectEquipment(id: Int) {
        viewModelScope.launch {
            val equipo = repository.getEquipmentById(id)
            _equipmentDetail.value = equipo
        }
    }

    fun createLoan(ambiente: String, proposito: String, duracion: Int, photoUrl: String? = null) {
        val currentEquipment = _equipmentDetail.value ?: return
        if (_isRequesting.value) return
        _isRequesting.value = true

        viewModelScope.launch {
            val solicitud = SolicitudPrestamo(
                id = 0,
                equipoId = currentEquipment.id,
                ambienteDestino = ambiente,
                proposito = proposito,
                duracionHoras = duracion,
                estado = EstadoSolicitud.SOLICITADA,
                fotoValidacionUrl = photoUrl,
                borrowerName = _userName.value
            )
            
            repository.createLoanRequest(solicitud, photoUrl)
                .onSuccess { 
                    _isRequesting.value = false 
                    refreshData()
                }
                .onFailure { _isRequesting.value = false }
        }
    }

    // Funciones del DUEÑO
    fun addNewEquipment(nombre: String, categoria: CategoriaEquipo, descripcion: String) {
        viewModelScope.launch {
            val nuevo = Equipo(
                id = 0,
                nombre = nombre,
                categoria = categoria,
                estado = EstadoEquipo.DISPONIBLE,
                descripcion = descripcion
            )
            repository.addEquipment(nuevo)
        }
    }

    fun updateEquipmentAdmin(
        id: Int,
        nombre: String,
        categoria: CategoriaEquipo,
        descripcion: String,
        estado: EstadoEquipo = EstadoEquipo.DISPONIBLE
    ) {
        viewModelScope.launch {
            val modificado = Equipo(
                id = id,
                nombre = nombre,
                categoria = categoria,
                estado = estado,
                descripcion = descripcion
            )
            repository.updateEquipment(modificado)
        }
    }

    fun removeEquipment(id: Int) {
        viewModelScope.launch {
            repository.deleteEquipment(id)
        }
    }

    fun changeLoanStatus(loanId: Int, status: String) {
        viewModelScope.launch {
            repository.updateLoanStatus(loanId, status)
        }
    }

    fun finalizeLoan(loanId: Int) {
        viewModelScope.launch {
            repository.updateLoanStatus(loanId, "DEVUELTA")
        }
    }

    // Utilidad para calcular tiempo restante compatible con API 24
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