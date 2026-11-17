package ru.givler.mbo.decor;

import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.*;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;
import ru.givler.mbo.client.font.ModernFontRenderer;
import ru.givler.mbo.client.gui.GuiDescriptionWorkbench;
import ru.givler.mbo.item.ItemDescription;
import ru.givler.mbo.tileentity.TileEntityDescriptionWorkbench;
import sun.misc.Unsafe;

/** Render real editor widgets and live-preview text in a hidden OpenGL context at a narrow GUI scale. */
public final class DescriptionEditorGuiSmoke {
    private static void set(Object target, Class<?> type, String name, Object value) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); field.set(target, value);
    }
    private static Object get(Object target, Class<?> type, String name) throws Exception {
        Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(target);
    }
    public static void main(String[] args) throws Exception {
        Pbuffer buffer = new Pbuffer(668, 620, new PixelFormat(8, 24, 0), null, null);
        try {
            buffer.makeCurrent();
            Field uf = Unsafe.class.getDeclaredField("theUnsafe"); uf.setAccessible(true); Unsafe unsafe = (Unsafe)uf.get(null);
            cpw.mods.fml.common.Loader loader = (cpw.mods.fml.common.Loader)unsafe.allocateInstance(cpw.mods.fml.common.Loader.class);
            set(null, cpw.mods.fml.common.Loader.class, "instance", loader);
            set(loader, cpw.mods.fml.common.Loader.class, "namedMods", Collections.emptyMap());
            Minecraft mc = (Minecraft)unsafe.allocateInstance(Minecraft.class);
            set(null, Minecraft.class, "theMinecraft", mc);
            final ZipFile vanilla = new ZipFile("C:/Users/givle/.gradle/caches/minecraft/net/minecraft/minecraft/1.7.10/minecraft-1.7.10.jar");
            IResourceManager resources = (IResourceManager)Proxy.newProxyInstance(IResourceManager.class.getClassLoader(), new Class[]{IReloadableResourceManager.class}, (p,m,a) -> {
                if (m.getName().equals("getResourceDomains")) return new HashSet<String>(Arrays.asList("minecraft", "mbo"));
                if (!m.getName().equals("getResource") && !m.getName().equals("getAllResources")) return null;
                ResourceLocation location = (ResourceLocation)a[0];
                String path = "assets/" + location.getResourceDomain() + "/" + location.getResourcePath();
                Path local = Paths.get("src/main/resources").resolve(path);
                byte[] bytes;
                if (Files.exists(local)) bytes = Files.readAllBytes(local);
                else {
                    ZipEntry entry = vanilla.getEntry(path);
                    if (entry == null) throw new FileNotFoundException(path);
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try (InputStream in = vanilla.getInputStream(entry)) { byte[] chunk = new byte[8192]; for (int n; (n=in.read(chunk))>=0;) out.write(chunk,0,n); }
                    bytes = out.toByteArray();
                }
                final byte[] data = bytes;
                Object resource = Proxy.newProxyInstance(IResource.class.getClassLoader(), new Class[]{IResource.class}, (rp,rm,ra) -> {
                    if (rm.getName().equals("getInputStream")) return new ByteArrayInputStream(data);
                    if (rm.getName().equals("hasMetadata")) return false;
                    return null;
                });
                return m.getName().equals("getAllResources") ? Collections.singletonList(resource) : resource;
            });
            GameSettings settings = (GameSettings)unsafe.allocateInstance(GameSettings.class); settings.language = "ru_RU";
            mc.gameSettings = settings; mc.displayWidth = 668; mc.displayHeight = 620;
            TextureManager textures = new TextureManager(resources); mc.renderEngine = textures;
            set(mc, Minecraft.class, "mcResourceManager", resources);
            net.minecraft.client.resources.Locale locale = new net.minecraft.client.resources.Locale();
            locale.loadLocaleDataFiles(resources, Arrays.asList("en_US", "ru_RU"));
            set(null, I18n.class, "i18nLocale", locale);
            ModernFontRenderer font = new ModernFontRenderer(settings, textures); font.onResourceManagerReload(resources); font.setUnicodeFlag(true); mc.fontRenderer = font;
            EntityClientPlayerMP player = (EntityClientPlayerMP)unsafe.allocateInstance(EntityClientPlayerMP.class);
            player.inventory = new InventoryPlayer(player); mc.thePlayer = player;
            TileEntityDescriptionWorkbench tile = new TileEntityDescriptionWorkbench();
            ItemStack item = new ItemStack(new Item().setUnlocalizedName("example"));
            ItemDescription.write(item, Arrays.asList("Древний клинок", "Слово другого цвета", "Третья строка"),
                Arrays.asList(new int[]{0xffaa00,0xffaa00,0xffaa00,0xffaa00,0xffaa00,0xffaa00,0xffaa00,0xffffff,0xffffff,0xffffff,0xffffff,0xffffff,0xffffff},
                    new int[]{0x55ffff,0x55ffff,0x55ffff,0x55ffff,0x55ffff,0xffffff,0xff55ff,0xff55ff,0xff55ff,0xff55ff,0xff55ff,0xff55ff,0xff55ff,0xffffff,0xffffff,0xffffff,0xffffff,0xffffff}, new int[0]), 0xd0c4ad);
            tile.setInventorySlotContents(0, item);
            GuiDescriptionWorkbench gui = new GuiDescriptionWorkbench(player.inventory, tile);
            set(gui, GuiScreen.class, "mc", mc); set(gui, GuiScreen.class, "fontRendererObj", font);
            gui.width = 334; gui.height = 310; mc.currentScreen = gui; gui.initGui();
            List<int[]> originalStyles = (List<int[]>)get(gui,GuiDescriptionWorkbench.class,"styles");
            int[] originalFirst = originalStyles.get(0).clone();
            Method mouseClick = GuiDescriptionWorkbench.class.getDeclaredMethod("mouseClicked",int.class,int.class,int.class);
            mouseClick.setAccessible(true); mouseClick.invoke(gui,4+12+15*8+2,12+126,0);
            if (!Arrays.equals(originalFirst, originalStyles.get(0))) throw new AssertionError("Brush selection recoloured existing text");
            Field palette = GuiDescriptionWorkbench.class.getDeclaredField("COLORS"); palette.setAccessible(true);
            if (((int[])palette.get(null)).length != 16) throw new AssertionError("Minecraft palette must have 16 colours");
            set(gui, GuiDescriptionWorkbench.class, "activeLine", 0);
            set(gui, GuiDescriptionWorkbench.class, "selectionStart", 0);
            set(gui, GuiDescriptionWorkbench.class, "selectionEnd", 7);
            GuiTextField hex = (GuiTextField)get(gui,GuiDescriptionWorkbench.class,"hex");
            String originalHex = hex.getText(); hex.setText("FF0000");
            Method apply = GuiDescriptionWorkbench.class.getDeclaredMethod("applyColor",int.class); apply.setAccessible(true); apply.invoke(gui,2);
            List<int[]> styles = (List<int[]>)get(gui,GuiDescriptionWorkbench.class,"styles");
            if (styles.get(0)[0] != 0xff0000 || styles.get(0)[7] != 0xffffff) throw new AssertionError("Selection color escaped its word");
            Method undo = GuiDescriptionWorkbench.class.getDeclaredMethod("undo"); undo.setAccessible(true); undo.invoke(gui);
            styles = (List<int[]>)get(gui,GuiDescriptionWorkbench.class,"styles");
            if (styles.get(0)[0] != 0xffaa00) throw new AssertionError("Undo did not restore word color");
            hex.setText(originalHex);
            List<GuiTextField> fields = (List<GuiTextField>)get(gui,GuiDescriptionWorkbench.class,"fields");
            String previousLine = fields.get(3).getText(); fields.get(3).setText("Unsaved draft");
            gui.initGui();
            fields = (List<GuiTextField>)get(gui,GuiDescriptionWorkbench.class,"fields");
            if (!fields.get(3).getText().equals("Unsaved draft")) throw new AssertionError("Resize discarded unsaved text");
            fields.get(3).setText(previousLine);
            GL11.glViewport(0,0,668,620); GL11.glMatrixMode(GL11.GL_PROJECTION); GL11.glLoadIdentity(); GL11.glOrtho(0,334,310,0,-100,100);
            GL11.glMatrixMode(GL11.GL_MODELVIEW); GL11.glLoadIdentity(); GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glClearColor(.08F,.09F,.1F,1); GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            Method background = GuiDescriptionWorkbench.class.getDeclaredMethod("drawGuiContainerBackgroundLayer",float.class,int.class,int.class); background.setAccessible(true); background.invoke(gui,0F,-1,-1);
            GL11.glEnable(GL11.GL_ALPHA_TEST); GL11.glAlphaFunc(GL11.GL_GREATER,.1F);
            Method foreground = GuiDescriptionWorkbench.class.getDeclaredMethod("drawGuiContainerForegroundLayer",int.class,int.class); foreground.setAccessible(true);
            int left = (334 - 326) / 2, top = (310 - 286) / 2;
            GL11.glPushMatrix(); GL11.glTranslatef(left,top,0); foreground.invoke(gui,-1,-1); GL11.glPopMatrix();
            for (Object button : (List<?>)get(gui,GuiScreen.class,"buttonList")) {
                if (((GuiButton)button).height != 20) throw new AssertionError("Vanilla button texture needs its full 20-pixel height");
                if (button.getClass() != GuiButton.class) throw new AssertionError("Editor must use vanilla buttons");
                ((GuiButton)button).drawButton(mc,-1,-1);
            }
            ByteBuffer pixels = BufferUtils.createByteBuffer(668*620*4); GL11.glReadPixels(0,0,668,620,GL11.GL_RGBA,GL11.GL_UNSIGNED_BYTE,pixels);
            BufferedImage image = new BufferedImage(668,620,BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<620;y++) for(int x=0;x<668;x++) {int r=pixels.get()&255,g=pixels.get()&255,b=pixels.get()&255,a=pixels.get()&255; image.setRGB(x,619-y,a<<24|r<<16|g<<8|b);}
            ImageIO.write(image,"png",Paths.get("build/description-editor-preview.png").toFile());
            vanilla.close();
            System.out.println("Description editor narrow-layout widgets and rich-colour preview rendered successfully.");
        } finally { buffer.destroy(); }
    }
}
