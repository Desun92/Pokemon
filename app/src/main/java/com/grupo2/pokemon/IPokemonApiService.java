package com.grupo2.pokemon;

import retrofit2.Call;
import retrofit2.http.GET;

public interface IPokemonApiService {

    @GET("pokemon/?limit=800")
    Call<PokemonFetchResults> getPokemons();

    @GET("pokemon/{id}")
    Call<PokemonFetchResults> getDetails();
}
