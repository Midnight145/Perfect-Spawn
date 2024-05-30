
package lumien.perfectspawn.Transformer;

import java.util.Map;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.SortingIndex;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;

@TransformerExclusions({ "lumien.perfectspawn.Transformer.TransformUtils" })
@SortingIndex(1001)
public class PSLoadingPlugin implements IFMLLoadingPlugin {

    public static boolean IN_MCP;

    public String[] getASMTransformerClass() {
        return new String[] { PSClassTransformer.class.getName() };
    }

    public String getModContainerClass() {
        return null;
    }

    public String getSetupClass() {
        return null;
    }

    public void injectData(Map<String, Object> data) {
        IN_MCP = !((Boolean) data.get("runtimeDeobfuscationEnabled")).booleanValue();
    }

    public String getAccessTransformerClass() {
        return null;
    }
}

/*
 * Location: /home/midnight/Downloads/PerfectSpawn-1.1-deobf.jar!/lumien/perfectspawn/Transformer/PSLoadingPlugin.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version: 1.1.3
 */
