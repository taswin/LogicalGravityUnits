package me.taswin.logicalGravityUnits;

import api.config.BlockConfig;
import api.listener.Listener;
import api.listener.events.block.SegmentPieceActivateByPlayer;
import api.listener.events.block.SegmentPieceActivateEvent;
import api.listener.events.block.SendableSegmentControllerFireActivationEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.textures.StarLoaderTexture;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PositionControl;
import org.schema.game.common.controller.SendableSegmentController;
import org.schema.game.common.controller.elements.BlockActivationListenerInterface;
import org.schema.game.common.data.SegmentPiece;
import org.schema.game.common.data.element.*;
import org.schema.game.common.data.physics.PairCachingGhostObjectAlignable;
import org.schema.game.common.data.player.PlayerCharacter;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3f;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;

public class LogicalGravityUnits extends StarMod
{
    ElementInformation logicalGravityUnit;
    short[] customTextureIds;

    static final Vector3f[] gravityDirections = new Vector3f[]
        {
                new Vector3f(0.0F, 0.0F, 0.0F),
                new Vector3f(0.0F, 0.0F, -9.89F),
                new Vector3f(0.0F, 0.0F, 9.89F),
                new Vector3f(0.0F, 9.89F, 0.0F),
                new Vector3f(0.0F, -9.89F, 0.0F),
                new Vector3f(9.89F, 0.0F, 0.0F),
                new Vector3f(-9.89F, 0.0F, 0.0F)
        };

    @Override
    public void onBlockConfigLoad(BlockConfig config)
    {
        short[] lguTextures = new short[]
        {
            customTextureIds[0], customTextureIds[2], customTextureIds[4], customTextureIds[4], customTextureIds[4], customTextureIds[4]//on
            //customTextureIds[1], customTextureIds[3], customTextureIds[5], customTextureIds[5], customTextureIds[5], customTextureIds[5]//off
        };


        logicalGravityUnit = BlockConfig.newElement(this, "Logical Gravity Unit", lguTextures);//ElementKeyMap.getInfo(56).getTextureIds());//new short[]{288, 289, 290, 290, 290, 290});
        logicalGravityUnit.setBuildIconNum(ElementKeyMap.getInfo(56).buildIconNum);

        logicalGravityUnit.volume = 0.1f;
        logicalGravityUnit.setMaxHitPointsE(100);
        logicalGravityUnit.setArmorValue(1);

        logicalGravityUnit.setOrientatable(true);
        logicalGravityUnit.setHasActivationTexure(true);
        logicalGravityUnit.sideTexturesPointToOrientation = true;

        logicalGravityUnit.setInventoryGroup("LGU");

        logicalGravityUnit.setCanActivate(true);
        logicalGravityUnit.signal = true;
        logicalGravityUnit.drawLogicConnection = true;

        //logicalGravityUnit.text

        BlockConfig.add(logicalGravityUnit);

        BlockConfig.addRecipe(logicalGravityUnit, 5, 3,
                new FactoryResource(1, (short) 56),//gravity Unit
                new FactoryResource(10, (short) 440),//Metal Mesh
                new FactoryResource(10, (short) 220));//Crystal Compisite

        //BlockConfig.setBlocksConnectable(ElementKeyMap.getInfo(405), logicalGravityUnit);//Activation Module
        //BlockConfig.setBlocksConnectable(ElementKeyMap.getInfo(666), logicalGravityUnit);//Button
        //BlockConfig.setBlocksConnectable(ElementKeyMap.getInfo(413), logicalGravityUnit);//Trigger (Area) Controller

        //BlockConfig.setBlocksConnectable(logicalGravityUnit, ElementKeyMap.getInfo(405));//Button
        //BlockConfig.setBlocksConnectable(logicalGravityUnit, logicalGravityUnit);

        //logicalGravityUnit.setCanActivate(false);
    }

