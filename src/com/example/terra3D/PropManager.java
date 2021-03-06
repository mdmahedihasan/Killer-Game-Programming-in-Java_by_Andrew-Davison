package com.example.terra3D;

import com.sun.j3d.loaders.Scene;
import ncsa.j3d.loaders.ModelLoader;

import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class PropManager {

    private static final int X_AXIS = 0;
    private static final int Y_AXIS = 1;
    private static final int Z_AXIS = 2;
    private static final int INCR = 0;
    private static final int DECR = 1;

    private static final double MOVE_INCR = 0.1;
    private static final double ROT_INCR = 10;
    private static final double ROT_AMT = Math.toRadians(ROT_INCR);

    private TransformGroup moveTG, rotTG, scaleTG;
    private Transform3D transform3D;
    private Transform3D chgTransform3D;

    private String filename;
    private double xRot, yRot, zRot;
    private ArrayList rotInfo;
    private double scale;

    private DecimalFormat decimalFormat;

    public PropManager(String loadFnm, boolean hasCoordsInfo) {
        filename = loadFnm;
        xRot = 0.0;
        yRot = 0.0;
        zRot = 0.0;
        rotInfo = new ArrayList();
        scale = 1.0;

        transform3D = new Transform3D();
        chgTransform3D = new Transform3D();

        decimalFormat = new DecimalFormat("0.###");

        loadFile(loadFnm);
        if (hasCoordsInfo) {
            getFileCoords(loadFnm);
        }
    }

    private void loadFile(String fnm) {
        System.out.println("Loading object file : models/" + fnm);

        Scene scene = null;
        ModelLoader modelLoader = new ModelLoader();
        try {
            scene = modelLoader.load("/media/mahedi/4ACA850ECA84F789/FALSE/WORKSPACE/JAVA/Killer Game Programming in Java_Andrew Davison/src/com/example/terra3D/models/" + fnm);
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }

        BranchGroup sceneBG = scene.getSceneGroup();

        TransformGroup objBoundsTG = new TransformGroup();
        objBoundsTG.addChild(sceneBG);

        String ext = getExtension(fnm);
        BoundingSphere objBounds = (BoundingSphere) sceneBG.getBounds();
        setBSPosn(objBoundsTG, objBounds.getRadius(), ext);

        scaleTG = new TransformGroup();
        scaleTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        scaleTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        scaleTG.addChild(objBoundsTG);

        rotTG = new TransformGroup();
        rotTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        rotTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        rotTG.addChild(scaleTG);

        moveTG = new TransformGroup();
        moveTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        moveTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        moveTG.addChild(rotTG);
    }

    private String getExtension(String fnm) {
        int dotPosn = fnm.lastIndexOf(".");
        if (dotPosn == -1) {
            return "(none)";
        } else {
            return fnm.substring(dotPosn + 1).toLowerCase();
        }
    }

    private void setBSPosn(TransformGroup objBoundsTG, double radius, String ext) {
        Transform3D objectTransform3D = new Transform3D();
        objBoundsTG.getTransform(objectTransform3D);

        Transform3D scaleTransform3D = new Transform3D();
        double scaleFactor = 1.0 / radius;
        scaleTransform3D.setScale(scaleFactor);
        objectTransform3D.mul(scaleTransform3D);

        if (ext.equals("3ds")) {
            Transform3D rotTransform3D = new Transform3D();
            rotTransform3D.rotX(-Math.PI / 2.0);
            objectTransform3D.mul(rotTransform3D);
        }

        objBoundsTG.setTransform(objectTransform3D);
    }

    public TransformGroup getMoveTG() {
        return moveTG;
    }

    private void getFileCoords(String fnm) {
        String coordFile = "/media/mahedi/4ACA850ECA84F789/FALSE/WORKSPACE/JAVA/Killer Game Programming in Java_Andrew Davison/src/com/example/terra3D/models/" + getName(fnm) + "Coords.txt";
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(coordFile));
            bufferedReader.readLine();
            String line;
            char ch;
            while ((line = bufferedReader.readLine()) != null) {
                ch = line.charAt(1);
                if (ch == 'p') {
                    setCurrentPosn(line);
                } else if (ch == 'r') {
                    setCurrentRotation(line);
                } else if (ch == 's') {
                    setCurrentScale(line);
                } else {
                    System.out.println(coordFile + " : did not recognise line : " + line);
                }
            }
            bufferedReader.close();
            System.out.println("Read in coords file : " + coordFile);
        } catch (IOException e) {
            System.out.println("Error reading coords file : " + coordFile);
            System.exit(1);
        }
    }

    private String getName(String fnm) {
        int dotPosn = fnm.lastIndexOf(".");
        if (dotPosn == -1) {
            return fnm;
        } else {
            return fnm.substring(0, dotPosn);
        }
    }

    private void setCurrentPosn(String line) {
        double vals[] = new double[3];
        vals[0] = 0;
        vals[1] = 0;
        vals[2] = 0;

        StringTokenizer tokenizer = new StringTokenizer(line);
        String token = tokenizer.nextToken();
        int count = 0;
        while (tokenizer.hasMoreTokens()) {
            token = tokenizer.nextToken();
            try {
                vals[count] = Double.parseDouble(token);
                count++;
            } catch (NumberFormatException e) {
                System.out.println("Incorrect format for position data in coords file");
                break;
            }
        }
        if (count != 3) {
            System.out.println("Insufficient position data in coords file");
        }
        doMove(new Vector3d(vals[0], vals[1], vals[2]));
    }

    private void setCurrentRotation(String line) {
        int rotNum;
        StringTokenizer tokenizer = new StringTokenizer(line);
        String token = tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens()) {
            return;
        }
        token = tokenizer.nextToken();
        for (int i = 0; i < token.length(); i++) {
            try {
                rotNum = Character.digit(token.charAt(i), 10);
            } catch (NumberFormatException e) {
                System.out.println("Incorrect format for rotation data in coords file");
                break;
            }
            if (rotNum == 1) {
                rotate(X_AXIS, INCR);
            } else if (rotNum == 2) {
                rotate(X_AXIS, DECR);
            } else if (rotNum == 3) {
                rotate(Y_AXIS, INCR);
            } else if (rotNum == 4) {
                rotate(Y_AXIS, DECR);
            } else if (rotNum == 5) {
                rotate(Z_AXIS, INCR);
            } else if (rotNum == 6) {
                rotate(Z_AXIS, DECR);
            } else {
                System.out.println("Did not recognise the rotation info in the coords file");
            }
        }
    }

    private void setCurrentScale(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line);
        String token = tokenizer.nextToken();
        double startScale;

        token = tokenizer.nextToken();
        try {
            startScale = Double.parseDouble(token);
        } catch (NumberFormatException e) {
            System.out.println("Incorrect format for scale data in coords file");
            startScale = 1.0;
        }

        if (startScale != 1.0) {
            scale(startScale);
        }
    }

    public void move(int axis, int change) {
        double moveStep = (change == INCR) ? MOVE_INCR : -MOVE_INCR;
        Vector3d moveVector3d;
        if (axis == X_AXIS) {
            moveVector3d = new Vector3d(moveStep, 0, 0);
        } else if (axis == Y_AXIS) {
            moveVector3d = new Vector3d(0, moveStep, 0);
        } else {
            moveVector3d = new Vector3d(0, 0, moveStep);
        }
        doMove(moveVector3d);
    }

    private void doMove(Vector3d theMoveVector3d) {
        moveTG.getTransform(transform3D);
        chgTransform3D.setIdentity();
        chgTransform3D.setTranslation(theMoveVector3d);
        transform3D.mul(chgTransform3D);
        moveTG.setTransform(transform3D);
    }

    public void rotate(int axis, int change) {
        doRotate(axis, change);
        storeRotate(axis, change);
    }

    private void doRotate(int axis, int change) {
        double radians = (change == INCR) ? ROT_AMT : -ROT_AMT;
        rotTG.getTransform(transform3D);
        chgTransform3D.setIdentity();
        switch (axis) {
            case X_AXIS:
                chgTransform3D.rotX(radians);
                break;
            case Y_AXIS:
                chgTransform3D.rotY(radians);
                break;
            case Z_AXIS:
                chgTransform3D.rotZ(radians);
                break;
            default:
                System.out.println("Unknown axis of rotation");
                break;
        }
        transform3D.mul(chgTransform3D);
        rotTG.setTransform(transform3D);
    }

    private void storeRotate(int axis, int change) {
        double degrees = (change == INCR) ? ROT_INCR : -ROT_INCR;
        switch (axis) {
            case X_AXIS:
                storeRotateX(degrees);
                break;
            case Y_AXIS:
                storeRotateY(degrees);
                break;
            case Z_AXIS:
                storeRotateZ(degrees);
                break;
            default:
                System.out.println("Unknown storage axis of rotation");
                break;
        }
    }

    private void storeRotateX(double degrees) {
        xRot = (xRot + degrees) % 360;
        if (degrees == ROT_INCR) {
            rotInfo.add(new Integer(1));
        } else if (degrees == -ROT_INCR) {
            rotInfo.add(new Integer(2));
        } else {
            System.out.println("No X-axis rotation number for " + degrees);
        }
    }

    private void storeRotateY(double degrees) {
        yRot = (yRot + degrees) % 360;
        if (degrees == ROT_INCR) {
            rotInfo.add(new Integer(3));
        } else if (degrees == -ROT_INCR) {
            rotInfo.add(new Integer(4));
        } else {
            System.out.println("No Y-axis rotation number for " + degrees);
        }
    }

    private void storeRotateZ(double degrees) {
        zRot = (zRot + degrees) % 360;
        if (degrees == ROT_INCR) {
            rotInfo.add(new Integer(5));
        } else if (degrees == -ROT_INCR) {
            rotInfo.add(new Integer(6));
        } else {
            System.out.println("No Z-axis rotation number for " + degrees);
        }
    }

    public void scale(double d) {
        scaleTG.getTransform(transform3D);
        chgTransform3D.setIdentity();
        chgTransform3D.setScale(d);
        transform3D.mul(chgTransform3D);
        scaleTG.setTransform(transform3D);

        scale *= d;
    }

    public Vector3d getLoc() {
        moveTG.getTransform(transform3D);
        Vector3d transVector3d = new Vector3d();
        transform3D.get(transVector3d);
        return transVector3d;
    }

    public Point3d getRotations() {
        return new Point3d(xRot, yRot, zRot);
    }

    public double getScale() {
        return scale;
    }

    public void saveCoordFile() {
        String coordFnm = "/media/mahedi/4ACA850ECA84F789/FALSE/WORKSPACE/JAVA/Killer Game Programming in Java_Andrew Davison/src/com/example/terra3D/models/" + getName(filename) + "Coords.txt";
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter(coordFnm));

            printWriter.println(filename);
            Vector3d currLoc = getLoc();
            printWriter.println("-p " + decimalFormat.format(currLoc.x) + " " + decimalFormat.format(currLoc.y) + " "
                    + decimalFormat.format(currLoc.z));

            printWriter.print("-r ");
            for (int i = 0; i < rotInfo.size(); i++) {
                printWriter.print("" + ((Integer) rotInfo.get(i)).intValue());
            }
            printWriter.println("");

            printWriter.println("-s " + decimalFormat.format(scale));

            printWriter.close();
            System.out.println("Saved to coord file : " + coordFnm);
        } catch (IOException e) {
            System.out.println("Error writing to coord file : " + coordFnm);
        }
    }

    private void printTG(TransformGroup transformGroup, String id) {
        Transform3D currTransform3D = new Transform3D();
        transformGroup.getTransform(currTransform3D);
        Vector3d currTransVector3d = new Vector3d();
        currTransform3D.get(currTransVector3d);
        printTuple(currTransVector3d, id);
    }

    private void printTuple(Tuple3d tuple3d, String id) {
        System.out.println(id + " x: " + decimalFormat.format(tuple3d.x) + ", " + id + " y: " +
                decimalFormat.format(tuple3d.y) + ", " + id + " z: " + decimalFormat.format(tuple3d.z));
    }
}
