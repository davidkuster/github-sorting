package net.talldave.githubsort

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import net.talldave.githubsort.dto.RateLimitDTO
import net.talldave.githubsort.dto.RepoDTO

class GitHubApiServiceTests {

    GitHubApiService service
    AuthService authService
    def mockRestClientService

    @Before
    void setUp() {
        service = new GitHubApiService()
        authService = new AuthService()
        service.authService = authService

        mockRestClientService = new Expando()
        mockRestClientService.getDefaultTimeout = { -> 1000 }
        mockRestClientService.getAsJson = { uri, timeout, headers ->
            if (uri =~ 'rate_limit') {
                if (headers?.keySet()?.contains('Authorization')) {
                    return JsonUtil.getCachedJson('RateLimit_basic_auth')
                }
                else {
                    return JsonUtil.getCachedJson('RateLimit_no_auth')
                }
            }
            else if (uri =~ 'orgs/netflix/repos') {
                return JsonUtil.getCachedJson('NetflixRepos_5')
            }
            else if (uri =~ 'repos/netflix') {
                // parse out repo name
                int i = uri.indexOf('repos/netflix/') + 'repos/netflix/'.length()
                String repoName = uri.substring( i, uri.indexOf('/', i) )
                return JsonUtil.getCachedJson("NetflixPulls_$repoName")
            }
        }
        service.restClientService = mockRestClientService
    }

    @Test
    void 'rate limit should increase if user is logged in'() {
        // user isn't logged in
        Assert.assertFalse(authService.isLoggedIn())

        // get rate limit for non-logged in user
        RateLimitDTO rateLimit1 = service.retrieveRemainingRateLimit()
        Assert.assertEquals(60, rateLimit1.limit)
        Assert.assertEquals(40, rateLimit1.remaining)

        // log user in
        authService.login('username', 'password')
        Assert.assertTrue(authService.isLoggedIn())

        // get rate limit for logged in user
        RateLimitDTO rateLimit2 = service.retrieveRemainingRateLimit()
        Assert.assertEquals(5000, rateLimit2.limit)
        Assert.assertEquals(4000, rateLimit2.remaining)
    }

    @Test
    void 'should sort repos by pull requests'() {
        List<RepoDTO> repoDTOs = service.retrieveReposOrderedByPullRequestsWithSecondSort('netflix')

        Assert.assertEquals(5, repoDTOs.size())

        Assert.assertEquals('astyanax', repoDTOs[0].repoName)
        Assert.assertEquals('Priam', repoDTOs[1].repoName)
        Assert.assertEquals('servo', repoDTOs[2].repoName)
        Assert.assertEquals('curator', repoDTOs[3].repoName)
        Assert.assertEquals('CassJMeter', repoDTOs[4].repoName)

        Assert.assertTrue(
            repoDTOs[0].pullRequests > repoDTOs[1].pullRequests
            && repoDTOs[1].pullRequests > repoDTOs[2].pullRequests
            && repoDTOs[2].pullRequests > repoDTOs[3].pullRequests
            && repoDTOs[3].pullRequests > repoDTOs[4].pullRequests)
    }

    @Test
    void 'should do secondary sort alphabetically'() {
        // mock method on service object so number of pull requests is always the same and secondary sort will take precedence
        service.metaClass.retrieveNumberOfPullRequests = { String orgName, String repoName ->
            return 100
        }

        List<RepoDTO> repoDTOs = service.retrieveReposOrderedByPullRequestsWithSecondSort('netflix')

        Assert.assertEquals(5, repoDTOs.size())

        Assert.assertEquals('astyanax', repoDTOs[0].repoName)
        Assert.assertEquals('CassJMeter', repoDTOs[1].repoName)
        Assert.assertEquals('curator', repoDTOs[2].repoName)
        Assert.assertEquals('Priam', repoDTOs[3].repoName)
        Assert.assertEquals('servo', repoDTOs[4].repoName)

        Assert.assertTrue(
            repoDTOs[0].sortValue.toLowerCase() < repoDTOs[1].sortValue.toLowerCase()
            && repoDTOs[1].sortValue.toLowerCase() < repoDTOs[2].sortValue.toLowerCase()
            && repoDTOs[2].sortValue.toLowerCase() < repoDTOs[3].sortValue.toLowerCase()
            && repoDTOs[3].sortValue.toLowerCase() < repoDTOs[4].sortValue.toLowerCase())

        repoDTOs.each {
            Assert.assertEquals(100, it.pullRequests)
            Assert.assertEquals(SortOption.ALPHABETICAL, it.sortOption)
        }
    }

