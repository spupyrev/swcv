package edu.cloudy.geom;

/**
 * @author spupyrev
 * Aug 26, 2013
 */
public class SWCRectangle
{
    //lower-left corner of the rectangle
    private double x;
    private double y;
    private double width;
    private double height;

    public SWCRectangle(double x, double y, double width, double height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public SWCRectangle()
    {
        this.x = 0;
        this.y = 0;
        this.width = 0;
        this.height = 0;
    }

    public SWCRectangle(SWCRectangle rect)
    {
        this.x = rect.x;
        this.y = rect.y;
        this.width = rect.width;
        this.height = rect.height;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.y = y;
    }

    public double getWidth()
    {
        return width;
    }

    public void setWidth(double width)
    {
        this.width = width;
    }

    public double getHeight()
    {
        return height;
    }

    public void setHeight(double height)
    {
        this.height = height;
    }

    public double getMaxX()
    {
        return x + width;
    }

    public double getMinX()
    {
        return x;
    }

    public double getMaxY()
    {
        return y + height;
    }

    public double getMinY()
    {
        return y;
    }

    public double getCenterX()
    {
        return x + width / 2.0;
    }

    public double getCenterY()
    {
        return y + height / 2.0;
    }

    /*public SWCPoint getPosition()
    {
        return new SWCPoint(getX(), getY());
    }*/

    public SWCPoint getCenter()
    {
        return new SWCPoint(getCenterX(), getCenterY());
    }

    public void setRect(double x, double y, double width, double height)
    {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void setRect(SWCRectangle rect)
    {
        setRect(rect.x, rect.y, rect.width, rect.height);
    }

    public boolean intersects(SWCRectangle rect)
    {
        if (width <= 0 || height <= 0 || rect.getWidth() <= 0 || rect.getHeight() <= 0)
            return false;

        double x0 = getX();
        double y0 = getY();

        return (rect.x + rect.getWidth() > x0 && rect.y + rect.getHeight() > y0 && rect.x < x0 + getWidth() && rect.y < y0 + getHeight());
    }

    public void add(SWCRectangle rect)
    {
        double x1 = Math.min(getMinX(), rect.getMinX());
        double y1 = Math.min(getMinY(), rect.getMinY());
        double x2 = Math.max(getMaxX(), rect.getMaxX());
        double y2 = Math.max(getMaxY(), rect.getMaxY());

        setRect(x1, y1, x2 - x1, y2 - y1);
    }

    public boolean contains(SWCPoint p)
    {
        return contains(p.x(), p.y());
    }

    public boolean contains(double x, double y)
    {
        return (this.x <= x && x <= this.x + width) && (this.y <= y && y <= this.y + height);
    }

    public void move(double dx, double dy)
    {
        setRect(x + dx, y + dy, width, height);
    }

    public void moveTo(double nx, double ny)
    {
        setRect(nx, ny, width, height);
    }

    public void scale(double factor)
    {
        width *= factor;
        height *= factor;
    }

    public void shrink(double w, double h)
    {
        double cenX = getCenterX();
        double cenY = getCenterY();
        
        width -= w;
        height -= h;
        setCenter(cenX, cenY);
    }

    public SWCRectangle createIntersection(SWCRectangle rect)
    {
        double x1 = Math.max(getMinX(), rect.getMinX());
        double y1 = Math.max(getMinY(), rect.getMinY());
        double x2 = Math.min(getMaxX(), rect.getMaxX());
        double y2 = Math.min(getMaxY(), rect.getMaxY());

        return new SWCRectangle(x1, y1, x2 - x1, y2 - y1);
    }

    public void setCenter(double x, double y)
    {
        setRect(x - (width / 2.0), y - (height / 2.0), width, height);
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("x: " + getX() + "\n");
        sb.append("y: " + getY() + "\n");
        sb.append("width: " + getWidth() + "\n");
        sb.append("height: " + getHeight() + "\n");
        return sb.toString();
    }

}
