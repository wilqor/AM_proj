package pl.gda.pg.eti.kask.am.backend;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import pl.gda.pg.eti.kask.am.backend.ejb.ProductTagAssociationBean;
import pl.gda.pg.eti.kask.am.backend.model.ProductTagRelation;
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
@Path("/relations")
public class ProductsTagResource {

    @EJB
    private ProductTagAssociationBean productTagAssociationBean;

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateRelationsList(@HeaderParam("ownerGoogleId") String ownerGoogleId,
                                        @HeaderParam("ownerGoogleToken") String ownerGoogleToken,
                                        List<ProductTagRelation> relations) {
        if (Strings.isNullOrEmpty(ownerGoogleId) || Strings.isNullOrEmpty(ownerGoogleToken)
                || relations == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        if (!AccountVerifier.verified(ownerGoogleId, ownerGoogleToken)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        List<ProductTagRelation> responseRelations = productTagAssociationBean.updateRelations(relations, ownerGoogleId);
        return Response.ok(relationsToGenericList(responseRelations)).build();
    }

    private GenericEntity<List<ProductTagRelation>> relationsToGenericList(final List<ProductTagRelation> result) {
        return new GenericEntity<List<ProductTagRelation>>(result) {
        };
    }
}
