package org.rootio.tools.radio;

public class Station {
	private String Location;
	private String Owner;
	private float frequency;
	private String telephoneNumber;
	private String name;
	private StationStatus stationStatus;
	
	public Station()
	{
		
	}
	
	public String getLocation()
	{
		return this.Location;
	}
	
	public String getOwner()
	{
		return this.Owner;
	}
	
	public String getTelephoneNumber()
	{
		return this.telephoneNumber;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public float getFrequency()
	{
		return this.frequency;
	}
	
	public StationStatus getStationStatus()
	{
		return this.stationStatus;
	}

}
