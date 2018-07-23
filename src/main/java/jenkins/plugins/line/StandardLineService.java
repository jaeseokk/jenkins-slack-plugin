package jenkins.plugins.line;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;

import java.util.logging.Level;
import java.util.logging.Logger;

import jenkins.model.Jenkins;
import hudson.ProxyConfiguration;

public class StandardLineService implements LineService {

    private static final Logger logger = Logger.getLogger(StandardLineService.class.getName());

    private String api = "https://notify-api.line.me/api/notify";
    private String accessToken;

    public StandardLineService(String accessToken) {
        super();
        this.accessToken = accessToken;
    }

    public void send(String message) {
        logger.info("Posting: " + api + " : " + message);
        HttpClient client = getHttpClient();
        PostMethod post = new PostMethod(api);

        try {
            post.addRequestHeader("Authorization", "Bearer " + accessToken);
            post.addParameter("message", message);
            post.getParams().setContentCharset("UTF-8");
            int responseCode = client.executeMethod(post);
            String response = post.getResponseBodyAsString();
            if (responseCode != HttpStatus.SC_OK) {
                logger.log(Level.WARNING, "Line post may have failed. Response: " + response);
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error posting to Line", e);
        } finally {
            post.releaseConnection();
        }
    }

    private HttpClient getHttpClient() {
        HttpClient client = new HttpClient();
        if (Jenkins.getInstance() != null) {
            ProxyConfiguration proxy = Jenkins.getInstance().proxy;
            if (proxy != null) {
                client.getHostConfiguration().setProxy(proxy.name, proxy.port);
            }
        }
        return client;
    }

    void setApi(String api) {
        this.api = api;
    }
}
