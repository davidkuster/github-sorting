/*
 * Somewhat lifted (with love) from com.netflix.asgard.RestClientService
 */
package net.talldave.githubsort

import grails.converters.JSON
import java.util.concurrent.TimeUnit
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.HttpResponseException
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.params.ClientPNames
import org.apache.http.entity.ContentType
import org.apache.http.impl.client.AutoRetryHttpClient
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.DefaultServiceUnavailableRetryStrategy
import org.apache.http.impl.conn.PoolingClientConnectionManager
import org.apache.http.params.HttpConnectionParams
import org.apache.http.util.EntityUtils
import org.codehaus.groovy.grails.web.json.JSONElement
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.springframework.beans.factory.InitializingBean

class RestClientService implements InitializingBean {

    static transactional = false

    def grailsApplication

    final static ContentType APPLICATION_JSON_UTF8 = ContentType.APPLICATION_JSON
    final static Integer DEFAULT_TIMEOUT_MILLIS = 10000

    final PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager()

    HttpClient httpClient

    public void afterPropertiesSet() throws Exception {
        HttpClient baseClient = new DefaultHttpClient(connectionManager)
        httpClient = new AutoRetryHttpClient(baseClient, new DefaultServiceUnavailableRetryStrategy())
        httpClient.params.setLongParameter(ClientPNames.CONN_MANAGER_TIMEOUT, 50 * 1000)
        // removed code to work around a proxy, avoiding long caching of DNS results, etc.  don't feel it's necessary for an MVP
        log.debug "httpClient initialized"
    }


    Integer getDefaultTimeout() {
        DEFAULT_TIMEOUT_MILLIS
    }


    /**
     * Gets text over HTTP with a JSON content type, and uses grails.converters.JSON to parse the text.
     *
     * @param uri the URI to connect to
     * @param timeoutMillis the value to use as socket and connection timeout when making the request
     * @return the JSON parsed object
     */
    JSONElement getAsJson(String uri, Integer timeoutMillis = DEFAULT_TIMEOUT_MILLIS,
                          Map<String,String> extraHeaders = [:]) {
        String content = get(uri, APPLICATION_JSON_UTF8, timeoutMillis, extraHeaders)

        try {
            return content ? JSON.parse(content) : null
        // surprised codenarc didn't flag this in asgard
        //} catch (Exception e) {
        } catch (ConverterException e) {
            log.error "Parsing JSON content from ${uri} failed: ${content}", e
            return null
        }
    }


    /**
     * Gets JSON text over HTTP using a JSON content type, but leaves the payload in text form for alternate parsing.
     *
     * @param uri the URI to connect to
     * @param timeoutMillis the value to use as socket and connection timeout when making the request
     * @return the raw JSON string
     */
    String getJsonAsText(String uri, Integer timeoutMillis = DEFAULT_TIMEOUT_MILLIS,
                         Map<String,String> extraHeaders = [:]) {
        get(uri, APPLICATION_JSON_UTF8, timeoutMillis, extraHeaders)
    }


    private String get(String uri, ContentType contentType = ContentType.DEFAULT_TEXT,
                       Integer timeoutMillis = DEFAULT_TIMEOUT_MILLIS,
                       Map<String,String> extraHeaders = [:]) {
        try {
            HttpGet httpGet = getWithTimeout(uri, timeoutMillis)
            String contentTypeString = contentType.toString()
            httpGet.setHeader('Content-Type', contentTypeString)
            httpGet.setHeader('Accept', contentTypeString)
            extraHeaders.each { header, value -> httpGet.setHeader(header,value) }
            executeAndProcessResponse(httpGet) { HttpResponse httpResponse ->
                int statusCode = httpResponse.statusLine.statusCode
                if (statusCode == HttpURLConnection.HTTP_OK) {
                    HttpEntity httpEntity = httpResponse.getEntity()
                    return EntityUtils.toString(httpEntity)
                }
                else {
                    String msg = "Non-OK response of [$statusCode] for uri [$uri]"
                    log.warn msg
                    // there's got to be a better way to do this...
                    if (statusCode < 200 || statusCode >= 300) {
                        throw new HttpResponseException(statusCode, msg)
                    }
                    return null
                }
            }
        } catch (HttpResponseException e) {
            throw e
        } catch (Exception e) {
            log.error "GET from ${uri} failed: ${e}"
            return null
        }
    }


    /**
     * Convenience method to create a http-client HttpGet with the timeout parameters set
     *
     * @param uri the URI to connect to
     * @param timeoutMillis the value to use as socket and connection timeout when making the request
     * @return HttpGet object with parameters set
     */
    private HttpGet getWithTimeout(String uri, int timeoutMillis) {
        HttpGet httpGet = new HttpGet(uri)
        HttpConnectionParams.setConnectionTimeout(httpGet.params, timeoutMillis)
        HttpConnectionParams.setSoTimeout(httpGet.params, timeoutMillis)
        httpGet
    }


    /**
     * Template method to execute a HttpUriRequest object (HttpGet, HttpPost, etc.), process the response with a
     * closure, and perform the cleanup necessary to return the connection to the pool.
     *
     * @param request an http action to execute with httpClient
     * @param responseHandler handles the response from the request and provides the return value for this method
     * @return the return value of executing responseHandler
     */
    private Object executeAndProcessResponse(HttpUriRequest request, Closure responseHandler,
                                             Map<String,String> extraHeaders = [:]) {
        extraHeaders.each { header,value -> request.setHeader(header,value) }
        try {
            HttpResponse httpResponse = httpClient.execute(request)
            Object retVal = responseHandler(httpResponse)
            // Ensure the connection gets released to the manager.
            EntityUtils.consume(httpResponse.entity)
            return retVal
        } catch (Exception e) {
            request.abort()
            throw e
        } finally {
            // Save memory per http://stackoverflow.com/questions/4999708/httpclient-memory-management
            connectionManager.closeIdleConnections(60, TimeUnit.SECONDS)
        }
    }

}
