
package lumien.perfectspawn.Transformer;

import net.minecraft.launchwrapper.IClassTransformer;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class PSClassTransformer implements IClassTransformer {

    String OBF_SERVER_CONFIGURATION_MANAGER = "ld";
    String OBF_WORLD_PROVIDER = "apa";
    String BED_OBFUSCATED = "aht";

    Logger logger = LogManager.getLogger("PerfectSpawnCore");

    public byte[] transform(String name, String transformedName, byte[] data) {
        this.logger.log(Level.DEBUG, "Transforming " + name);
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(data);
        classReader.accept((ClassVisitor) classNode, 0);

        if (classNode.superName.equals("net/minecraft/world/WorldProvider")
            || classNode.superName.equals("net/minecraft/world/WorldProviderHell")
            || classNode.superName.equals("net/minecraft/world/WorldProviderSurface")
            || classNode.superName.equals("net/minecraft/world/apa")
            || transformedName.equals("net.minecraft.world.WorldProvider")) {
            return patchWorldProvider(data);
        }
        if (transformedName.equals("net.minecraft.block.BlockBed")) {
            return patchBed(data);
        }
        if (transformedName.equals("net.minecraft.server.dedicated.DedicatedServer")) {
            return patchDedicatedServer(data);
        }
        if (transformedName.equals("net.minecraft.entity.player.EntityPlayer")) {
            return patchEntityPlayer(data);
        }
        return data;
    }

    private byte[] patchEntityPlayer(byte[] data) {
        this.logger.log(Level.INFO, "Patching EntityPlayer Class");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(data);
        classReader.accept((ClassVisitor) classNode, 0);

        String onUpdateName = MCPNames.method("func_70071_h_");
        MethodNode onUpdate = null;

        for (MethodNode mn : classNode.methods) {

            if (mn.name.equals(onUpdateName)) {

                onUpdate = mn;

                break;
            }
        }
        if (onUpdate != null) {

            String isDayTime = MCPNames.method("func_72935_r");

            for (int i = 0; i < onUpdate.instructions.size(); i++) {

                AbstractInsnNode ain = onUpdate.instructions.get(i);
                if (ain instanceof MethodInsnNode) {

                    MethodInsnNode min = (MethodInsnNode) ain;

                    if (min.name.equals(isDayTime)) {

                        AbstractInsnNode nextNode = onUpdate.instructions.get(i + 1);
                        if (nextNode != null && nextNode instanceof JumpInsnNode) {

                            this.logger.log(Level.INFO, "- Patched Staying in Bed Check");
                            JumpInsnNode jin = (JumpInsnNode) nextNode;
                            jin.setOpcode(160);

                            onUpdate.instructions
                                .insertBefore((AbstractInsnNode) jin, (AbstractInsnNode) new VarInsnNode(25, 0));
                            onUpdate.instructions.insertBefore(
                                (AbstractInsnNode) jin,
                                (AbstractInsnNode) new MethodInsnNode(
                                    184,
                                    "lumien/perfectspawn/Core/CoreHandler",
                                    "canWakeUp",
                                    "(Lnet/minecraft/entity/player/EntityPlayer;)Z"));
                        }
                    }
                }
            }
        }

        ClassWriter writer = new ClassWriter(3);
        classNode.accept(writer);

        return writer.toByteArray();
    }

    private byte[] patchDedicatedServer(byte[] data) {
        this.logger.log(Level.INFO, "Patching DedicatedServer Class");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(data);
        classReader.accept((ClassVisitor) classNode, 0);

        String isBlockProtectedName = MCPNames.method("func_96290_a");
        MethodNode isBlockProtected = null;

        for (MethodNode mn : classNode.methods) {

            if (mn.name.equals(isBlockProtectedName)) {

                isBlockProtected = mn;

                break;
            }
        }
        if (isBlockProtected != null) {
            for (int i = 0; i < isBlockProtected.instructions.size(); i++) {

                AbstractInsnNode ain = isBlockProtected.instructions.get(i);

                if (ain instanceof FieldInsnNode) {

                    FieldInsnNode fin = (FieldInsnNode) ain;
                    if (fin.getOpcode() == 180) {
                        if (fin.name.equals(MCPNames.field("field_76574_g"))) {

                            this.logger.log(Level.INFO, "- Patched Spawn Protection Control");

                            isBlockProtected.instructions.insert(
                                (AbstractInsnNode) fin,
                                (AbstractInsnNode) new MethodInsnNode(
                                    184,
                                    "lumien/perfectspawn/Core/CoreHandler",
                                    "isBlockNotProtectedByDimension",
                                    "(I)Z"));

                            break;
                        }
                    }
                }
            }
        }
        ClassWriter writer = new ClassWriter(3);
        classNode.accept(writer);

        return writer.toByteArray();
    }

    private byte[] patchBed(byte[] data) {
        this.logger.log(Level.INFO, "Patching Bed Class");
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(data);
        classReader.accept((ClassVisitor) classNode, 0);

        String onBlockActivatedName = MCPNames.method("func_149727_a");
        MethodNode onBlockActivated = null;

        for (MethodNode mn : classNode.methods) {

            if (mn.name.equals(onBlockActivatedName)) {

                onBlockActivated = mn;

                break;
            }
        }
        if (onBlockActivated != null) {
            for (int i = 0; i < onBlockActivated.instructions.size(); i++) {

                AbstractInsnNode ain = onBlockActivated.instructions.get(i);

                if (ain instanceof FieldInsnNode) {

                    FieldInsnNode fin = (FieldInsnNode) ain;
                    if (fin.getOpcode() == 178) {

                        String biomegenbase = "Lnet/minecraft/world/biome/BiomeGenBase;";
                        if (fin.desc.equals(biomegenbase)) {

                            this.logger.log(Level.INFO, "- Patched Bed Biome Restriction");
                            onBlockActivated.instructions.insert(ain, (AbstractInsnNode) new InsnNode(1));
                            onBlockActivated.instructions.remove(ain);

                            break;
                        }
                    }
                }
            }
        }
        ClassWriter writer = new ClassWriter(3);
        classNode.accept(writer);

        return writer.toByteArray();
    }

    private byte[] patchWorldProvider(byte[] data) {
        ClassNode classNode = new ClassNode();
        ClassReader classReader = new ClassReader(data);
        classReader.accept((ClassVisitor) classNode, 0);

        String canRespawnHereName = MCPNames.method("func_76567_e");
        String getRespawnDimensionName = "getRespawnDimension";
        String isSurfaceWorldName = MCPNames.method("func_76569_d");
        String getRandomizedSpawnPointName = "getRandomizedSpawnPoint";

        this.logger.log(Level.INFO, "Patching " + classNode.name);

        MethodNode canRespawnHere = null;
        MethodNode getRespawnDimension = null;
        MethodNode isSurfaceWorld = null;
        MethodNode getRandomizedSpawnPoint = null;

        for (MethodNode mn : classNode.methods) {

            if (mn.name.equals(getRespawnDimensionName)) {

                getRespawnDimension = mn;
                continue;
            }
            if (mn.name.equals(canRespawnHereName) && mn.desc.equals("()Z")) {

                canRespawnHere = mn;
                continue;
            }
            if (mn.name.equals(isSurfaceWorldName) && mn.desc.equals("()Z")) {

                isSurfaceWorld = mn;
                continue;
            }
            if (mn.name.equals(getRandomizedSpawnPointName)) {
                getRandomizedSpawnPoint = mn;
            }
        }
        String worldProviderName = "net/minecraft/world/WorldProvider";
        String chunkCoordinatesName = "net/minecraft/util/ChunkCoordinates";

        if (canRespawnHere != null) {

            this.logger.log(Level.INFO, "- Patched canRespawnHere");
            LabelNode l0 = new LabelNode(new Label());
            LabelNode l1 = new LabelNode(new Label());
            LabelNode l2 = new LabelNode(new Label());

            canRespawnHere.instructions.insert((AbstractInsnNode) new InsnNode(87));
            canRespawnHere.instructions.insert((AbstractInsnNode) l2);
            canRespawnHere.instructions.insert((AbstractInsnNode) new InsnNode(172));
            canRespawnHere.instructions.insert((AbstractInsnNode) l1);
            canRespawnHere.instructions.insert((AbstractInsnNode) new JumpInsnNode(155, l2));
            canRespawnHere.instructions.insert((AbstractInsnNode) new InsnNode(89));
            canRespawnHere.instructions.insert(
                (AbstractInsnNode) new MethodInsnNode(
                    184,
                    "lumien/perfectspawn/Core/CoreHandler",
                    "canRespawnHere",
                    "(L" + worldProviderName + ";)I"));
            canRespawnHere.instructions.insert((AbstractInsnNode) new VarInsnNode(25, 0));
            canRespawnHere.instructions.insert((AbstractInsnNode) l0);
        }

        if (getRespawnDimension != null) {

            this.logger.log(Level.INFO, "- Patched getRespawnDimension");
            String entityPlayerMPName = "net/minecraft/entity/player/EntityPlayerMP";
            LabelNode l0 = new LabelNode(new Label());
            LabelNode l1 = new LabelNode(new Label());
            LabelNode l2 = new LabelNode(new Label());

            getRespawnDimension.instructions.insert((AbstractInsnNode) l2);
            getRespawnDimension.instructions.insert((AbstractInsnNode) new InsnNode(172));
            getRespawnDimension.instructions.insert((AbstractInsnNode) l1);
            getRespawnDimension.instructions.insert((AbstractInsnNode) new JumpInsnNode(159, l2));
            getRespawnDimension.instructions.insert((AbstractInsnNode) new IntInsnNode(16, -126));
            getRespawnDimension.instructions.insert((AbstractInsnNode) new InsnNode(89));
            getRespawnDimension.instructions.insert(
                (AbstractInsnNode) new MethodInsnNode(
                    184,
                    "lumien/perfectspawn/Core/CoreHandler",
                    "getRespawnDimension",
                    "(L" + worldProviderName + ";L" + entityPlayerMPName + ";)I"));
            getRespawnDimension.instructions.insert((AbstractInsnNode) new VarInsnNode(25, 1));
            getRespawnDimension.instructions.insert((AbstractInsnNode) new VarInsnNode(25, 0));
            getRespawnDimension.instructions.insert((AbstractInsnNode) l0);
        }

        if (getRandomizedSpawnPoint != null) {

            this.logger.log(Level.INFO, "- Patched getRandomizedSpawnPoint");

            LabelNode l0 = new LabelNode(new Label());
            LabelNode l1 = new LabelNode(new Label());
            LabelNode l2 = new LabelNode(new Label());

            getRandomizedSpawnPoint.instructions.insert((AbstractInsnNode) new InsnNode(87));
            getRandomizedSpawnPoint.instructions.insert((AbstractInsnNode) l2);
            getRandomizedSpawnPoint.instructions.insert((AbstractInsnNode) new InsnNode(176));
            getRandomizedSpawnPoint.instructions.insert((AbstractInsnNode) l1);
            getRandomizedSpawnPoint.instructions.insert((AbstractInsnNode) new JumpInsnNode(198, l2));
            getRandomizedSpawnPoint.instructions.insert((AbstractInsnNode) new InsnNode(89));
            getRandomizedSpawnPoint.instructions.insert(
                (AbstractInsnNode) new MethodInsnNode(
                    184,
                    "lumien/perfectspawn/Core/CoreHandler",
                    "getRandomizedSpawnPoint",
                    "(L" + worldProviderName + ";)L" + chunkCoordinatesName + ";"));
            getRandomizedSpawnPoint.instructions.insert((AbstractInsnNode) new VarInsnNode(25, 0));
            getRandomizedSpawnPoint.instructions.insert((AbstractInsnNode) l0);
        }

        ClassWriter writer = new ClassWriter(3);
        classNode.accept(writer);

        return writer.toByteArray();
    }
}

/*
 * Location:
 * /home/midnight/Downloads/PerfectSpawn-1.1-deobf.jar!/lumien/perfectspawn/Transformer/PSClassTransformer.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
