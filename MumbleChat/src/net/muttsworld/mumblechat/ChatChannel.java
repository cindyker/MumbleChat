package net.muttsworld.mumblechat;

public class ChatChannel {
	private String Name;
	private String Permission;
	private Boolean muteable;
	private String color;
	private Boolean defaultchannel;
	private String alias;
	private Double distance;
	private Boolean filter;
	
	ChatChannel(String _Name, String _color, String _Permission, Boolean _muteable,Boolean _filter, Boolean _defaultchannel, String _alias,Double _distance)
	{
		Name = _Name;
		Permission = _Permission;
		muteable = _muteable;
		setColor(_color);
		setDefaultchannel(_defaultchannel);
		setAlias(_alias);
		setDistance(_distance);
		setFilter(_filter);
		
		
	}
	
	String getName(){return Name;}
	String getPermission(){return Permission;}
	Boolean isMuteable(){return muteable;}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Boolean isDefaultchannel() {
		return defaultchannel;
	}

	public void setDefaultchannel(Boolean defaultchannel) {
		this.defaultchannel = defaultchannel;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}
	
	public Boolean isDistance()
	{
		if (distance > 0)
			return true;
		else
			return false;
	}
	
	public Boolean hasPermission()
	{
		if(Permission.equalsIgnoreCase("None"))
			return false;
		return true;
	}

	public Boolean isFiltered() {
		return filter;
	}

	public void setFilter(Boolean filter) {
		this.filter = filter;
	}
	

	
}
