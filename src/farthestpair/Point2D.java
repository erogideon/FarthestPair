package farthestpair;

import java.util.Comparator;

public final class Point2D implements Comparable<Point2D> {

    public final int x;
    public final int y;

    public Point2D(int x, int y) {
        
        this.x = x;
        this.y = y;
        
    }
    
    public double getDistanceTo(Point2D other) {
        
        return Math.hypot(other.y-this.y, other.x-this.x);
        
    }

    /**
     * Returns the rotation direction of the three points
     * @param a first point
     * @param b second point
     * @param c third point
     * @return { -1, 0, +1 } if a->b->c is a { clockwise, collinear; counterclocwise } turn.
     */
    public static int getRotation(Point2D a, Point2D b, Point2D c) {
        double area2 = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
        if (area2 < 0) {
            return -1;
        } else if (area2 > 0) { 
            return +1;
        } else {
            return  0;
        }
    }
    
    //Returns the area of the triangle created by the two points
    //which is calculated using Heron's formula
    public static double getArea(Point2D p1, Point2D p2, Point2D p3) {
        
        double a = p1.getDistanceTo(p2);
        double b = p2.getDistanceTo(p3);
        double c = p3.getDistanceTo(p1);
        
        double p = (a+b+c)/2;
        
        return Math.sqrt(p*(p-a)*(p-b)*(p-c));
        
    }

    public int compareTo(Point2D that) {
        if (this.y < that.y) return -1;
        if (this.y > that.y) return +1;
        if (this.x < that.x) return -1;
        if (this.x > that.x) return +1;
        return 0;
    }

    public Comparator<Point2D> getPolarOrderComparator() {
        return new PolarOrderComparator();
    }

    private class PolarOrderComparator implements Comparator<Point2D> {
        
        public int compare(Point2D q1, Point2D q2) {
            
            double dx1 = q1.x - x;
            double dy1 = q1.y - y;
            double dx2 = q2.x - x;
            double dy2 = q2.y - y;

            if (dy1 >= 0 && dy2 < 0) { // q1 above; q2 below
                return -1;
            }    
            else if (dy2 >= 0 && dy1 < 0) { // q1 below; q2 above
                return +1;
            } else if (dy1 == 0 && dy2 == 0) { // 3-collinear and horizontal
                if (dx1 >= 0 && dx2 < 0) {
                    return -1;
                } else if (dx2 >= 0 && dx1 < 0) {
                    return +1;
                } else {
                    return  0;
                }
            } else { // both above or below
                return -getRotation(Point2D.this, q1, q2);
            }
            
        }
        
    }


    @Override
    public boolean equals(Object other) {
        
        if (other == this) { return true; }
        if (other == null) { return false; }
        
        if (other.getClass() != this.getClass()) { return false; }
        
        Point2D that = (Point2D) other;
        
        return this.x == that.x && this.y == that.y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    
}
