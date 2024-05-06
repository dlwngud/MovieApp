package com.wngud.movieapp.presentation.detail

import com.wngud.movieapp.domain.model.Movie

data class DetailState(
    val isLoading: Boolean = false,
    val movie: Movie? = null,
)
