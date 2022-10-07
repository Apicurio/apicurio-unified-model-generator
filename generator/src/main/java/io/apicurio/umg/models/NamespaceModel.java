package io.apicurio.umg.models;

import java.util.HashMap;
import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;

@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class NamespaceModel {

	@Include
	private NamespaceModel parent;
	@Include
	private String name;
	private final Map<String, NamespaceModel> children = new HashMap<>();
	private final Map<String, EntityModel> entities = new HashMap<>();

	public String fullName() {
		return (parent != null ? parent.fullName() + "." : "") + name;
	}

}
