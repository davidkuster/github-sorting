package net.talldave.githubsort.dto

import groovy.transform.ToString
import net.talldave.githubsort.SortOption

@ToString(includeNames=true)
class RepoDTO {

    String repoName
    Integer pullRequests
    SortOption sortOption
    String sortValue

}