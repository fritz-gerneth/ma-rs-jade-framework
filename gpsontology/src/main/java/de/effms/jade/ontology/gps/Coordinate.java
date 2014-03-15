package de.effms.jade.ontology.gps;

import jade.content.Concept;

public class Coordinate implements Concept
{
    private float longitude;

    private float latitude;

    public float getLongitude()
    {
        return longitude;
    }

    public void setLongitude(float longitude)
    {
        this.longitude = longitude;
    }

    public float getLatitude()
    {
        return latitude;
    }

    public void setLatitude(float latitude)
    {
        this.latitude = latitude;
    }
}
