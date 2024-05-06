package com.wngud.movieapp.presentation.movielist

sealed interface MovieListUiEvent {
    data class Paginate(val category: String) : MovieListUiEvent
    object Navigate: MovieListUiEvent
}