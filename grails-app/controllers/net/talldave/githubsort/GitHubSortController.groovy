package net.talldave.githubsort

import groovy.transform.ToString

import net.talldave.githubsort.dto.RateLimitDTO
import net.talldave.githubsort.dto.RepoDTO

class GitHubSortController {

    def authService
    def gitHubApiService

    // TODO: move flash messages to messages.properties

    def index(GitHubSortCommand cmd) {
        // get remaining request limit
        cmd.rateLimit = gitHubApiService.retrieveRemainingRateLimit()
        [cmd:cmd, loggedInUser:authService.getUser()]
        // some dupe code between this and the next action
    }

    def search(GitHubSortCommand cmd) {
        try {
            // get sorted results
            cmd.results = gitHubApiService.retrieveReposOrderedByPullRequestsWithSecondSort(cmd.orgName, cmd.sortOption, cmd.numRepos)
        }
        catch (e) {
            log.warn "Failed to get GitHub data", e
            flash.error = "Could not retrieve data from GitHub: ${e.message}"
        }

        // get remaining request limit
        cmd.rateLimit = gitHubApiService.retrieveRemainingRateLimit()

        render view:'index', model:[cmd:cmd, loggedInUser:authService.getUser()]
    }

    def login(String username, String password) {
        if (request.method == 'POST') {
            if (! username || ! password) {
                flash.error = "Both username and password are required."
            }
            else {
                authService.login(username, password)
                flash.message = 'Your GitHub credentials are now stored in memory on the server and will be used for subsequent requests.'
                redirect(action: 'index')
            }
        }
    }

    def logout() {
        authService.logout()
        flash.message = 'Your GitHub credentials have been removed from the server.'
        redirect(action: 'index')
    }

}


@ToString(includeNames=true)
class GitHubSortCommand {
    // input
    String orgName
    SortOption sortOption
    Integer numRepos = 5

    // output (reusing command obj as dto)
    RateLimitDTO rateLimit
    List<RepoDTO> results
}
