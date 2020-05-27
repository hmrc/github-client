package uk.gov.hmrc.githubclient;

import org.eclipse.egit.github.core.Repository;

import java.util.Date;

public class ExtendedRepository extends Repository {

    private Boolean archived;

    public boolean isArchived() {
        return archived;
    }

    public ExtendedRepository setName(String name) {
        super.setName(name);
        return this;
    }

    public ExtendedRepository setDescription(String description) {
        super.setDescription(description);
        return this;
    }

    public ExtendedRepository setId(long id) {
        super.setId(id);
        return this;
    }

    public ExtendedRepository setHtmlUrl(String htmlUrl) {
        super.setHtmlUrl(htmlUrl);
        return this;
    }

    public ExtendedRepository setIsFork(Boolean fork) {
        super.setFork(fork);
        return this;
    }

    public ExtendedRepository setCreatedAt(Date createdAt) {
        super.setCreatedAt(createdAt);
        return this;
    }

    public ExtendedRepository setPushedAt(Date pushedAt) {
        super.setPushedAt(pushedAt);
        return this;
    }

    public ExtendedRepository setIsPrivate(Boolean isPrivate) {
        super.setPrivate(isPrivate);
        return this;
    }

    public ExtendedRepository setLanguage(String language) {
        super.setLanguage(language);
        return this;
    }

    public ExtendedRepository setArchived(Boolean archived) {
        this.archived = archived;
        return this;
    }
}
