package com.example.prestamolabctma.data

sealed class PrestamoError(message: String) : Exception(message) {
    class ValidationError(message: String) : PrestamoError(message)
    class NotFoundError(message: String) : PrestamoError(message)
    class BusinessError(message: String) : PrestamoError(message)
    class ServerError(message: String = "Error en el servidor, inténtelo más tarde") : PrestamoError(message)
}