    @Test
    void 'should do secondary sorts on integer values'() {
        // mock method on service object so number of pull requests is always the same and secondary sort will take precedence
        service.metaClass.retrieveNumberOfPullRequests = { String orgName, String repoName ->
            return 100
        }

        List<SortOption> sortOptions = [
            SortOption.NUM_FORKS,
            SortOption.NUM_STARS,
            SortOption.NUM_WATCHERS,
            SortOption.NUM_OPEN_ISSUES
        ]

        sortOptions.each { sortBy ->
            // ************* HACK *****************
            // service is somehow losing these references as soon as it steps into this loop... which I can't figure out???
            service.restClientService = mockRestClientService
            service.authService = authService
            service.metaClass.retrieveNumberOfPullRequests = { String orgName, String repoName ->
                return 100
            }
            // but this test is totally fine in the REPL and doesn't have this problem there...
            // whadafa?
            // *********** END HACK ***************

            List<RepoDTO> repoDTOs = service.retrieveReposOrderedByPullRequestsWithSecondSort('netflix', sortBy)

            println "sortBy = $sortBy, dtos = $repoDTOs\n"

            Assert.assertEquals(5, repoDTOs.size())

            Assert.assertTrue(
                repoDTOs[0].sortValue.toInteger() > repoDTOs[1].sortValue.toInteger()
                && repoDTOs[1].sortValue.toInteger() > repoDTOs[2].sortValue.toInteger()
                && repoDTOs[2].sortValue.toInteger() > repoDTOs[3].sortValue.toInteger()
                && repoDTOs[3].sortValue.toInteger() > repoDTOs[4].sortValue.toInteger())

            repoDTOs.each {
                Assert.assertEquals(100, it.pullRequests)
                Assert.assertEquals(sortBy, it.sortOption)
            }
        }
    }

    @Test
    void 'should do secondary sorts on date values'() {
        // mock method on service object so number of pull requests is always the same and secondary sort will take precedence
        service.metaClass.retrieveNumberOfPullRequests = { String orgName, String repoName ->
            return 100
        }

        List<SortOption> sortOptions = [
            SortOption.CREATE_DATE,
            SortOption.UPDATE_DATE,
            SortOption.PUSH_DATE
        ]

        sortOptions.each { sortBy ->
            // ************* HACK *****************
            // having to include the same hack as in the last test
            service.restClientService = mockRestClientService
            service.authService = authService
            service.metaClass.retrieveNumberOfPullRequests = { String orgName, String repoName ->
                return 100
            }
            // *********** END HACK ***************

            List<RepoDTO> repoDTOs = service.retrieveReposOrderedByPullRequestsWithSecondSort('netflix', sortBy)

            Assert.assertEquals(5, repoDTOs.size())

            Assert.assertTrue(
                service.dateFormatter.parse(repoDTOs[0].sortValue) > service.dateFormatter.parse(repoDTOs[1].sortValue)
                && service.dateFormatter.parse(repoDTOs[1].sortValue) > service.dateFormatter.parse(repoDTOs[2].sortValue)
                && service.dateFormatter.parse(repoDTOs[2].sortValue) > service.dateFormatter.parse(repoDTOs[3].sortValue)
                && service.dateFormatter.parse(repoDTOs[3].sortValue) > service.dateFormatter.parse(repoDTOs[4].sortValue))

            repoDTOs.each {
                Assert.assertEquals(100, it.pullRequests)
                Assert.assertEquals(sortBy, it.sortOption)
            }
        }
    }

    @Test(expected=IllegalArgumentException)
    void 'should only allow legitimate number of results params (low end)'() {
        List<RepoDTO> repoDTOs = service.retrieveReposOrderedByPullRequestsWithSecondSort('netflix', SortOption.ALPHABETICAL, 0)
    }

    @Test(expected=IllegalArgumentException)
    void 'should only allow legitimate number of results params (high end)'() {
        List<RepoDTO> repoDTOs = service.retrieveReposOrderedByPullRequestsWithSecondSort('netflix', SortOption.ALPHABETICAL, 101)
    }

    @Test(expected=IllegalArgumentException)
    void 'should ensure org name is input'() {
        List<RepoDTO> repoDTOs = service.retrieveReposOrderedByPullRequestsWithSecondSort(null)
    }

}