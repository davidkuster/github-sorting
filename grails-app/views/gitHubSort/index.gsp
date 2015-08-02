<%@ page import="net.talldave.githubsort.SortOption" %>
<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main">
        <title>GitHub Sorting</title>
    </head>
    <body>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <g:if test="${! loggedInUser}">
                    <li><g:link action="login">Login (enter GitHub credentials)</g:link></li>
                </g:if>
                <g:else>
                    <li><g:link action="logout">Logout ${loggedInUser}</g:link></li>
                </g:else>
            </ul>
        </div>

        <div id="search">
            <g:if test="${flash.error}">
                <div class="errors" role="status">${flash.error}</div>
            </g:if>
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>

            <g:form action="search" >
                <fieldset class="form">
                    <div class="fieldcontain">
                        <label>Organization Name</label>
                        <g:textField name="orgName" value="${cmd?.orgName}"/>
                    </div>
                    <div class="fieldcontain">
                        <label>Secondary Sort</label>
                        <g:select name="sortOption" value="${cmd?.sortOption}" from="${SortOption.values()}" />
                    </div>
                    <div class="fieldcontain">
                        <label>Max Results</label>
                        <g:textField name="numRepos" value="${cmd?.numRepos}"/>
                        <label class='infoMsg'>(allowed range is 1-100)</label>
                    </div>
                </fieldset>
                <fieldset class="buttons">
                    <g:submitButton name="search" class="save" value="Search and Sort" />
                </fieldset>
            </g:form>
        </div>

        <g:if test="${cmd?.results}">
            <div id="list-parent" class="content scaffold-list" role="main">
                <h1>Results</h1>
                <table>
                    <thead>
                        <tr>
                            <th>Repo Name</th>
                            <th>Pull Requests</th>
                            <th>${cmd?.sortOption}</th>
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${cmd?.results}" status="i" var="repoDTO">
                        <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
                            <td>${repoDTO?.repoName}</td>
                            <td>${repoDTO?.pullRequests}</td>
                            <td>${repoDTO?.sortValue}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </g:if>

        <g:if test="${cmd?.rateLimit}">
            <div id="list-parent" class="content scaffold-list" role="main">
                <h1>Rate Limit</h1>
                <table>
                    <thead>
                        <tr>
                            <th>Request/Hour</th>
                            <th>Remaining</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td>${cmd?.rateLimit?.limit}</td>
                            <td>${cmd?.rateLimit?.remaining}</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </g:if>
    </body>
</html>
