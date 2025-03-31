package org.forpdi.planning.structure;

import java.io.Serializable;
import java.util.List;

public class StructureLevelInstanceListDto implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private List<StructureLevelInstance> data;
	private Long total;
	private List<StructureLevelInstance> parents;

	public StructureLevelInstanceListDto(List<StructureLevelInstance> list, Long total,
			List<StructureLevelInstance> parents) {
		this.data = list;
		this.total = total;
		this.parents = parents;
	}

	public List<StructureLevelInstance> getData() {
		return data;
	}

	public void setData(List<StructureLevelInstance> list) {
		this.data = list;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public List<StructureLevelInstance> getParents() {
		return parents;
	}

	public void setParents(List<StructureLevelInstance> parents) {
		this.parents = parents;
	}
}
