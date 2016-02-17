# github-sorting

GitHub Sorting
==============

Experimentation with the GitHub API. (Note that this may have been part of a job interview for a well known media streaming company...)


Goal
----

Build a tool that returns a top 5 list of an organization's Github projects ranked by pull requests (i.e. the repository with the most pull requests would be listed first). After sorting by pull requests, do a secondary sort by some other (parameterized criteria).


Some references
---------------

[Github API](http://developer.github.com/v3/) <br>
[List an organization’s repos](http://developer.github.com/v3/repos/#list-organization-repositories) <br>
[List pull requests](http://developer.github.com/v3/pulls/) <br>


Build, Test and Run Steps
-------------------------

Built with Grails 2.3.11 on Java 7.  (I'm assuming Java 7 is already on the local machine.)

Install Grails 2.3.11.  Using [GVM](http://gvmtool.net/) is probably the simplest if it's not already installed on the local machine:

    curl get.gvmtool.net | bash

You may be prompted for an additional step or two to complete installation.  Once GVM install is complete install Grails:

    gvm install grails 2.3.11

In the directory where this project was cloned from GitHub run:

    grails clean
    grails compile

To run the tests, and generate a code coverage report:

    grails test-app unit: integration: -coverage

The coverage report is available on the file system at (cloned dir)/target/test-reports/cobertura/index.html.  (Note: this will generate a stacktrace which is to be expected - more details in the dev notes section below.)

To run the application itself:

    grails run-app

Then open the app in a browser at [http://localhost:8080/github-sorting/](http://localhost:8080/github-sorting/).  Note that due to GitHub throttling issues it will most likely be necessary to almost immediately use the login link in the app to enter your GitHub credentials.

