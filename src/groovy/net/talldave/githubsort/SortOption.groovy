package net.talldave.githubsort

enum SortOption {

    ALPHABETICAL('name'),
    NUM_FORKS('forks_count'),
    NUM_STARS('stargazers_count'),
    NUM_WATCHERS('watchers_count'),
    NUM_OPEN_ISSUES('open_issues_count'),
    CREATE_DATE('created_at'),
    UPDATE_DATE('updated_at'),
    PUSH_DATE('pushed_at')


    String code

    SortOption(String code) {
        this.code = code
    }

}