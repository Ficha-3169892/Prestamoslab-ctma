package com.example.prestamolabctma.model

enum class CategoriaEquipo {
    ELECTRONICA,
    HERRAMIENTAS,
    COMPUTO,
    MEDICION,
    PERIFERICOS
}

enum class EstadoEquipo {
    DISPONIBLE,
    RESERVADO,
    PRESTADO,
    NO_DISPONIBLE
}

enum class EstadoSolicitud {
    SOLICITADA,
    APROBADA,
    ENTREGADA,
    DEVUELTA,
    CANCELADA,
    RECHAZADA
}