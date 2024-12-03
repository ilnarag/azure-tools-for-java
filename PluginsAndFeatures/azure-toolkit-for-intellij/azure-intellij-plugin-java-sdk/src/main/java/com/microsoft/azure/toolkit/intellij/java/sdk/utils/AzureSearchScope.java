package com.microsoft.azure.toolkit.intellij.java.sdk.utils;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.ProjectAndLibrariesScope;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class AzureSearchScope extends ProjectAndLibrariesScope {
    private final @NotNull Project project;

    public AzureSearchScope(@NotNull final Project project) {
        super(project);
        this.project = project;
    }

    @Override
    public boolean contains(@NotNull VirtualFile virtualFile) {
        if(super.contains(virtualFile)) {
            final PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (psiFile == null) {
                return false;
            }
            if (psiFile instanceof PsiJavaFile) {
                final PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
                if (psiJavaFile.getPackageName().startsWith("com.azure")) {
                    return Arrays.stream(psiJavaFile.getClasses())
                            .anyMatch(psiClass -> psiClass.getAnnotation("com.azure.core.annotation.ServiceClient") != null);
                }
            }
            return false;
        }
        return false;
    }
}
