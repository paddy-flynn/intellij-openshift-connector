/*******************************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.intellij.openshift.actions.project;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import com.redhat.devtools.intellij.common.utils.UIHelper;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectBuilder;
import org.jboss.tools.intellij.openshift.actions.cluster.LoggedInClusterAction;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.application.ProjectNode;
import org.jboss.tools.intellij.openshift.utils.odo.Odo;

import javax.swing.tree.TreePath;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static org.jboss.tools.intellij.openshift.Constants.GROUP_DISPLAY_ID;

public class CreateProjectAction extends LoggedInClusterAction {
  @Override
  public void actionPerformed(AnActionEvent anActionEvent, TreePath path, Object selected, Odo odo) {
    LazyMutableTreeNode clusterNode = (LazyMutableTreeNode) selected;
    String projectName = Messages.showInputDialog("Project name", "New project", Messages.getQuestionIcon());
    if ((projectName != null) && projectName.trim().length() > 0) {
      CompletableFuture.runAsync(() -> {
        try {
          Notification notif = new Notification(GROUP_DISPLAY_ID, "Create project", "Creating project " + projectName, NotificationType.INFORMATION);
          Notifications.Bus.notify(notif);
          odo.createProject(projectName);
          notif.expire();
          Notifications.Bus.notify(new Notification(GROUP_DISPLAY_ID, "Create project", "Project " + projectName + " successfully created", NotificationType.INFORMATION));
          Project project = new ProjectBuilder().editOrNewMetadata().withName(projectName).endMetadata().build();
          clusterNode.add(new ProjectNode(project));
        } catch (IOException e) {
          UIHelper.executeInUI(() -> Messages.showErrorDialog("Error: " + e.getLocalizedMessage(), "Create project"));
        }
      });
    }
  }
}
