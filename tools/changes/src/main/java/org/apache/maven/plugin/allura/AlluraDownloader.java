package org.apache.maven.plugin.allura;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.apache.maven.plugin.issues.Issue;
import org.apache.maven.project.MavenProject;

public class AlluraDownloader {

    /**
     * A boolean to indicate if we should include open issues as well
     */
    private boolean includeOpenIssues;

    /**
     * A boolean to indicate if we should only include issues with milestones
     */
    private boolean onlyMilestoneIssues;

    /**
     * The name of the allura project.
     */
    private String alluraRepo;

    /**
     * The url to the github repo's issue management
     */
    private String alluraURL;

    private AlluraRestAccess alluraRestAccess;

    public AlluraDownloader(MavenProject project, String alluraScheme, int alluraPort, boolean includeOpenIssues, boolean onlyMilestoneIssues) throws IOException {
        this.includeOpenIssues = includeOpenIssues;
        this.onlyMilestoneIssues = onlyMilestoneIssues;

        this.alluraURL = project.getIssueManagement().getUrl();
        if (!this.alluraURL.endsWith("/")) {
            this.alluraURL = this.alluraURL + "/";
        }
        URL alluraURL = new URL(this.alluraURL);

        String urlPath = alluraURL.getPath();
        if (urlPath.startsWith("/")) {
            urlPath = urlPath.substring(1);
        }
        if (urlPath.endsWith("/")) {
            urlPath = urlPath.substring(0, urlPath.length() - 2);
        }
        String[] urlPathParts = urlPath.split("/");

        this.alluraRepo = urlPathParts[urlPathParts.length - 2];

        alluraRestAccess = new AlluraRestAccess(this.alluraURL);
    }

    public List<Issue> getIssueList() throws IOException {
        return alluraRestAccess.getIssues();
    }

}
