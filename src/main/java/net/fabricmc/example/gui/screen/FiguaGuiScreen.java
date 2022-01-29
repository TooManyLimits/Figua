package net.fabricmc.example.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class FiguaGuiScreen extends Screen {

    private final FiguaFramebuffer framebuffer;


    public FiguaGuiScreen(Text title) {
        super(title);
        framebuffer = new FiguaFramebuffer();
    }

    public void onClose() {
        MinecraftClient.getInstance().textRenderer.draw()


        framebuffer.close();
        super.onClose();
    }
}
