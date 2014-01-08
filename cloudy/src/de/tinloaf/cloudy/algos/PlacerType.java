package de.tinloaf.cloudy.algos;

public enum PlacerType
{
    RECURSIVE_SPIRAL, SINGLE_SPIRAL, FORCE_DIRECTED, EXHAUSTIVE_FORCE_DIRECTED;

    @Override
    public String toString()
    {
        switch (this)
        {
        case RECURSIVE_SPIRAL:
            return "R_SPRL";
        case SINGLE_SPIRAL:
            return "1_SPRL";
        case FORCE_DIRECTED:
            return "FD";
        case EXHAUSTIVE_FORCE_DIRECTED:
            return "EFD";
        default:
            throw new IllegalArgumentException();
        }
    }
}
