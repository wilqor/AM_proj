package pl.gda.pg.eti.kask.am.backend;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import java.util.Arrays;

/**
 * Created by Kuba on 2015-11-10.
 */
public class AccountVerifier {

    private static final String CLIENT_ID = "967613229375-h8fdimc3cr81jpt6s02hcva1bbn8fb58.apps.googleusercontent.com";

    private final String accountId;
    private final String tokenId;
    private final GsonFactory jsonFactory;
    private final GoogleIdTokenVerifier verifier;

    public AccountVerifier(String accountId, String tokenId) {
        this.accountId = accountId;
        this.tokenId = tokenId;

        NetHttpTransport transport = new NetHttpTransport();
        jsonFactory = new GsonFactory();
        verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Arrays.asList(CLIENT_ID))
                .setIssuer("https://accounts.google.com")
                .build();
    }

    public boolean verify() {
        GoogleIdToken idToken;
        try {
            idToken = verifier.verify(tokenId);
            if (idToken == null) {
                System.out.println("Invalid ID token.");
                return false;
            }
            String subjectId = idToken.getPayload().getSubject();
            if (subjectId != null && subjectId.equals(accountId)) {
                System.out.println("User ID: " + subjectId);
                return true;
            } else {
                System.out.println("User ID does not match provided one");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
