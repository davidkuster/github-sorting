package net.talldave.githubsort.dto

import groovy.transform.ToString

@ToString(includeNames=true)
class RateLimitDTO {

    Integer limit
    Integer remaining

}