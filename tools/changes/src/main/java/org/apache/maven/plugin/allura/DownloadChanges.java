package org.apache.maven.plugin.allura;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;
import javax.json.stream.JsonParser;

import org.apache.commons.httpclient.HttpException;
import org.apache.maven.plugin.changes.ChangesXML;
import org.apache.maven.plugin.issues.Issue;
import org.apache.maven.plugins.changes.model.Action;
import org.apache.maven.plugins.changes.model.Body;
import org.apache.maven.plugins.changes.model.ChangesDocument;
import org.apache.maven.plugins.changes.model.FixedIssue;
import org.apache.maven.plugins.changes.model.Release;
import org.apache.maven.plugins.changes.model.io.xpp3.ChangesXpp3Writer;
import org.apache.maven.shared.utils.WriterFactory;
import org.codehaus.plexus.util.ReaderFactory;

public class DownloadChanges {

    static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    public static void generateChangesXml(String sourceforgeUrl) throws IOException, HttpException {
        AlluraIssueManagementSystem system = new AlluraIssueManagementSystem();
        ChangesDocument changesDocument = new ChangesDocument();
        changesDocument.setBody(new Body());

        AlluraRestAccess alluraRestAccess = new AlluraRestAccess(sourceforgeUrl);
        List<Issue> issues = alluraRestAccess.getIssues();

        Map<String, Release> versions = new HashMap<String, Release>();
        for (Issue issue : issues) {
            Release release = versions.get(issue.getFixVersions().get(0));
            if (release == null) {
                release = new Release();
                release.setVersion(issue.getFixVersions().get(0));
                alluraRestAccess.getMilesoneDescription(release.getVersion(), release);
                versions.put(release.getVersion(), release);
                changesDocument.getBody().addRelease(release);
            }
            Action action = new Action();
            action.setDate(dateFormat.format(issue.getUpdated()));
            action.setDev(issue.getAssignee());
            action.setSystem("SourceForge");
            action.setType(system.getIssueTypeMap().get(issue.getType()).configurationKey());
            action.setAction(issue.getTitle());

            release.addAction(action);
            FixedIssue fixedIssue = new FixedIssue();
            fixedIssue.setIssue(issue.getKey());
            action.addFixedIssue(fixedIssue);

        }
        Collections.sort(issues, new Comparator<Issue>() {

            public int compare(Issue o1, Issue o2) {
                int fixversiondif = o1.getFixVersions().get(0).compareTo(o1.getFixVersions().get(0));
                if (fixversiondif != 0) {
                    return fixversiondif;
                }
                return o1.getId().compareTo(o2.getId());
            }
        });

        Writer writer = WriterFactory.newXmlWriter(new File("changes.xml"));

        new ChangesXpp3Writer().write(writer, changesDocument);
    }
}
