package me.taswin.logicalGravityUnits;

import api.config.BlockConfig;
import api.listener.Listener;
import api.listener.events.block.SegmentPieceActivateByPlayer;
import api.listener.events.block.SegmentPieceActivateEvent;
import api.listener.events.block.SendableSegmentControllerFireActivationEvent;
import api.mod.StarLoader;
import api.mod.StarMod;
import api.utils.StarRunnable;
import api.utils.textures.StarLoaderTexture;
import org.schema.common.util.linAlg.Vector3i;
import org.schema.game.common.controller.PositionControl;
import org.schema.game.common.controller.SendableSegmentController;
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
    ElementInformation antiGravityUnit;
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

    enum directionNames
    {
        None,
        Port,
        Starboard,
        Zenith,
        Nadir,
        Aft,
        Bow
    }

    @Override
    public void onBlockConfigLoad(BlockConfig config)
    {
        short[] lguTextures = new short[]
        {
            customTextureIds[0], customTextureIds[2], customTextureIds[4], customTextureIds[4], customTextureIds[4], customTextureIds[4]//on
        };

        logicalGravityUnit = BlockConfig.newElement(this, "Logical Gravity Unit", lguTextures);
        logicalGravityUnit.setBuildIconNum(ElementKeyMap.getInfo(56).buildIconNum);

        logicalGravityUnit.volume = 0.1f;
        logicalGravityUnit.setMaxHitPointsE(100);
        logicalGravityUnit.setArmorValue(1);

        logicalGravityUnit.setOrientatable(true);
        //logicalGravityUnit.setHasActivationTexure(true);
        logicalGravityUnit.sideTexturesPointToOrientation = true;

        logicalGravityUnit.setInventoryGroup("LGU");

        //logicalGravityUnit.setCanActivate(true);
        //logicalGravityUnit.signal = true;
        logicalGravityUnit.drawLogicConnection = true;

        BlockConfig.add(logicalGravityUnit);

        BlockConfig.addRecipe(logicalGravityUnit, 5, 3,
                new FactoryResource(1, (short) 56),//gravity Unit
                new FactoryResource(10, (short) 440),//Metal Mesh
                new FactoryResource(10, (short) 220));//Crystal Compisite

        short[] aguTextures = new short[]
        {
            customTextureIds[1]
        };

        antiGravityUnit = BlockConfig.newElement(this, "Anti Gravity Unit", aguTextures);
        antiGravityUnit.setBuildIconNum(ElementKeyMap.getInfo(56).buildIconNum);

        antiGravityUnit.volume = 0.1f;
        antiGravityUnit.setMaxHitPointsE(100);
        antiGravityUnit.setArmorValue(1);

        antiGravityUnit.setOrientatable(false);
        //logicalGravityUnit.setHasActivationTexure(true);
        //logicalGravityUnit.sideTexturesPointToOrientation = true;

        antiGravityUnit.setInventoryGroup("LGU");

        //logicalGravityUnit.setCanActivate(true);
        //logicalGravityUnit.signal = true;
        antiGravityUnit.drawLogicConnection = true;

        BlockConfig.add(antiGravityUnit);

        //BlockConfig.addRecipe(antiGravityUnit, 5, 3,
        //        new FactoryResource(1, (short) 56),//gravity Unit
        //        new FactoryResource(10, (short) 440),//Metal Mesh
        //        new FactoryResource(10, (short) 220));//Crystal Compisite

        for (short id : ElementKeyMap.signalArray) BlockConfig.setBlocksConnectable(ElementKeyMap.getInfo(id), logicalGravityUnit);
        BlockConfig.setBlocksConnectable(ElementKeyMap.getInfo(ElementKeyMap.SIGNAL_TRIGGER_AREA_CONTROLLER), logicalGravityUnit);//Trigger (Area) Controller

        BlockConfig.setBlocksConnectable(logicalGravityUnit, ElementKeyMap.getInfo(ElementKeyMap.ACTIVAION_BLOCK_ID));//Activation Module
        BlockConfig.setBlocksConnectable(logicalGravityUnit, logicalGravityUnit);
        BlockConfig.setBlocksConnectable(logicalGravityUnit, antiGravityUnit);

        for (short id : ElementKeyMap.signalArray) BlockConfig.setBlocksConnectable(ElementKeyMap.getInfo(id), antiGravityUnit);
        BlockConfig.setBlocksConnectable(ElementKeyMap.getInfo(ElementKeyMap.SIGNAL_TRIGGER_AREA_CONTROLLER), antiGravityUnit);//Trigger (Area) Controller

        BlockConfig.setBlocksConnectable(antiGravityUnit, ElementKeyMap.getInfo(ElementKeyMap.ACTIVAION_BLOCK_ID));//Activation Module
        BlockConfig.setBlocksConnectable(antiGravityUnit, antiGravityUnit);
        BlockConfig.setBlocksConnectable(antiGravityUnit, logicalGravityUnit);
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
                SegmentPiece segmentPiece = event.getSegmentPiece();
                debug();
                /*if (segmentPiece.getType() == logicalGravityUnit.id)
                {
                    debug();
                    //event.setCanceled(true);
                }
                /*if (segmentPiece.getInfo().isSignal() && segmentPiece.isActive())
                {
                    ArrayList<SegmentPiece> lgus = getControlled(segmentPiece, logicalGravityUnit.id);
                    if (lgus.size() > 0)
                    {
                        for (SegmentPiece lgu : lgus)
                        {
                            lgu.setActive(!lgu.isActive());
                        }
                    }
                }
            }
        }, this);*/

        StarLoader.registerListener(SegmentPieceActivateByPlayer.class, new Listener<SegmentPieceActivateByPlayer>()
        {
            @Override
            public void onEvent(final SegmentPieceActivateByPlayer event)
            {
                PlayerCharacter player = event.getPlayer().getAssingedPlayerCharacter();
                final SegmentPiece segmentPiece = event.getSegmentPiece();
                //Vector3f gravityDirection = getVectorFromDirection(segmentPiece.getOrientation());

                if (segmentPiece.getType() == logicalGravityUnit.id || segmentPiece.getType() == antiGravityUnit.id)
                {
                    //segmentPiece.setActive(!segmentPiece.isActive());
                    checkLogicalGravity(player, segmentPiece);
                }
                /*else if (segmentPiece.getType() == antiGravityUnit.id)
                {
                    //segmentPiece.setActive(!segmentPiece.isActive());

                    player.scheduleGravity(gravityDirections[0], segmentPiece.getSegmentController());
                    player.sendClientMessage("Has left gravity!", 3);
                }*/
                else if (segmentPiece.getType() == ElementKeyMap.ACTIVAION_BLOCK_ID || segmentPiece.getType() == ElementKeyMap.LOGIC_BUTTON_NORM)
                {
                    SegmentPiece gravityUnit = getFirstControlled(segmentPiece, logicalGravityUnit.id);
                    if (gravityUnit == null)
                        gravityUnit = getFirstControlled(segmentPiece, antiGravityUnit.id);
                    if (gravityUnit != null)
                    {
                        /*final long a = gravityUnit.getAbsoluteIndex();
                        final boolean b = gravityUnit.isActive();
                        new StarRunnable()
                        {
                            @Override
                            public void run()
                            {
                                event.getPlayer().getAssingedPlayerCharacter().sendClientMessage("2", 3);
                                event.getSegmentPiece().getSegmentController().getSegmentBuffer().getPointUnsave(a).setActive(true);//a);
                            }
                        }.runLater(100);*/
                        checkLogicalGravity(player, gravityUnit);
                    }
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
                        if (firstLGU == null)
                            firstLGU = getFirstControlled(pointUnsave, antiGravityUnit.id);
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
    }/*

    void debug()
    {
        return;
    }*/

    public boolean checkLogicalGravity(PlayerCharacter player, SegmentPiece segmentPiece)
    {
        byte targetDir = 0;
        if (segmentPiece.getType() == logicalGravityUnit.id)
            targetDir = (byte)(segmentPiece.getOrientation() + 1);

        Boolean state = true;//segmentPiece.isActive();//

        SegmentPiece activationMod = getFirstControlled(segmentPiece, ElementKeyMap.ACTIVAION_BLOCK_ID);
        if (activationMod != null)
            state = activationMod.isActive();

        ArrayList<SegmentPiece> slaves = getControlled(segmentPiece, logicalGravityUnit.id);
        Boolean[] list = new Boolean[] {false, false, false, false, false, false, false};
        for (SegmentPiece sp : slaves)
        {
            //list[sp.getOrientation() + 1] = sp.isActive();
            activationMod = getFirstControlled(sp, ElementKeyMap.ACTIVAION_BLOCK_ID);
            if (activationMod != null)
                list[sp.getOrientation() + 1] = !activationMod.isActive();
        }
        SegmentPiece slave = getFirstControlled(segmentPiece, antiGravityUnit.id);
        if (slave != null)
        {
            activationMod = getFirstControlled(slave, ElementKeyMap.ACTIVAION_BLOCK_ID);
            if (activationMod != null)
                list[0] = !activationMod.isActive();
        }

        byte currentGravityDir = getDirectionFromVector(player.getGravity().getAcceleration());



        if (player.getGravity().source == segmentPiece.getSegment().getSegmentController() && currentGravityDir >= 0)
        {
            if (currentGravityDir == targetDir)
            {
                player.sendClientMessage("Already in this gravity!", 3);
                return false;
            }
        }
        else
        {
            currentGravityDir = 0;
        }

        if (list[currentGravityDir] == state)
        {
            if (targetDir == 0)
                player.sendClientMessage("Has left gravity!", 1);
            else
                player.sendClientMessage("Switched to gravity! (" + directionNames.values()[targetDir] + ")", 1);
            player.scheduleGravity(gravityDirections[targetDir], segmentPiece.getSegmentController());
            //player.activateGravity(segmentPiece);
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
            return segmentPiece.getSegmentController().getSegmentBuffer().getPointUnsave(grav);
        }
        return null;
    }

    public byte getDirectionFromVector(Vector3f gravityDirection)
    {
        for (byte i = 0; i < 7; i++)
            if (gravityDirection.equals(gravityDirections[i]))
                return i;

        return -1;
    }

    public Vector3f getVectorFromDirection(Byte direction)
    {
        return gravityDirections[direction + 1];
    }
}
