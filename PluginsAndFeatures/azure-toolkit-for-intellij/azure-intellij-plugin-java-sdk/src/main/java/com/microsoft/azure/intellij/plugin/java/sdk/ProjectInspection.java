package com.microsoft.azure.intellij.plugin.java.sdk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.lang.StdLanguages;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.microsoft.azure.intellij.plugin.java.sdk.report.*;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ProjectInspection implements ProjectActivity, DumbAware {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Nullable
    @Override
    public Object execute(@Nonnull Project project, @Nonnull Continuation<? super Unit> continuation) {
        System.out.println("Running the startup activity");
        EXECUTOR_SERVICE.scheduleWithFixedDelay(() -> generateReport(project), 0, 30, TimeUnit.SECONDS);
        return null;
    }

    private static void generateReport(@Nonnull Project project) {
        System.out.println("Running the project inspection");
        final BuildReport buildReport = new BuildReport();
        inspectPomFile(project, buildReport);
        inspectJavaFiles(project, buildReport);
        System.out.println("Finished generating report");
        try {
            System.out.println(OBJECT_MAPPER.writeValueAsString(buildReport));
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private static void inspectJavaFiles(@Nonnull Project project, BuildReport buildReport) {
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

    private static void inspectPomFile(@Nonnull Project project, BuildReport buildReport) {
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

    private static void checkDependencyManagement(XmlTag rootTag, BuildReport buildReport) {
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
                        buildReport.setBomVersion(versionId);
                        if (!versionId.equals("1.2.24")) {
                            buildReport.addError(new BuildError("Newer version  of azure-sdk-bom available", BuildErrorCode.OUTDATED_DEPENDENCY, BuildErrorLevel.WARNING));
                            System.out.println("Newer version of azure-sdk-bom is available. Update to version 1.2.24");
                        }
                    }
                }
            }
        }
    }

    private static void checkDependencies(XmlTag rootTag, BuildReport buildReport) {
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

                if("com.azure".equals(groupId)) {
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
