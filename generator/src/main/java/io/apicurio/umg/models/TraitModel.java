package io.apicurio.umg.models;

import java.util.LinkedHashMap;
import java.util.Map;

import io.apicurio.umg.beans.beans.Specification;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Include;

@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TraitModel {

	private Specification spec;
	@Include
	private NamespaceModel namespace;
	@Include
	private String name;
	private final Map<String, PropertyModel> properties = new LinkedHashMap<>();
	private boolean transparent;

	public String fullyQualifiedName() {
		return namespace.fullName() + "." + name;
	}

}