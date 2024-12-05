package org.espify.utils;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class YTSearchAPI {
    private static final String APPLICATION_NAME = "API code samples";
    @SuppressWarnings("deprecation")
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String API_KEY = "AIzaSyDw04yFqxbum0fdHirJ8-KpVafK_R0Zld4";

    public static void main(String[] args) {
        try {
            System.out.println(Search("bebecita"));
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public static String Search(String keywords) throws GeneralSecurityException, IOException {
        // Crear el cliente de la API
        final YouTube youtubeService = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                null
        ).setApplicationName(APPLICATION_NAME).build();

        // Definir y ejecutar la solicitud de búsqueda
        YouTube.Search.List request = youtubeService.search()
                .list("snippet")
                .setQ(keywords) // Palabras clave de búsqueda
                .setType("video") // Solo buscar videos
                .setMaxResults(1L) // Limitar resultados
                .setVideoCategoryId("10") // Categoria de videos (Música)
                .setSafeSearch("moderate") // Filtro de busqueda
                .setKey(API_KEY); // Agregar la clave de API
                

        SearchListResponse response = request.execute();

        // Mostrar los IDs de los videos encontrados
        return response.getItems().get(0).getId().getVideoId();
    }
}

