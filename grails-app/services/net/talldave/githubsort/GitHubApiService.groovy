package net.talldave.githubsort

import java.text.SimpleDateFormat
import org.codehaus.groovy.grails.web.json.JSONElement

import net.talldave.githubsort.dto.RateLimitDTO
import net.talldave.githubsort.dto.RepoDTO

class GitHubApiService {

    static transactional = false

    // looks for an ISO 8601 formatted string ("2011-01-26T19:06:43Z")
    private static final DATE_REGEX = /\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}Z/

    private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")

    def restClientService
    def authService


    /**
     * Retrieves a list of the given organization's repos ranked by pull requests.
     * (The repo with the most pull requests will be ranked first.)  Default to the
     * top 5 repos, but this can optionally be overridden.
     *
     * Returns an ordered List<RepoDTO>
     */
    List<RepoDTO> retrieveReposOrderedByPullRequestsWithSecondSort(String orgName, SortOption secondSort=SortOption.ALPHABETICAL, Integer numRepos=5)
    {
        // could be doing this validation in the cmd obj instead...
        if (! orgName) {
            throw new IllegalArgumentException("orgName is a required input")
        }
        if (numRepos < 1 || numRepos > 100) {
            throw new IllegalArgumentException("The number of repos to retrieve must be between 1 and 100, [$numRepos] is not a valid value")
        }

        List<JSONElement> reposJSON = retrieveReposForOrganization(orgName)

        List<RepoDTO> repos = reposJSON?.collect { repoJSON ->
            RepoDTO dto = new RepoDTO()
            dto.with {
                repoName = repoJSON.name
                sortOption = secondSort
                sortValue = repoJSON."${sortOption.code}"
                // lookup number of pull requests for each repo
                // (note that these lookups could all be run in parallel)
                pullRequests = retrieveNumberOfPullRequests(orgName, repoJSON.name)
            }
            dto
        }

        // sort by pull requests and second ordering (if necessary) and return top X results
        repos.sort { a, b ->
            int order = b.pullRequests <=> a.pullRequests
            if (! order) {
                String aVal = a.sortValue
                String bVal = b.sortValue

                if (aVal.isNumber() && bVal.isNumber()) {
                    // sort digits descending
                    order = bVal.toBigDecimal() <=> aVal.toBigDecimal()
                }
                else if (aVal =~ DATE_REGEX && bVal =~ DATE_REGEX) {
                    // try and sort by timestamp
                    // going to assume we should order dates descending, although i could see this as an additional user option...
                    try {
                        Date aDate = dateFormatter.parse(aVal)
                        Date bDate = dateFormatter.parse(bVal)
                        order = bDate <=> aDate
                    }
                    catch (e) {
                        log.warn "Could not parse [$aVal] and/or [$bVal] as dates"
                    }
                }

                if (! order) {
                    // sort alpha ascending, lower cased
                    order = aVal.toLowerCase() <=> bVal.toLowerCase()
                }
            }
            order
        }.take(numRepos)
    }


    /**
     * Retrieves the GitHub repos names for the given organization name, up to a max of 100 repos
     */
    private List<JSONElement> retrieveReposForOrganization(String orgName) {
        JSONElement repos = makeRestRequest("https://api.github.com/orgs/$orgName/repos?per_page=100")
        repos
    }


    /**
     * Retrieves the number of pull requests for the given repo.
     */
    private Integer retrieveNumberOfPullRequests(String orgName, String repoName) {
        // retrieve the most recent pull request
        // (sort=created and direction=desc are the API defaults, but explicitly specifying them as the API is subject to change)
        JSONElement pullRequest = makeRestRequest("https://api.github.com/repos/$orgName/$repoName/pulls?state=all&per_page=1&sort=created&direction=desc")
        // note that this isn't very HATEOAS because I'm not reading the "repos_url" off the response from /orgs/:org/repos but oh well

        // making an assumption that the pull requests are sequentially numbered
        if (pullRequest) {
            pullRequest[0].number
        }
        // if no pull requests are found, return zero
        else {
            return 0
        }
    }


    /**
     * Retrieves the remaining number of requests for the current IP address.
     *
     * API doc: https://developer.github.com/v3/rate_limit/
     * Rate Limit rules: https://developer.github.com/v3/#rate-limiting
     */
    RateLimitDTO retrieveRemainingRateLimit() {
        // note: would like to also include the time until the limit resets but that's sent in the headers and RestClientService looks like it totally ignores response headers.
        // maybe stealing that from Asgard wasn't the best idea...
        // TODO: extend RestClientService to support reading response headers.
        // looking at the HttpClient docs looks like it uses a lot of deprecated methods as well.
        // may need to rip out and redo.
        JSONElement json = makeRestRequest('https://api.github.com/rate_limit')

        RateLimitDTO rateLimit = new RateLimitDTO()
        rateLimit.remaining = json?.resources?.core?.remaining
        rateLimit.limit = json?.resources?.core?.limit
        rateLimit
    }


    // very tempted to add caching here (except for the rate limit calls),
    // but going to resist in the interests of MVP...
    private JSONElement makeRestRequest(String uri) {
        log.debug "Making request to uri [$uri]"

        Integer timeout = restClientService.getDefaultTimeout()

        Map headers = ['Accept':'application/vnd.github.com/mondragon-preview+json']
        if (authService.isLoggedIn()) {
            headers << ['Authorization': "Basic ${authService.encodeCredentials()}"]
        }

        restClientService.getAsJson(uri, timeout, headers)
    }

}
