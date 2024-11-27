package com.microsoft.azure.toolkit.intellij.java.sdk.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.idea.maven.project.MavenProject;

public class MavenRootProjectScope extends GlobalSearchScope {
    private final VirtualFile rootDirectory;

    public MavenRootProjectScope(Project project, MavenProject mavenProject) {
        super(project);
        this.rootDirectory = mavenProject.getDirectoryFile();
    }

    @Override
    public boolean contains(VirtualFile file) {
        return rootDirectory != null && file.getPath().startsWith(rootDirectory.getPath());
    }

    @Override
    public boolean isSearchInModuleContent(Module module) {
        return true;
    }

    @Override
    public boolean isSearchInLibraries() {
        return false;
    }
}