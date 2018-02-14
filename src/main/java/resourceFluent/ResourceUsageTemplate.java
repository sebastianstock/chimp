package resourceFluent;

public class ResourceUsageTemplate {

	/** The resource's name */
	private final String resourceName;
	
	/** The type of the fluent */
	private String fluentType;
	
	/** The indices of the NameVariables that need to be testet. */
	private final int[] resourceRequirementPositions;
	
	/** The Strings the NameVariables should be grounded to. */
	private final String[] resourceRequirements;
	
	/**  The amount of capacity the resource uses */
	private final int resourceUsageLevel;
	
	public ResourceUsageTemplate(String resourceName, int[] resourceRequirementPositions,
			String[] resourceRequirements, int resourceUsageLevel) {
		super();
		this.resourceName = resourceName;
		this.resourceRequirementPositions = resourceRequirementPositions;
		this.resourceRequirements = resourceRequirements;
		this.resourceUsageLevel = resourceUsageLevel;
	}
	
	public ResourceUsageTemplate(String resourceName, String fluentType, int[] resourceRequirementPositions,
			String[] resourceRequirements, int resourceUsageLevel) {
		this(resourceName, resourceRequirementPositions, resourceRequirements, resourceUsageLevel);
		this.fluentType = fluentType;
	}

	public String getResourceName() {
		return resourceName;
	}
	
	public String getFluentType() {
		if (fluentType == null) {
			throw new IllegalStateException("FluentType is not set");
		}
		return fluentType;
	}
	
	public int[] getResourceRequirementPositions() {
		return resourceRequirementPositions;
	}

	public String[] getResourceRequirements() {
		return resourceRequirements;
	}

	public int getResourceUsageLevel() {
		return resourceUsageLevel;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append(resourceName);
		ret.append(" : ");
		ret.append(resourceUsageLevel);
		return ret.toString();
	}
	
	
}
