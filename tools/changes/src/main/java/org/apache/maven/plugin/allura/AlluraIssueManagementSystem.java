package org.apache.maven.plugin.allura;

import org.apache.maven.plugin.changes.IssueType;
import org.apache.maven.plugin.issues.AbstractIssueManagementSystem;

public class AlluraIssueManagementSystem  extends AbstractIssueManagementSystem
{

    private static final String DEFAULT_ADD_TYPE = "ISSUE";

    public AlluraIssueManagementSystem()
    {
        super();
        issueTypeMap.put( DEFAULT_ADD_TYPE, IssueType.UPDATE );
    }

    @Override
    public String getName()
    {
        return "Allura";
    }

}
