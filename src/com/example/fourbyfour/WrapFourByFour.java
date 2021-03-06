package com.example.fourbyfour;

import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.universe.SimpleUniverse;

import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import java.awt.*;

public class WrapFourByFour extends JPanel {

    private static final int PWIDTH = 400;
    private static final int PHEIGHT = 400;

    private static final int BOUNDSIZE = 100;

    private SimpleUniverse simpleUniverse;
    private BranchGroup sceneBG;
    private BoundingSphere boundingSphere;

    public WrapFourByFour(FourByFour fourByFour) {
        setLayout(new BorderLayout());
        setOpaque(false);
        setPreferredSize(new Dimension(PWIDTH, PHEIGHT));

        GraphicsConfiguration configuration = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas3D = new Canvas3D(configuration);
        add("Center", canvas3D);
        canvas3D.setFocusable(true);
        canvas3D.requestFocus();

        simpleUniverse = new SimpleUniverse(canvas3D);

        createSceneGraph(canvas3D, fourByFour);
        initUserPosition();

        simpleUniverse.addBranchGraph(sceneBG);
    }

    private void createSceneGraph(Canvas3D canvas3D, FourByFour fourByFour) {
        sceneBG = new BranchGroup();
        boundingSphere = new BoundingSphere(new Point3d(0, 0, 0), BOUNDSIZE);

        TransformGroup gameTG = new TransformGroup();
        gameTG.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        gameTG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        sceneBG.addChild(gameTG);

        lightScene();
        addBackground();

        gameTG.addChild(makePoles());

        Positions positions = new Positions();
        Board board = new Board(positions, fourByFour);

        gameTG.addChild(positions.getGroup());

        mouseControls(canvas3D, board, gameTG);

        sceneBG.compile();
    }

    private void lightScene() {
        Color3f white = new Color3f(1.0f, 1.0f, 1.0f);

        AmbientLight ambientLight = new AmbientLight(white);
        ambientLight.setInfluencingBounds(boundingSphere);
        sceneBG.addChild(ambientLight);

        Vector3f vector3f = new Vector3f(-1.0f, -1.0f, -1.0f);
        DirectionalLight directionalLight = new DirectionalLight(white, vector3f);
        directionalLight.setInfluencingBounds(boundingSphere);
        sceneBG.addChild(directionalLight);
    }

    private void addBackground() {
        Background background = new Background();
        background.setApplicationBounds(boundingSphere);
        background.setColor(0.05f, 0.05f, 0.2f);
        sceneBG.addChild(background);
    }

    private void mouseControls(Canvas3D canvas3D, Board board, TransformGroup transformGroup) {
        PickDragBehavior pickDragBehavior = new PickDragBehavior(canvas3D, board, sceneBG, transformGroup);
        pickDragBehavior.setSchedulingBounds(boundingSphere);
        sceneBG.addChild(pickDragBehavior);
    }

    private void initUserPosition() {
        View view = simpleUniverse.getViewer().getView();
        view.setProjectionPolicy(View.PARALLEL_PROJECTION);

        TransformGroup transformGroup = simpleUniverse.getViewingPlatform().getViewPlatformTransform();
        Transform3D transform3D = new Transform3D();
        transform3D.set(65.0f, new Vector3f(0.0f, 0.0f, 400.0f));
        transformGroup.setTransform(transform3D);
    }

    private BranchGroup makePoles() {
        Color3f grey = new Color3f(0.25f, 0.25f, 0.25f);
        Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
        Color3f diffuseWhite = new Color3f(0.7f, 0.7f, 0.7f);
        Color3f specularWhite = new Color3f(0.9f, 0.9f, 0.9f);

        Material material = new Material(grey, black, diffuseWhite, specularWhite, 110.0f);
        material.setLightingEnable(true);
        Appearance appearance = new Appearance();
        appearance.setMaterial(material);

        BranchGroup branchGroup = new BranchGroup();
        float x = -30.0f;
        float z = -30.0f;

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                Transform3D transform3D = new Transform3D();
                transform3D.set(new Vector3f(x, 0.0f, z));
                TransformGroup transformGroup = new TransformGroup(transform3D);
                Cylinder cylinder = new Cylinder(1.0f, 60.0f, appearance);
                cylinder.setPickable(false);
                transformGroup.addChild(cylinder);
                branchGroup.addChild(transformGroup);
                x += 20.0f;
            }
            x = -30.0f;
            z += 20.0f;
        }
        return branchGroup;
    }
}
