/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.component;

import com.intellij.openapi.actionSystem.AnAction;
import org.jboss.tools.intellij.openshift.actions.ActionTest;
import org.jboss.tools.intellij.openshift.actions.url.CreateURLAction;

public class CreateURLActionTest extends ActionTest {
    @Override
    public AnAction getAction() {
        return new CreateURLAction();
    }

    @Override
    protected void verifyPushedComponent(boolean visible) {
        assertTrue(visible);
    }

    @Override
    protected void verifyNotPushedComponent(boolean visible) {
        assertTrue(visible);
    }

    @Override
    protected void verifyNoContextComponent(boolean visible) {
        assertTrue(visible);
    }
}