    @Override
    public void onEnable()
    {
        BufferedImage img = null;
        try
        {
            img = ImageIO.read(getJarResource("me/taswin/logicalGravityUnits/resources/blocks/logicalGravityUnit.png"));
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        if (img != null)
        {
            customTextureIds = new short[]
            {
                (short)StarLoaderTexture.newBlockTexture(img.getSubimage(0, 0, 256, 256)).getTextureId(),//On top
                (short)StarLoaderTexture.newBlockTexture(img.getSubimage(768, 0, 256, 256)).getTextureId(),//Off top
                (short)StarLoaderTexture.newBlockTexture(img.getSubimage(256, 0, 256, 256)).getTextureId(),//On bottom
                (short)StarLoaderTexture.newBlockTexture(img.getSubimage(1024, 0, 256, 256)).getTextureId(),//Off bottom
                (short)StarLoaderTexture.newBlockTexture(img.getSubimage(512, 0, 256, 256)).getTextureId(),//On side
                (short)StarLoaderTexture.newBlockTexture(img.getSubimage(1280, 0, 256, 256)).getTextureId()//Off side
            };
        }

        /*StarLoader.registerListener(SegmentPieceActivateEvent.class, new Listener<SegmentPieceActivateEvent>()
        {
            @Override
            public void onEvent(SegmentPieceActivateEvent event)
            {
                //debug();
                SegmentPiece segmentPiece = event.getSegmentPiece();
                if (segmentPiece.getType() == logicalGravityUnit.id)
                {
                    //debug();
                    //checkLogicalGravity(player, segmentPiece);
                    //segmentPiece.setTextureId(textureLGUon);
                }
            }
        }, this);*/

        StarLoader.registerListener(SegmentPieceActivateByPlayer.class, new Listener<SegmentPieceActivateByPlayer>()
        {
            @Override
            public void onEvent(SegmentPieceActivateByPlayer event)
            {
                PlayerCharacter player = event.getPlayer().getAssingedPlayerCharacter();
                SegmentPiece segmentPiece = event.getSegmentPiece();
                //Vector3f gravityDirection = getVectorFromDirection(segmentPiece.getOrientation());

                /*if (segmentPiece.getType() == logicalGravityUnit.id)
                {
                    //debug();
                    //checkLogicalGravity(player, segmentPiece);
                }
                else */if (segmentPiece.getType() == ElementKeyMap.ACTIVAION_BLOCK_ID || segmentPiece.getType() == ElementKeyMap.LOGIC_BUTTON_NORM)
                {
                    SegmentPiece gravityUnit = getFirstControlled(segmentPiece, logicalGravityUnit.id);
                    if (gravityUnit != null)
                        checkLogicalGravity(player, gravityUnit);
                }
            }
        }, this);

        StarLoader.registerListener(SendableSegmentControllerFireActivationEvent.class, new Listener<SendableSegmentControllerFireActivationEvent>()
        {
            @Override
            public void onEvent(SendableSegmentControllerFireActivationEvent event)
            {
                ActivationTrigger act = event.getTrigger();
                SendableSegmentController seg = event.getController();

                if (act.getType() == ElementKeyMap.SIGNAL_TRIGGER_AREA)
                {
                    SegmentPiece pointUnsave = seg.getSegmentBuffer().getPointUnsave(act.pos);
                    PlayerCharacter player = null;
                    if (act.obj instanceof PairCachingGhostObjectAlignable)
                    {
                        PairCachingGhostObjectAlignable gs = (PairCachingGhostObjectAlignable) act.obj;
                        if (gs.getObj() instanceof PlayerCharacter)
                        {
                            player = ((PlayerCharacter) gs.getObj());
                        }
                    }

                    if (player != null && pointUnsave != null)
                    {
                        SegmentPiece firstLGU = getFirstControlled(pointUnsave, logicalGravityUnit.id);
                        if (firstLGU != null)
                        {
                            checkLogicalGravity(player, firstLGU);
                        }

                        /*ArrayList<SegmentPiece> slaveLGUs = getControlled(pointUnsave, logicalGravityUnit.id);
                        boolean[] dirAllReadyApplied = new boolean[6];
                        if (slaveLGUs.size() > 0)
                        {
                            for (SegmentPiece slave : slaveLGUs)
                            {
                                if(!dirAllReadyApplied[slave.getOrientation()])
                                {
                                    dirAllReadyApplied[slave.getOrientation()] = true;
                                    checkLogicalGravity(player, slave);
                                }
                            }
                        }*/

                        ArrayList<SegmentPiece> logicSlaves = getControlled(pointUnsave, ElementKeyMap.ACTIVAION_BLOCK_ID);
                        logicSlaves.addAll(getControlled(pointUnsave, ElementKeyMap.LOGIC_BUTTON_NORM));
                        if (logicSlaves.size() > 0)
                        {
                            for (SegmentPiece slave : logicSlaves)
                            {
                                seg.sendBlockActivation(ElementCollection.getEncodeActivation(slave, true, !slave.isActive(), false));
                            }
                        }
                    }
                }
            }
        }, this);
    }

    /*void debug()
    {
        return;
    }*/

    public boolean checkLogicalGravity(PlayerCharacter player, SegmentPiece segmentPiece)
    {
        Boolean state = segmentPiece.isActive();//true;

        /*SegmentPiece activationMod = getFirstControlled(segmentPiece, ElementKeyMap.ACTIVAION_BLOCK_ID);
        if (activationMod != null)
            state = activationMod.isActive();*/

        ArrayList<SegmentPiece> slaves = getControlled(segmentPiece, logicalGravityUnit.id);

        Boolean[] list = new Boolean[] {false, false, false, false, false, false, false};
        for (SegmentPiece sp : slaves)
        {
            list[sp.getOrientation() + 1] = sp.isActive();

            /*activationMod = getFirstControlled(sp, ElementKeyMap.ACTIVAION_BLOCK_ID);
            if (activationMod == null)
                a[sp.getOrientation() + 1] = !state;
            else
                a[sp.getOrientation() + 1] = activationMod.isActive() ^ state;*/
        }

        byte currentGravityDir = getDirectionFromVector(player.getGravity().getAcceleration());
        //Vector3f gravityDirection = getVectorFromDirection(segmentPiece.getOrientation());

        if (player.getGravity().source != segmentPiece.getSegment().getSegmentController() || currentGravityDir == -1)
        {
            currentGravityDir = 0;
        }

        if (list[currentGravityDir] ^ state)
        {
            player.activateGravity(segmentPiece);
            return true;
        }

        //player.scheduleGravity(gravityDirection, segmentPiece.getSegmentController());
        player.sendClientMessage("Can't switch to this gravity!", 3);
        return false;
    }

    public ArrayList<SegmentPiece> getControlled(SegmentPiece segmentPiece, short typeId)
    {
        ArrayList<SegmentPiece> segmentPieces = new ArrayList<>();
        PositionControl gravityElements = segmentPiece.getSegmentController().getControlElementMap().getControlledElements(typeId, segmentPiece.getAbsolutePos(new Vector3i()));

        if (gravityElements.getControlMap().size() > 0)
        {
            for (Long grav : gravityElements.getControlMap())
            {
                SegmentPiece pointUnsave = segmentPiece.getSegmentController().getSegmentBuffer().getPointUnsave(grav);
                if (pointUnsave != null)
                {
                    segmentPieces.add(pointUnsave);
                }
            }
        }

        return segmentPieces;
    }

    public SegmentPiece getFirstControlled(SegmentPiece segmentPiece, short typeId)
    {
        PositionControl gravityElements = segmentPiece.getSegmentController().getControlElementMap().getControlledElements(typeId, segmentPiece.getAbsolutePos(new Vector3i()));

        if (gravityElements.getControlMap().size() > 0)
        {
            long grav = gravityElements.getControlPosMap().iterator().nextLong();
            SegmentPiece pointUnsave = segmentPiece.getSegmentController().getSegmentBuffer().getPointUnsave(grav);

            if (pointUnsave != null)
            {
                return pointUnsave;
            }
        }

        return null;
    }

    public byte getDirectionFromVector(Vector3f gravityDirection)
    {
        for (byte i = 0; i < 7; i++)
            if (gravityDirection.equals(gravityDirections[i]))
                return i;
        //if (gravityDirection.equals(new Vector3f(0.0F, 0.0F, 0.0F)))
        //    return -1;

        return -1;
    }

    public Vector3f getVectorFromDirection(Byte direction)
    {
        return gravityDirections[direction + 1];
    }
}
