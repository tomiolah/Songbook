package projector.api;

import com.bence.projector.common.dto.BibleDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import projector.api.assembler.BibleAssembler;
import projector.api.retrofit.ApiManager;
import projector.api.retrofit.BibleApi;
import projector.model.Bible;
import retrofit2.Call;

import java.net.UnknownHostException;
import java.util.List;

public class BibleApiBean {
    private static final Logger LOG = LoggerFactory.getLogger(BibleApiBean.class);
    private BibleApi bibleApi;
    private BibleAssembler bibleAssembler;

    public BibleApiBean() {
        bibleApi = ApiManager.getClient().create(BibleApi.class);
        bibleAssembler = BibleAssembler.getInstance();
    }

    public List<Bible> getBibles() {
        Call<List<BibleDTO>> call = bibleApi.getBibles();
        return executeBiblesCall(call);
    }

    public List<Bible> getBibleTitles() {
        Call<List<BibleDTO>> call = bibleApi.getBibleTitles();
        return executeBiblesCall(call);
    }

    public Bible getBible(String uuid) {
        Call<BibleDTO> call = bibleApi.getBible(uuid);
        return doCallBible(call);
    }

    private List<Bible> executeBiblesCall(Call<List<BibleDTO>> call) {
        try {
            List<BibleDTO> bibleDTOs = call.execute().body();
            return bibleAssembler.createModelList(bibleDTOs);
        } catch (UnknownHostException e) {
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    private Bible doCallBible(Call<BibleDTO> call) {
        try {
            BibleDTO bibleDTO = call.execute().body();
            return bibleAssembler.createModel(bibleDTO);
        } catch (UnknownHostException e) {
            return null;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public Bible uploadBible(Bible bible) {
        final BibleDTO dto = bibleAssembler.createDto(bible);
        Call<BibleDTO> call = bibleApi.postBible(dto);
        return doCallBible(call);
    }
}
