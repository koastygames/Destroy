package petrolpark.mc.destroy.client;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import petrolpark.mc.library.Petrolpark;
import petrolpark.mc.library.core.client.rendering.IGuiTexture;

public enum DestroyGuiTexture implements IGuiTexture {
  
    // Seismograph
	SEISMOGRAPH_BACKGROUND("seismograph", 0, 0, 64, 64, 64, 64),
	SEISMOGRAPH_OVERLAY("seismograph_overlay", 0, 0, 64, 64, 64, 64),
	SEISMOGRAPH_TICK("seismograph_symbols", 0, 0, 5, 5, 64, 64),
	SEISMOGRAPH_CROSS("seismograph_symbols", 5, 0, 5, 5, 64, 64),
	SEISMOGRAPH_GUESSED_TICK("seismograph_symbols", 10, 0, 5, 5, 64, 64),
	SEISMOGRAPH_GUESSED_CROSS("seismograph_symbols", 15, 0, 5, 5, 64, 64),
	SEISMOGRAPH_1("seismograph_symbols", 0, 5, 3, 5, 64, 64),
	SEISMOGRAPH_2("seismograph_symbols", 3, 5, 3, 5, 64, 64),
	SEISMOGRAPH_3("seismograph_symbols", 6, 5, 3, 5, 64, 64),
	SEISMOGRAPH_4("seismograph_symbols", 9, 5, 3, 5, 64, 64),
	SEISMOGRAPH_5("seismograph_symbols", 12, 5, 3, 5, 64, 64),
	SEISMOGRAPH_6("seismograph_symbols", 15, 5, 3, 5, 64, 64),
	SEISMOGRAPH_7("seismograph_symbols", 18, 5, 3, 5, 64, 64),
	SEISMOGRAPH_8("seismograph_symbols", 21, 5, 3, 5, 64, 64),
	SEISMOGRAPH_UNKNOWN("seismograph_symbols", 0, 10, 3, 5, 64, 64),
	SEISMOGRAPH_HIGHLIGHT_ROW("seismograph_symbols", 0, 57, 57, 7, 64, 64),
	SEISMOGRAPH_HIGHLIGHT_COLUMMN("seismograph_symbols", 57, 0, 7, 57, 64, 64),
	SEISMOGRAPH_HIGHLIGHT_CROSS("seismograph_symbols", 0, 38, 19, 19, 64, 64),
	SEISMOGRAPH_HIGHLIGHT_CELL("seismograph_symbols", 0, 31, 7, 7, 64, 64),
	;

	public final ResourceLocation location;
	public final int width, height, startX, startY, textureWidth, textureHeight;

	private DestroyGuiTexture(String location, int width, int height) {
		this(location, 0, 0, width, height);	
	};

	private DestroyGuiTexture(String location, int startX, int startY, int width, int height) {
		this(location, startX, startY, width, height, 256, 256);
	};

    private DestroyGuiTexture(String location, int startX, int startY, int width, int height, int textureWidth, int textureHeight) {
		this.location = Petrolpark.asResource("textures/gui/" + location + ".png");
		this.startX = startX;
		this.startY = startY;
		this.width = width;
		this.height = height;
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
	};

    @OnlyIn(Dist.CLIENT)
    @Override
	public void bind() {
		RenderSystem.setShaderTexture(0, location);
	};

	@OnlyIn(Dist.CLIENT)
	public void render(@Nonnull GuiGraphics graphics, int x, int y) {
		graphics.blit(location, x, y, startX, startY, width, height, textureWidth, textureHeight);
	};

    @Override
    public ResourceLocation getLocation() {
        return location;
    };

    @Override
    public int getStartX() {
        return startX;
    }

    @Override
    public int getStartY() {
        return startY;
    };

    @Override
    public int getWidth() {
        return width;
    };

    @Override
    public int getHeight() {
        return height;
    };

    @Override
    public int getTextureWidth() {
        return textureWidth;
    };

    @Override
    public int getTextureHeight() {
        return textureHeight;
    };
};
