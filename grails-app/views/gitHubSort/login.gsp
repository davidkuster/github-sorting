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
            </ul>
        </div>

        <div id="login">
            <g:if test="${flash.message}">
                <div class="message" role="status">${flash.message}</div>
            </g:if>
            <g:if test="${flash.error}">
                <div class="errors" role="status">${flash.error}</div>
            </g:if>

            <g:form action="login" >
                <fieldset class="form">
                    <div class="fieldcontain">
                        <label>GitHub Username</label>
                        <g:textField name="username" />
                    </div>
                    <div class="fieldcontain">
                        <label>GitHub Password</label>
                        <g:passwordField name="password"/>
                    </div>
                </fieldset>
                <fieldset class="buttons">
                    <g:submitButton name="search" class="save" value="Login" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
