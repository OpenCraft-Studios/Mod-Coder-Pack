package org.mcphackers.mcp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.mcphackers.mcp.tasks.Task.Side;
import org.mcphackers.mcp.tools.mappings.MappingUtil;

import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class MappingData {
	
	private final Side side;
	private Path saveFile;
	private final MappingTree mappingTree = new MemoryMappingTree();
	public Map<String, String> packages = new HashMap<>();
	
	public MappingData(Side side, Path mappingFile) throws IOException {
		this.side = side;
		this.saveFile = mappingFile;
		MappingUtil.readMappings(saveFile, mappingTree);
		reloadPackages();
	}
	
	private void reloadPackages() {
		mappingTree.getClasses().forEach(classEntry -> {
			String obfName = classEntry.getName("official");
			String deobfName = classEntry.getName("named");
			if (deobfName != null) {
				String obfPackage = obfName.lastIndexOf("/") >= 0 ? obfName.substring(0, obfName.lastIndexOf("/") + 1) : "";
				String deobfPackage = deobfName.lastIndexOf("/") >= 0 ? deobfName.substring(0, deobfName.lastIndexOf("/") + 1) : "";
				if (!packages.containsKey(deobfPackage) && !deobfPackage.equals(obfPackage)) {
					packages.put(deobfPackage, obfPackage);
				}
			}
		});
	}

	public MemoryMappingTree flipMappingTree() throws IOException {
		Map<String, String> namespaces = new HashMap<>();
		namespaces.put("named", "official");
		MemoryMappingTree namedTree = new MemoryMappingTree();
		MappingNsCompleter nsCompleter = new MappingNsCompleter(namedTree, namespaces);
		MappingSourceNsSwitch nsSwitch = new MappingSourceNsSwitch(nsCompleter, "named");
		mappingTree.accept(nsSwitch);
		return namedTree;
	}
	
	public Side getSide() {
		return side;
	}
	
	public MappingTree getTree() {
		return mappingTree;
	}
	
	public void save() throws IOException {
		save(saveFile);
	}
	
	public void save(Path file) throws IOException {
		MappingUtil.writeMappings(file, mappingTree);
	}

}