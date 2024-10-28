package com.microsoft.azure.toolkit.intellij.java.sdk;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManagerListener;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.microsoft.azure.toolkit.intellij.java.sdk.utils.MavenUtils;
import com.microsoft.azure.toolkit.intellij.java.sdk.report.*;
import com.microsoft.azure.toolkit.intellij.java.sdk.telemetry.*;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public final class MavenProjectReportGenerator implements ProjectActivity, DumbAware, ProjectManagerListener {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private static final String APP_INSIGHTS_ENDPOINT = "https://centralus-2.in.applicationinsights.azure.com/";
    private static final String APP_INSIGHTS_INSTRUMENTATION_KEY = "1d377c0e-44f8-4d56-bee7-7f13a3fef594";
    private static final String AZURE_SDK_REPORT_SOURCE = "azure-sdk-intellij-plugin";
    private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };
    private final AtomicReference<BuildReport> currentBuildReport;
    private final ApplicationInsightsClient applicationInsightsClient;
    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public MavenProjectReportGenerator() {
        this.applicationInsightsClient = new ApplicationInsightsClientBuilder()
                .host(APP_INSIGHTS_ENDPOINT)
                .buildClient();
        this.currentBuildReport = new AtomicReference<>();
    }

    @Override
    public void projectClosed(@Nonnull Project project) {
        System.out.println("Project closed");
        scheduledExecutor.shutdown();
    }

    @Nullable
    @Override
    public Object execute(@Nonnull Project project, @Nonnull Continuation<? super Unit> continuation) {
        System.out.println("Running the startup activity");
        final MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstanceIfCreated(project);

        if (mavenProjectsManager == null || !mavenProjectsManager.isMavenizedProject()) {
            System.out.println("Not a maven project. Exiting.");
            return null;
        }
        scheduledExecutor.scheduleWithFixedDelay(() -> generateReport(project), 1, 5, TimeUnit.MINUTES);
        return null;
    }

    private void generateReport(@Nonnull Project project) {
        if (Registry.is("Azure SDK Report Enabled", true)) {
            System.out.println("Running Azure SDK report generation");
            final BuildReport buildReport = new BuildReport();
            inspectPomFile(project, buildReport);
            inspectJavaFiles(project, buildReport);
            System.out.println("Finished generating report");
            try {
                System.out.println(OBJECT_MAPPER.writeValueAsString(buildReport));
                if(!buildReport.equals(currentBuildReport.get())) {
                    sendReportToAppInsights(buildReport);
                    currentBuildReport.set(buildReport);
                } else {
                    System.out.println("No change to build report. Skipped sending report to Application Insights");
                }
            } catch (final Exception e) {
                System.out.println("Unable to send the Azure SDK report " + e.getMessage());
            }
        } else {
            System.out.println("Azure SDK Report generation is disabled");
        }
    }

    private void sendReportToAppInsights(BuildReport report) {
        try {
            TelemetryItem telemetryItem = new TelemetryItem();
            telemetryItem.setTime(OffsetDateTime.now());
            telemetryItem.setName(AZURE_SDK_REPORT_SOURCE);
            telemetryItem.setInstrumentationKey(APP_INSIGHTS_INSTRUMENTATION_KEY);
            TelemetryEventData data = new TelemetryEventData();
            Map<String, String> customEventProperties = getCustomEventProperties(report);
            data.setProperties(customEventProperties);
            MonitorBase monitorBase = new MonitorBase();
            monitorBase.setBaseData(data).setBaseType("EventData");
            data.setName("azure-sdk-java-build-telemetry");
            telemetryItem.setData(monitorBase);
            List<TelemetryItem> telemetryItems = new ArrayList<>();
            telemetryItems.add(telemetryItem);
            applicationInsightsClient.trackAsync(telemetryItems).block();
            System.out.println("Successfully sent the report to Application Insights");
        } catch (Exception ex) {
            System.out.println("Unable to send report to Application Insights. " + ex.getMessage());
        }
    }

    private Map<String, String> getCustomEventProperties(BuildReport report) {
        Map<String, Object> properties = OBJECT_MAPPER.convertValue(report, MAP_TYPE_REFERENCE);
        Map<String, String> customEventProperties = new HashMap<>(properties.size());
        // AppInsights customEvents table does not support nested JSON objects in "properties" field
        // So, we have to convert the nested objects to strings
        properties.forEach((key, value) -> {
            if (value instanceof String) {
                customEventProperties.put(key, (String) value);
            } else {
                customEventProperties.put(key, BinaryData.fromObject(value).toString());
            }
        });
        return customEventProperties;
    }

    private void inspectJavaFiles(@Nonnull Project project, BuildReport buildReport) {
        final Collection<VirtualFile> javaFiles = FileTypeIndex.getFiles(com.intellij.ide.highlighter.JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        final Map<String, Integer> methodCallFrequency = new HashMap<>();

        for (final VirtualFile javaFile : javaFiles) {
            // Access the content of the file
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(javaFile);
            if (psiFile != null) {
                System.out.println("Scanning file: " + psiFile.getName());
                Collection<PsiMethodCallExpression> methodCalls = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethodCallExpression.class);
                methodCalls
                        .forEach(methodCall -> {
                            final PsiMethod psiMethod = methodCall.resolveMethod();
                            System.out.println("Processing method call " + psiMethod.getName());
                            final PsiAnnotation[] annotations = psiMethod.getAnnotations();
                            Arrays.stream(annotations).forEach(annotation -> {
                                if (Objects.equals(annotation.getQualifiedName(), "com.azure.core.annotation.ServiceMethod")) {
                                    final String methodName = psiMethod.getName();
                                    final String returnType = psiMethod.getReturnType().getCanonicalText();
                                    final String params = Arrays.stream(psiMethod.getParameterList().getParameters())
                                            .map(parameter -> parameter.getType().getCanonicalText())
                                            .collect(Collectors.joining(","));
                                    final String methodSignature = returnType + " " + methodName + "(" + params + ")";
                                    methodCallFrequency.compute(methodSignature, (k, v) -> v == null ? 1 : v + 1);
                                    System.out.println("Calling a method annotated as service method " + methodSignature);
                                }
                            });
                        });
            }
        }

        final List<MethodCallDetails> methodCallDetails = methodCallFrequency.entrySet().stream()
                .map(entry -> new MethodCallDetails()
                        .setMethodName(entry.getKey())
                        .setCallFrequency(entry.getValue()))
                .toList();
        buildReport.setServiceMethodCalls(methodCallDetails);
    }

    private void inspectPomFile(@Nonnull Project project, BuildReport buildReport) {
        FileTypeIndex.getFiles(XmlFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                .forEach(virtualFile -> {
                    PsiFile file = PsiManager.getInstance(project).findFile(virtualFile);
                    if (!"pom.xml".equals(file.getName())) {
                        return;
                    }
                    MavenProjectsManager mavenProjectsManager = MavenProjectsManager.getInstance(file.getProject());
                    if (!mavenProjectsManager.isMavenizedProject()) {
                        return;
                    }
                    FileViewProvider viewProvider = file.getViewProvider();
                    XmlFile xmlFile = (XmlFile) viewProvider.getPsi(StdLanguages.XML);

                    XmlTag rootTag = xmlFile.getRootTag();
                    if (rootTag != null && "project".equals(rootTag.getName())) {
                        final String artifactId = rootTag.findFirstSubTag("artifactId").getValue().getText();
                        final String groupId = rootTag.findFirstSubTag("groupId").getValue().getText();
                        final String version = rootTag.findFirstSubTag("version").getValue().getText();

                        buildReport.setArtifactId(artifactId);
                        buildReport.setGroupId(groupId);
                        buildReport.setVersion(version);

                        checkDependencyManagement(rootTag, buildReport);
                        checkDependencies(rootTag, buildReport);
                    }
                });
    }

    private void checkDependencyManagement(XmlTag rootTag, BuildReport buildReport) {
        final XmlTag dependencyManagement = rootTag.findFirstSubTag("dependencyManagement");
        if (dependencyManagement != null) {
            final XmlTag dependenciesTag = dependencyManagement.findFirstSubTag("dependencies");
            if (dependenciesTag != null) {
                final XmlTag[] dependencyTags = dependenciesTag.findSubTags("dependency");
                for (final XmlTag dependencyTag : dependencyTags) {
                    final String groupId = dependencyTag.findFirstSubTag("groupId").getValue().getText();
                    final String artifactId = dependencyTag.findFirstSubTag("artifactId").getValue().getText();
                    String versionId = null;

                    if (dependencyTag.findFirstSubTag("version") != null) {
                        versionId = dependencyTag.findFirstSubTag("version").getValue().getText();
                    }

                    if ("com.azure".equals(groupId) && artifactId.equals("azure-sdk-bom")) {
                        final String latestArtifactVersion = MavenUtils.getLatestArtifactVersion(groupId, artifactId);

                        if (versionId != null && !versionId.equals(latestArtifactVersion)) {
                            buildReport.setBomVersion(versionId);
                            buildReport.addError(new BuildError("Newer version  of azure-sdk-bom available",
                                    BuildErrorCode.OUTDATED_DEPENDENCY, BuildErrorLevel.WARNING,
                                    List.of(groupId + ":" + artifactId + ":" + versionId)));
                            System.out.println("Newer version of azure-sdk-bom is available. Update to version " + latestArtifactVersion);
                        }
                    }
                }
            }
        }
    }

    private void checkDependencies(XmlTag rootTag, BuildReport buildReport) {
        final XmlTag dependenciesTag = rootTag.findFirstSubTag("dependencies");

        final List<String> azureDependencies = new ArrayList<>();
        buildReport.setAzureDependencies(azureDependencies);

        if (dependenciesTag != null) {
            final XmlTag[] dependencyTags = dependenciesTag.findSubTags("dependency");
            for (final XmlTag dependencyTag : dependencyTags) {
                final String groupId = dependencyTag.findFirstSubTag("groupId").getValue().getText();
                final String artifactId = dependencyTag.findFirstSubTag("artifactId").getValue().getText();
                String versionId = null;
                if (dependencyTag.findFirstSubTag("version") != null) {
                    versionId = dependencyTag.findFirstSubTag("version").getValue().getText();
                }

                if ("com.azure".equals(groupId)) {
                    azureDependencies.add(groupId + ":" + artifactId + ":" + versionId);
                }

                if ("com.azure".equals(groupId) && versionId != null && !versionId.contains("beta")) {
                    buildReport.addError(new BuildError("Use Azure SDK Bom (azure-sdk-bom) instead of specifying version in dependency tag",
                            BuildErrorCode.BOM_VERSION_OVERRIDDEN, BuildErrorLevel.WARNING));
                }

                if ("com.azure".equals(groupId) && versionId != null && versionId.contains("beta")) {
                    buildReport.addError(new BuildError("Using a beta version in production is not recommended. Please use a stable version.",
                            BuildErrorCode.BETA_DEPENDENCY_USED, BuildErrorLevel.WARNING));
                }
            }
        }

    }
}
