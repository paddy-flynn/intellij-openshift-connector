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
package org.jboss.tools.intellij.openshift.tree.application;

import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.tree.BaseTreeModel;
import com.intellij.util.messages.MessageBusConnection;
import com.redhat.devtools.intellij.common.utils.ConfigHelper;
import com.redhat.devtools.intellij.common.utils.ConfigWatcher;
import com.redhat.devtools.intellij.common.utils.ExecHelper;
import io.fabric8.kubernetes.api.model.Config;
import io.fabric8.kubernetes.api.model.Context;
import io.fabric8.kubernetes.client.internal.KubeConfigUtils;
import org.apache.commons.codec.binary.StringUtils;
import org.jboss.tools.intellij.openshift.tree.LazyMutableTreeNode;
import org.jboss.tools.intellij.openshift.tree.RefreshableTreeModel;
import org.jboss.tools.intellij.openshift.utils.odo.ComponentDescriptor;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApplicationTreeModel extends BaseTreeModel<Object>
        implements ConfigWatcher.Listener, RefreshableTreeModel, LazyMutableTreeNode.ChangeListener, ModuleListener {
    private ApplicationsRootNode ROOT;
    private final Project project;
    private Config config;

    private final Map<String, ComponentDescriptor> components = new HashMap<>();

    public ApplicationTreeModel(Project project) {
        initConfigWatcher();
        ROOT = new ApplicationsRootNode(this);
        ROOT.addChangeListener(this);
        this.project = project;
        loadProjectModel(project);
        registerProjectListener(project);
        this.config = loadConfig();
    }

    protected void initConfigWatcher() {
        ExecHelper.submit(new ConfigWatcher(Paths.get(ConfigHelper.getKubeConfigPath()), this));
    }

    protected Config loadConfig() {
        return ConfigHelper.safeLoadKubeConfig();
    }

    public static VirtualFile getModuleRoot(Module module) {
        return LocalFileSystem.getInstance().findFileByPath(new File(module.getModuleFilePath()).getParent());
    }

    protected void loadProjectModel(Project project) {
        for(Module module : project.getComponent(ModuleManager.class).getModules()) {
            addContext(getModuleRoot(module));
        }
    }

    @Override
    public void moduleAdded(@NotNull Project project, @NotNull Module module) {
        addContext(getModuleRoot(module));
    }

    @Override
    public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        removeContext(getModuleRoot(module));
    }

    private void addContextToSettings(String path, ComponentDescriptor descriptor) {
        if (!components.containsKey(path)) {
            components.put(path, descriptor);
            refresh();
        }
    }

    private void addContext(VirtualFile modulePathFile) {
        if (modulePathFile != null && modulePathFile.isValid()) {
            try {
                List<ComponentDescriptor> descriptors = ROOT.getOdo().discover(modulePathFile.getPath());
                descriptors.forEach(descriptor -> addContextToSettings(descriptor.getPath(), descriptor));
            } catch (IOException e) { }
        }
    }

    public void addContext(String modulePath) {
        addContext(LocalFileSystem.getInstance().refreshAndFindFileByPath(modulePath));
    }

    private void removeContextFromSettings(String modulePath) {
        if (components.containsKey(modulePath)) {
            components.remove(modulePath);
            refresh();
        }
    }
    private void removeContext(File file) {
        if (file.exists()) {
            removeContextFromSettings(file.getPath());
        }
    }

    private void removeContext(VirtualFile modulePathFile) {
                removeContextFromSettings(modulePathFile.getPath());
    }

    protected void registerProjectListener(Project project) {
        MessageBusConnection connection = project.getMessageBus().connect(project);
        connection.subscribe(ProjectTopics.MODULES, this);
    }

    public Map<String, ComponentDescriptor> getComponents() {
        return components;
    }

    public Project getProject() {
        return project;
    }

    @Override
    public List<Object> getChildren(Object o) {
        if (o instanceof LazyMutableTreeNode) {
            LazyMutableTreeNode node = (LazyMutableTreeNode) o;
            if (!node.isLoaded()) {
                node.load();
            }
            return Collections.list((Enumeration) ((MutableTreeNode)o).children());

        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Object getRoot() {
        return ROOT;
    }

    @Override
    public void onUpdate(ConfigWatcher source, Config config) {
        if (hasContextChanged(config, this.config)) {
            refresh();
        }
        this.config = config;
    }

    private boolean hasContextChanged(Config newConfig, Config currentConfig) {
        Context currentContext = KubeConfigUtils.getCurrentContext(currentConfig).getContext();
        Context newContext = KubeConfigUtils.getCurrentContext(newConfig).getContext();
        return hasServerChanged(newContext, currentContext)
                || hasNewToken(newContext, newConfig, currentContext, currentConfig);
    }

    private boolean hasServerChanged(Context newContext, Context currentContext) {
        return newContext == null
                || currentContext == null
                || !StringUtils.equals(currentContext.getCluster(), newContext.getCluster())
                || !StringUtils.equals(currentContext.getUser(), newContext.getUser());
    }

    private boolean hasNewToken(Context newContext, Config newConfig, Context currentContext, Config currentConfig) {
        if (newContext == null) {
            return false;
        }
        if (currentContext == null) {
            return true;
        }
        String newToken = KubeConfigUtils.getUserToken(newConfig, newContext);
        if (newToken == null) {
            // logout, do not refresh, LogoutAction already refreshes
            return false;
        }
        String currentToken = KubeConfigUtils.getUserToken(currentConfig, currentContext);
        return !StringUtils.equals(newToken, currentToken);
  }

  @Override
    public synchronized  void refresh() {
        TreePath path = new TreePath(ROOT);
        try {
            ROOT = new ApplicationsRootNode(this);
            ROOT.addChangeListener(this);
            this.treeStructureChanged(path, new int[0], new Object[0]);
        } catch (Exception e) {
        }
    }

    @Override
    public void onChildAdded(LazyMutableTreeNode source, Object child, int index) {
        if (child instanceof LazyMutableTreeNode) {
            ((LazyMutableTreeNode)child).addChangeListener(this);
        }
        treeNodesInserted(new TreePath(source.getPath()), new int[] { index }, new Object[] { child });
    }

    @Override
    public void onChildRemoved(LazyMutableTreeNode source, Object child, int index) {
        if (child instanceof LazyMutableTreeNode) {
            ((LazyMutableTreeNode)child).removeChangeListener(this);
        }
        treeNodesRemoved(new TreePath(source.getPath()), new int[] { index }, new Object[] { child });
    }

    @Override
    public void onChildrensRemoved(LazyMutableTreeNode source) {
        treeStructureChanged(new TreePath(source.getPath()), new int[0], new Object[0]);
    }
}
