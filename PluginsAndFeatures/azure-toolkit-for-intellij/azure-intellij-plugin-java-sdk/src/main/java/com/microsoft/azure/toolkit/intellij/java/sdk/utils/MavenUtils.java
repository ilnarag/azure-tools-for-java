package com.microsoft.azure.toolkit.intellij.java.sdk.utils;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class MavenUtils {
    private static final Map<String, MavenArtifact> MAVEN_ARTIFACTS = new ConcurrentHashMap<>();

    private MavenUtils() {
    }

    /**
     * Gets the latest released version of the given artifact from Maven repository.
     *
     * @param groupId    The group id of the artifact.
     * @param artifactId The artifact id of the artifact.
     * @return The latest version or {@code null} if an error occurred while retrieving the latest
     * version.
     */
    public static String getLatestArtifactVersion(String groupId, String artifactId) {
        MavenArtifact mavenArtifact = MAVEN_ARTIFACTS.get(groupId + ":" + artifactId);
        if (mavenArtifact == null || mavenArtifact.getLastUpdated().isBefore(OffsetDateTime.now().minusDays(1))) {
            HttpURLConnection connection = null;
            try {
                groupId = groupId.replace(".", "/");
                final URL url = new URL("https://repo1.maven.org/maven2/" + groupId + "/" + artifactId + "/maven-metadata.xml");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("accept", "application/xml");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                final int responseCode = connection.getResponseCode();
                if (HttpURLConnection.HTTP_OK == responseCode) {
                    try (final InputStream responseStream = connection.getInputStream()) {

                        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                        final DocumentBuilder db = dbf.newDocumentBuilder();
                        final Document doc = db.parse(responseStream);

                        // Maven-metadata.xml lists versions from oldest to newest. Therefore, we want the bottom-most version
                        // that is not a beta release.
                        final NodeList versionsList = doc.getElementsByTagName("version");

                        String latestVersion = null;
                        String latestBetaVersion = null;

                        for (int i = versionsList.getLength() - 1; i >= 0; i--) {
                            final Node versionNode = versionsList.item(i);
                            if (!versionNode.getTextContent().contains("beta")) {
                                latestVersion = versionNode.getTextContent();
                                break;
                            } else if (latestBetaVersion == null) {
                                latestBetaVersion = versionNode.getTextContent();
                            }
                        }
                        latestVersion = latestVersion == null ? latestBetaVersion : latestVersion;

                        System.out.println("The latest version of " + artifactId + " is " + latestVersion);
                        if (mavenArtifact == null) {
                            mavenArtifact = new MavenArtifact(groupId, artifactId);
                        }
                        mavenArtifact.setLastUpdated(OffsetDateTime.now());
                        mavenArtifact.setVersion(latestVersion);
                        MAVEN_ARTIFACTS.put(groupId + ":" + artifactId, mavenArtifact);
                        return latestVersion;
                    }
                } else {
                    System.out.println("Got a non-successful response for  " + artifactId + ": " + responseCode);
                }
            } catch (ParserConfigurationException | IOException | SAXException exception) {
                System.out.println("Got error getting latest maven dependency version. " + exception.getMessage());
            } finally {
                if (connection != null) {
                    // closes the input streams and discards the socket
                    connection.disconnect();
                }
            }
            return mavenArtifact == null ? null : mavenArtifact.getVersion();
        } else {
            System.out.println("Maven artifact " + groupId + ":" + artifactId + " was last updated on " + mavenArtifact.getLastUpdated() + ". Not refreshing cache.");
            return mavenArtifact.getVersion();
        }
    }
}
