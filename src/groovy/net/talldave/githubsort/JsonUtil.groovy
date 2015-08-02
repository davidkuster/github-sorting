package net.talldave.githubsort

import grails.converters.JSON
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONElement

class JsonUtil {

    private final static log = Logger.getLogger(JsonUtil)


    static JSONElement getCachedJson(String filename) {
        String content = getCachedJsonAsString(filename)

        try {
            return content ? JSON.parse(content) : null
        } catch (Exception e) {
            log.error "Parsing JSON content from ${filename} failed: ${content}", e
            return null
        }
    }

    private static String getCachedJsonAsString(String filename) {
        // currently this is only being used for tests, so may make more sense for
        // the dir to be under /test as opposed to /src...
        // basically treating this like fixtures data.
        JsonUtil.classLoader.getResource("src/main/resources/${filename}.json")?.text
    }

}