/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/

package org.fusesource.ide.server.fabric8.ui.runtime.fabric8;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.ui.wizard.IWizardHandle;
import org.fusesource.ide.server.karaf.ui.runtime.AbstractKarafRuntimeComposite;
import org.fusesource.ide.server.karaf.ui.runtime.KarafWizardDataModel;
import org.fusesource.ide.server.karaf.ui.runtime.v2x.KarafRuntimeWizardFragment2x;

/**
 * @author lhein
 */
public class Fabric8RuntimeWizardFragment1x extends
		KarafRuntimeWizardFragment2x {
	
	/*
	 * (non-Javadoc)
	 * @see org.fusesource.ide.server.karaf.ui.runtime.v2x.KarafRuntimeWizardFragment2x#getRuntimeComposite(org.eclipse.swt.widgets.Composite, org.eclipse.wst.server.ui.wizard.IWizardHandle, org.fusesource.ide.server.karaf.ui.runtime.KarafWizardDataModel)
	 */
	@Override
	protected AbstractKarafRuntimeComposite getRuntimeComposite(
			Composite parent, IWizardHandle handle, KarafWizardDataModel model) {
		return new Fabric8RuntimeComposite1x(parent, handle, model);
	}
}