package pl.gda.pg.eti.kask.am.backend;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import pl.gda.pg.eti.kask.am.backend.ejb.TagsServiceBean;
import pl.gda.pg.eti.kask.am.backend.model.Product;
import pl.gda.pg.eti.kask.am.backend.model.Tag;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * Created by Kuba on 2016-01-24.
 */
@Path("/tags")
public class TagsResource {

    @EJB
    private TagsServiceBean tagsServiceBean;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateProductList(@HeaderParam("ownerGoogleId") String ownerGoogleId,
                                      @HeaderParam("ownerGoogleToken") String ownerGoogleToken,
                                      List<Tag> tags) {
        if (Strings.isNullOrEmpty(ownerGoogleId) || Strings.isNullOrEmpty(ownerGoogleToken)
                || tags == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!AccountVerifier.verified(ownerGoogleId, ownerGoogleToken)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        List<Tag> responseTags = tagsServiceBean.updateTags(tags, ownerGoogleId);
        return Response.ok(tagsToGenericList(responseTags)).build();
    }

    private GenericEntity<List<Tag>> tagsToGenericList(final List<Tag> resultTags) {
        return new GenericEntity<List<Tag>>(resultTags) {
        };
    }
}
