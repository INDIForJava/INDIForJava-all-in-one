package org.apache.maven.plugin.allura;

import org.junit.Test;

public class I4JReportTest {

    @Test
    public void testGeneratI4JChagesReport() throws Exception {

        String sourceforgeUrl = "https://sourceforge.net/rest/p/indiforjava/tickets";

        DownloadChanges.generateChangesXml(sourceforgeUrl);

    }
}
