/**
 *  Copyright (c) 2013-2014 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package tern.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import tern.TernException;
import tern.server.ITernModule;
import tern.server.ITernPlugin;
import tern.server.ModuleType;
import tern.utils.ExtensionUtils;
import tern.utils.TernModuleHelper;

/**
 * Tern repository implementation.
 *
 */
public class TernRepository implements ITernRepository {

	private static final String DEFS_FOLDER = "defs";
	private static final String PLUGIN_FOLDER = "plugin";
	private static final String NODE_MODULES_FOLDER = "node_modules";

	private final String name;
	private File ternBaseDir;
	private final boolean defaultRepository;
	private Map<String, ITernModule> modules;
	private ITernPlugin[] linters;

	public TernRepository(String name, File ternBaseDir) {
		this(name, ternBaseDir, false);
	}

	public TernRepository(String name, File ternFile, boolean defaultRepository) {
		this.name = name;
		this.ternBaseDir = ternFile;
		this.defaultRepository = defaultRepository;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ITernModule[] getModules() throws TernException {
		intializeIfNeeded();
		return modules.values().toArray(ITernModule.EMPTY_MODULE);
	}

	@Override
	public ITernModule getModule(String name) {
		try {
			intializeIfNeeded();
			return modules.get(name);
		} catch (TernException e) {
			return null;
		}

	}

	private void intializeIfNeeded() throws TernException {
		if (modules == null) {
			modules = loadModules();
			linters = searchLinters(modules.values());
		}
	}

	private ITernPlugin[] searchLinters(Collection<ITernModule> values) {
		Collection<ITernPlugin> linters = new ArrayList<ITernPlugin>();
		for (ITernModule module : values) {
			if (module.getModuleType() == ModuleType.Plugin
					&& ((ITernPlugin) module).isLinter()) {
				linters.add((ITernPlugin) module);
			}
		}
		return linters.toArray(ITernPlugin.EMPTY_PLUGIN);
	}

	private Map<String, ITernModule> loadModules() throws TernException {
		Map<String, ITernModule> modules = new HashMap<String, ITernModule>();
		// defs
		loadModules(modules, DEFS_FOLDER);
		// plugin
		loadModules(modules, PLUGIN_FOLDER);
		// node_modules
		loadModules(modules, NODE_MODULES_FOLDER);
		return modules;
	}

	private void loadModules(Map<String, ITernModule> modules, String dir)
			throws TernException {
		File baseDir = new File(getTernBaseDir(), dir);
		if (baseDir.exists()) {
			File[] files = baseDir.listFiles();
			File file = null;
			ITernModule module = null;
			for (int i = 0; i < files.length; i++) {
				file = files[i];
				module = TernModuleHelper.getModule(file.getName());
				if (module != null) {
					modules.put(module.getName(), module);
				}
			}
		}
	}

	@Override
	public void refresh() {
		this.modules = null;
	}

	@Override
	public File getTernBaseDir() {
		return ternBaseDir;
	}

	@Override
	public String getTernBaseDirAsString() {
		return TernModuleHelper.getPath(getTernBaseDir());
	}

	@Override
	public void setTernBaseDir(File ternBaseDir) {
		this.ternBaseDir = ternBaseDir;
	}

	@Override
	public boolean isDefault() {
		return defaultRepository;
	}

	@Override
	public File getFile(ITernModule module) {
		// check if module is inside tern/defs or tern/plugin
		String fileName = TernModuleHelper.getFileName(module);
		File moduleFile = null;
		switch (module.getModuleType()) {
		case Plugin:
		case Configurable:
			moduleFile = new File(ternBaseDir, new StringBuilder(PLUGIN_FOLDER)
					.append('/').append(fileName).toString());
			break;
		case Def:
			moduleFile = new File(ternBaseDir, new StringBuilder(DEFS_FOLDER)
					.append('/').append(fileName).toString());
			break;
		}
		if (moduleFile.exists()) {
			return moduleFile;
		}
		moduleFile = new File(ternBaseDir, new StringBuilder(
				NODE_MODULES_FOLDER).append('/')
				.append(ExtensionUtils.TERN_SUFFIX).append(module.getName())
				.append('/').append(fileName).toString());
		if (moduleFile.exists()) {
			return moduleFile;
		}
		return null;
	}

	@Override
	public ITernPlugin[] getLinters() {
		try {
			intializeIfNeeded();
		} catch (TernException e) {
			e.printStackTrace();
			return ITernPlugin.EMPTY_PLUGIN;
		}
		return linters;
	}
}
