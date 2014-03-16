package de.effms.jade.ontology.gps;

import jade.content.Predicate;

public class LocatedAt implements Predicate
{
    private Coordinate coordinate;

    public Coordinate getCoordinate()
    {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate)
    {
        this.coordinate = coordinate;
    }
}
