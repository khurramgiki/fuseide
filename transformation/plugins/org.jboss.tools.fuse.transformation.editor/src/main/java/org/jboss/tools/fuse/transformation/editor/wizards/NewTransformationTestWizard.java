/******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. and others. 
 * All rights reserved. This program and the accompanying materials are 
 * made available under the terms of the Eclipse Public License v1.0 which 
 * accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: JBoss by Red Hat - Initial implementation.
 *****************************************************************************/
package org.jboss.tools.fuse.transformation.editor.wizards;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.jboss.tools.fuse.transformation.camel.CamelConfigBuilder;
import org.jboss.tools.fuse.transformation.editor.Activator;
import org.jboss.tools.fuse.transformation.editor.internal.util.CamelConfigurationHelper;
import org.jboss.tools.fuse.transformation.editor.internal.util.CamelFileTypeHelper;
import org.jboss.tools.fuse.transformation.editor.internal.util.JavaUtil;
import org.jboss.tools.fuse.transformation.editor.internal.wizards.TransformTestWizardPage;

/**
 *
 */
@SuppressWarnings("restriction")
public class NewTransformationTestWizard extends NewElementWizard implements INewWizard {

    static final String DEFAULT_DOZER_CONFIG_FILE_NAME = "dozerBeanMapping.xml";

    IProject project;
    IJavaProject javaProject;
    IFile camelConfigFile;
    CamelConfigBuilder builder;
    String transformID = null;
    String packageName = null;
    String className = null;
    
    private TransformTestWizardPage _page = null;

    private static final String JDT_EDITOR = 
            "org.eclipse.jdt.ui.CompilationUnitEditor"; //$NON-NLS-1$

    /**
     *
     */
    public NewTransformationTestWizard() {
        setWindowTitle("New Fuse Transformation Test");
        _page = new TransformTestWizardPage();
    }

    @Override
    public void addPages() {
        addPage(_page);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // what are we passing in? assume we're right-clicking on the dozer file
        if (selection.size() != 1) {
            return;
        }
        _page.init(selection);
        Object selectedObject = selection.getFirstElement();
        if (selectedObject != null) {
            if (selectedObject instanceof IResource) {
                project = ((IResource)selectedObject).getProject();
            }
            if (selectedObject instanceof IFile) {
                IFile selectedFile = (IFile) selectedObject;
                boolean isCamelConfig = CamelFileTypeHelper.isSupportedCamelFile(project, selectedFile.getProjectRelativePath().toPortableString());
                if (isCamelConfig) {
                    camelConfigFile = selectedFile;
                }
            }
            _page.setTypeName("TransformationTest", true);
            if (project != null) {
                _page.setProject(project);
                javaProject = JavaCore.create(project);
            }
            if (project != null && camelConfigFile == null) {
                IResource findCamelContext =
                        project.findMember("src/main/resources/META-INF/spring/camel-context.xml");
                if (findCamelContext != null) {
                    camelConfigFile = (IFile) findCamelContext;
                }
            }
            if (camelConfigFile != null) {
                _page.setCamelConfigFile(camelConfigFile);
                File file = new File(camelConfigFile.getLocationURI());
                try {
                    builder = CamelConfigurationHelper.load(file).getConfigBuilder();
                    _page.setBuilder(builder);
                } catch (Exception e) {
                    Activator.error(e);
                }
            }
            if (javaProject != null) {
                _page.setJavaProject(javaProject);
                
                IFolder srcFolder = javaProject.getProject().getFolder("src/test/java");
                if (!JavaUtil.findFolderOnProjectClasspath(javaProject, srcFolder)) {
                    JavaUtil.addFolderToProjectClasspath(javaProject, srcFolder);
                }
                if (!srcFolder.exists()) {
                    try {
                        srcFolder.refreshLocal(IResource.DEPTH_INFINITE, null);
                    } catch (CoreException e) {
                        e.printStackTrace();
                    }
                }
                
                IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(srcFolder);
                _page.setPackageFragmentRoot(root, true);
            }
        }
    }

    @Override
    public boolean canFinish() {
       return super.canFinish() &&
                   getContainer().getCurrentPage() == _page;
    }
 
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
        _page.createType(monitor);
        if (_page.getGeneratedResource() != null) {
            if (project == null && _page.getProject() != null) {
                project = _page.getProject();
            }
            project.refreshLocal(IProject.DEPTH_INFINITE, new NullProgressMonitor());
            IFile resource = (IFile) _page.getGeneratedResource();
            openResource(resource);
        } else {
            throw new CoreException(new Status(IStatus.ERROR, 
                "TransformationEditor", 
                "Problem encountered while creating test class."));
        }
    }
 
    @Override
    public IJavaElement getCreatedElement() {
        return _page.getCreatedType();
    }
    
    protected void openResource(final IFile resource) {
        if (resource.getType() != IResource.FILE) {
            return;
        }
        final Display display = getShell().getDisplay();
        display.asyncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                if (window == null) {
                    return;
                }

                IWorkbenchPage activePage = window.getActivePage();
                if (activePage != null) {
                    try {
                        IDE.openEditor(activePage, resource, JDT_EDITOR, true);
                        BasicNewResourceWizard.selectAndReveal(resource, activePage
                                .getWorkbenchWindow());
                    } catch (PartInitException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
