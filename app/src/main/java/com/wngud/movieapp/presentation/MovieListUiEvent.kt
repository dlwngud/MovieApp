package com.wngud.movieapp.presentation

sealed interface MovieListUiEvent {
    data class Paginate(val category: String) : MovieListUiEvent
    object Navigate: MovieListUiEvent
}