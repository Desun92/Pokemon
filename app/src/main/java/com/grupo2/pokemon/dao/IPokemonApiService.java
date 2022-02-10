package com.grupo2.pokemon.dao;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IPokemonApiService {

    @GET("pokemon/?limit=50")
    Call getPokemons();

}
