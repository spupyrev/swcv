package de.tinloaf.cloudy.algos;

public enum CycleType
{
    WRAPPED, REGULAR;

    @Override
    public String toString()
    {
        switch (this)
        {
        case WRAPPED:
            return "WRAP";
        case REGULAR:
            return "REG";
        default:
            throw new IllegalArgumentException();
        }
    }
}
