package cc.hyperium.gui;

import cc.hyperium.Hyperium;
import cc.hyperium.mods.sk1ercommon.Multithreading;
import cc.hyperium.mods.sk1ercommon.ResolutionUtil;
import cc.hyperium.netty.NettyClient;
import cc.hyperium.netty.packet.packets.serverbound.ServerCrossDataPacket;
import cc.hyperium.purchases.PurchaseApi;
import cc.hyperium.utils.JsonHolder;
import cc.hyperium.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class CapesGui extends HyperiumGui {

    private HashMap<String, DynamicTexture> textures = new HashMap<>();
    private HashMap<String, BufferedImage> texturesImage = new HashMap<>();
    private JsonHolder cosmeticCallback = new JsonHolder();
    private boolean purchasing = false;

    public CapesGui() {
        scollMultiplier = 2;
        updatePurchases();
        Multithreading.runAsync(() -> {
            JsonHolder capeAtlas = PurchaseApi.getInstance().getCapeAtlas();
            for (String s : capeAtlas.getKeys()) {
                Multithreading.runAsync(() -> {
                    JsonHolder jsonHolder = capeAtlas.optJSONObject(s);
                    try {
                        URL url = null;
                        url = new URL(jsonHolder.optString("url"));
                        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setUseCaches(true);
                        connection.addRequestProperty("User-Agent", "Mozilla/4.76 Hyperium ");
                        connection.setReadTimeout(15000);
                        connection.setConnectTimeout(15000);
                        connection.setDoOutput(true);
                        InputStream is = connection.getInputStream();


                        BufferedImage img = ImageIO.read(ImageIO.createImageInputStream(is));
                        texturesImage.put(s, img);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

            }
        });

    }

    @Override
    protected void pack() {

    }
//22x17
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        try {
            if (!texturesImage.isEmpty()) {
                for (String s : texturesImage.keySet()) {
                    if (!textures.containsKey(s))
                        textures.put(s, new DynamicTexture(texturesImage.get(s)));
                }
            }

            super.drawScreen(mouseX, mouseY, partialTicks);
            final int blockWidth = 128;
            final int blockHeight = 256;
            final int blocksPerLine = 2;
            JsonHolder capeAtlas = PurchaseApi.getInstance().getCapeAtlas();
            ScaledResolution current = ResolutionUtil.current();

            int totalRows = capeAtlas.getKeys().size() / blocksPerLine + (capeAtlas.getKeys().size() % blocksPerLine == 0 ? 0 : 1);
            int row = 0;
            int pos = 1;
            int printY = 15 - offset;
            GlStateManager.scale(2F, 2F, 2F);
            fontRendererObj.drawString("Capes", (ResolutionUtil.current().getScaledWidth() / 2 - fontRendererObj.getStringWidth("Capes")) / 2, printY / 2, new Color(249, 99, 0).getRGB(), true);
            String s1 = PurchaseApi.getInstance().getSelf().getPurchaseSettings().optJSONObject("cape").optString("type");
            String s2 = capeAtlas.optJSONObject(s1).optString("name");
            if (s2.isEmpty())
                s2 = "NONE";
            String text = "Active Cape: " + s2;
            fontRendererObj.drawString(text, (ResolutionUtil.current().getScaledWidth() / 2 - fontRendererObj.getStringWidth(text)) / 2, (printY + 20) / 2, new Color(249, 99, 0).getRGB(), true);

            GlStateManager.scale(.5F, .5F, .5F);
            printY += 25;
            printY += 35;
            int scaledWidth = current.getScaledWidth();
            RenderUtils.drawSmoothRect(scaledWidth / 2 - blockWidth - 16, printY - 4, scaledWidth / 2 + blockWidth + 16, printY + (blockHeight + 16) * totalRows + 4, new Color(53, 106, 110).getRGB());
            for (String s : capeAtlas.getKeys()) {
                if (pos > blocksPerLine) {
                    pos = 1;
                    row++;
                }
                int thisBlocksCenter = pos == 1 ? scaledWidth / 2 - 8 - blockWidth / 2 : scaledWidth / 2 + 8 + blockWidth / 2;
                int thisTopY = printY + row * (16 + blockHeight);
                RenderUtils.drawSmoothRect(thisBlocksCenter - blockWidth / 2, thisTopY,
                        (thisBlocksCenter + blockWidth / 2), thisTopY + blockHeight, Color.WHITE.getRGB());
                JsonHolder cape = capeAtlas.optJSONObject(s);
                DynamicTexture dynamicTexture = textures.get(s);
                if (dynamicTexture != null) {
                    int imgW = 120;
                    int imgH = 128;
                    GlStateManager.bindTexture(dynamicTexture.getGlTextureId());
                    float capeScale = .75F;
                    int topLeftX = (int) (thisBlocksCenter - imgW / (2F / capeScale));
                    int topLeftY = thisTopY + 4;
                    GlStateManager.translate(topLeftX, topLeftY, 0);
                    GlStateManager.scale(capeScale, capeScale, capeScale);
                    drawTexturedModalRect(0, 0, imgW/12, 0, imgW, imgH*2);
                    GlStateManager.scale(1F / capeScale, 1F / capeScale, 1F / capeScale);
                    GlStateManager.translate(-topLeftX, -topLeftY, 0);
                }


                String nameCape = cape.optString("name");
                GlStateManager.scale(2F, 2F, 2F);
                int x = thisBlocksCenter - fontRendererObj.getStringWidth(nameCape);
                fontRendererObj.drawString(nameCape, x / 2, (thisTopY - 8 + blockHeight / 2 + 64 + 16) / 2, new Color(249, 99, 0).getRGB(), true);
                GlStateManager.scale(.5F, .5F, .5F);

                if (cosmeticCallback.getKeys().size() == 0 || purchasing) {
                    String string = "Loading";
                    fontRendererObj.drawString(string, thisBlocksCenter - fontRendererObj.getStringWidth(string), (thisTopY - 8 + blockHeight / 2 + 64 + 48), new Color(91, 102, 249).getRGB(), true);
                    return;
                }
                JsonHolder jsonHolder = cosmeticCallback.optJSONObject(s);
                boolean purchased = jsonHolder.optBoolean("purchased");
                if (purchased) {
                    String string = "Purchased";
                    int widthThing3 = fontRendererObj.getStringWidth(string) / 2;
                    int leftThing3 = thisBlocksCenter - widthThing3;
                    int topThing3 = thisTopY - 8 + blockHeight / 2 + 64 + 36;
                    fontRendererObj.drawString(string, leftThing3, topThing3, new Color(41, 249, 18).getRGB(), true);

                    if (s.equalsIgnoreCase(s1)) {
                        string = "Active";
                        int stringWidth = fontRendererObj.getStringWidth(string);
                        int i = thisBlocksCenter - stringWidth;
                        int i1 = thisTopY - 8 + blockHeight / 2 + 64 + 48;
                        GlStateManager.scale(2F, 2F, 2F);

                        fontRendererObj.drawString(string, i / 2, i1 / 2, new Color(249, 55, 241).getRGB(), true);
                        GlStateManager.scale(.5F, .5F, .5F);
                    } else {
                        int stringWidth = fontRendererObj.getStringWidth(string);
                        int i = thisBlocksCenter - stringWidth;
                        int i1 = thisTopY - 8 + blockHeight / 2 + 64 + 48;
                        string = "Make Active";
                        GuiBlock block = new GuiBlock(i, i + stringWidth * 2, i1, i1 + 20);
                        actions.put(block, () -> {
                            JsonHolder purchaseSettings = PurchaseApi.getInstance().getSelf().getPurchaseSettings();
                            if (!purchaseSettings.has("cape")) {
                                purchaseSettings.put("cape", new JsonHolder());
                            }
                            purchaseSettings.optJSONObject("cape").put("type", s);
                            NettyClient client = NettyClient.getClient();
                            if (client != null) {
                                client.write(ServerCrossDataPacket.build(new JsonHolder().put("internal", true).put("set_cape", true).put("value", s)));
                            }
                            Multithreading.runAsync(() -> {
                                try {
                                    Thread.sleep(3000L);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                //assume this is enough time
                                PurchaseApi.getInstance().refreshSelf();
                                Hyperium.INSTANCE.getHandlers().getCapeHandler().deleteCape(Minecraft.getMinecraft().getSession().getProfile().getId());
                            });
                        });
                        GlStateManager.scale(2F, 2F, 2F);

                        fontRendererObj.drawString(string, i / 2, i1 / 2, new Color(249, 55, 241).getRGB(), true);
                        GlStateManager.scale(.5F, .5F, .5F);
                    }


                } else {
                    if (jsonHolder.optBoolean("enough")) {
                        String string = "Click to purchase";
                        int stringWidth = fontRendererObj.getStringWidth(string);
                        int left = thisBlocksCenter - stringWidth / 2;
                        int i = thisTopY - 8 + blockHeight / 2 + 64 + 48;
                        fontRendererObj.drawString(string, left, i, new Color(249, 76, 238).getRGB(), true);
                        GuiBlock block = new GuiBlock(left, left + stringWidth, i, i + 10);
                        actions.put(block, () -> {
                            System.out.println("Attempting to purchase " + s);
                            purchasing = true;
                            NettyClient client = NettyClient.getClient();
                            if (client != null) {
                                client.write(ServerCrossDataPacket.build(new JsonHolder().put("internal", true).put("cosmetic_purchase", true).put("value", s)));
                            }
                        });
                    } else {
                        String string = "Insufficient Credits";
                        int stringWidth = fontRendererObj.getStringWidth(string);
                        int left = thisBlocksCenter - stringWidth / 2;
                        int i = thisTopY - 8 + blockHeight / 2 + 64 + 48;
                        fontRendererObj.drawString(string, left, i, new Color(249, 9, 0).getRGB(), true);

                    }
                }
                pos++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePurchases() {
        Multithreading.runAsync(() -> {
            cosmeticCallback = PurchaseApi.getInstance().get("https://api.hyperium.cc/cosmetics/" + Minecraft.getMinecraft().getSession().getProfile().getId().toString().replace("-", ""));
            purchasing = false;
        });

    }

}
