package net.talldave.githubsort

import org.junit.Assert
import org.junit.Test
import org.apache.http.client.HttpResponseException
import org.codehaus.groovy.grails.web.json.JSONElement

class RestClientServiceIntegrationTests {

    RestClientService restClientService


    // Realized that this is a stupid test and I should remove it. No need to
    // determine that the same repos I have in my pseudo-fixture file are what's
    // still on GitHub. Instead should create a test that pings GitHub and verifies
    // the JSON properties I'm expecting (in SortOption enum) still exist.
    // That at least will provide some indication if the GitHub API has changed.

    /*@Test
    void "test cached json data contains same repos as actual API call"() {
        // get cached data
        JSONElement cached = JsonUtil.getCachedJson('NetflixRepos_all')

        // make actual API call to GitHub
        JSONElement notCached = restClientService.getAsJson('https://api.github.com/orgs/netflix/repos?per_page=100',
                                    restClientService.DEFAULT_TIMEOUT_MILLIS,
                                    ['Accept':'application/vnd.github.com/mondragon-preview+json'])

        // only look at the repo names for simplicity (at the moment)
        Object[] cachedIds = cached*.name?.sort()?.toArray()
        Object[] notCachedIds = notCached*.name?.sort()?.toArray()

        // ensure the results are the same
        Assert.assertArrayEquals(cachedIds, notCachedIds)
    }*/


    @Test
    void "test expected json elements in GitHub API have not changed"() {
        // make actual API call to GitHub
        try {
            JSONElement json = restClientService.getAsJson(
                "https://api.github.com/orgs/netflix/repos?per_page=1",
                restClientService.DEFAULT_TIMEOUT_MILLIS,
                ['Accept':'application/vnd.github.com/mondragon-preview+json'])

            SortOption.values().each { sortOption ->
                println "testing that $sortOption code ${sortOption.code} still exists"
                Assert.assertNotNull(json[0]."${sortOption.code}")
            }
        }
        catch (HttpResponseException e) {
            if (e.statusCode == 403) {
                println "Over the rate limit, not going to break the proverbial build for that reason: ${e.message}"
            }
            else {
                throw e
            }
        }
    }

}