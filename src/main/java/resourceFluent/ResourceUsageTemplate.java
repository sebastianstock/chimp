package resourceFluent;

public class ResourceUsageTemplate {

	/** The resource's name */
	private final String resourceName;
	
	/** The indices of the NameVariables that need to be testet. */
	private final int[] resourceRequirementPositions;
	
	/** The Strings the NameVariables schould be grounded to. */
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

	public String getResourceName() {
		return resourceName;
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
}
