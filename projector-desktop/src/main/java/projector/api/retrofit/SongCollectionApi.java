package projector.api.retrofit;

import com.bence.projector.common.dto.SongCollectionDTO;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.util.List;

public interface SongCollectionApi {
    @GET("/api/songCollections/language/{language}/lastModifiedDate/{lastModifiedDate}")
    Call<List<SongCollectionDTO>> getSongCollections(@Path("language") String language, @Path("lastModifiedDate") Long lastModifiedDate);
}
