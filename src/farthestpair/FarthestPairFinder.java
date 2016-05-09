package farthestpair;

import javax.swing.JFrame;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Stack;

public class FarthestPairFinder extends JFrame {

     int pointSize = 12;
     int numPoints = 100;
     
     Point2D[] S = new Point2D[ numPoints ]; //the set S
     Point2D[] farthestPair = new Point2D[ 2 ]; //the two points of the farthest pair
     
     ArrayList<Point2D> convexHull = new ArrayList<Point2D>(); //the vertices of the convex hull of S
     
     int numDistanceCalcs = 0;
     
     Color convexHullColour = Color.white;
     Color genericColour = Color.yellow;
     
     private AffineTransform t = new AffineTransform();

    
    //fills S with random points
    public void makeRandomPoints() {
        Random rand = new Random();
 
        for (int i = 0; i < numPoints; i++) {
            int x = rand.nextInt(501);
            int y = rand.nextInt(501);
            S[i] = new Point2D( x, y );            
        }        
    }

    
    public void paint(Graphics g) {
        
        BufferedImage image = new BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) image.getGraphics();
        
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, 800, 800);
        
        t.setToTranslation(150, 150);
        g2.setTransform(t);
        
        //draw the points in S
        g2.setColor(genericColour);
        for (Point2D p: S) {
            g2.fillOval(p.x-(pointSize/2), p.y-(pointSize/2), pointSize, pointSize);
        }
        
        //draw the points in the convex hull
        g2.setColor(convexHullColour);
        Point2D last = null;
        for (Point2D p: convexHull) {
            g2.fillOval(p.x-(pointSize/2), p.y-(pointSize/2), pointSize, pointSize);
            if (last != null) {
                g2.drawLine(last.x, last.y, p.x, p.y);
            }
            last = p;
        }
        g2.drawLine(last.x, last.y, convexHull.get(0).x, convexHull.get(0).y);
        
        //draw a red line connecting the farthest pair
        g2.setColor(Color.RED);
        g2.drawLine(farthestPair[0].x, farthestPair[0].y, farthestPair[1].x, farthestPair[1].y);
        
        g.drawImage(image, 0, 0, null);
        
    }
    
    
    public void findConvexHull() {
    
        //Sort points by ascending y-value
        Arrays.sort(S);
        
        //Sort points by the polar order from the point in the top-left corner
        Arrays.sort(S, 1, numPoints, S[0].getPolarOrderComparator());
        
        //Create a stack and ad the top left point to it
        Stack<Point2D> hull = new Stack();
        hull.push(S[0]);
        
        //Find the first point not equal to the first point
        int k1;
        for (k1 = 1; k1 < numPoints; k1++) {
            if (!S[0].equals(S[k1])) break;
        }
        if (k1 == numPoints) return;
        
        //Get the first non-colinear point
        int k2;
        for (k2 = k1 + 1; k2 < numPoints; k2++)
            if (Point2D.getRotation(S[0], S[k1], S[k2]) != 0) break;
        hull.push(S[k2-1]);
        
        //For each of the remaining points
        for (int i = k2; i < numPoints; i++) {
            //Get the last point in the hull
            Point2D top = hull.pop();
            
            //While the rotation between the second last point, the last piont and the current point is clockwise
            //backtrack until its counter clockwise
            while (Point2D.getRotation(hull.peek(), top, S[i]) <= 0) {
                top = hull.pop();
            }
            
            //Add the new point on the hull
            hull.push(top);
            hull.push(S[i]);
        }
        
        //Put the whole stack in a list
        convexHull.addAll(hull);
    
    }
    
    public void findFarthestPair_EfficientWay() {
        
        numDistanceCalcs = 0;
        
        //Get the index of the last point in the convex hull
        int n = convexHull.size()-1;
        
        //Begin at the second point in the convex hull and loop through all points
        //until the area of the trianlge formed by the current point, the first point
        //and the last point begins getting smaller
        int k = 1;
        while (Point2D.getArea(convexHull.get(n), convexHull.get(k+1), convexHull.get(0)) > Point2D.getArea(convexHull.get(n), convexHull.get(k), convexHull.get(0))) {
            k++;
        }
        //The first point and point k are antipodal
        
        //Set the current largest value to be infinetly small
        double best = Double.NEGATIVE_INFINITY;
        
        //Set the second iterator equal to the other point in the antipodal pair
        int j = k;
        
        //Loop from the first point up to the other antipodal pair
        for (int i = 0; i < k; i++) {
            
            //If the distance between points i and j is larger than the current best, update the farthest pair
            double distance = convexHull.get(i).getDistanceTo(convexHull.get(j));
            numDistanceCalcs++;
            if (distance > best) {
                farthestPair[0] = convexHull.get(i);
                farthestPair[1] = convexHull.get(j);
                best = distance;
            }
            
            //Loop through the second point until the two points are no longer antipodal then move to the next i value
            while ((j < n) && Point2D.getArea(convexHull.get(i), convexHull.get(j+1), convexHull.get(i+1)) > Point2D.getArea(convexHull.get(i), convexHull.get(j), convexHull.get(i+1))) {
                j++;
                
                //If the distance between points i and j is larger than the current best, update the farthest pair
                distance = convexHull.get(i).getDistanceTo(convexHull.get(j));
                numDistanceCalcs++;
                if (distance > best) {
                    farthestPair[0] = convexHull.get(i);
                    farthestPair[1] = convexHull.get(j);
                    best = distance;
                }
            }
        }
        
    
    }
    
    public void findFarthestPair_BruteForceWay() {
        
        //code this just for fun, to see how many more distance calculations and comparisons it does than the efficient way
        
        numDistanceCalcs = 0;
        
        double best = Double.NEGATIVE_INFINITY;
        
        
        //Go through evry point and compare it to every point after itself.
        for (int i = 0; i < convexHull.size(); i++) {
            for (int j = i+1; j < convexHull.size(); j++) {
                double distance = convexHull.get(i).getDistanceTo(convexHull.get(j));
                numDistanceCalcs++;
                if (distance > best) {
                    farthestPair[0] = convexHull.get(i);
                    farthestPair[1] = convexHull.get(j);
                    best = distance;
                }
            }
        }
        
    }
    
   
    public static void main(String[] args) {

        //no changes are needed in main().  Just code the blank methods above.
        
        FarthestPairFinder fpf = new FarthestPairFinder();
        
        fpf.setBackground(Color.BLACK);
        fpf.setSize(800, 800);
        fpf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        fpf.makeRandomPoints();
        
        fpf.findConvexHull();
        
        fpf.findFarthestPair_EfficientWay();
        System.out.println("Rotating Calipers took " + fpf.numDistanceCalcs + " distance calculations.");
        
        fpf.findFarthestPair_BruteForceWay();
        System.out.println("Brute-Force took " + fpf.numDistanceCalcs + " distance calculations.");
        
        fpf.setVisible(true); 
    }
}
