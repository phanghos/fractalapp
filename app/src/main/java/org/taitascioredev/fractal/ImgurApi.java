package org.taitascioredev.fractal;

/*
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
*/

/**
 * Created by roberto on 29/05/15.
 */
public interface ImgurApi {

    public static final String server = "https://api.imgur.com";
    public static final String CLIENT_ID = "84051e58aa0400d";
    public static String AUTHORIZATION_HEADER = "Client-ID " + CLIENT_ID;

    /*
    @POST("/3/image")ImageResponse postImage (
            @Header("Authorization")String auth,
            @Query("title")String title,
            @Query("description")String description,
            @Query("album")String albumId,
            @Query("account_url")String username,
            @Body TypedFile file
    );
    */

    /*
    @GET("/3/gallery/image/{id}")
    Call<ImageResponse> getGallery(
            @Header("Authorization")String auth,
            @Path("id") String id
    );
    */
}
