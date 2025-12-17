package com.spa.appointments.ui.home

import androidx.lifecycle.ViewModel
import com.spa.appointments.core.security.TokenStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenStorage: TokenStorage
) : ViewModel() {

    val userName: String =
        tokenStorage.getUser() ?: "Usuario"
}
