/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.fusesource.ide.server.fabric8.core.server.subsystems;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.wst.server.core.IServer;
import org.fusesource.ide.server.fabric8.core.util.IFabric8ToolingConstants;
import org.fusesource.ide.server.karaf.core.runtime.IKarafRuntime;
import org.fusesource.ide.server.karaf.core.server.subsystems.Karaf2xStartupLaunchConfigurator;

/**
 * @author lhein
 */
public class Fabric81xStartupLaunchConfigurator extends
		Karaf2xStartupLaunchConfigurator {
	
	public Fabric81xStartupLaunchConfigurator(IServer server)
			throws CoreException {
		super(server);
	}
		
	/*
	 * (non-Javadoc)
	 * @see org.fusesource.ide.server.karaf.core.server.subsystems.Karaf2xStartupLaunchConfigurator#doConfigure(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	protected void doConfigure(ILaunchConfigurationWorkingCopy workingCopy)
			throws CoreException {

		IKarafRuntime runtime = null;
		if (server.getRuntime() != null) {
			runtime = (IKarafRuntime)server.getRuntime().loadAdapter(IKarafRuntime.class, null);
		}
		
		if (runtime != null) {
			String karafInstallDir = runtime.getLocation().toOSString();
			String mainProgram = null;
			String vmArguments = null;
			
			String version = runtime.getVersion();
			if (version != null) {
				if (version.startsWith(IFabric8ToolingConstants.FABRIC8_VERSION_1x)) {
					// handle 4x specific program arguments
					vmArguments = getVMArguments(karafInstallDir);
					mainProgram = getMainProgram();
				} else {
					System.err.println("Unhandled JBoss Fuse Version (" + version + ")!");
				}
			}
			
			// For java tabs
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, karafInstallDir);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, mainProgram);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, vmArguments);
			
			List<String> classPathList = new LinkedList<String>();
			String[] classPathEntries = getClassPathEntries(karafInstallDir);
			if (classPathEntries != null && classPathEntries.length > 0) {
				for (String jarName : classPathEntries) {
					IPath jarPath = new Path(jarName);
					IRuntimeClasspathEntry entry = JavaRuntime.newArchiveRuntimeClasspathEntry(jarPath);
					classPathList.add(entry.getMemento());
				}
			} else {
				// FIXME No jar files.
			}
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, classPathList);
			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, getJreContainerPath(runtime));
		}
	}

	/* (non-Javadoc)
	 * @see org.fusesource.ide.server.karaf.core.server.subsystems.Karaf2xStartupLaunchConfigurator#getVMArguments(java.lang.String)
	 */
	@Override
	protected String getVMArguments(String karafInstallDir) {
		StringBuilder vmArguments = new StringBuilder();

		String endorsedDirs = System.getProperty("java.endorsed.dirs");
		String extDirs = System.getProperty("java.ext.dirs");
		
		IKarafRuntime runtime = null;
		if (server.getRuntime() != null) {
			runtime = (IKarafRuntime)server.getRuntime().loadAdapter(IKarafRuntime.class, null);
			File vmLoc = runtime.getVM().getInstallLocation();
			
//			JAVA_ENDORSED_DIRS="${JAVA_HOME}/jre/lib/endorsed:${JAVA_HOME}/lib/endorsed:${KARAF_HOME}/lib/endorsed"
			endorsedDirs = String.format("%s%sjre%slib%sendorsed%s%s%slib%sendorsed%s%s%slib%sendorsed", 
										vmLoc.getPath(), SEPARATOR, SEPARATOR, SEPARATOR,
										File.pathSeparator, 
										vmLoc.getPath(), SEPARATOR, SEPARATOR,
										File.pathSeparator,
										karafInstallDir, SEPARATOR, SEPARATOR);
//		    JAVA_EXT_DIRS="${JAVA_HOME}/jre/lib/ext:${JAVA_HOME}/lib/ext:${KARAF_HOME}/lib/ext"
			extDirs = String.format("%s%sjre%slib%sext%s%s%slib%sext%s%s%slib%sext", 
					vmLoc.getPath(), SEPARATOR, SEPARATOR, SEPARATOR,
					File.pathSeparator, 
					vmLoc.getPath(), SEPARATOR, SEPARATOR,
					File.pathSeparator,
					karafInstallDir, SEPARATOR, SEPARATOR);
		}
		
		vmArguments.append("-server -Xms128M  -Xmx512M -XX:+UnlockDiagnosticVMOptions -XX:+UnsyncloadClass ");
		vmArguments.append(SPACE + "-XX:PermSize=16M -XX:MaxPermSize=128M ");
		vmArguments.append(SPACE + "-Dcom.sun.management.jmxremote ");
		vmArguments.append(SPACE + "-Djava.endorsed.dirs=" + QUOTE + endorsedDirs + QUOTE);
		vmArguments.append(SPACE + "-Djava.ext.dirs=" + QUOTE + extDirs + QUOTE);
		vmArguments.append(SPACE + "-Dkaraf.instances=" + QUOTE + karafInstallDir + SEPARATOR + "instances" + QUOTE);
		vmArguments.append(SPACE + "-Dkaraf.home=" + QUOTE + karafInstallDir + QUOTE); 
		vmArguments.append(SPACE + "-Dkaraf.base=" + QUOTE + karafInstallDir + QUOTE);
		vmArguments.append(SPACE + "-Dkaraf.data=" + QUOTE + karafInstallDir + SEPARATOR + "data" + QUOTE);
		vmArguments.append(SPACE + "-Dkaraf.etc=" + QUOTE + karafInstallDir + SEPARATOR + "etc" + QUOTE);
		vmArguments.append(SPACE + "-Djava.io.tmpdir=" + QUOTE + karafInstallDir + SEPARATOR + "data" + SEPARATOR + "tmp" + QUOTE);
		vmArguments.append(SPACE + "-Djava.util.logging.config.file=" + QUOTE + karafInstallDir + SEPARATOR + "etc" + SEPARATOR + "java.util.logging.properties" + QUOTE);
		vmArguments.append(SPACE + "-Dkaraf.startLocalConsole=false");
		vmArguments.append(SPACE + "-Dkaraf.startRemoteShell=true");
		vmArguments.append(SPACE + "-Densemble.auto.start=true");
		vmArguments.append(SPACE + "-Dagent.auto.start=true");
		
		return vmArguments.toString();
	}	

	
	
	/*
	 * (non-Javadoc)
	 * @see org.fusesource.ide.server.karaf.core.server.subsystems.Karaf2xStartupLaunchConfigurator#doOverrides(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	protected void doOverrides(ILaunchConfigurationWorkingCopy launchConfig)
			throws CoreException {
		super.doOverrides(launchConfig);
	}
}
