package net.talldave.githubsort

import org.junit.Assert
import org.junit.Before
import org.junit.Test
import net.talldave.githubsort.dto.RateLimitDTO
import net.talldave.githubsort.dto.RepoDTO

class GitHubSortControllerTests {

    GitHubSortController controller

    @Before
    void setUp() {
        controller = new GitHubSortController()
        controller.authService = new AuthService()

        def mockGitHubApiService = new Expando()
        mockGitHubApiService.retrieveRemainingRateLimit = { ->
            new RateLimitDTO()
        }
        mockGitHubApiService.retrieveReposOrderedByPullRequestsWithSecondSort = { String orgName, SortOption sortOption, Integer numRepos ->
            [new RepoDTO()]
        }
        controller.gitHubApiService = mockGitHubApiService

        // override render method to just return the model
        controller.metaClass.render = { Map map ->
            return map.model
        }
    }

    @Test
    void 'should get rate limit for index action'() {
        Map model = controller.index(new GitHubSortCommand())
        Assert.assertNotNull(model.cmd.rateLimit)
        Assert.assertNull(model.loggedInUser)
    }

    @Test
    void 'should get rate limit and user for index action when user is logged in'() {
        controller.authService.login('user', 'pass')
        Map model = controller.index(new GitHubSortCommand())
        Assert.assertNotNull(model.cmd.rateLimit)
        Assert.assertEquals('user', model.loggedInUser)
    }

    @Test
    void 'should get rate limit for search action'() {
        Map model = controller.search(new GitHubSortCommand())
        Assert.assertNotNull(model.cmd.rateLimit)
        Assert.assertNull(model.loggedInUser)
    }

    @Test
    void 'should get rate limit and user for search action when user is logged in'() {
        controller.authService.login('user', 'pass')
        Map model = controller.search(new GitHubSortCommand())
        Assert.assertNotNull(model.cmd.rateLimit)
        Assert.assertEquals('user', model.loggedInUser)
    }

    @Test
    void 'should retrieve repos in search action'() {
        Map model = controller.search(new GitHubSortCommand())
        Assert.assertNotNull(model.cmd.results)
    }

    @Test
    void 'should login user in login action on POST'() {
        // need to set request.method to post for this...
        controller.metaClass.getRequest = { ->
            [method: 'POST']
        }

        Assert.assertFalse(controller.authService.isLoggedIn())
        controller.login('user', 'pass')
        Assert.assertTrue(controller.authService.isLoggedIn())
    }

    @Test
    void 'should logout user in logout action'() {
        controller.authService.login('user', 'pass')
        Assert.assertTrue(controller.authService.isLoggedIn())
        controller.logout()
        Assert.assertFalse(controller.authService.isLoggedIn())
    }

}